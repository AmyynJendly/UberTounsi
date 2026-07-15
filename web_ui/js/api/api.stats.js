Object.assign(window.API, {
    getUserStats: async (userId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/stats/my?userId=${userId}`);
            const data = await res.json();
            if (data && typeof data.totalTrips !== 'undefined') return data;
            throw new Error("Invalid stats data");
        } catch (e) {
            return { totalTrips: 0, moneySaved: 0, co2Saved: 0, trees: 0 };
        }
    },
    getStats: async () => {
        const u = window.API.getCurrentUser();
        if (!u) return { totalTrips: 0, moneySaved: 0, co2Saved: 0, trees: 0 };
        const bks = await window.API.getMyBookings();
        const filteredBks = bks.filter(b => b.userId === u.id);
        const co2 = filteredBks.length * 2.1;
        return { totalTrips: filteredBks.length, moneySaved: filteredBks.reduce((s, b) => s + b.totalCost, 0), co2Saved: co2, trees: co2 / 22 };
    },
});
