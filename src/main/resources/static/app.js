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
    tr.appendChild(cell('status', t.status));
    const actions = document.createElement('td');
    actions.className = 'actions';
    actions.appendChild(actionButton(t.id, 'SERVING', 'Serve'));
    actions.appendChild(actionButton(t.id, 'COMPLETED', 'Complete'));
    actions.appendChild(actionButton(t.id, 'CANCELLED', 'Cancel'));
    tr.appendChild(actions);
    tokenBody.appendChild(tr);
  });
}

async function loadTokens() {
  const res = await call('api/tokens');
  if (res.ok) { render(res.data); } else { show(res.text, true); }
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

document.getElementById('createBtn').addEventListener('click', createToken);
document.getElementById('searchBtn').addEventListener('click', search);
document.getElementById('clearBtn').addEventListener('click', showAll);
document.getElementById('refreshDashboard').addEventListener('click', loadDashboard);
loadTokens();