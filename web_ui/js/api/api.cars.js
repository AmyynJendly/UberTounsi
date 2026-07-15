Object.assign(window.API, {
    getCars: async () => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/cars/list`);
            return await res.json();
        } catch (e) {
            return []; // No offline fallback for cars
        }
    },
    addCar: async (carData) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/cars/add`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(carData)
            });
            return await res.json();
        } catch (e) {
            console.error("Add Car Error:", e);
            return { success: false, error: "Serveur injoignable" };
        }
    },
    deleteCar: async (carId) => {
        try {
            const res = await fetch(`${window.API.API_BASE}/api/cars/delete`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ carId })
            });
            return await res.json();
        } catch (e) {
            return { success: false };
        }
    },
});
