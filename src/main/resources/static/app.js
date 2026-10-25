const CATEGORIES = ["Food", "Rent", "Travel", "Shopping", "Education", "Healthcare", "Bills", "Salary", "Other"];
const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 });
const authView = document.querySelector("#auth-view");
const appView = document.querySelector("#app-view");
const authForm = document.querySelector("#auth-form");
const authMessage = document.querySelector("#auth-message");
const appMessage = document.querySelector("#app-message");
const monthPicker = document.querySelector("#month-picker");
let csrfToken = "";
let currentUser = null;
let editingId = null;
let chart = null;
let isRegistering = false;

function showMessage(element, message, kind = "error") {
    element.textContent = message;
    element.className = `notice show ${kind}`;
}

function clearMessage(element) {
    element.textContent = "";
    element.className = "notice";
}

async function refreshCsrf() {
    const response = await fetch("/api/csrf", { credentials: "same-origin" });
    const data = await response.json();
    if (!response.ok || !data.token) throw new Error("Could not start a secure session. Refresh the page.");
    csrfToken = data.token;
}

async function api(url, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set("Accept", "application/json");
    if (options.body) headers.set("Content-Type", "application/json");
    if (["POST", "PUT", "DELETE", "PATCH"].includes((options.method || "GET").toUpperCase())) {
        if (!csrfToken) await refreshCsrf();
        headers.set("X-XSRF-TOKEN", csrfToken);
    }
    const response = await fetch(url, { ...options, headers, credentials: "same-origin" });
    const payload = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) {
        if (response.status === 401) {
            showAuth();
            throw new Error(payload?.message || "Please sign in again.");
        }
        throw new Error(payload?.message || `Request failed (${response.status}).`);
    }
    return payload;
}

function showAuth() {
    currentUser = null;
    appView.classList.add("hidden");
    authView.classList.remove("hidden");
}

function setAuthMode(registering) {
    isRegistering = registering;
    clearMessage(authMessage);
    document.querySelector("#name-field").classList.toggle("hidden", !registering);
    document.querySelector("#auth-name").required = registering;
    document.querySelector("#auth-password").autocomplete = registering ? "new-password" : "current-password";
    document.querySelector("#auth-title").textContent = registering ? "Create your account" : "Welcome back";
    document.querySelector("#auth-description").textContent = registering
        ? "Your personal finances, securely in one place."
        : "Sign in to see your personal finances.";
    document.querySelector("#auth-submit").textContent = registering ? "Create account" : "Sign in";
    document.querySelector("#auth-switch-copy").textContent = registering ? "Already have an account?" : "New to Paise Track?";
    document.querySelector("#auth-switch").textContent = registering ? "Sign in" : "Create an account";
}

function selectedMonth() {
    return monthPicker.value;
}

function monthLabel(month) {
    const [year, number] = month.split("-").map(Number);
    return new Intl.DateTimeFormat("en-IN", { month: "long", year: "numeric" }).format(new Date(year, number - 1, 1));
}

function updateIdentity(user) {
    const firstName = user.name.trim().split(/\s+/)[0];
    document.querySelector("#user-name").textContent = user.name;
    document.querySelector("#welcome-name").textContent = firstName;
    document.querySelector("#user-avatar").textContent = firstName.charAt(0).toUpperCase();
    document.querySelector("#today-label").textContent = new Intl.DateTimeFormat("en-IN", {
        weekday: "short", day: "numeric", month: "short", year: "numeric"
    }).format(new Date());
}

async function loadDashboard() {
    clearMessage(appMessage);
    const month = encodeURIComponent(selectedMonth());
    const [summary, report, transactions] = await Promise.all([
        api(`/api/summary?month=${month}`),
        api(`/api/report?month=${month}`),
        api(`/api/transactions?month=${month}`)
    ]);
    document.querySelector("#month-name").textContent = monthLabel(selectedMonth());
    document.querySelector("#total-income").textContent = inr.format(summary.income);
    document.querySelector("#total-expenses").textContent = inr.format(summary.expenses);
    document.querySelector("#monthly-budget").textContent = inr.format(summary.budget);
    document.querySelector("#remaining-budget").textContent = inr.format(summary.remainingBudget);
    document.querySelector("#savings").textContent = inr.format(summary.savings);
    document.querySelector("#budget-amount").value = summary.budget > 0 ? Number(summary.budget) : "";
    document.querySelector("#spent-label").textContent = `${inr.format(summary.expenses)} spent`;
    document.querySelector("#budget-limit-label").textContent = `${inr.format(summary.budget)} limit`;
    document.querySelector("#budget-caption").textContent = summary.budget > 0
        ? `${Math.max(0, Math.round((summary.expenses / summary.budget) * 100))}% of budget used`
        : "Set a budget to track your limit";
    const progress = document.querySelector("#budget-progress");
    progress.style.width = `${summary.budget > 0 ? Math.min(100, (summary.expenses / summary.budget) * 100) : 0}%`;
    progress.classList.toggle("over", summary.budget > 0 && summary.expenses > summary.budget);
    renderTransactions(transactions);
    renderChart(report.categories);
}

