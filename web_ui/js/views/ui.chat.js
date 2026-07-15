/**
 * Feature 3 – Trip Group Chat UI
 *
 * Renders a chat panel for a specific trip. Accessible to the driver and
 * all accepted passengers. Opened via openTripChatModal(tripId, tripLabel).
 */

window._chatTripId = null;
window._chatPollInterval = null;

window.openTripChatModal = async function (tripId, tripLabel) {
    window._chatTripId = tripId;

    const modal = document.getElementById('generic-modal');
    if (!modal) { Toast.show('Modal non disponible', 'error'); return; }

    document.getElementById('generic-modal-body').innerHTML = `
        <div style="display:flex;flex-direction:column;height:70vh;max-height:600px">
            <div style="padding:16px 20px;border-bottom:1px solid var(--border);flex-shrink:0">
                <h2 style="font-size:16px;font-weight:800;margin:0">💬 Chat du trajet</h2>
                <div style="font-size:12px;color:var(--text-muted);margin-top:2px">${esc(tripLabel || '')}</div>
            </div>
            <div id="chat-messages-list" style="flex:1;overflow-y:auto;padding:12px 16px;display:flex;flex-direction:column;gap:8px">
                <div style="text-align:center;color:var(--text-muted);font-size:13px">Chargement…</div>
            </div>
            <div style="padding:12px 16px;border-top:1px solid var(--border);display:flex;gap:8px;flex-shrink:0">
                <input type="text" id="chat-message-input" class="input" style="flex:1"
                    placeholder="Votre message…"
                    onkeydown="if(event.key==='Enter')sendChatMessage()">
                <button class="btn btn-lime" style="padding:0 16px;height:40px" onclick="sendChatMessage()">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                </button>
            </div>
        </div>`;

    modal.classList.add('open');
    await loadChatMessages();

    // Poll for new messages every 4 seconds while modal is open
    clearInterval(window._chatPollInterval);
    window._chatPollInterval = setInterval(async () => {
        if (!modal.classList.contains('open')) {
            clearInterval(window._chatPollInterval);
            return;
        }
        await loadChatMessages(true);
    }, 4000);

    // Stop polling when modal closes
    const closeBtn = modal.querySelector('.modal-close');
    if (closeBtn) {
        const orig = closeBtn.onclick;
        closeBtn.onclick = function () {
            clearInterval(window._chatPollInterval);
            if (orig) orig.call(this);
            else modal.classList.remove('open');
        };
    }
};

async function loadChatMessages(silent = false) {
    const tripId = window._chatTripId;
    if (!tripId) return;

    const messages = await window.API.getTripChatMessages(tripId);
    const container = document.getElementById('chat-messages-list');
    if (!container) return;

    const currentUser = window.API.getCurrentUser();
    const myId = currentUser ? currentUser.id : -1;

    if (!Array.isArray(messages) || messages.length === 0) {
        if (!silent) container.innerHTML = `<div style="text-align:center;color:var(--text-muted);font-size:13px;padding:24px">Aucun message. Soyez le premier !</div>`;
        return;
    }

    const wasAtBottom = container.scrollHeight - container.scrollTop <= container.clientHeight + 40;

    container.innerHTML = messages.map(m => {
        const isMe = m.senderId === myId;
        return `
            <div style="display:flex;flex-direction:column;align-items:${isMe ? 'flex-end' : 'flex-start'}">
                ${!isMe ? `<div style="font-size:10px;color:var(--text-muted);margin-bottom:2px;padding-left:4px">${esc(m.senderName || 'Participant')}</div>` : ''}
                <div style="
                    max-width:78%;
                    padding:8px 12px;
                    border-radius:${isMe ? '14px 14px 4px 14px' : '14px 14px 14px 4px'};
                    background:${isMe ? 'var(--lime)' : 'var(--surface)'};
                    color:${isMe ? '#000' : 'var(--text)'};
                    font-size:13px;
                    line-height:1.4;
                    word-break:break-word;
                ">${esc(m.content)}</div>
                <div style="font-size:10px;color:var(--text-muted);margin-top:2px;padding:0 4px">
                    ${m.sentAt ? new Date(m.sentAt).toLocaleTimeString('fr-FR', {hour:'2-digit',minute:'2-digit'}) : ''}
                </div>
            </div>`;
    }).join('');

    if (wasAtBottom || !silent) {
        container.scrollTop = container.scrollHeight;
    }
}

window.sendChatMessage = async function () {
    const input = document.getElementById('chat-message-input');
    const content = input ? input.value.trim() : '';
    if (!content) return;

    input.value = '';
    input.disabled = true;

    const res = await window.API.sendTripChatMessage(window._chatTripId, content);
    input.disabled = false;
    input.focus();

    if (res.error) {
        Toast.show(res.error, 'error');
        input.value = content; // restore on failure
        return;
    }
    await loadChatMessages();
};
