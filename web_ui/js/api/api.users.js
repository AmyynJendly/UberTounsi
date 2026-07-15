Object.assign(window.API, {
    toggleSocialBlock: async (blockedId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/users/block`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ blockedId })
            });
            return await res.json();
        } catch (e) {
            return { error: "Erreur de connexion" };
        }
    },
    isBlocked: async (id1, id2) => {
        try {
            const otherId = (window.API.getCurrentUser()?.id === id1) ? id2 : id1;
            if (!otherId) return false;
            const res = await fetch(`${window.API.API_BASE}/api/users/is-blocked?otherId=${otherId}`);
            const data = await res.json();
            return data.blocked || false;
        } catch (e) { return false; }
    },
    getUsersList: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/users/list`);
            return await res.json();
        } catch (e) { return []; }
    },
    updateProfile: async (data) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/users/update-profile`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            return await res.json();
        } catch (e) { return { error: "Erreur réseau" }; }
    },
    deleteAccount: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/users/delete-account`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            const data = await res.json();
            if (data.success) {
                Toast.show("Compte supprimé definitivement");
                window.API.logout();
            } else {
                Toast.show(data.error || "Erreur lors de la suppression", "error");
            }
        } catch (e) { Toast.show("Serveur injoignable", "error"); }
    },
});
