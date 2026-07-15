// ── Search state ─────────────────────────────────────────────────────────────
window._searchState = window._searchState || {
    priceMin: 0,
    priceMax: 100,
    minSeats: 1,
};

function renderSearchPanel() {
    const govOpts = GOV_NAMES.map(g => `<option value="${g}">${g}</option>`).join('');
    const s = window._searchState;

    document.getElementById('view-search').innerHTML = `
    <div class="panel-header">
        <h1 class="panel-title">Rechercher</h1>
        <p class="panel-subtitle">Trouvez le trajet idéal</p>
    </div>
    <div class="panel-body" style="padding-top:16px">

        <!-- Departure / Arrival -->
        <div class="form-row">
            <div class="form-group">
                <label class="form-label">Gouvernorat départ</label>
                <select class="form-input" id="s-gov-dep" onchange="updateCitySelect('s-gov-dep','s-city-dep')">
                    <option value="">Choisir</option>${govOpts}
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Ville départ</label>
                <select class="form-input" id="s-city-dep"><option value="">Choisir gouvernorat</option></select>
            </div>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label class="form-label">Gouvernorat arrivée</label>
                <select class="form-input" id="s-gov-arr" onchange="updateCitySelect('s-gov-arr','s-city-arr')">
                    <option value="">Choisir</option>${govOpts}
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Ville arrivée</label>
                <select class="form-input" id="s-city-arr"><option value="">Choisir gouvernorat</option></select>
            </div>
        </div>

        <!-- Price range slider -->
        <div class="form-group" style="margin-top:4px">
            <label class="form-label">Fourchette de prix</label>
            <div class="price-range-wrap">
                <div class="price-range-current" id="price-display">
                    <span id="price-min-label">${s.priceMin}</span> TND — <span id="price-max-label">${s.priceMax}</span> TND
                </div>
                <div class="price-range-track" id="price-track">
                    <div class="price-range-fill" id="price-fill"></div>
                    <input type="range" id="s-price-min" min="0" max="100" step="1" value="${s.priceMin}"
                        oninput="updatePriceSlider('min', this.value)">
                    <input type="range" id="s-price-max" min="0" max="100" step="1" value="${s.priceMax}"
                        oninput="updatePriceSlider('max', this.value)">
                </div>
                <div class="price-range-labels">
                    <span>0 TND</span>
                    <span>100 TND</span>
                </div>
            </div>
        </div>

        <!-- Min seats -->
        <div class="form-group">
            <label class="form-label">Places minimum</label>
            <div class="seat-chips" id="seat-chips">
                ${[1,2,3,4].map(n => `
                    <div class="seat-chip ${s.minSeats === n ? 'active' : ''}" onclick="selectMinSeats(${n})">${n}</div>
                `).join('')}
            </div>
        </div>

        <!-- Preference chips -->
        <div class="form-group">
            <label class="form-label" style="margin-bottom:12px">Équipements &amp; Préférences</label>
            <div style="display:flex; gap:8px; flex-wrap:wrap;">
                <label class="choice-chip"><input type="checkbox" id="s-ac" hidden><span>${ICO.ac} Climatisation</span></label>
                <label class="choice-chip"><input type="checkbox" id="s-music" hidden><span>${ICO.music} Musique</span></label>
                <label class="choice-chip"><input type="checkbox" id="s-smoking" hidden><span>${ICO.smoking} Fumeur</span></label>
                <label class="choice-chip"><input type="checkbox" id="s-flex" hidden><span>${ICO.pin} Flexible</span></label>
                <label class="choice-chip"><input type="checkbox" id="s-women" hidden><span>${ICO.women} Femmes only</span></label>
                <label class="choice-chip"><input type="checkbox" id="s-pets" hidden><span>${ICO.pets} Animaux</span></label>
            </div>
        </div>

        <!-- Luggage policy -->
        <div class="form-group">
            <label class="form-label">Politique bagages</label>
            <select class="form-input" id="s-luggage">
                <option value="">Peu importe</option>
                <option value="NONE">Aucun bagage</option>
                <option value="SMALL">Petit bagage</option>
                <option value="LARGE">Grand bagage</option>
            </select>
        </div>

        <div style="display:flex;gap:8px;margin-top:4px">
            <button class="btn btn-primary" style="flex:4" onclick="doSearch()">Rechercher</button>
            <button class="btn btn-outline icon-md" style="flex:1;display:flex;align-items:center;justify-content:center"
                onclick="openAlertModal()" title="Créer une alerte">${ICO.bell}</button>
        </div>

        <div id="search-alerts-box" style="margin-top:16px"></div>
        <div id="search-results"></div>
    </div>`;

    updatePriceFill();
    renderSearchAlerts();
}

// ── Price slider logic ────────────────────────────────────────────────────────
window.updatePriceSlider = function(side, val) {
    val = parseInt(val);
    const s = window._searchState;
    if (side === 'min') {
        s.priceMin = Math.min(val, s.priceMax - 1);
        document.getElementById('s-price-min').value = s.priceMin;
    } else {
        s.priceMax = Math.max(val, s.priceMin + 1);
        document.getElementById('s-price-max').value = s.priceMax;
    }
    document.getElementById('price-min-label').textContent = s.priceMin;
    document.getElementById('price-max-label').textContent = s.priceMax;
    updatePriceFill();
};

function updatePriceFill() {
    const fill = document.getElementById('price-fill');
    if (!fill) return;
    const s = window._searchState;
    const pct = (v) => (v / 100) * 100;
    fill.style.left   = pct(s.priceMin) + '%';
    fill.style.width  = (pct(s.priceMax) - pct(s.priceMin)) + '%';
}

