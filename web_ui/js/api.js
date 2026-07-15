/**
 * FLAWLESS DOMAIN ENGINE
 * Manages entirely offline Mock DB via localStorage.
 */

window.handleFileSelect = function (e, callback) {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function (event) {
        const img = new Image();
        img.onload = function () {
            // Resize and compress
            const canvas = document.createElement('canvas');
            let width = img.width;
            let height = img.height;
            const MAX_SIZE = 800;

            if (width > height) {
                if (width > MAX_SIZE) { height *= MAX_SIZE / width; width = MAX_SIZE; }
            } else {
                if (height > MAX_SIZE) { width *= MAX_SIZE / height; height = MAX_SIZE; }
            }

            canvas.width = width;
            canvas.height = height;
            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, width, height);

            // Return compressed Base64 (JPEG 0.7 is very efficient)
            callback(canvas.toDataURL('image/jpeg', 0.7));
        };
        img.src = event.target.result;
    };
    reader.readAsDataURL(file);
};

window.DB = {
    get: (key) => JSON.parse(localStorage.getItem(key)) || null,
    set: (key, val) => localStorage.setItem(key, JSON.stringify(val)),
    init: async function () {
        const CURRENT_VERSION = 11;
        const storedVersion = DB.get('cov_version') || 0;

        if (storedVersion < CURRENT_VERSION) {
            DB.set('cov_version', CURRENT_VERSION);
        }
    },
    clearAndSeed: () => {
        console.log("%c[DB] Mocks disabled. App is connected to active Java backend.", "color:#10b981; font-weight:bold;");
    },
    toggleTheme: () => {
        const isDark = document.body.classList.toggle('dark-theme');
        localStorage.setItem('cov_theme', isDark ? 'dark' : 'light');
        if (window.updateMapTheme) window.updateMapTheme();

        // Refresh map control icon if it exists
        const themeBtn = document.getElementById('map-theme-btn');
        if (themeBtn) {
            themeBtn.innerHTML = isDark ? ICO.sun : ICO.moon;
        }
    },
    applyTheme: () => {
        const theme = localStorage.getItem('cov_theme');
        // Default to dark if no preference is set yet
        if (theme === 'dark' || theme === null) {
            document.body.classList.add('dark-theme');
        } else {
            document.body.classList.remove('dark-theme');
        }
    }
};
window.DB.applyTheme();

const delay = ms => new Promise(res => setTimeout(res, ms));

/**
 * SIMULATION: Requirement I - Email/SMS notifications
 */
function simulateNotification(userId, type, message) {
    const target = `User #${userId}`;
    console.log(`%c[SIMULATION ${type}]`, "color:#3b82f6; font-weight:bold;", `To: ${target} | Content: ${message}`);
}

window.Toast = {
    show: function (msg, type) {
        type = type || 'success';
        var container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        var existing = container.querySelectorAll('.toast');
        if (existing.length >= 3) existing[0].remove();
        var t = document.createElement('div');
        t.className = 'toast ' + type;
        var icon = type === 'success'
            ? '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>'
            : '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>';
        var safeMsg = window.esc ? window.esc(msg) : msg;
        t.innerHTML = icon + '<span>' + safeMsg + '</span>';
        container.appendChild(t);
        setTimeout(function () {
            t.style.animation = 'fadeOut 0.3s forwards';
            setTimeout(function () { t.remove(); }, 300);
        }, 4000);
    }
};



