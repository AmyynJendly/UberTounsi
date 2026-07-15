Object.assign(window.API, {
    getAdminStats: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/stats`);
            return await res.json();
        } catch (e) { return { totalUsers: 0, totalTrips: 0, totalRevenue: 0, activeBookings: 0 }; }
    },
    adminGetReports: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/reports`);
            return await res.json();
        } catch (e) { return []; }
    },
    adminDismissReport: async (id) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/dismiss-report`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id })
            });
            return await res.json();
        } catch (e) { return { error: "Erreur" }; }
    },
    adminGetUsers: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/users`);
            return await res.json();
        } catch (e) { return []; }
    },
    broadcastMessage: async (msg) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/broadcast`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: msg })
            });
            return await res.json();
        } catch (e) { return { error: "Erreur serveur" }; }
    },
    adminBlockAccount: async (id, block = true) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/block-user`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId: id, block })
            });
            return await res.json();
        } catch (e) { return { error: "Erreur" }; }
    },
    adminDeleteTrip: async (id) => { let ts = await window.API.fetchTrips(); DB.set('cov_trips', ts.filter(t => t.id !== id)); return { success: true }; },
});
