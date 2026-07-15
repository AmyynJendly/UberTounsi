/**
 * API module for all new features (1–14).
 * Follows the same pattern as existing api.*.js files:
 *   Object.assign(window.API, { ... })
 */
Object.assign(window.API, {

    // ── Feature 1: Trip Recurrence ───────────────────────────────────────────
    getTripOccurrences: async (tripId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/trips/occurrences?tripId=${tripId}`);
            return await res.json();
        } catch (e) { return []; }
    },

    // ── Feature 2: Boarding Code Verification ───────────────────────────────
    verifyBoardingCode: async (requestId, code) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/bookings/verify-code`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ requestId, code })
            });
            return await res.json();
        } catch (e) { return { valid: false }; }
    },

    // ── Feature 3: Trip Group Chat ───────────────────────────────────────────
    sendTripChatMessage: async (tripId, content) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/chat/send`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ tripId, content })
            });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },
    getTripChatMessages: async (tripId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/chat/messages?tripId=${tripId}`);
            return await res.json();
        } catch (e) { return []; }
    },

    // ── Feature 4: OTP Verification ─────────────────────────────────────────
    generateOtp: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/otp/generate`, { method: 'POST' });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },
    verifyOtp: async (code) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/otp/verify`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code })
            });
            const data = await res.json();
            if (data.success) await window.API.fetchCurrentUser();
            return data;
        } catch (e) { return { success: false }; }
    },

    // ── Feature 5: ID Verification (admin) ──────────────────────────────────
    adminGetPendingIdVerifications: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/id-verifications`);
            return await res.json();
        } catch (e) { return []; }
    },
    adminApproveId: async (driverId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/id-verify-approve`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ driverId })
            });
            return await res.json();
        } catch (e) { return { success: false }; }
    },
    adminRejectId: async (driverId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/id-verify-reject`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ driverId })
            });
            return await res.json();
        } catch (e) { return { success: false }; }
    },

    // ── Feature 7: Favorite Drivers ──────────────────────────────────────────
    addFavoriteDriver: async (driverId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/favorites/add`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ driverId })
            });
            return await res.json();
        } catch (e) { return { success: false }; }
    },
    removeFavoriteDriver: async (driverId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/favorites/remove`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ driverId })
            });
            return await res.json();
        } catch (e) { return { success: false }; }
    },
    getMyFavoriteDrivers: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/favorites/my`);
            return await res.json();
        } catch (e) { return []; }
    },
    isDriverFavorite: async (driverId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/favorites/is-favorite?driverId=${driverId}`);
            const data = await res.json();
            return data.favorite === true;
        } catch (e) { return false; }
    },

    // ── Feature 8: Price Negotiation ─────────────────────────────────────────
    proposeCounterOffer: async (requestId, price) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/bookings/counter-offer`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ requestId, price })
            });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },
    acceptCounterOffer: async (requestId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/bookings/accept-offer`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ requestId })
            });
            const data = await res.json();
            if (!data.error) await window.API.fetchCurrentUser();
            return data;
        } catch (e) { return { error: 'Erreur réseau' }; }
    },
    rejectCounterOffer: async (requestId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/bookings/reject-offer`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ requestId })
            });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },

    // ── Feature 10: Driver Earnings ──────────────────────────────────────────
    getDriverEarnings: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/stats/earnings`);
            return await res.json();
        } catch (e) { return { moneyEarned: 0, cancellationRate: 0 }; }
    },

    // ── Feature 12: Promo Codes ──────────────────────────────────────────────
    validatePromoCode: async (code, price) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/promo/validate?code=${encodeURIComponent(code)}&price=${price}`);
            return await res.json();
        } catch (e) { return { valid: false }; }
    },
    adminCreatePromoCode: async (code, discountPct, discountFixed, maxUses) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/promo/create`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code, discountPct, discountFixed, maxUses })
            });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },
    adminGetPromoCodes: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/promo/list`);
            return await res.json();
        } catch (e) { return []; }
    },
    adminDeactivatePromoCode: async (id) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/promo/deactivate`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id })
            });
            return await res.json();
        } catch (e) { return { success: false }; }
    },

    // ── Admin: new endpoints ──────────────────────────────────────────────────
    adminDeleteUser: async (userId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/delete-user`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId })
            });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },
    getAuditLog: async (limit = 50) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/audit-log?limit=${limit}`);
            return await res.json();
        } catch (e) { return []; }
    },
    getSparkline: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/sparkline`);
            return await res.json();
        } catch (e) { return []; }
    },
    uploadIdDocument: async (documentBase64) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/users/upload-id-doc`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ document: documentBase64 })
            });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },
    adminGetIdDocument: async (userId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/get-id-doc?userId=${userId}`);
            return await res.json();
        } catch (e) { return { document: null }; }
    },
    adminRequestIdResubmit: async (driverId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/admin/request-id-resubmit`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ driverId })
            });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },

    // ── Trip History ─────────────────────────────────────────────────────────
    getTripHistory: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/trips/history`);
            return await res.json();
        } catch (e) { return []; }
    },

    // ── Feature 13: Disputes ─────────────────────────────────────────────────
    fileDispute: async (tripId, reason) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/disputes/file`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ tripId, reason })
            });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },
    adminGetDisputes: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/disputes/list`);
            return await res.json();
        } catch (e) { return []; }
    },
    adminResolveDispute: async (disputeId, note, refund) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/disputes/resolve`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ disputeId, note, refund: refund || 0 })
            });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },
    adminDismissDispute: async (disputeId, note) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/disputes/dismiss`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ disputeId, note })
            });
            return await res.json();
        } catch (e) { return { error: 'Erreur réseau' }; }
    },
});
