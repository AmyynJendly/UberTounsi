Object.assign(window.API, {
    saveSearch: async (searchData) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/searches/save`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(searchData)
            });
            return await res.json();
        } catch (e) { return { error: "Erreur" }; }
    },
    getSavedSearches: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/searches/my`);
            return await res.json();
        } catch (e) { return []; }
    },
    deleteSavedSearch: async (id) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/searches/delete`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id })
            });
            return await res.json();
        } catch (e) { return { error: "Erreur" }; }
    },
});
