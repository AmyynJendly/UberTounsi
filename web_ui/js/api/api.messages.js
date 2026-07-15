Object.assign(window.API, {
    getMessages: async (receiverId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/messages/history?otherId=${receiverId}`);
            const data = await res.json();
            if (Array.isArray(data)) return data;
        } catch (e) { }
        return [];
    },
    sendMessage: async (receiverId, content) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/messages/send`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ receiverId, content })
            });
            return await res.json();
        } catch (e) {
            return { error: "Erreur de connexion" };
        }
    },
});