function renderTransactions(items) {
    const tbody = document.querySelector("#transaction-list");
    tbody.replaceChildren();
    document.querySelector("#transactions-empty").classList.toggle("hidden", items.length > 0);
    for (const item of items) {
        const row = document.createElement("tr");
        const title = document.createElement("td");
        const titleText = document.createElement("span");
        titleText.className = "transaction-name";
        titleText.textContent = item.category;
        title.append(titleText);
        if (item.note) {
            const note = document.createElement("span");
            note.className = "transaction-note";
            note.textContent = item.note;
            title.append(note);
        }
        const category = document.createElement("td");
        const pill = document.createElement("span");
        pill.className = "category-pill";
        pill.textContent = item.category;
        category.append(pill);
        const date = document.createElement("td");
        date.textContent = new Intl.DateTimeFormat("en-IN", { day: "numeric", month: "short", year: "numeric" })
            .format(new Date(`${item.transactionDate}T00:00:00`));
        const amount = document.createElement("td");
        amount.className = item.type === "INCOME" ? "amount-income" : "amount-expense";
        amount.textContent = `${item.type === "INCOME" ? "+" : "−"}${inr.format(item.amount)}`;
        const actions = document.createElement("td");
        const actionWrap = document.createElement("div");
        actionWrap.className = "row-actions";
        const edit = document.createElement("button");
        edit.type = "button";
        edit.className = "icon-button";
        edit.setAttribute("aria-label", `Edit ${item.category} transaction`);
        edit.title = "Edit";
        edit.textContent = "✎";
        edit.addEventListener("click", () => openEditor(item));
        const remove = document.createElement("button");
        remove.type = "button";
        remove.className = "icon-button";
        remove.setAttribute("aria-label", `Delete ${item.category} transaction`);
        remove.title = "Delete";
        remove.textContent = "⌫";
        remove.addEventListener("click", () => deleteTransaction(item));
        actionWrap.append(edit, remove);
        actions.append(actionWrap);
        row.append(title, category, date, amount, actions);
        tbody.append(row);
    }
}

function renderChart(categories) {
    const canvas = document.querySelector("#category-chart");
    const empty = document.querySelector("#chart-empty");
    if (chart) {
        chart.destroy();
        chart = null;
    }
    if (!categories.length) {
        canvas.classList.add("hidden");
        empty.classList.add("show");
        return;
    }
    canvas.classList.remove("hidden");
    empty.classList.remove("show");
    if (typeof Chart === "undefined") {
        empty.textContent = "The chart library could not load. Check your internet connection and refresh.";
        empty.classList.add("show");
        return;
    }
    chart = new Chart(canvas, {
        type: "doughnut",
        data: {
            labels: categories.map(entry => entry.category),
            datasets: [{ data: categories.map(entry => Number(entry.amount)),
                backgroundColor: ["#17896b", "#87c6a9", "#f0bd5c", "#7284c4", "#e18573", "#78b9ca", "#b6a0d3", "#d795b0", "#adb8bf"],
                borderWidth: 0, hoverOffset: 5 }]
        },
        options: {
            responsive: true, maintainAspectRatio: false, cutout: "70%",
            plugins: { legend: { position: "right", labels: { usePointStyle: true, pointStyle: "circle", boxWidth: 7, padding: 13, font: { family: "DM Sans", size: 10 } } },
                tooltip: { callbacks: { label: context => ` ${context.label}: ${inr.format(context.raw)}` } } }
        }
    });
}

async function startApp(user) {
    currentUser = user;
    updateIdentity(user);
    authView.classList.add("hidden");
    appView.classList.remove("hidden");
    try {
        await loadDashboard();
    } catch (error) {
        showMessage(appMessage, error.message);
    }
}