// ── Seat selector ─────────────────────────────────────────────────────────────
window.selectMinSeats = function(n) {
    window._searchState.minSeats = n;
    document.querySelectorAll('.seat-chip').forEach((el, i) => {
        el.classList.toggle('active', i + 1 === n);
    });
};

// ── Search execution ──────────────────────────────────────────────────────────
async function renderSearchAlerts() {
    const alerts = await window.API.getSavedSearches();
    const box = document.getElementById('search-alerts-box');
    if (!box) return;
    if (!Array.isArray(alerts) || alerts.length === 0) { box.innerHTML = ''; return; }
    box.innerHTML = `<div class="section-title">Mes Alertes (${alerts.length})</div>` +
        `<div style="display:flex;gap:8px;overflow-x:auto;padding-bottom:10px">` +
        alerts.map(a => `<div class="badge-item" style="flex-shrink:0">${a.dep} → ${a.arr}
            <span style="margin-left:6px;cursor:pointer;color:var(--error)" onclick="deleteAlert(${a.id})">&times;</span>
        </div>`).join('') + `</div>`;
}

window.updateCitySelect = function(govId, cityId) {
    const gov = document.getElementById(govId).value;
    const cityEl = document.getElementById(cityId);
    const cities = GOVERNORATES[gov] || [];
    cityEl.innerHTML = '<option value="">Choisir</option>' + cities.map(c => `<option value="${c}">${c}</option>`).join('');
    if (govId.startsWith('p-') || cityId.startsWith('p-')) {
        const d = document.getElementById('p-city-dep').value || document.getElementById('p-gov-dep').value;
        const a = document.getElementById('p-city-arr').value || document.getElementById('p-gov-arr').value;
        if (d && a && typeof highlightRoute === 'function') highlightRoute(d, a);
    }
};

window.clearFilters = function() {
    ['s-ac','s-music','s-smoking','s-flex','s-women','s-pets'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.checked = false;
    });
    window._searchState.priceMin = 0;
    window._searchState.priceMax = 100;
    window._searchState.minSeats = 1;
    document.getElementById('search-results').innerHTML = '';
    renderSearchPanel();
};

window.doSearch = async function() {
    const dep      = document.getElementById('s-city-dep').value || document.getElementById('s-gov-dep').value;
    const arr      = document.getElementById('s-city-arr').value || document.getElementById('s-gov-arr').value;
    const ac       = document.getElementById('s-ac').checked;
    const music    = document.getElementById('s-music').checked;
    const smoking  = document.getElementById('s-smoking').checked;
    const flex     = document.getElementById('s-flex').checked;
    const women    = document.getElementById('s-women').checked;
    const pets     = document.getElementById('s-pets').checked;
    const luggage  = document.getElementById('s-luggage')?.value || '';
    const s        = window._searchState;

    const resultsEl = document.getElementById('search-results');
    if (resultsEl && typeof skeletonCards === 'function') resultsEl.innerHTML = skeletonCards(3);

    // Send all filters to backend
    const results = await window.API.searchTrips({
        dep, arr, ac, music, smoking, flex, women, pets,
        luggage,
        minSeats: s.minSeats,
        priceMin: s.priceMin,
        priceMax: s.priceMax,
    });

    // Apply price and seat filters client-side as well (belt-and-suspenders for
    // params the backend may not filter on yet)
    let trips = (Array.isArray(results) ? results : [])
        .filter(t => t.status === 'ACTIVE' || t.status === '1')
        .filter(t => (t.price || 0) >= s.priceMin && (t.price || 0) <= s.priceMax)
        .filter(t => (t.availableSeats ?? 0) >= s.minSeats);

    if (luggage) trips = trips.filter(t => !t.luggagePolicy || t.luggagePolicy === luggage);

    if (dep && arr) highlightRoute(dep, arr);

    const activeFilters = [ac, music, smoking, flex, women, pets, !!luggage, s.minSeats > 1].filter(Boolean).length;
    const hasPrice = s.priceMin > 0 || s.priceMax < 100;
    const totalActive = activeFilters + (hasPrice ? 1 : 0);
    const clearBtn = totalActive > 0
        ? `<button class="clear-filters-btn" onclick="clearFilters()">Effacer les filtres (${totalActive})</button>`
        : '';

    if (resultsEl) {
        resultsEl.innerHTML = trips.length
            ? `<div class="section-title" style="display:flex;justify-content:space-between;align-items:center">
                   <span>${trips.length} résultat(s)</span>${clearBtn}
               </div>` + trips.map(t => tripCardHTML(t)).join('')
            : `<div class="empty-state"><div class="empty-state-icon">🔍</div>
               <h3>Aucun trajet trouvé</h3>
               <p>Essayez d'autres villes ou relâchez les filtres</p>${clearBtn}</div>`;
        if (trips.length) syncFavoriteHearts();
    }
};

window.triggerPublishRoute = function() {
    const d = document.getElementById('p-city-dep').value || document.getElementById('p-gov-dep').value;
    const a = document.getElementById('p-city-arr').value || document.getElementById('p-gov-arr').value;
    if (d && a && typeof highlightRoute === 'function') highlightRoute(d, a);
};

window.Router.register("search", renderSearchPanel);
window.Sidebar.register({ view: 'search', icon: ICO.search, tip: () => window.t('nav_search'), order: 2 });
