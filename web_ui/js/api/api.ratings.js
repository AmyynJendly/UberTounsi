Object.assign(window.API, {
    addRating: async (ratingData) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/ratings/add`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(ratingData)
            });
            return await res.json();
        } catch (e) { return { error: "Non disponible" }; }
    },
    getRatings: async (userId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/ratings/list?userId=${userId}`);
            return await res.json();
        } catch (e) { return []; }
    },
    getTripRatings: async (tripId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/ratings/list?tripId=${tripId}`);
            return await res.json();
        } catch (e) { return []; }
    },
    getUserRating: async (userId) => {
        return 5.0; // Server doesn't average ratings yet.
    },
});
