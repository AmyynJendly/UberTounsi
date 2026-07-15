async function renderNotificationsPanel() {
    const notes = await window.API.getNotifications();
    document.getElementById('view-notifications').innerHTML = `
        <div class="panel-header"><div style="display:flex;justify-content:space-between;align-items:center"><h1 class="panel-title">Notifications</h1><span style="font-size:12px;color:var(--text-muted);font-weight:600">${notes.filter(n => !n.isRead).length} non lue(s)</span></div>
        <div style="display:flex;gap:8px;margin-top:12px;padding-bottom:12px">
            <button class="btn btn-outline btn-xs" onclick="readAllNotifs()">✓ Tout marquer lu</button>
            <button class="btn btn-danger btn-xs" onclick="deleteAllNotifs()">🗑️ Tout supprimer</button>
        </div></div>
        <div class="panel-body" style="padding-top:4px">${notes.length === 0 ? '<div class="empty-state"><h3>Pas de notifications</h3></div>' : notes.map(n => `<div class="notif-item"><span class="notif-dot ${n.isRead ? 'read' : ''}"></span><div class="notif-content" onclick="window.API.markNotificationRead(${n.id});renderNotificationsPanel()"><div class="notif-title">${n.title || 'Notification'}</div><div class="notif-content">${n.message || 'Notification sans texte'}</div><div class="notif-date">${n.sentAt || ''}</div></div><button class="notif-delete" onclick="deleteNotif(${n.id})" title="Supprimer">${ICO.trash}</button></div>`).join('')}</div>`;
}
window.readAllNotifs = async function () {
    await window.API.readAllNotifs();
    Toast.show('Tout marqué comme lu'); renderNotificationsPanel(); setupSidebar();
};
window.deleteNotif = async function (id) {
    await window.API.deleteNotif(id);
    renderNotificationsPanel();
};
window.deleteAllNotifs = async function () {
    await window.API.deleteAllNotifs();
    Toast.show('Notifications supprimées'); renderNotificationsPanel(); setupSidebar();
};

// --- MESSAGES (trip info + fixed input) ---

window.Router.register("notifications", renderNotificationsPanel);
window.Sidebar.register({ view: 'notifications', icon: ICO.bell, tip: () => window.t('notifications'), badge: async () => (await window.API.getUnreadCount()) > 0, order: 6 });
