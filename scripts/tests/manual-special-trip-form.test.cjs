const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const template = fs.readFileSync(path.join(__dirname, '../../src/main/resources/templates/reservation-form.html'), 'utf8');
const script = [...template.matchAll(/<script(?:\s[^>]*)?>([\s\S]*?)<\/script>/g)].map(match => match[1]).join('\n');

// DOM mínimo para ejecutar el script de la vista, sin instalar dependencias de frontend.
class Element {
  constructor(attributes = {}) {
    this.attributes = attributes;
    this.value = attributes.value || '';
    this.checked = attributes.checked || false;
    this.disabled = attributes.disabled || false;
    this.required = attributes.required || false;
    this.max = attributes.max;
    this.options = [];
    this.listeners = {};
    this.dataset = {};
    this.textContent = '';
    this.innerText = '';
    const classes = new Set((attributes.class || '').split(/\s+/));
    this.classList = {
      add: name => classes.add(name), remove: name => classes.delete(name),
      toggle: (name, on) => on ? classes.add(name) : classes.delete(name), contains: name => classes.has(name),
    };
  }
  addEventListener(name, callback) { this.listeners[name] = callback; }
  appendChild(child) { this.options.push(child); }
  setAttribute(name, value) { this[name] = value; }
  removeAttribute(name) { delete this[name]; }
  focus() { this.focused = true; }
  set innerHTML(html) { this.options = [...html.matchAll(/<option\b/g)].map(() => ({})); }
}

function setup(category = 'SPECIAL', postResponse = async () => ({ ok: true, status: 201 })) {
  const elements = new Map();
  const controls = [];
  for (const match of template.matchAll(/<([a-z][\w-]*)\b([^>]*?)>/gi)) {
    const attributes = Object.fromEntries([...match[2].matchAll(/([\w:-]+)="([^"]*)"/g)].map(attr => [attr[1], attr[2]]));
    for (const flag of ['disabled', 'required', 'checked']) attributes[flag] = new RegExp(`(?:^|\\s)${flag}(?:\\s|$)`).test(match[2]);
    const element = new Element(attributes);
    element.parentElement = new Element();
    if (attributes.id) elements.set(attributes.id, element);
    if (attributes.name && ['input', 'select', 'textarea'].includes(match[1])) controls.push(element);
  }
  const get = id => {
    assert.ok(elements.has(id), `El script requiere el elemento ${id}`);
    return elements.get(id);
  };
  const form = get('manualTripForm');
  form.dataset.specialApiUrl = form.attributes['th:data-special-api-url'].match(/@\{([^}]+)\}/)[1];
  form.dataset.agendaUrl = form.attributes['th:data-agenda-url'].match(/@\{([^}]+)\}/)[1];
  get('tripCategory').value = category;
  const requests = [];
  const navigation = [];
  const windowEvents = {};
  const context = vm.createContext({
    document: { getElementById: get, createElement: () => new Element() },
    window: { addEventListener: (event, callback) => { windowEvents[event] = callback; }, location: { assign: url => navigation.push(url) } },
    URLSearchParams, console,
    FormData: class {
      constructor() {
        this.fields = new Map();
        for (const control of controls) {
          if (control.disabled) continue;
          if (['radio', 'checkbox'].includes(control.attributes.type) && !control.checked) continue;
          this.fields.set(control.attributes.name, control.value);
        }
      }
      get(name) { return this.fields.get(name) ?? null; }
    },
    fetch: async (url, options) => {
      requests.push({ url, options });
      if (options?.method === 'POST') return postResponse();
      if (url.includes('localidades')) return { ok: true, json: async () => ['Morteros'] };
      throw new Error(`Consulta inesperada en prueba: ${url}`);
    },
  });
  vm.runInContext(script, context);
  windowEvents.DOMContentLoaded();
  const setField = (name, value) => {
    const control = controls.find(item => item.attributes.name === name && !item.disabled);
    assert.ok(control, `El campo ${name} debe estar habilitado`);
    control.value = value;
  };
  function fillSpecial() {
    for (const [name, value] of Object.entries({
      firstName: 'Ana', lastName: 'Pérez', phone: '+54 9 351 123 4567',
      travelDate: '2030-01-02', pickupAddress: 'Belgrano 100',
      originCustom: 'De Suardi', destinationCustom: 'Alta Gracia',
      passengerCount: '4', customPrice: '83000.00', departureSchedule: '09:30',
    })) setField(name, value);
  }
  async function submit() {
    const event = { prevented: false, preventDefault() { this.prevented = true; } };
    await form.listeners.submit(event);
    return event;
  }
  return { get, requests, navigation, context, submit, fillSpecial };
}

