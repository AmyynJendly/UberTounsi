async function renderMessagesPanel() {
    const u = window.API.getCurrentUser();
    const allUsers = await window.API.getUsersList() || [];
    const trips = await window.API.fetchTrips() || [];
    const bks = await window.API.getMyBookings() || [];

    if (!Array.isArray(allUsers)) {
        document.getElementById('view-messages').innerHTML = '<div class="empty-state"><p>Erreur de chargement.</p></div>';
        return;
    }
    const users = allUsers.filter(x => x.id !== u.id);

    // Accepted bookings with a trip → group chat entries
    const acceptedBks = bks.filter(b => b.status === 'ACCEPTED');
    const groupChats = acceptedBks.map(b => {
        const trip = trips.find(t => t.id === b.tripId);
        return trip ? { tripId: trip.id, label: `${trip.departure} → ${trip.arrival}` } : null;
    }).filter(Boolean);
    // Also add trips the user drives that have accepted passengers
    if (u.role !== 'PASSENGER') {
        const myTrips = await window.API.fetchTrips().then(all => all.filter(t => t.driverId === u.id)).catch(() => []);
        myTrips.forEach(t => {
            if (!groupChats.find(g => g.tripId === t.id))
                groupChats.push({ tripId: t.id, label: `${t.departure} → ${t.arrival}` });
        });
    }

    const groupSection = groupChats.length === 0 ? '' : `
        <div style="padding:0 16px 4px">
            <div style="font-size:11px;font-weight:800;text-transform:uppercase;letter-spacing:0.5px;color:var(--text-muted);margin-bottom:8px">💬 Chats de trajet</div>
            ${groupChats.map(g => `
                <div class="msg-item" onclick="openTripChatModal(${g.tripId}, '${g.label.replace(/'/g, "\\'")}')">
                    <div style="width:40px;height:40px;border-radius:50%;background:var(--lime);display:flex;align-items:center;justify-content:center;font-size:20px;flex-shrink:0;color:var(--dark)">${ICO.groupChat}</div>
                    <div class="msg-info">
                        <div class="msg-name">${g.label}</div>
                        <div class="msg-preview">Chat du groupe trajet</div>
                    </div>
                </div>`).join('')}
            <div style="height:1px;background:var(--border);margin:12px 0"></div>
        </div>`;

    document.getElementById('view-messages').innerHTML = `
        <div class="panel-header" style="display:flex; justify-content:space-between; align-items:center">
            <div>
                <h1 class="panel-title">Messages</h1>
                <p class="panel-subtitle">${users.length} contact(s) concerné(s)</p>
            </div>
            ${u.role === 'ADMIN' ? `<button class="btn btn-lime btn-xs" onclick="broadcastChat()">📢 Diffuser</button>` : ''}
        </div>
        <div class="panel-body" style="padding-top:8px">
            ${groupSection}
            ${users.length === 0 ? '<div class="empty-state"><p>Aucun contact pour le moment.</p></div>' : users.map(c => {
        const rel = trips.find(t => t.driverId === c.id) || trips.find(t => bks.some(b => b.userId === c.id && b.tripId === t.id));
        const tripLabel = rel ? `${rel.departure} → ${rel.arrival}` : '';
        return `<div class="msg-item" onclick="openChat(${c.id})"><img class="msg-avatar" src="${c.avatar || 'https://ui-avatars.com/api/?name=' + c.name.replace(' ', '+')}"><div class="msg-info"><div class="msg-name">${c.name}</div>${tripLabel ? `<span class="msg-trip">${tripLabel}</span>` : ''}<div class="msg-preview">${c.role === 'DRIVER' ? 'Chauffeur' : c.role === 'ADMIN' ? 'Support Admin' : 'Passager'}</div></div></div>`;
    }).join('')}
        </div>`;
}

