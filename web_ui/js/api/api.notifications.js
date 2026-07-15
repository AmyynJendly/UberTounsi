Object.assign(window.API, {
    getNotifications: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/notifications/list`);
            return await res.json();
        } catch (e) { return []; }
    },
    getUnreadCount: async () => {
        const notes = await window.API.getNotifications();
        return Array.isArray(notes) ? notes.filter(n => !n.isRead).length : 0;
    },
    markNotificationRead: async (id) => {
        try { await fetch(`${window.API.API_BASE}/api/notifications/read?id=${id}`, { method: 'POST' }); } catch (e) { }
    },
    readAllNotifs: async () => {
        try { await fetch(`${window.API.API_BASE}/api/notifications/read-all`, { method: 'POST' }); } catch (e) { }
    },
    deleteAllNotifs: async () => {
        try { await fetch(`${window.API.API_BASE}/api/notifications/delete-all`, { method: 'POST' }); } catch (e) { }
    },
    deleteNotif: async (id) => {
        try { await fetch(`${window.API.API_BASE}/api/notifications/delete?id=${id}`, { method: 'POST' }); } catch (e) { }
    },
});