test('los botones visibles alternan el modo y sincronizan el valor enviado y el estado accesible', () => {
  const page = setup('REGULAR');
  page.get('specialTripMode').listeners.click();
  assert.equal(page.get('tripCategory').value, 'SPECIAL');
  assert.equal(page.get('specialTripMode')['aria-pressed'], 'true');
  assert.equal(page.get('regularTripMode')['aria-pressed'], 'false');
  assert.equal(page.get('originCustom').required, true);
  assert.equal(page.get('inputOrigen').disabled, true);
  assert.equal(page.get('specialTripStatus').classList.contains('d-none'), false);
  page.get('regularTripMode').listeners.click();
  assert.equal(page.get('tripCategory').value, 'REGULAR');
  assert.equal(page.get('specialTripMode')['aria-pressed'], 'false');
  assert.equal(page.get('originCustom').disabled, true);
  assert.equal(page.get('inputOrigen').disabled, false);
});

test('el acceso especial habilita los campos libres y evita consultar tarifas/turnos fijos', () => {
  const page = setup();
  assert.equal(page.get('inputOrigen').disabled, true);
  assert.equal(page.get('inputDestino').disabled, true);
  for (const id of ['originCustom', 'destinationCustom', 'customPrice', 'customDepartureSchedule']) {
    assert.equal(page.get(id).disabled, false);
    assert.equal(page.get(id).required, true);
  }
  assert.equal(page.get('inputAsientos').max, '9');
  assert.equal(page.get('specialTripStatus').classList.contains('d-none'), false);
  assert.equal(page.get('manualAmount').disabled, true);
  assert.equal(page.requests.length, 0);
});

test('genera hasta ocho acompañantes, conserva nombres al cambiar cantidad y los envía en orden', async () => {
  const page = setup();
  assert.equal(page.get('companionFields').classList.contains('d-none'), true);
  page.fillSpecial();
  page.get('inputAsientos').value = '9';
  page.get('inputAsientos').listeners.input();
  assert.equal(vm.runInContext('companionControls.length', page.context), 8);
  vm.runInContext("companionControls.forEach((input, i) => { input.value = ' Nombre ' + (i + 1) + ' '; });", page.context);
  page.get('inputAsientos').value = '2';
  page.get('inputAsientos').listeners.input();
  assert.equal(page.get('companionNames').value, 'Nombre 1');
  page.get('inputAsientos').value = '9';
  page.get('inputAsientos').listeners.input();
  assert.equal(page.get('previewMonto').innerText, '747.000,00');
  await page.submit();
  const payload = JSON.parse(page.requests.find(request => request.options?.method === 'POST').options.body);
  assert.equal(payload.passengerCount, 9);
  assert.equal(payload.companionNames, Array.from({length: 8}, (_, i) => `Nombre ${i + 1}`).join(', '));
});

test('un titular solo oculta acompañantes y limpia los nombres enviados en modo regular', async () => {
  const page = setup('REGULAR');
  page.get('inputAsientos').value = '2';
  page.get('inputAsientos').listeners.input();
  vm.runInContext("companionControls[0].value = 'Ana';", page.context);
  await page.submit();
  assert.equal(page.get('companionNames').value, 'Ana');
  page.get('inputAsientos').value = '1';
  page.get('inputAsientos').listeners.input();
  assert.equal(page.get('companionFields').classList.contains('d-none'), true);
  await page.submit();
  assert.equal(page.get('companionNames').value, '');
});

test('multiplica el precio por pasajeros y mantiene el total cuando se agrega la vuelta', () => {
  const page = setup();
  page.fillSpecial();
  page.get('customPrice').listeners.input();
  assert.equal(page.get('previewMonto').innerText, '332.000,00');
  page.get('inputAsientos').value = '2';
  page.get('inputAsientos').listeners.input();
  assert.equal(page.get('previewMonto').innerText, '166.000,00');
  page.get('isRoundTripCheck').checked = true;
  page.get('isRoundTripCheck').listeners.change();
  assert.equal(page.get('previewMonto').innerText, '166.000,00');
});

