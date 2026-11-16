(() => {
    const table = document.getElementById('tablaPasajeros');
    const element = document.getElementById('passengerEditModal');
    const form = document.getElementById('passengerEditForm');
    const fields = document.getElementById('passengerEditFields');
    const save = document.getElementById('passengerSave');
    const status = document.getElementById('passengerEditStatus');
    let activeRow;
    let version = 0;
    let saving = false;

    async function responseData(response) {
        const data = await response.json().catch(() => ({}));
        if (!response.ok) throw new Error(data.error || 'No se pudo completar la operación.');
        return data;
    }

    async function open(row) {
        if (saving) return;
        const current = ++version;
        activeRow = row;
        form.reset();
        fields.disabled = true;
        save.disabled = true;
        status.textContent = 'Cargando datos del pasajero…';
        bootstrap.Modal.getOrCreateInstance(element).show();
        try {
            const passenger = await fetch(`/passengers/${encodeURIComponent(row.dataset.passengerId)}`, {
                headers: { Accept: 'application/json' }, cache: 'no-store'
            }).then(responseData);
            if (current !== version) return;
            for (const input of fields.querySelectorAll('[name]')) {
                input.value = passenger[input.name] ?? '';
            }
            fields.disabled = false;
            save.disabled = false;
            status.textContent = '';
        } catch (error) {
            if (current === version) status.textContent = error.message;
        }
    }

    table.addEventListener('click', event => {
        if (event.target.closest('a, button, input, select, textarea')) return;
        const row = event.target.closest('[data-passenger-id]');
        if (row) open(row);
    });
    table.addEventListener('keydown', event => {
        if (event.target.matches('[data-passenger-id]') && ['Enter', ' '].includes(event.key)) {
            event.preventDefault();
            open(event.target);
        }
    });
    element.addEventListener('hide.bs.modal', event => {
        if (saving) event.preventDefault();
        else ++version;
    });
    element.addEventListener('hidden.bs.modal', () => activeRow?.focus());
    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (saving || fields.disabled) return;
        const payload = Object.fromEntries(new FormData(form));
        saving = true;
        fields.disabled = true;
        save.disabled = true;
        status.textContent = 'Guardando…';
        try {
            const headers = { 'Content-Type': 'application/json', Accept: 'application/json' };
            headers[document.querySelector('meta[name="_csrf_header"]').content] =
                document.querySelector('meta[name="_csrf"]').content;
            const passenger = await fetch(`/passengers/${encodeURIComponent(activeRow.dataset.passengerId)}`, {
                method: 'PUT', headers, body: JSON.stringify(payload)
            }).then(responseData);
            const cells = activeRow.cells;
            cells[0].textContent = `${passenger.firstName} ${passenger.lastName}`;
            cells[1].textContent = passenger.cuil || '-';
            cells[2].textContent = passenger.phone || '-';
            cells[3].querySelector('a').href = `/admin/chat/${encodeURIComponent(passenger.phone || '')}`;
            status.textContent = 'Cambios guardados.';
            filtrarPasajeros();
        } catch (error) {
            status.textContent = error.message;
        } finally {
            saving = false;
            fields.disabled = false;
            save.disabled = false;
        }
    });
})();
