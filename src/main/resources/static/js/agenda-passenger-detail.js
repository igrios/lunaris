(() => {
    const board = document.getElementById('agendaBoard');
    const modalElement = document.getElementById('passengerDetailModal');
    let activeCard;
    const setText = (id, value, fallback) => {
        document.getElementById(id).textContent = value?.trim() || fallback;
    };
    function openDetail(card) {
        activeCard = card;
        const data = card.dataset;
        for (const [field, fallback] of Object.entries({
            Name: 'Sin nombre', Phone: 'Sin teléfono', Origin: 'Sin origen',
            Address: 'Sin dirección registrada', Destination: 'Sin destino',
            Schedule: 'Sin horario', Seats: '1', Companions: 'Sin acompañantes',
            Notes: 'Sin observaciones'
        })) {
            const key = field === 'Name' ? 'passengerName' : field.toLowerCase();
            setText(`detail${field}`, data[key], fallback);
        }
        const amount = data.amount?.trim();
        setText('detailAmount', amount && Number.isFinite(Number(amount))
            ? new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(Number(amount))
            : '', 'Sin importe registrado');
        const whatsapp = document.getElementById('detailWhatsapp');
        const phone = (data.phone || '').replace(/\D/g, '');
        whatsapp.hidden = !phone;
        whatsapp.removeAttribute('href');
        if (phone) whatsapp.href = `https://wa.me/${phone}`;
        const receipt = document.getElementById('detailReceipt');
        receipt.hidden = true;
        receipt.removeAttribute('href');
        try {
            const url = new URL(data.receipt);
            if (['https:', 'http:'].includes(url.protocol)) {
                receipt.href = url.href;
                receipt.hidden = false;
            }
        } catch (_) { /* La reserva no tiene una URL de comprobante. */ }
        bootstrap.Modal.getOrCreateInstance(modalElement).show();
    }
    board.addEventListener('click', event => {
        const card = event.target.closest('.passenger-card');
        if (card && board.contains(card)) openDetail(card);
    });
    board.addEventListener('keydown', event => {
        const card = event.target.closest('.passenger-card');
        if (card && (event.key === 'Enter' || event.key === ' ')) {
            event.preventDefault();
            openDetail(card);
        }
    });
    modalElement.addEventListener('hidden.bs.modal', () => activeCard?.focus());
    for (const mode of ['board', 'list']) {
        document.getElementById(`${mode}ViewButton`).addEventListener('click', () => {
            board.hidden = mode !== 'board';
            document.getElementById('agendaList').hidden = mode !== 'list';
            for (const option of ['board', 'list']) {
                const button = document.getElementById(`${option}ViewButton`);
                button.setAttribute('aria-pressed', String(option === mode));
                button.classList.toggle('btn-primary', option === mode);
                button.classList.toggle('btn-outline-primary', option !== mode);
            }
        });
    }
})();
