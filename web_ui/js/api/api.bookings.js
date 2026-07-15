Object.assign(window.API, {
    getMyBookings: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/bookings/my`);
            return await res.json();
        } catch (e) { return []; }
    },
    bookTrip: async (tripId, qty) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/trips/book`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ tripId, qty })
            });
            const data = await res.json();
            if (data.success) {
                await window.API.fetchCurrentUser(); // Refresh balance
                return { success: true };
            }
            return { error: data.error };
        } catch (e) {
            return { error: "Erreur serveur. Impossible de réserver hors-ligne." };
        }
    },
    acceptBooking: async (id) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/bookings/respond`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ requestId: id, status: 'ACCEPTED' })
            });
            const data = await res.json();
            if (data.success) await window.API.fetchCurrentUser();
            return data;
        } catch (e) { return { error: "Non disponible" }; }
    },
    refuseBooking: async (id) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/bookings/respond`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ requestId: id, status: 'REJECTED' })
            });
            const data = await res.json();
            if (data.success) await window.API.fetchCurrentUser();
            return data;
        } catch (e) { return { error: "Non disponible" }; }
    },
    getDriverRequests: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/bookings/requests`);
            const data = await res.json();
            return Array.isArray(data) ? data : [];
        } catch (e) { return []; }
    },
    verifyPassengerCode: async (requestId, code) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/bookings/verify`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ requestId, code })
            });
            const data = await res.json();
            if (data.success) await window.API.fetchCurrentUser();
            return data;
        } catch (e) {
            return { error: "Vérification impossible." };
        }
    },
    cancelBooking: async (id) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/bookings/cancel`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ bookingId: id })
            });
            const data = await res.json();
            if (data.success || data.message) await window.API.fetchCurrentUser();
            return data;
        } catch (e) {
            return { error: "Annulation impossible. Serveur injoignable." };
        }
    },
});
