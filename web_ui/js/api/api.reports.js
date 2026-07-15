Object.assign(window.API, {
    submitReport: async (reportedId, reason) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/reports/submit`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ reportedId, reason })
            });
            return await res.json();
        } catch (e) {
            return { error: "Erreur réseau" };
        }
    },
});