// --- I18N ENGINE ---
const I18N = {
    fr: {
        profile: "Mon Profil", settings: "Paramètres", general: "Général",
        bio: "Ma Biographie", vehicles: "Mes Véhicules", logout: "Se déconnecter",
        appearance: "Apparence", dark_mode: "Mode Sombre", notifications: "Notifications",
        push: "Notifications Push", emails: "Emails récapitulatifs", sms: "Alertes SMS",
        lang: "Langue", account: "Compte", change_pass: "Changer le mot de passe",
        del_acc: "Supprimer le compte", save: "Enregistrer",
        edit_profile: "Modifier mon profil", name: "Nom complet", phone: "Téléphone", call: "Appeler", bio_placeholder: "Partagez quelque chose sur vous...",
        update_success: "Profil mis à jour !", lang_success: "Langue modifiée",
        nav_home: "Accueil", nav_search: "Recherche", nav_msgs: "Messages", nav_bks: "Réservations",
        nav_profile: "Profil", title_trips: "Trajets Disponibles", title_search: "Trouver un trajet",
        publish: "Publier", wallet: "Portefeuille", search_btn: "Rechercher",
        from: "Départ", to: "Arrivée", date: "Date", time: "Heure", price: "Prix", seats: "Places",
        brand: "Marque", model: "Modèle", plate: "Immatriculation", color: "Couleur",
        add_vehicle: "Ajouter un véhicule", no_trips: "Aucun trajet disponible", no_results: "Aucun résultat trouvé",
        driver: "Chauffeur", book: "Réserver", accept: "Accepter", refuse: "Refuser",
        complete: "Terminer", cancel: "Annuler", distance: "Distance",
        balance: "Solde", add_funds: "Ajouter des fonds", transactions: "Transactions",
        msg_placeholder: "Votre message...", contact: "Contact", send: "Envoyer",
        mark_read: "Marquer comme lu", del_all: "Tout supprimer",
        confirm_del: "Êtes-vous sûr de vouloir supprimer votre compte ?"
    },
    ar: {
        profile: "ملفي الشخصي", settings: "الإعدادات", general: "عام",
        bio: "سيرتي الذاتية", vehicles: "مركباتي", logout: "تسجيل الخروج",
        appearance: "المظهر", dark_mode: "الوضع الداكن", notifications: "التنبيهات",
        push: "تنبيهات الهاتف", emails: "ملخصات البريد", sms: "تنبيهات SMS",
        lang: "اللغة", account: "الحساب", change_pass: "تغيير كلمة المرور",
        del_acc: "حذف الحساب", save: "حفظ",
        edit_profile: "تعديل الملف الشخصي", name: "الاسم الكامل", phone: "رقم الهاتف", call: "اتصال", bio_placeholder: "شارك شيئاً عن نفسك...",
        update_success: "تم تحديث الملف الشخصي!", lang_success: "تم تغيير اللغة",
        nav_home: "الرئيسية", nav_search: "البحث", nav_msgs: "الرسائل", nav_bks: "الحجوزات",
        nav_profile: "الملف", title_trips: "الرحلات المتاحة", title_search: "البحث عن رحلة",
        publish: "نشر", wallet: "المحفظة", search_btn: "بحث",
        from: "من", to: "إلى", date: "التاريخ", time: "الوقت", price: "السعر", seats: "المقاعد",
        brand: "العلامة التجارية", model: "الموديل", plate: "رقم اللوحة", color: "اللون",
        add_vehicle: "إضافة مركبة", no_trips: "لا توجد رحلات متاحة", no_results: "لا توجد نتائج",
        driver: "السائق", book: "حجز", accept: "قبول", refuse: "رفض",
        complete: "إتمام", cancel: "إلغاء", distance: "المسافة",
        balance: "الرصيد", add_funds: "إضافة أموال", transactions: "المعاملات",
        msg_placeholder: "رسالتك...", contact: "جهة الاتصال", send: "إرسال",
        mark_read: "تمييز كـ مقروء", del_all: "حذف الكل",
        confirm_del: "هل أنت متأكد من حذف حسابك؟"
    },
    en: {
        profile: "My Profile", settings: "Settings", general: "General",
        bio: "My Biography", vehicles: "My Vehicles", logout: "Log out",
        appearance: "Appearance", dark_mode: "Dark Mode", notifications: "Notifications",
        push: "Push Notifications", emails: "Email Summaries", sms: "SMS Alerts",
        lang: "Language", account: "Account", change_pass: "Change Password",
        del_acc: "Delete Account", save: "Save Changes",
        edit_profile: "Edit Profile", name: "Full Name", bio_placeholder: "Share something about yourself...",
        update_success: "Profile updated!", lang_success: "Language changed",
        nav_home: "Home", nav_search: "Search", nav_msgs: "Messages", nav_bks: "Bookings",
        nav_profile: "Profile", title_trips: "Available Trips", title_search: "Find a Trip",
        publish: "Publish", wallet: "Wallet", search_btn: "Search",
        from: "Departure", to: "Destination", date: "Date", time: "Time", price: "Price", seats: "Seats",
        brand: "Brand", model: "Model", plate: "License Plate", color: "Color",
        add_vehicle: "Add Vehicle", no_trips: "No trips available", no_results: "No results found",
        driver: "Driver", book: "Book", accept: "Accept", refuse: "Decline",
        complete: "Complete", cancel: "Cancel", distance: "Distance",
        balance: "Balance", add_funds: "Add Funds", transactions: "Transactions",
        msg_placeholder: "Your message...", contact: "Contact", send: "Send",
        mark_read: "Mark as read", del_all: "Delete all",
        confirm_del: "Are you sure you want to delete your account?"
    }
};

