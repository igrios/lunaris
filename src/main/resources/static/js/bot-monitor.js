(() => {
    const badge = document.getElementById('connStatus');
    let refreshing = false;
    let pending = false;
    async function refreshRows() {
        pending = true;
        if (refreshing) return;
        refreshing = true;
        try {
            while (pending) {
                pending = false;
                const response = await fetch('/admin/bot/monitor/rows');
                if (!response.ok || response.redirected) throw new Error('No se pudo actualizar el monitor');
                const html = document.createElement('table');
                html.innerHTML = await response.text();
                const rows = html.querySelector('#monitorBody');
                if (!rows) throw new Error('Respuesta inválida del monitor');
                document.getElementById('monitorBody').replaceWith(rows);
            }
        } catch (error) {
            badge.textContent = error.message;
            badge.className = 'badge bg-warning';
        } finally { refreshing = false; }
    }
    document.addEventListener('click', async event => {
        const button = event.target.closest('button[data-session-id]');
        if (!button) return;
        button.disabled = true;
        try {
            const headers = {'Content-Type': 'application/x-www-form-urlencoded'};
            const csrf = document.querySelector('meta[name="_csrf"]');
            if (csrf) headers[document.querySelector('meta[name="_csrf_header"]').content] = csrf.content;
            const response = await fetch('/admin/bot/monitor/pause', {
                method: 'POST', headers,
                body: new URLSearchParams({id: button.dataset.sessionId, paused: button.dataset.paused})
            });
            if (!response.ok || response.redirected) throw new Error('No se pudo cambiar el estado del bot');
            await refreshRows();
        } catch (error) { alert(error.message); }
        finally { button.disabled = false; }
    });
    function connect() {
        const stomp = Stomp.over(new SockJS('/chat-websocket'));
        stomp.debug = null;
        stomp.connect({}, () => {
            badge.textContent = 'Sistema en Vivo';
            badge.className = 'badge bg-success';
            stomp.subscribe('/topic/bot-monitor', refreshRows);
            stomp.subscribe('/topic/system-alerts', refreshRows);
            refreshRows();
        }, () => {
            badge.textContent = 'Reconectando…';
            badge.className = 'badge bg-warning';
            setTimeout(connect, 3000);
        });
    }
    connect();
})();
