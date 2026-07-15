
window.CITIES = {};
window.CITY_NAMES = [];
window.GOVERNORATES = {};
window.GOV_NAMES = [];
window.CAR_BRANDS = [];
window.CAR_COLORS = [];
window.DISTANCE_MATRIX = {};

async function loadData() {
    try {
        const [cities, govs, brands, colors, dists] = await Promise.all([
            fetch('data/cities.json').then(r => r.json()),
            fetch('data/governorates.json').then(r => r.json()),
            fetch('data/car_brands.json').then(r => r.json()),
            fetch('data/car_colors.json').then(r => r.json()),
            fetch('data/distances.json').then(r => r.json())
        ]);
        window.CITIES = cities;
        window.CITY_NAMES = Object.keys(cities);
        window.GOVERNORATES = govs;
        window.GOV_NAMES = Object.keys(govs);
        window.CAR_BRANDS = brands;
        window.CAR_COLORS = colors;
        window.DISTANCE_MATRIX = dists;
        console.log('[Data] Loaded successfully');
    } catch (e) {
        console.error('[Data] Failed to load', e);
    }
}

window.MapControls = {
    _plugins: [],
    register(plugin) { this._plugins.push(plugin); }
};


const CITIES = {
    'Tunis': [36.8065, 10.1815], 'Ariana': [36.8625, 10.1956], 'Ben Arous': [36.7472, 10.2281], 'Manouba': [36.8078, 10.0863],
    'Nabeul': [36.4561, 10.7376], 'Zaghouan': [36.4029, 10.1429], 'Bizerte': [37.2744, 9.8739], 'Béja': [36.7256, 9.1817],
    'Jendouba': [36.5011, 8.7802], 'Le Kef': [36.1742, 8.7049], 'Siliana': [36.0840, 9.3708], 'Kairouan': [35.6781, 10.0963],
    'Sousse': [35.8256, 10.6369], 'Monastir': [35.7643, 10.8113], 'Mahdia': [35.5047, 11.0622], 'Sfax': [34.7398, 10.7600],
    'Sidi Bouzid': [35.0382, 9.4849], 'Kasserine': [35.1675, 8.8365], 'Gabès': [33.8815, 10.0982], 'Médenine': [33.3549, 10.5055],
    'Tataouine': [32.9297, 10.4518], 'Gafsa': [34.4250, 8.7842], 'Tozeur': [33.9197, 8.1339], 'Kébili': [33.7043, 8.9690]
};
const CITY_NAMES = Object.keys(CITIES);

const GOVERNORATES = {
    'Tunis': ['Tunis', 'La Marsa', 'Le Bardo', 'Carthage'],
    'Ariana': ['Ariana', 'Soukra', 'Raoued', 'Mnihla'],
    'Ben Arous': ['Ben Arous', 'Hammam Lif', 'Radès', 'Mégrine'],
    'Manouba': ['Manouba', 'Douar Hicher', 'Oued Ellil'],
    'Nabeul': ['Nabeul', 'Hammamet', 'Kélibia', 'Dar Chaâbane'],
    'Sousse': ['Sousse', 'Msaken', 'Kalaâ Kebira', 'Akouda'],
    'Monastir': ['Monastir', 'Moknine', 'Jemmal', 'Ksibet el-Médiouni'],
    'Sfax': ['Sfax', 'Sakiet Ezzit', 'Sakiet Eddaïer'],
    'Bizerte': ['Bizerte', 'Menzel Bourguiba', 'Mateur', 'Ras Jebel'],
    'Kairouan': ['Kairouan', 'Haffouz', 'Sbikha', 'Nasrallah'],
    'Gabès': ['Gabès', 'El Hamma', 'Mareth', 'Matmata'],
    'Gafsa': ['Gafsa', 'Métlaoui', 'Redeyef', 'Moulares'],
    'Tozeur': ['Tozeur', 'Nefta', 'Degache', 'Tamerza'],
    'Médenine': ['Médenine', 'Djerba', 'Zarzis', 'Ben Guerdane'],
    'Kébili': ['Kébili', 'Douz', 'Souk Lahad'],
    'Mahdia': ['Mahdia', 'Chebba', 'El Jem'],
    'Béja': ['Béja', 'Testour', 'Teboursouk'],
    'Jendouba': ['Jendouba', 'Tabarka', 'Aïn Draham'],
    'Le Kef': ['Le Kef', 'Dahmani', 'Tajerouine'],
    'Siliana': ['Siliana', 'Makthar', 'Bou Arada'],
    'Sidi Bouzid': ['Sidi Bouzid', 'Regueb', 'Meknassy'],
    'Kasserine': ['Kasserine', 'Sbeïtla', 'Thala'],
    'Tataouine': ['Tataouine', 'Ghomrassen', 'Remada'],
    'Zaghouan': ['Zaghouan', 'El Fahs', 'Nadhour']
};
const GOV_NAMES = Object.keys(GOVERNORATES);