window.t = function (key) {
    const lang = localStorage.getItem('cov_lang') || 'fr';
    return I18N[lang][key] || key;
};

window.API = { API_BASE: "http://localhost:8080" };
// API domains are loaded from js/api/

// ── Feature 14: WebSocket live-notification client ───────────────────────────
window.WS = {
    _socket: null,
    _retryMs: 3000,
    connect: function (userId) {
        if (!userId || userId === -1) return;
        try {
            const ws = new WebSocket(`ws://localhost:8081?userId=${userId}`);
            ws.onmessage = function (event) {
                try {
                    const msg = JSON.parse(event.data);
                    if (msg.type === 'notification') {
                        Toast.show(msg.title + (msg.message ? ' — ' + msg.message : ''), 'info');
                        // Refresh notification badge if the sidebar badge function exists
                        if (window.refreshNotifBadge) window.refreshNotifBadge();
                    }
                } catch (e) {}
            };
            ws.onclose = function () {
                // Reconnect after a delay
                setTimeout(() => window.WS.connect(userId), window.WS._retryMs);
            };
            ws.onerror = function () { ws.close(); };
            window.WS._socket = ws;
        } catch (e) {
            // WebSocket not available (e.g. server not running) — silently ignore
        }
    },
    disconnect: function () {
        if (window.WS._socket) { window.WS._socket.close(); window.WS._socket = null; }
    }
};



// ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ 
// UI ENGINE v2 —  All improvements cascaded
// ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ 

// -------------------------------------------------------
// XSS-safe HTML escaper
// -------------------------------------------------------
window.esc = function (str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
};

// -------------------------------------------------------
// Debounce utility
// -------------------------------------------------------
window.debounce = function (fn, ms) {
    let timer;
    return function () {
        var args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () { fn.apply(null, args); }, ms);
    };
};

// -------------------------------------------------------
// LocalStorage trip cache (5-minute TTL)
// -------------------------------------------------------
window.TripCache = {
    _KEY: 'cov_trips_cache',
    _TTL: 5 * 60 * 1000,
    get: function () {
        try {
            var raw = localStorage.getItem(this._KEY);
            if (!raw) return null;
            var parsed = JSON.parse(raw);
            if (Date.now() - parsed.ts > this._TTL) { localStorage.removeItem(this._KEY); return null; }
            return parsed.data;
        } catch (e) { return null; }
    },
    set: function (data) {
        try { localStorage.setItem(this._KEY, JSON.stringify({ ts: Date.now(), data: data })); } catch (e) { }
    },
    clear: function () { localStorage.removeItem(this._KEY); }
};

// -------------------------------------------------------
// Polished confirm dialog — replaces native confirm()
// showConfirm({ icon, title, body, okLabel, okClass, onOk })
// -------------------------------------------------------
window.showConfirm = function (opts) {
    var modal = document.getElementById('confirm-modal');
    document.getElementById('confirm-modal-icon').innerHTML = opts.icon || '';
    document.getElementById('confirm-modal-title').textContent = opts.title || 'Confirmation';
    document.getElementById('confirm-modal-body').textContent = opts.body || '';
    var okBtn = document.getElementById('confirm-modal-ok');
    okBtn.textContent = opts.okLabel || 'Confirmer';
    okBtn.className = 'btn btn-full ' + (opts.okClass || 'btn-lime');
    okBtn.onclick = function () {
        modal.classList.remove('open');
        if (opts.onOk) opts.onOk();
    };
    document.getElementById('confirm-modal-cancel').textContent = opts.cancelLabel || 'Annuler';
    modal.classList.add('open');
};
