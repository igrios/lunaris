const {test} = require('node:test');
const assert = require('node:assert/strict');
const {readFileSync} = require('node:fs');
const vm = require('node:vm');
const template = readFileSync('src/main/resources/templates/reservation-form.html', 'utf8');
const source = template.slice(template.indexOf('        const passengerLookupFields'), template.indexOf('        const isRoundTripCheck'));

function setup(fetch) {
    const field = value => ({value, listeners: {}, addEventListener(event, callback) {this.listeners[event] = callback;}});
    const address = field('');
    const context = {
        firstNameInput: field(''), lastNameInput: field(''), phoneInput: field(''), cuilInput: field('12345678'),
        document: {querySelector: () => address},
        alertaDiv: {classList: {add() {}, remove() {}}}, saldoSpan: {},
        fetch, console: {error() {}}
    };
    vm.createContext(context);
    vm.runInContext(source, context);
    return {...context, address};
}
const passenger = {firstName: 'Ana', lastName: 'Pérez', phone: '543511234567', cuil: '12345678', address: 'San Martín 100'};
const response = data => ({ok: true, status: 200, json: async () => data});

test('document blur fills contact and pickup address and refreshes balance', async () => {
    const urls = [];
    const form = setup(async url => {urls.push(url); return response(url.startsWith('/passengers') ? passenger : {existe: true, saldo: 500});});
    await form.cuilInput.listeners.blur();
    assert.equal(urls[0], '/passengers/by-cuil?cuil=12345678');
    assert.equal(form.firstNameInput.value, 'Ana');
    assert.equal(form.lastNameInput.value, 'Pérez');
    assert.equal(form.phoneInput.value, passenger.phone);
    assert.equal(form.address.value, passenger.address);
    assert.equal(form.saldoSpan.innerText, '500.00');
});

test('phone blur uses phone endpoint and fills the document', async () => {
    const urls = [];
    const form = setup(async url => {urls.push(url); return response(url.startsWith('/passengers') ? passenger : {});});
    form.phoneInput.value = passenger.phone;
    form.cuilInput.value = '';
    await form.phoneInput.listeners.blur();
    assert.equal(urls[0], '/passengers/by-phone?phone=543511234567');
    assert.equal(form.cuilInput.value, passenger.cuil);
});

for (const result of [{status: 204}, {ok: false, status: 500}]) {
    test(`HTTP ${result.status} preserves manually entered data`, async () => {
        const form = setup(async () => result);
        form.firstNameInput.value = 'Manual';
        form.address.value = 'Dirección manual';
        await form.cuilInput.listeners.blur();
        assert.equal(form.firstNameInput.value, 'Manual');
        assert.equal(form.address.value, 'Dirección manual');
    });
}

test('late result cannot overwrite another document or edits during lookup', async () => {
    let resolve;
    const form = setup(() => new Promise(done => {resolve = done;}));
    const pending = form.cuilInput.listeners.blur();
    form.cuilInput.value = '87654321';
    form.cuilInput.listeners.input();
    resolve(response(passenger));
    await pending;
    assert.equal(form.firstNameInput.value, '');
    assert.equal(form.cuilInput.value, '87654321');
});

test('manual name typed during lookup is preserved', async () => {
    let resolve;
    let first = true;
    const form = setup(() => first ? (first = false, new Promise(done => {resolve = done;})) : Promise.resolve(response({})));
    const pending = form.cuilInput.listeners.blur();
    form.firstNameInput.value = 'Nombre corregido';
    resolve(response(passenger));
    await pending;
    assert.equal(form.firstNameInput.value, 'Nombre corregido');
    assert.equal(form.address.value, passenger.address);
});

for (const lookup of ['phone', 'cuil']) {
    test(`${lookup} lookup fills only missing fields and preserves manual data`, async () => {
        const form = setup(async url => response(url.startsWith('/passengers') ? passenger : {}));
        form.firstNameInput.value = 'Nombre manual';
        form.lastNameInput.value = '   ';
        form.address.value = 'Retiro acordado con el operador';
        form.phoneInput.value = lookup === 'phone' ? '3511234567' : '';
        form.cuilInput.value = lookup === 'cuil' ? passenger.cuil : '';
        await form[`${lookup}Input`].listeners.blur();
        assert.equal(form.firstNameInput.value, 'Nombre manual');
        assert.equal(form.lastNameInput.value, passenger.lastName);
        assert.equal(form.address.value, 'Retiro acordado con el operador');
        assert.equal(form.phoneInput.value, lookup === 'phone' ? '3511234567' : passenger.phone);
        assert.equal(form.cuilInput.value, passenger.cuil);
    });

    test(`${lookup} without match permits manual entry and a subsequent successful lookup`, async () => {
        let found = false;
        const form = setup(async url => url.startsWith('/passengers')
            ? (found ? response(passenger) : {status: 204}) : response({}));
        const input = form[`${lookup}Input`];
        input.value = '99999999';
        await input.listeners.blur();
        assert.equal(form.firstNameInput.value, '');
        assert.equal(form.address.value, '');
        form.firstNameInput.value = 'Nombre manual';
        found = true;
        input.value = passenger[lookup];
        input.listeners.input();
        await input.listeners.blur();
        assert.equal(form.firstNameInput.value, 'Nombre manual');
        assert.equal(form.lastNameInput.value, passenger.lastName);
        assert.equal(form.address.value, passenger.address);
    });
}

test('empty identifiers do not send requests or disable manual entry', async () => {
    let calls = 0;
    const form = setup(async () => {calls++; return response(passenger);});
    form.phoneInput.value = '  ';
    form.cuilInput.value = '';
    await form.phoneInput.listeners.blur();
    await form.cuilInput.listeners.blur();
    assert.equal(calls, 0);
    form.firstNameInput.value = 'Alta nueva';
    assert.equal(form.firstNameInput.value, 'Alta nueva');
});