const CAR_BRANDS = [
    { name: 'Peugeot', logo: 'img/logos/peugeot.png' }, { name: 'Renault', logo: 'img/logos/renault.png' },
    { name: 'Citroën', logo: 'img/logos/citroen.png' }, { name: 'Volkswagen', logo: 'img/logos/volkswagen.png' },
    { name: 'Toyota', logo: 'img/logos/toyota.png' }, { name: 'Hyundai', logo: 'img/logos/hyundai.png' },
    { name: 'Kia', logo: 'img/logos/kia.png' }, { name: 'Fiat', logo: 'img/logos/fiat.png' },
    { name: 'Mercedes', logo: 'img/logos/mercedes.png' }, { name: 'BMW', logo: 'img/logos/bmw.png' },
    { name: 'Audi', logo: 'img/logos/audi.png' }, { name: 'SEAT', logo: 'img/logos/seat.png' },
];

const CAR_COLORS = [
    { name: 'Noir', hex: '#1a1a1a' }, { name: 'Blanc', hex: '#f5f5f5' },
    { name: 'Gris', hex: '#9ca3af' }, { name: 'Rouge', hex: '#ef4444' },
    { name: 'Bleu', hex: '#3b82f6' }, { name: 'Vert', hex: '#22c55e' },
    { name: 'Jaune', hex: '#eab308' }, { name: 'Orange', hex: '#f97316' },
];

let map, tileLayer, routeLayer, activeMarkers = [], locationMarker = null, selectedTripId = null, isSatellite = localStorage.getItem('cov_satellite') === 'true';

// MAP
function initMap() {
    map = L.map('map', { zoomControl: false }).setView([35.8256, 10.6369], 7);
    window.map = map;
    const isDark = document.body.classList.contains('dark-theme');

    let tileUrl;
    if (isSatellite) {
        tileUrl = 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}';
    } else {
        tileUrl = isDark
            ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
            : 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png';
    }

    tileLayer = L.tileLayer(tileUrl, {
        attribution: '&copy; OSM &copy; CartoDB', maxZoom: 18
    }).addTo(map);

    if (isSatellite) {
        document.body.classList.add('satellite-mode');
    }

    Object.entries(window.CITIES).forEach(([name, coords]) => {
        L.marker(coords, { icon: L.divIcon({ className: 'city-marker', iconSize: [10, 10], iconAnchor: [5, 5] }) }).addTo(map);
        L.marker(coords, { icon: L.divIcon({ className: 'city-label', html: name, iconSize: [120, 20], iconAnchor: [-8, 8] }), interactive: false }).addTo(map);
    });

    const container = document.getElementById('map-controls');
    if (container) {
        container.innerHTML = '';
        window.MapControls._plugins
            .sort((a, b) => (a.order || 99) - (b.order || 99))
            .forEach(p => {
                const btn = document.createElement('button');
                btn.className = 'map-ctrl-btn';
                btn.id = p.id;
                btn.title = p.title;
                btn.innerHTML = p.icon;
                if (p.id === 'map-satellite-btn' && isSatellite) btn.classList.add('active');
                btn.onclick = p.onClick;
                container.appendChild(btn);
            });
    }
}
// Map Search: geocode any city name
window.mapSearchGo = async function () {
    const query = (document.getElementById('map-search-input').value || '').trim();
    if (!query) return;

    // 1. Local CITIES lookup (instant)
    const localKey = Object.keys(CITIES).find(k => k.toLowerCase() === query.toLowerCase());
    if (localKey) {
        const [lat, lng] = CITIES[localKey];
        map.setView([lat, lng], 13, { animate: true });
        _flashSearchResult(localKey, lat, lng);
        return;
    }

    // 2. Governorate sub-cities
    for (const [gov, cities] of Object.entries(GOVERNORATES)) {
        const match = cities.find(c => c.toLowerCase() === query.toLowerCase());
        if (match && CITIES[gov]) {
            const [lat, lng] = CITIES[gov];
            map.setView([lat, lng], 13, { animate: true });
            _flashSearchResult(match, lat, lng);
            return;
        }
    }

    // 3. Nominatim geocoding fallback (any world city)
    try {
        const q = encodeURIComponent(query);
        const url = 'https://nominatim.openstreetmap.org/search?q=' + q + '&format=json&limit=1';
        const res = await fetch(url, { headers: { 'Accept-Language': 'fr,en' } });
        const data = await res.json();
        if (data && data.length > 0) {
            const lat = parseFloat(data[0].lat);
            const lng = parseFloat(data[0].lon);
            const name = data[0].display_name.split(',')[0];
            map.setView([lat, lng], 13, { animate: true });
            _flashSearchResult(name, lat, lng);
        } else {
            alert('Aucun resultat pour "' + query + '"');
        }
    } catch (e) {
        alert('Erreur de connexion au service de recherche.');
    }
};

