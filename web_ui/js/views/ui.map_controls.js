// Register Search Button
window.MapControls.register({
    id: 'map-search-btn',
    order: 1,
    title: 'Rechercher',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8" /><path d="m21 21-4.3-4.3" /></svg>',
    onClick: function() {
        const searchBtn = document.getElementById('map-search-btn');
        const searchOverlay = document.getElementById('map-search-overlay');
        const searchInput = document.getElementById('map-search-input');
        searchOverlay.classList.toggle('open');
        if (searchOverlay.classList.contains('open')) {
            searchBtn.classList.add('active');
            setTimeout(() => searchInput.focus(), 320);
        } else {
            searchBtn.classList.remove('active');
            searchInput.value = '';
        }
    }
});

// Register Theme Toggle Button
window.MapControls.register({
    id: 'map-theme-btn',
    order: 1.5, // Between Search and Location
    title: 'Thème',
    icon: document.body.classList.contains('dark-theme') ? ICO.sun : ICO.moon,
    onClick: function() {
        window.DB.toggleTheme();
    }
});

// Search input listener (needs to be attached once)
document.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
        const searchInput = document.getElementById('map-search-input');
        if (searchInput) {
            searchInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') window.mapSearchGo();
                if (e.key === 'Escape') {
                    const searchOverlay = document.getElementById('map-search-overlay');
                    const searchBtn = document.getElementById('map-search-btn');
                    if (searchOverlay) searchOverlay.classList.remove('open');
                    if (searchBtn) searchBtn.classList.remove('active');
                    searchInput.value = '';
                }
            });
        }
    }, 100);
});

// Register Precise Location Button
window.MapControls.register({
    id: 'map-center-btn',
    order: 2,
    title: 'Ma position',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10" /><circle cx="12" cy="12" r="3" /><path d="M12 2v4M12 18v4M2 12h4M18 12h4" /></svg>',
    onClick: function() {
        const centerBtn = document.getElementById('map-center-btn');
        if (!navigator.geolocation) {
            alert("La geolocalisation n'est pas disponible sur votre navigateur.");
            return;
        }
        centerBtn.classList.add('active');
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                const { latitude: lat, longitude: lng } = pos.coords;
                if (window.locationMarker) { window.map.removeLayer(window.locationMarker); window.locationMarker = null; }
                window.locationMarker = L.marker([lat, lng], {
                    icon: L.divIcon({
                        className: 'user-location-dot',
                        iconSize: [18, 18],
                        iconAnchor: [9, 9]
                    })
                }).addTo(window.map);
                window.map.setView([lat, lng], 15, { animate: true });
                setTimeout(() => centerBtn.classList.remove('active'), 1500);
            },
            (err) => {
                centerBtn.classList.remove('active');
                const msgs = {
                    1: "Permission refusee. Autorisez la localisation dans votre navigateur.",
                    2: "Position indisponible. Verifiez votre GPS.",
                    3: "Delai depasse. Reessayez."
                };
                alert(msgs[err.code] || 'Impossible de vous localiser.');
            },
            { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
        );
    }
});

// Register Satellite Button
window.MapControls.register({
    id: 'map-satellite-btn',
    order: 3,
    title: 'Satellite',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8z" /><path d="M12 6a6 6 0 1 0 6 6 6 6 0 0 0-6-6zm0 10a4 4 0 1 1 4-4 4 4 0 0 1-4 4z" /></svg>',
    onClick: function() {
        if (window.toggleSatellite) window.toggleSatellite();
    }
});