test('envía $83.000 por persona con ruta libre y pago pendiente al API y abre la agenda de ese día', async () => {
  const page = setup();
  page.fillSpecial();
  assert.equal((await page.submit()).prevented, true);
  assert.equal(page.requests.length, 1);
  const { url, options } = page.requests[0];
  assert.equal(url, '/api/admin/trips');
  assert.equal(options.credentials, 'same-origin');
  assert.deepEqual(JSON.parse(options.body), {
    tripCategory: 'SPECIAL', firstName: 'Ana', lastName: 'Pérez', phone: '5493511234567', cuil: null,
    travelDate: '2030-01-02', pickupAddress: 'Belgrano 100', originCustom: 'De Suardi', destinationCustom: 'Alta Gracia',
    passengerCount: 4, customPrice: '83000.00', departureSchedule: '09:30', roundTrip: false,
    returnDate: null, returnDepartureSchedule: null, requiresInvoice: false, companionNames: null,
    notes: null, paymentStatus: 'PENDING', paymentVerified: false,
  });
  assert.deepEqual(page.navigation, ['/agenda/view-detalle?date=2030-01-02']);
});

test('un rechazo de dominio conserva el formulario, muestra el motivo y permite corregirlo', async () => {
  const page = setup('SPECIAL', async () => ({ ok: false, status: 400, json: async () => ({ error: 'El origen personalizado es obligatorio.' }) }));
  page.fillSpecial();
  await page.submit();
  assert.equal(page.get('reservationFormError').textContent, 'El origen personalizado es obligatorio.');
  assert.equal(page.get('reservationFormError').classList.contains('d-none'), false);
  assert.equal(page.get('reservationFormError').focused, true);
  assert.equal(page.get('originCustom').value, 'De Suardi');
  assert.equal(page.get('customPrice').value, '83000.00');
  assert.equal(page.get('saveManualTrip').disabled, false);
  assert.equal(page.navigation.length, 0);
});

test('mientras guarda bloquea el segundo envío y conserva el monto decimal sin convertirlo', async () => {
  let finish;
  const page = setup('SPECIAL', () => new Promise(resolve => { finish = resolve; }));
  page.fillSpecial();
  const first = page.submit();
  await page.submit();
  assert.equal(page.requests.length, 1);
  assert.equal(page.get('saveManualTrip').disabled, true);
  finish({ ok: true, status: 201 });
  await first;
  assert.equal(page.navigation.length, 1);
});

test('regreso programado especial envía fecha/horario libres y el pedido de factura', async () => {
  const page = setup();
  page.fillSpecial();
  page.get('isRoundTripCheck').checked = true;
  page.get('retornoProgramadoForm').checked = true;
  vm.runInContext('actualizarCalendarioVueltaForm()', page.context);
  page.get('returnDateInput').value = '2030-01-03';
  page.get('customReturnSchedule').value = '19:15';
  page.get('requiresInvoice').checked = true;
  await page.submit();
  const payload = JSON.parse(page.requests[0].options.body);
  assert.equal(payload.roundTrip, true);
  assert.equal(payload.returnDate, '2030-01-03');
  assert.equal(payload.returnDepartureSchedule, '19:15');
  assert.equal(payload.requiresInvoice, true);
  assert.equal(payload.paymentStatus, 'PENDING');
});

test('volver a regular restaura localidades y conserva el envío tradicional del formulario', async () => {
  const page = setup();
  page.get('inputAsientos').value = '4';
  page.get('tripCategory').value = 'REGULAR';
  vm.runInContext('alternarTipoDeViaje()', page.context);
  assert.equal(page.get('inputOrigen').disabled, false);
  assert.equal(page.get('customPrice').disabled, true);
  assert.equal(page.get('manualAmount').disabled, false);
  assert.equal(page.get('inputAsientos').max, '9');
  assert.equal(page.get('inputAsientos').value, '4');
  assert.equal((await page.submit()).prevented, false);
  assert.equal(page.requests.filter(request => request.options?.method === 'POST').length, 0);
});