let _searchResultMarker = null;
function _flashSearchResult(name, lat, lng) {
    if (_searchResultMarker) { map.removeLayer(_searchResultMarker); _searchResultMarker = null; }
    _searchResultMarker = L.marker([lat, lng], {
        icon: L.divIcon({
            className: 'city-label active',
            html: '\u{1F4CD} ' + name,
            iconSize: [160, 30],
            iconAnchor: [-8, 12]
        })
    }).addTo(map);
    var overlay = document.getElementById('map-search-overlay');
    var btn = document.getElementById('map-search-btn');
    if (overlay) { overlay.classList.remove('open'); if (btn) btn.classList.remove('active'); }
    setTimeout(function() {
        if (_searchResultMarker) { map.removeLayer(_searchResultMarker); _searchResultMarker = null; }
    }, 5000);
}

window.toggleSatellite = function () {
    isSatellite = !isSatellite;
    localStorage.setItem('cov_satellite', isSatellite);
    const btn = document.getElementById('map-satellite-btn');
    if (isSatellite) {
        tileLayer.setUrl('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}');
        btn.classList.add('active');
        document.body.classList.add('satellite-mode');
    } else {
        btn.classList.remove('active');
        document.body.classList.remove('satellite-mode');
        window.updateMapTheme();
    }
};

window.updateMapTheme = function () {
    if (!map || !tileLayer || isSatellite) return;
    const isDark = document.body.classList.contains('dark-theme');
    const newUrl = isDark
        ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
        : 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png';
    tileLayer.setUrl(newUrl);
};
function findCoords(cityName) {
    if (CITIES[cityName]) return CITIES[cityName];
    // Fallback: search which governorate the city belongs to
    for (const [gov, cities] of Object.entries(GOVERNORATES)) {
        if (cities.includes(cityName) || gov === cityName) return CITIES[gov];
    }
    return null;
}

window.highlightRoute = async function (dep, arr) {
    clearRoute();
    const a = findCoords(dep), b = findCoords(arr);
    if (!a || !b) return;

    try {
        // Fetch real road route geometry only
        const res = await fetch(`https://router.project-osrm.org/route/v1/driving/${a[1]},${a[0]};${b[1]},${b[0]}?overview=full&geometries=geojson`);
        const data = await res.json();

        if (data.code === 'Ok' && data.routes.length > 0) {
            const coords = data.routes[0].geometry.coordinates.map(c => [c[1], c[0]]);
            routeLayer = L.featureGroup([
                L.polyline(coords, { color: '#c3f832', weight: 12, opacity: 0.2, lineJoin: 'round' }),
                L.polyline(coords, { color: '#c3f832', weight: 5, opacity: 1, lineJoin: 'round' })
            ]).addTo(map);
        } else {
            routeLayer = L.featureGroup([
                L.polyline([a, b], { color: '#c3f832', weight: 10, opacity: 0.2, dashArray: '10 10' }),
                L.polyline([a, b], { color: '#c3f832', weight: 4, opacity: 1, dashArray: '10 10' })
            ]).addTo(map);
        }
    } catch (e) {
        routeLayer = L.polyline([a, b], { color: '#c3f832', weight: 4, dashArray: '10 10', opacity: 0.8 }).addTo(map);
    }

    const points = [{ n: dep, c: a, type: 'dep' }, { n: arr, c: b, type: 'arr' }];
    points.forEach(point => {
        activeMarkers.push(L.marker(point.c, { 
            icon: L.divIcon({ 
                className: 'city-marker active ' + point.type, 
                iconSize: [12, 12], 
                iconAnchor: [6, 6] 
            }) 
        }).addTo(map));
        
        activeMarkers.push(L.marker(point.c, { 
            icon: L.divIcon({ 
                className: 'city-label active', 
                html: `<span class="marker-svg ${point.type}">${point.type === 'dep' ? ICO.startPin : ICO.endPin}</span>` + point.n, 
                iconSize: [150, 30], 
                iconAnchor: [-8, 12] 
            }), 
            interactive: false 
        }).addTo(map));
    });

    map.fitBounds([a, b], { padding: [100, 100], maxZoom: 10 });
};

