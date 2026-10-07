import { useEffect, useState } from 'react';

/** Referencia para integrar en la app React externa. Requiere sesión de ADMIN/OPERADOR. */
export default function ManualTripForm({ localities, apiBaseUrl = '', onCreated = () => {} }) {
  const [form, setForm] = useState({
    tripCategory: 'REGULAR', firstName: '', lastName: '', phone: '', cuil: '',
    travelDate: '', pickupAddress: '', pickupLocality: '', destination: '',
    originCustom: '', destinationCustom: '', passengerCount: '1',
    customPrice: '', amount: '', departureSchedule: '', requiresInvoice: false,
  });
  const [schedules, setSchedules] = useState([]);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const special = form.tripCategory === 'SPECIAL';

  useEffect(() => {
    setSchedules([]);
    if (special || !form.travelDate || !form.pickupLocality) return;
    const controller = new AbortController();
    const query = new URLSearchParams({ travelDate: form.travelDate, pickupLocality: form.pickupLocality });
    fetch(`${apiBaseUrl}/api/v1/schedules?${query}`, { credentials: 'include', signal: controller.signal })
      .then(async response => {
        if (!response.ok) throw new Error('No se pudieron consultar los horarios.');
        return response.json();
      })
      .then(rows => setSchedules(rows.filter(row => row.available)))
      .catch(reason => { if (reason.name !== 'AbortError') setError(reason.message); });
    return () => controller.abort();
  }, [special, form.travelDate, form.pickupLocality, apiBaseUrl]);

  function update(event) {
    const { name, value, type, checked } = event.target;
    setForm(previous => {
      const next = { ...previous, [name]: type === 'checkbox' ? checked : value };
      if (['tripCategory', 'pickupLocality', 'travelDate'].includes(name)) next.departureSchedule = '';
      if (name === 'tripCategory' && value === 'REGULAR' && Number(next.passengerCount) > 4) next.passengerCount = '4';
      return next;
    });
  }

  async function submit(event) {
    event.preventDefault();
    setError('');
    setSaving(true);
    const payload = {
      tripCategory: form.tripCategory, firstName: form.firstName, lastName: form.lastName,
      phone: form.phone, cuil: form.cuil || null, travelDate: form.travelDate,
      pickupAddress: form.pickupAddress, passengerCount: Number(form.passengerCount),
      departureSchedule: form.departureSchedule, roundTrip: false,
      requiresInvoice: form.requiresInvoice, paymentStatus: 'PENDING',
      ...(special ? {
        originCustom: form.originCustom, destinationCustom: form.destinationCustom,
        customPrice: form.customPrice, // Decimal como string: evita cálculos binarios de dinero.
      } : {
        pickupLocality: form.pickupLocality, destination: form.destination,
        amount: form.amount || null,
      }),
    };
    try {
      const response = await fetch(`${apiBaseUrl}/api/admin/trips`, {
        method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const body = await response.json();
      if (!response.ok) throw new Error(body.error || 'No se pudo guardar el viaje.');
      onCreated(body);
    } catch (reason) {
      setError(reason.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={submit}>
      <label>Tipo de viaje
        <select name="tripCategory" value={form.tripCategory} onChange={update}>
          <option value="REGULAR">Regular</option><option value="SPECIAL">Especial</option>
        </select>
      </label>
      <label>Nombre<input name="firstName" value={form.firstName} onChange={update} required /></label>
      <label>Apellido<input name="lastName" value={form.lastName} onChange={update} required /></label>
      <label>WhatsApp<input name="phone" value={form.phone} onChange={update} pattern="[0-9]{10,15}" required /></label>
      <label>CUIL<input name="cuil" value={form.cuil} onChange={update} /></label>
      <label>Fecha<input name="travelDate" type="date" value={form.travelDate} onChange={update} required /></label>
      {special ? (
        <>
          <label>Origen<input name="originCustom" value={form.originCustom} onChange={update} maxLength={100} required /></label>
          <label>Destino<input name="destinationCustom" value={form.destinationCustom} onChange={update} maxLength={100} required /></label>
          <label>Precio total acordado ($)<input name="customPrice" type="number" min="0.01" max="99999999.99" step="0.01" value={form.customPrice} onChange={update} required /></label>
          <label>Horario<input name="departureSchedule" type="time" value={form.departureSchedule} onChange={update} required /></label>
          <p role="status">Pago pendiente. La factura se habilita después de registrar el pago.</p>
        </>
      ) : (
        <>
          {['pickupLocality', 'destination'].map((name, index) => (
            <label key={name}>{index === 0 ? 'Origen' : 'Destino'}
              <select name={name} value={form[name]} onChange={update} required>
                <option value="">Seleccionar</option>
                {localities.map(locality => <option key={locality} value={locality}>{locality}</option>)}
              </select>
            </label>
          ))}
          <label>Horario
            <select name="departureSchedule" value={form.departureSchedule} onChange={update} required>
              <option value="">Seleccionar horario disponible</option>
              {schedules.map(schedule => <option key={schedule.id} value={schedule.id}>{schedule.label}</option>)}
            </select>
          </label>
          <label>Monto manual ($)<input name="amount" type="number" min="0" step="0.01" value={form.amount} onChange={update} placeholder="Usar tarifa vigente" /></label>
        </>
      )}
      <label>Dirección de retiro<input name="pickupAddress" value={form.pickupAddress} onChange={update} required /></label>
      <label>Pasajeros<input name="passengerCount" type="number" min="1" max={special ? undefined : 4} step="1" value={form.passengerCount} onChange={update} required /></label>
      <label><input name="requiresInvoice" type="checkbox" checked={form.requiresInvoice} onChange={update} />Solicitar factura después del pago</label>
      {error && <p role="alert">{error}</p>}
      <button type="submit" disabled={saving}>{saving ? 'Guardando…' : 'Guardar viaje'}</button>
    </form>
  );
}