function openEditor(item = null) {
    editingId = item?.id ?? null;
    document.querySelector("#dialog-title").textContent = item ? "Edit transaction" : "Add transaction";
    document.querySelector("#save-transaction").textContent = item ? "Save changes" : "Save transaction";
    document.querySelector("#transaction-type").value = item?.type ?? "EXPENSE";
    document.querySelector("#transaction-amount").value = item?.amount ?? "";
    document.querySelector("#transaction-category").value = item?.category ?? "Food";
    document.querySelector("#transaction-date").value = item?.transactionDate ?? new Date().toISOString().slice(0, 10);
    document.querySelector("#transaction-note").value = item?.note ?? "";
    document.querySelector("#transaction-dialog").showModal();
}

async function deleteTransaction(item) {
    if (!window.confirm(`Delete this ${item.category.toLowerCase()} transaction?`)) return;
    try {
        await api(`/api/transactions/${item.id}`, { method: "DELETE" });
        showMessage(appMessage, "Transaction deleted.", "success");
        await loadDashboard();
    } catch (error) {
        showMessage(appMessage, error.message);
    }
}

authForm.addEventListener("submit", async event => {
    event.preventDefault();
    clearMessage(authMessage);
    const payload = {
        email: document.querySelector("#auth-email").value.trim(),
        password: document.querySelector("#auth-password").value
    };
    if (isRegistering) payload.name = document.querySelector("#auth-name").value.trim();
    try {
        const result = await api(isRegistering ? "/api/auth/register" : "/api/auth/login", {
            method: "POST", body: JSON.stringify(payload)
        });
        if (isRegistering) {
            setAuthMode(false);
            document.querySelector("#auth-email").value = payload.email;
            document.querySelector("#auth-password").value = "";
            showMessage(authMessage, result.message, "success");
        } else {
            await refreshCsrf();
            await startApp(result);
        }
    } catch (error) {
        showMessage(authMessage, error.message);
    }
});

document.querySelector("#auth-switch").addEventListener("click", () => setAuthMode(!isRegistering));
document.querySelector("#logout-button").addEventListener("click", async () => {
    try {
        await api("/api/auth/logout", { method: "POST" });
        await refreshCsrf();
        showAuth();
        setAuthMode(false);
        showMessage(authMessage, "You have signed out.", "success");
    } catch (error) {
        showMessage(appMessage, error.message);
    }
});
monthPicker.addEventListener("change", async () => {
    if (!currentUser) return;
    try { await loadDashboard(); } catch (error) { showMessage(appMessage, error.message); }
});
document.querySelector("#budget-form").addEventListener("submit", async event => {
    event.preventDefault();
    try {
        await api(`/api/budgets?month=${encodeURIComponent(selectedMonth())}`, {
            method: "POST",
            body: JSON.stringify({ amount: Number(document.querySelector("#budget-amount").value) })
        });
        showMessage(appMessage, "Monthly budget saved.", "success");
        await loadDashboard();
    } catch (error) { showMessage(appMessage, error.message); }
});
document.querySelector("#transaction-form").addEventListener("submit", async event => {
    event.preventDefault();
    const payload = {
        type: document.querySelector("#transaction-type").value,
        amount: Number(document.querySelector("#transaction-amount").value),
        category: document.querySelector("#transaction-category").value,
        transactionDate: document.querySelector("#transaction-date").value,
        note: document.querySelector("#transaction-note").value.trim() || null
    };
    try {
        await api(editingId ? `/api/transactions/${editingId}` : "/api/transactions", {
            method: editingId ? "PUT" : "POST", body: JSON.stringify(payload)
        });
        document.querySelector("#transaction-dialog").close();
        showMessage(appMessage, editingId ? "Transaction updated." : "Transaction added.", "success");
        await loadDashboard();
    } catch (error) { showMessage(appMessage, error.message); }
});
document.querySelector("#add-transaction").addEventListener("click", () => openEditor());
document.querySelector("#dialog-close").addEventListener("click", () => document.querySelector("#transaction-dialog").close());
document.querySelector("#cancel-transaction").addEventListener("click", () => document.querySelector("#transaction-dialog").close());
document.querySelectorAll("[data-scroll]").forEach(button => button.addEventListener("click", () => {
    document.getElementById(button.dataset.scroll)?.scrollIntoView({ behavior: "smooth" });
    document.querySelectorAll(".nav-item[data-scroll]").forEach(item => item.classList.toggle("active", item === button));
}));

async function initialize() {
    const now = new Date();
    monthPicker.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
    setAuthMode(false);
    try {
        await refreshCsrf();
        const user = await api("/api/auth/me");
        await startApp(user);
    } catch (error) {
        showAuth();
        if (!error.message.includes("Please sign in")) showMessage(authMessage, error.message);
    }
}

initialize();