window.clearRoute = function () {
    if (routeLayer) { map.removeLayer(routeLayer); routeLayer = null; }
    activeMarkers.forEach(m => map.removeLayer(m)); activeMarkers = [];
};

window.getDistance = (from, to) => {
    if (!from || !to || from === to) return 0;
    
    // 1. Static Matrix (Road distance)
    const matrix = {
        'Tunis-Sousse': 142, 'Sousse-Tunis': 142,
        'Tunis-Bizerte': 66, 'Bizerte-Tunis': 66,
        'Tunis-Hammamet': 64, 'Hammamet-Tunis': 64,
        'Tunis-Sfax': 270, 'Sfax-Tunis': 270,
        'Sousse-Sfax': 130, 'Sfax-Sousse': 130,
        'Tunis-Béja': 105, 'Tunis-Jendouba': 154,
        'Tunis-Monastir': 162, 'Tunis-Mahdia': 205,
        'Tunis-Gabès': 400, 'Tunis-Gafsa': 350,
        'Tunis-Tozeur': 450, 'Tunis-Tataouine': 530,
        'Sousse-Monastir': 22, 'Monastir-Mahdia': 45
    };
    const key = `${from}-${to}`;
    if (matrix[key]) return matrix[key];

    // 2. Haversine Fallback
    const c1 = findCoords(from), c2 = findCoords(to);
    if (!c1 || !c2) return Math.floor(Math.random() * 50 + 80);

    const R = 6371; // Earth Radius (km)
    const dLat = (c2[0] - c1[0]) * Math.PI / 180;
    const dLon = (c2[1] - c1[1]) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(c1[0] * Math.PI / 180) * Math.cos(c2[0] * Math.PI / 180) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const directDist = R * c;

    return Math.floor(directDist * 1.25); // Add 25% for road curves
};

function updateMoneyPill() {
    const u = window.API.getCurrentUser();
    const el = document.getElementById('money-val');
    if (el && u) el.textContent = (u.balance || 0).toFixed(2) + ' TND';
}

