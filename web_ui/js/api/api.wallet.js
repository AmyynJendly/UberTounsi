Object.assign(window.API, {
    addFunds: async (amount) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/wallet/add-funds`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ amount })
            });
            const data = await res.json();
            if (data.success) {
                await window.API.fetchCurrentUser(); // Refresh balance
                return { success: true };
            }
            return { error: data.error };
        } catch (e) {
            return { error: "Paiement impossible hors-ligne." };
        }
    },
    withdrawFunds: async (amount) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/wallet/withdraw`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ amount })
            });
            const data = await res.json();
            if (data.success) {
                await window.API.fetchCurrentUser(); // Refresh balance
                return { success: true };
            }
            return { error: data.error };
        } catch (e) {
            return { error: "Retrait impossible hors-ligne." };
        }
    },
    getPaymentMethods: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/payments/list`);
            const data = await res.json();
            return Array.isArray(data) ? data : [];
        } catch (e) { return []; }
    },
    addPaymentMethod: async (method) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/payments/add`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(method)
            });
            return await res.json();
        } catch (e) { return { error: "Erreur réseau" }; }
    },
    deletePaymentMethod: async (id) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/payments/delete`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id })
            });
            return await res.json();
        } catch (e) { return { error: "Erreur réseau" }; }
    }
});
