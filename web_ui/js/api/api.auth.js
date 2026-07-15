Object.assign(window.API, {
    getCurrentUser: () => {
        // Read from localStorage - written there by fetchCurrentUser after login
        const raw = localStorage.getItem('cov_active_user');
        try { return raw ? JSON.parse(raw) : null; } catch (e) { return null; }
    },
    logout: async () => {
        try { await fetch(`${window.API.API_BASE}/api/auth/logout`); } catch (e) { }
        if (window.WS) window.WS.disconnect(); // Feature 14
        localStorage.removeItem('cov_active_user');
        localStorage.removeItem('cov_session');
        window.location.href = 'auth.html';
    },
    init: async () => {
        await window.API.fetchCurrentUser();
    },
    fetchCurrentUser: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/auth/current-user`);
            const data = await res.json();
            if (data.user) {
                DB.set('cov_active_user', data.user);
                if (window.WS) window.WS.connect(data.user.id);
                if (window.setupSidebar) window.setupSidebar();
                if (window.updateMoneyPill) window.updateMoneyPill();
                return data.user;
            }
        } catch (e) { console.warn("Backend unreachable, using offline mode."); }
        return null;
    },
    login: async (email, password) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            const data = await res.json();
            if (data.success) {
                const userRes = await fetch(`${window.API.API_BASE}/api/auth/current-user`);
                const userData = await userRes.json();
                return { success: true, user: userData.user };
            }
            return { error: data.error };
        } catch (e) {
            return { error: "Backend non disponible." };
        }
    },
    register: async (name, email, password, gender, role = 'PASSENGER', phone) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password, gender, role, phone })
            });
            const data = await res.json();
            if (data.success) {
                const userRes = await fetch(`${window.API.API_BASE}/api/auth/current-user`);
                const userData = await userRes.json();
                return { success: true, user: userData.user };
            }
            return { error: data.error };
        } catch (e) {
            return { error: "Backend non disponible." };
        }
    },
});