// Ã¢â€â‚¬Ã¢â€â‚¬ ICONS Ã¢â€â‚¬Ã¢â€â‚¬
const ICO = {
    home: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>',
    search: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>',
    plus: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v8M8 12h8"/></svg>',
    calendar: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>',
    wallet: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"/><circle cx="18" cy="12" r="2"/><path d="M2 10h16"/></svg>',
    bell: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>',
    chat: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>',
    user: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
    shield: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>',
    logout: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>',
    msg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>',
    book: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>',
    trash: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>',
    coin: '<svg viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2.5"><path d="M12 6c3.3 0 6 1.3 6 3s-2.7 3-6 3-6-1.3-6-3 2.7-3 6-3Z"/><path d="M6 9v3c0 1.7 2.7 3 6 3s6-1.3 6-3V9"/><path d="M6 12v3c0 1.7 2.7 3 6 3s6-1.3 6-3v-3"/><path d="M6 15v3c0 1.7 2.7 3 6 3s6-1.3 6-3v-3"/></svg>',
    clock: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
    road: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v20M9 8H6M9 12H5M9 16H6M15 8h3M15 12h4M15 16h3"/></svg>',
    ac: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m10 20-1.25-2.5L6 18"/><path d="M10 4 8.75 6.5 6 6"/><path d="m14 20 1.25-2.5L18 18"/><path d="m14 4 1.25 2.5L18 6"/><path d="m17 21-3-6h-4"/><path d="m17 3-3 6 1.5 3"/><path d="M2 12h6.5L10 9"/><path d="m20 10-1.5 2 1.5 2"/><path d="M22 12h-6.5L14 15"/><path d="m4 10 1.5 2L4 14"/><path d="m7 21 3-6-1.5-3"/><path d="m7 3 3 6h4"/></svg>',
    music: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>',
    luggage: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 20a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2"/><path d="M8 18V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v14"/><path d="M10 20h4"/><circle cx="16" cy="20" r="2"/><circle cx="8" cy="20" r="2"/></svg>',
    pin: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>',
    car: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/><circle cx="7" cy="17" r="2"/><path d="M9 17h6"/><circle cx="17" cy="17" r="2"/></svg>',
    co2: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>',
    arbres: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22v-6M12 16a6 6 0 1 0 0-12 6 6 0 1 0 0 12ZM8 10a4 4 0 1 1 8 0"/></svg>',
    tnd: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v8M9 10h6M9 14h6"/></svg>',
    smoking: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 12H3a1 1 0 0 0-1 1v2a1 1 0 0 0 1 1h14"/><path d="M18 8c0-2.5-2-2.5-2-5"/><path d="M21 16a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1"/><path d="M22 8c0-2.5-2-2.5-2-5"/><path d="M7 12v4"/></svg>',
    pets: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="4" r="2"/><circle cx="18" cy="8" r="2"/><circle cx="20" cy="16" r="2"/><path d="M9 10a5 5 0 0 1 5 5v3.5a3.5 3.5 0 0 1-6.84 1.045Q6.52 17.48 4.46 16.84A3.5 3.5 0 0 1 5.5 10Z"/></svg>',
    women: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 15v7"/><path d="M9 19h6"/><circle cx="12" cy="9" r="6"/></svg>',
    star: '<svg viewBox="0 0 24 24" fill="currentColor" stroke="none" width="13" height="13"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>',
    leaf: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10z"/><path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"/></svg>',
    medal: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 15a7 7 0 1 0 0-14 7 7 0 0 0 0 14z"/><path d="M8.21 13.89L7 23l5-3 5 3-1.21-9.12"/></svg>',
    check: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>',
    x: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>',
    warning: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
    ban: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg>',
    flag: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" y1="22" x2="4" y2="15"/></svg>',
    scale: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v18M3 9l9-6 9 6M5 21h14"/><path d="M5 12H2l3 6h0a3 3 0 0 0 6 0h0l3-6H5z"/><path d="M19 12h-3l3 6h0a3 3 0 0 0 6 0h0l-3-6z"/></svg>',
    idcard: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="5" width="20" height="14" rx="2"/><circle cx="8" cy="12" r="2"/><path d="M14 9h4M14 12h4M14 15h2"/><path d="M6 19v-1a2 2 0 0 1 4 0v1"/></svg>',
    refresh: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>',
    switch: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 3l4 4-4 4"/><path d="M20 7H4"/><path d="M8 21l-4-4 4-4"/><path d="M4 17h16"/></svg>',
    heart: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>',
    heartFilled: '<svg viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>',
    download: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>',
    inbox: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 16 12 14 15 10 15 8 12 2 12"/><path d="M5.45 5.11L2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"/></svg>',
    moon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"></path></svg>',
    sun: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="4"></circle><path d="M12 2v2M12 20v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M2 12h2M20 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"></path></svg>',
    startPin: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-map-pin-house-icon lucide-map-pin-house"><path d="M15 22a1 1 0 0 1-1-1v-4a1 1 0 0 1 .445-.832l3-2a1 1 0 0 1 1.11 0l3 2A1 1 0 0 1 22 17v4a1 1 0 0 1-1 1z"/><path d="M18 10a8 8 0 0 0-16 0c0 4.993 5.539 10.193 7.399 11.799a1 1 0 0 0 .601.2"/><path d="M18 22v-3"/><circle cx="10" cy="10" r="3"/></svg>',
    endPin: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-flag-triangle-right-icon lucide-flag-triangle-right"><path d="M6 22V2.8a.8.8 0 0 1 1.17-.71l11.38 5.69a.8.8 0 0 1 0 1.44L6 15.5"/></svg>',
    groupChat: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-users-round-icon lucide-users-round"><path d="M18 21a8 8 0 0 0-16 0"/><circle cx="10" cy="8" r="5"/><path d="M22 20c0-3.37-2-6.5-4-8a5 5 0 0 0-.45-8.3"/></svg>'
};

// Ã¢â€â‚¬Ã¢â€â‚¬ ROUTER Ã¢â€â‚¬Ã¢â€â‚¬
