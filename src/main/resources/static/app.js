const roleSelect = document.getElementById('role');
const messageBox = document.getElementById('message');
const tokenBody = document.getElementById('tokenBody');

function role() { return roleSelect.value; }

function show(text, isError) {
  messageBox.textContent = text;
  messageBox.className = isError ? 'error' : 'ok';
}

async function call(url, options) {
  options = options || {};
  options.headers = Object.assign({ 'X-Role': role() }, options.headers || {});
  const res = await fetch(url, options);
  const text = await res.text();
  let data = null;
  try { data = JSON.parse(text); } catch (e) { data = null; }
  return { ok: res.ok, status: res.status, text: text, data: data };
}

function cell(cls, text) {
  const td = document.createElement('td');
  td.className = cls;
  td.textContent = text;
  return td;
}

function statusCell(status) {
  const td = document.createElement('td');
  td.className = 'status';
  const chip = document.createElement('span');
  chip.className = 'chip chip-' + status.toLowerCase();
  chip.textContent = status;
  td.appendChild(chip);
  return td;
}

function actionButton(id, status, label) {
  const b = document.createElement('button');
  b.type = 'button';
  b.className = 'btn-' + status.toLowerCase();
  b.textContent = label;
  b.addEventListener('click', function () { updateStatus(id, status); });
  return b;
}

function render(tokens) {
  tokenBody.innerHTML = '';
  tokens.forEach(function (t) {
    const tr = document.createElement('tr');
    tr.setAttribute('data-id', t.id);
    tr.appendChild(cell('number', t.tokenNumber));
    tr.appendChild(cell('name', t.customerName));
    tr.appendChild(cell('service', t.serviceType));
    tr.appendChild(statusCell(t.status));
    const actions = document.createElement('td');
    actions.className = 'actions';
    actions.appendChild(actionButton(t.id, 'SERVING', 'Serve'));
    actions.appendChild(actionButton(t.id, 'COMPLETED', 'Complete'));
    actions.appendChild(actionButton(t.id, 'CANCELLED', 'Cancel'));
    tr.appendChild(actions);
    tokenBody.appendChild(tr);
  });
  document.getElementById('emptyState').hidden = tokens.length > 0;
}

function updateBoard(tokens) {
  const servingList = document.getElementById('servingList');
  const waitingList = document.getElementById('waitingList');
  servingList.innerHTML = '';
  waitingList.innerHTML = '';
  const serving = tokens.filter(function (t) { return t.status === 'SERVING'; });
  const waiting = tokens.filter(function (t) { return t.status === 'WAITING'; })
    .sort(function (a, b) { return a.id - b.id; });
  if (serving.length === 0) {
    const p = document.createElement('p');
    p.className = 'muted';
    p.textContent = 'No token is being served right now';
    servingList.appendChild(p);
  }
  serving.slice(0, 4).forEach(function (t) {
    const card = document.createElement('div');
    card.className = 'serving-card';
    const big = document.createElement('div');
    big.className = 'big';
    big.textContent = t.tokenNumber;
    const meta = document.createElement('div');
    meta.className = 'meta';
    meta.textContent = t.serviceType;
    card.appendChild(big);
    card.appendChild(meta);
    servingList.appendChild(card);
  });
  if (waiting.length === 0) {
    const li = document.createElement('li');
    li.textContent = 'Queue is empty';
    waitingList.appendChild(li);
  }
  waiting.slice(0, 6).forEach(function (t) {
    const li = document.createElement('li');
    li.textContent = t.tokenNumber;
    const svc = document.createElement('span');
    svc.className = 'svc';
    svc.textContent = t.serviceType;
    li.appendChild(svc);
    waitingList.appendChild(li);
  });
  if (waiting.length > 6) {
    const li = document.createElement('li');
    li.textContent = '+ ' + (waiting.length - 6) + ' more waiting';
    waitingList.appendChild(li);
  }
}

async function loadTokens() {
  const res = await call('api/tokens');
  if (res.ok) { render(res.data); updateBoard(res.data); } else { show(res.text, true); }
}

async function createToken() {
  const nameField = document.getElementById('customerName');
  const body = {
    customerName: nameField.value,
    serviceType: document.getElementById('serviceType').value
  };
  const res = await call('api/tokens', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if (!res.ok) { show(res.text, true); return; }
  show('Token ' + res.data.tokenNumber + ' created', false);
  nameField.value = '';
  await loadTokens();
}

async function updateStatus(id, status) {
  const res = await call('api/tokens/' + id + '/status', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status: status })
  });
  if (!res.ok) { show(res.text, true); return; }
  show('Token ' + res.data.tokenNumber + ' is now ' + res.data.status, false);
  await loadTokens();
}

async function search() {
  const by = document.getElementById('searchBy').value;
  const value = document.getElementById('searchInput').value.trim();
  if (!value) { show('Enter a value to search', true); return; }
  const res = await call('api/tokens/search?' + by + '=' + encodeURIComponent(value));
  if (!res.ok) { render([]); show(res.text, true); return; }
  const list = Array.isArray(res.data) ? res.data : [res.data];
  render(list);
  show('Search by ' + by + ' ' + value + ': ' + list.length + ' token(s) found', false);
}

async function showAll() {
  document.getElementById('searchInput').value = '';
  await loadTokens();
  show('Showing all tokens', false);
}

async function loadDashboard() {
  const ids = ['countWaiting', 'countServing', 'countCompleted', 'countCancelled', 'countTotal'];
  ids.forEach(function (id) { document.getElementById(id).textContent = '-'; });
  const msg = document.getElementById('dashboardMessage');
  msg.textContent = '';
  msg.className = '';
  const res = await call('api/tokens/dashboard');
  if (!res.ok) { msg.textContent = res.text; msg.className = 'error'; return; }
  document.getElementById('countWaiting').textContent = res.data.WAITING;
  document.getElementById('countServing').textContent = res.data.SERVING;
  document.getElementById('countCompleted').textContent = res.data.COMPLETED;
  document.getElementById('countCancelled').textContent = res.data.CANCELLED;
  document.getElementById('countTotal').textContent = res.data.TOTAL;
}


const HINTS = {
  RECEPTIONIST: 'Receptionist: can create tokens, view and search. Cannot change status or open the dashboard.',
  TELLER: 'Teller: can serve, complete or cancel tokens, view and search. Cannot create tokens or open the dashboard.',
  MANAGER: 'Manager: can view, search and open the dashboard. Cannot create tokens or change status.'
};
function updateHint() { document.getElementById('roleHint').textContent = HINTS[role()] || ''; }
roleSelect.addEventListener('change', updateHint);
updateHint();

function tickClock() {
  document.getElementById('clock').textContent = new Date().toLocaleString('en-IN', {
    weekday: 'short', day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
  });
}
tickClock();
setInterval(tickClock, 30000);

document.getElementById('createBtn').addEventListener('click', createToken);
document.getElementById('searchBtn').addEventListener('click', search);
document.getElementById('clearBtn').addEventListener('click', showAll);
document.getElementById('refreshDashboard').addEventListener('click', loadDashboard);
loadTokens();