window.broadcastChat = function () {
    const modal = document.getElementById('book-modal');
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:12px">Diffusion Admin</h2>
        <p style="font-size:13px;color:var(--text-muted);margin-bottom:20px">Ce message sera envoyé à TOUS les utilisateurs de la plateforme.</p>
        <div class="form-group mb-4">
            <textarea class="form-input" id="broadcast-msg" style="height:120px;padding:12px" placeholder="Ecrivez votre message ici..."></textarea>
        </div>
        <button class="btn btn-primary btn-full" onclick="confirmBroadcast()">Diffuser le message</button>`;
    modal.classList.add('open');
};

window.confirmBroadcast = async function () {
    const msg = document.getElementById('broadcast-msg').value.trim();
    if (!msg) return Toast.show('Veuillez saisir un message', 'error');
    const res = await window.API.broadcastMessage(msg);
    if (res.success) {
        Toast.show('Message diffusé avec succès !');
        document.getElementById('book-modal').classList.remove('open');
    } else Toast.show(res.error, 'error');
};
window.openChat = async function (userId) {
    const u = window.API.getCurrentUser();
    const allUsers = await window.API.getUsersList() || [];
    const other = Array.isArray(allUsers) ? allUsers.find(x => x.id === userId) : null;
    if (!other) return;
    const msgs = await window.API.getMessages(userId);
    const panel = document.getElementById('view-messages');
    panel.innerHTML = `
        <div class="panel-header" style="padding-bottom:12px; display:flex; justify-content:space-between; align-items:center;">
            <div><button class="btn btn-ghost btn-sm" onclick="renderMessagesPanel()" style="margin-bottom:6px">&larr; Retour</button><h1 class="panel-title">${other.name}</h1></div>
            <button class="btn btn-lime btn-xs" onclick="openCallModal(${userId})" style="padding:0 12px; height:32px;">📞 ${t('call')}</button>
        </div>
        <div class="chat-container">
            <div class="chat-messages" id="chat-msgs">${msgs.map(m => `<div style="margin-bottom:10px;${m.senderId === u.id ? 'text-align:right' : ''}"><div style="display:inline-block;background:${m.senderId === u.id ? 'var(--dark);color:#fff' : 'var(--bg)'};padding:9px 14px;border-radius:14px;font-size:13px;max-width:75%;text-align:left">${m.content}</div><div style="font-size:10px;color:var(--text-muted);margin-top:3px">${new Date(m.sentAt).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}</div></div>`).join('')}</div>
            <div class="chat-input-bar"><input class="form-input" id="chat-input" placeholder="Votre message…" style="flex:1;height:42px" onkeydown="if(event.key==='Enter'){event.preventDefault();sendChatMsg(${userId})}"><button class="btn btn-primary btn-sm" onclick="sendChatMsg(${userId})">Envoyer</button></div>
        </div>`;
    const msgBox = document.getElementById('chat-msgs');
    msgBox.scrollTop = msgBox.scrollHeight;
};
window.sendChatMsg = async function (userId) {
    const input = document.getElementById('chat-input');
    const msg = input.value.trim();
    if (!msg) return;
    await window.API.sendMessage(userId, msg);
    input.value = '';
    openChat(userId);
    setTimeout(() => { const m = document.getElementById('chat-msgs'); if (m) m.scrollTop = m.scrollHeight; }, 50);
};

window.openCallModal = async function (userId) {
    const allUsers = await window.API.getUsersList() || [];
    const other = allUsers.find(x => x.id === userId);
    if (!other) return;

    const modal = document.getElementById('book-modal');
    document.getElementById('book-modal-body').innerHTML = `
        <div style="text-align:center; padding:10px 0;">
            <div style="width:80px; height:80px; border-radius:50%; margin:0 auto 16px; overflow:hidden; border:2px solid var(--lime); background:var(--bg)">
                <img src="${other.avatar || 'https://ui-avatars.com/api/?name=' + other.name.replace(' ', '+')}" style="width:100%; height:100%; object-fit:cover;">
            </div>
            <h2 style="font-size:18px; font-weight:800; margin-bottom:4px; color:var(--dark)">${other.name}</h2>
            <p style="font-size:13px; color:var(--text-muted); margin-bottom:24px;">Disponible pour coordonner votre trajet</p>
            
            <div style="background:var(--bg); border:1px solid var(--border); border-radius:var(--radius-sm); padding:24px; margin-bottom:24px; box-shadow:inset 0 2px 4px rgba(0,0,0,0.02)">
                <div style="font-size:11px; font-weight:800; color:var(--text-muted); text-transform:uppercase; margin-bottom:10px; letter-spacing:0.5px">${t('phone')}</div>
                <div style="font-size:26px; font-weight:900; color:var(--dark); letter-spacing:1.5px; font-family:monospace">${other.phone || '—'}</div>
            </div>

            <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                <button class="btn btn-outline btn-full" onclick="document.getElementById('book-modal').classList.remove('open')" style="height:48px; font-weight:700">Fermer</button>
                <a href="tel:${other.phone}" class="btn btn-lime btn-full" style="text-decoration:none; height:48px; display:flex; align-items:center; justify-content:center; font-weight:800; gap:8px">
                    <span>${ICO.phone || '📞'}</span> ${t('call')}
                </a>
            </div>
        </div>`;
    modal.classList.add('open');
};

// --- PROFILE (redesigned) ---
window._profileTab = 'general';
window.setProfileTab = function (t) { window._profileTab = t; renderProfilePanel(); };

window.Router.register("messages", renderMessagesPanel);
window.Sidebar.register({ view: 'messages', icon: ICO.chat, tip: () => window.t('nav_msgs'), order: 7 });
