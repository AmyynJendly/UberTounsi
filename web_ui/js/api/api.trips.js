Object.assign(window.API, {
    getTrips: async (forceRefresh) => {
        if (!forceRefresh && window.TripCache) {
            const cached = window.TripCache.get();
            if (cached) return cached;
        }
        try {
            const res = await fetch(`${window.API.API_BASE}/api/trips/all`);
            const data = await res.json();
            if (window.TripCache && Array.isArray(data)) window.TripCache.set(data);
            return data;
        } catch (e) { return []; }
    },
    fetchTrips: async () => { return await window.API.getTrips(true); },
    getDistance: (from, to) => {
        const matrix = window.DISTANCE_MATRIX || {};
        const key = `${from}-${to}`;
        return matrix[key] || Math.floor(Math.random() * (200 - 50) + 50);
    },
    searchTrips: async (params) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/trips/search`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(params)
            });
            const data = await res.json();
            return data;
        } catch (e) {
            console.error("API Error:", e);
            return []; // No mock fallback
        }
    },
    getTripDetails: async (id) => {
        const res = await fetch(`${window.API.API_BASE}/api/trips/${id}`);
        return res.json();
    },
    publishTrip: async (tripData) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/trips/publish`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(tripData)
            });
            const data = await res.json();
            if (data.success) return { success: true };
            return { error: data.error };
        } catch (e) {
            // Mock Fallback
            const u = window.API.getCurrentUser();
            let trips = await window.API.fetchTrips() || [];
            const newTrip = { id: Date.now(), driverId: u.id, driver: u.name, avatar: `https://ui-avatars.com/api/?name=${u.name.replace(' ', '+')}`, rating: 5.0, publishDate: new Date().toISOString(), date: tripData.date || new Date().toISOString().split('T')[0], comment: tripData.comment || '', flexible: !!tripData.flexible, ...tripData };
            trips.unshift(newTrip);
            DB.set('cov_trips', trips);
            return { success: true, trip: newTrip };
        }
    },
    cancelTrip: async (id) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/trips/cancel`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ tripId: id })
            });
            const data = await res.json();
            if (data.success || data.message) await window.API.fetchCurrentUser();
            return data;
        } catch (e) { return { error: "Non disponible" }; }
    },
    completeTrip: async (id) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/trips/complete`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ tripId: id })
            });
            return await res.json();
        } catch (e) { return { error: "Non disponible" }; }
    },
    getAcceptedPassengers: async (tripId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/trips/accepted-passengers?tripId=${tripId}`);
            return await res.json();
        } catch (e) { return []; }
    },
});
