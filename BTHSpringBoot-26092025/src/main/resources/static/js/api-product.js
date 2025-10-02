const PAPI = '/api/products';
const CAPI = '/api/categories';

let pPage = 0, pSize = 10, pQ = '', pCat = null;

async function loadProductPage() {
  const params = new URLSearchParams({
    q: pQ || '',
    page: pPage,
    size: pSize
  });
  if (pCat) params.append('categoryId', pCat);

  const res = await fetch(`${PAPI}?` + params.toString());
  const page = await res.json();

  const rows = (page.content || []).map(p => `
    <tr>
      <td>${p.id}</td>
      <td>${p.title}</td>
      <td class="text-end">${p.quantity}</td>
      <td>${p.description || ''}</td>
      <td class="text-end">${p.price}</td>
      <td>${p.category ? p.category.name : ''}</td>
      <td class="text-end">
        <button class="btn btn-sm btn-primary" onclick="openEdit(${p.id})">Edit</button>
        <button class="btn btn-sm btn-danger" onclick="delProduct(${p.id})">Delete</button>
      </td>
    </tr>`).join('');

  document.querySelector('#productBody').innerHTML = rows;
  document.querySelector('#p-info').textContent =
      `Showing ${page.number*page.size + (page.numberOfElements>0?1:0)}–${page.number*page.size + page.numberOfElements} of ${page.totalElements}`;
}

async function loadCategoryOptions(selectEl) {
  const res = await fetch(CAPI + '?size=999&page=0');
  const page = await res.json();
  selectEl.innerHTML = `<option value="">-- Category --</option>` +
    (page.content||[]).map(c=>`<option value="${c.id}">${c.name}</option>`).join('');
}

function openCreate(){
  const f = document.forms['pForm'];
  f.reset();
  f['id'].value = '';
  loadCategoryOptions(f['categoryId']);
  const modal = new bootstrap.Modal('#pModal'); modal.show();
}

async function openEdit(id){
  const res = await fetch(`${PAPI}/${id}`); const p = await res.json();
  const f = document.forms['pForm'];
  f['id'].value = p.id;
  f['title'].value = p.title;
  f['quantity'].value = p.quantity;
  f['description'].value = p.description || '';
  f['price'].value = p.price;
  await loadCategoryOptions(f['categoryId']);
  if (p.category) f['categoryId'].value = p.category.id;
  const modal = new bootstrap.Modal('#pModal'); modal.show();
}

async function submitProduct(ev){
  ev.preventDefault();
  const f = ev.target;
  const dto = {
    title: f['title'].value.trim(),
    quantity: parseInt(f['quantity'].value || '0', 10),
    description: f['description'].value.trim(),
    price: parseFloat(f['price'].value || '0'),
  };
  const catId = f['categoryId'].value;
  if (catId) dto.category = { id: Number(catId) };

  const hasId = f['id'].value;
  const res = await fetch(hasId ? `${PAPI}/${hasId}` : PAPI, {
    method: hasId ? 'PUT' : 'POST',
    headers: {'Content-Type':'application/json'},
    body: JSON.stringify(dto)
  });
  if (!res.ok) alert('Save failed');
  bootstrap.Modal.getInstance(document.getElementById('pModal')).hide();
  loadProductPage();
}

async function delProduct(id){
  if(!confirm('Delete product?')) return;
  await fetch(`${PAPI}/${id}`, { method:'DELETE' });
  loadProductPage();
}

// filters
function onSearch(){
  pQ = document.getElementById('p-q').value.trim();
  pPage = 0; loadProductPage();
}
async function onFilterCat(){
  pCat = document.getElementById('p-cat').value || null;
  pPage = 0; loadProductPage();
}

document.addEventListener('DOMContentLoaded', async () => {
  await loadProductPage();
  await loadCategoryOptions(document.getElementById('p-cat'));
  document.getElementById('pForm').addEventListener('submit', submitProduct);
});
