const GRAPHQL_ENDPOINT = '/graphql';
const UPLOAD_ENDPOINT  = '/api/upload';   // REST upload ảnh
const UPLOAD_PREFIX    = '/uploads/';     // static resource mapping

let cache = { categories: [], users: [], products: [] };

async function graphqlFetch(query, variables = {}) {
  const res = await fetch(GRAPHQL_ENDPOINT, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, variables })
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const payload = await res.json();
  if (payload.errors) throw new Error(payload.errors.map(e=>e.message).join('\n'));
  return payload.data;
}

function showAlert(type, msg) {
  const box = document.getElementById('global-alert');
  box.className = `alert alert-${type}`;
  box.textContent = msg;
  box.classList.remove('d-none');
  setTimeout(()=>box.classList.add('d-none'), 3500);
}

// ============ Helpers ============

function resetCategoryForm() {
  const f = document.getElementById('category-form');
  f.reset();
  document.getElementById('category-id').value = '';
  document.getElementById('category-image-url').value = '';
  const img = document.getElementById('category-image-preview');
  img.src = ''; img.classList.add('d-none');
}

function resetUserForm() {
  const f = document.getElementById('user-form');
  f.reset();
  document.getElementById('user-id').value = '';
  document.getElementById('user-password').required = true;
  document.getElementById('user-role').value = 'USER';
}

function resetProductForm() {
  const f = document.getElementById('product-form');
  f.reset();
  document.getElementById('product-id').value = '';
  document.getElementById('product-quantity').value = 0;
}

// render options, tables
function renderCategoryOptions() {
  const catSel = document.getElementById('product-category');
  const catFilter = document.getElementById('product-category-filter');
  const userCatSel = document.getElementById('user-categories');
  const opts = cache.categories.map(c=>`<option value="${c.id}">${c.name}</option>`).join('');
  catSel.innerHTML = '<option value="" disabled selected>-- Chọn danh mục --</option>'+opts;
  catFilter.innerHTML = '<option value="">-- Tất cả danh mục --</option>'+opts;
  userCatSel.innerHTML = opts;
}

function renderUserOptions() {
  const userSel = document.getElementById('product-user');
  userSel.innerHTML = '<option value="" disabled selected>-- Chọn người dùng --</option>'
    + cache.users.map(u=>`<option value="${u.id}">${u.fullname}</option>`).join('');
}

function renderCategoryTable() {
  const tbody = document.querySelector('#category-table tbody');
  tbody.innerHTML = cache.categories.map(cat => `
    <tr>
      <td>${cat.id}</td>
      <td>${cat.name}</td>
      <td>${cat.images ? `<img src="${cat.images}" class="img-thumb" alt="${cat.name}">` : '<span class="text-muted">—</span>'}</td>
      <td class="text-end">
        <button class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${cat.id}">Sửa</button>
        <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${cat.id}">Xóa</button>
      </td>
    </tr>`).join('') || '<tr><td colspan="4" class="text-center text-muted">Chưa có danh mục</td></tr>';
}

function renderUserTable() {
  const tbody = document.querySelector('#user-table tbody');
  tbody.innerHTML = cache.users.map(u => `
    <tr>
      <td>${u.id}</td>
      <td>${u.fullname}</td>
      <td>${u.email}</td>
      <td>${u.phone || ''}</td>
      <td>${u.role || 'USER'}</td>
      <td>${(u.categories||[]).map(c=>`<span class="badge badge-category me-1">${c.name}</span>`).join('')}</td>
      <td class="text-end">
        <button class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${u.id}">Sửa</button>
        <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${u.id}">Xóa</button>
      </td>
    </tr>`).join('') || '<tr><td colspan="7" class="text-center text-muted">Chưa có người dùng</td></tr>';
}

function renderProductTable() {
  const tbody = document.querySelector('#product-table tbody');
  tbody.innerHTML = cache.products.map(p => `
    <tr>
      <td>${p.id}</td>
      <td>${p.title}</td>
      <td class="text-end">${p.quantity}</td>
      <td>${p.description||''}</td>
      <td class="text-end">${p.price.toFixed(2)}</td>
      <td>${p.category? p.category.name:''}</td>
      <td>${p.user? p.user.fullname:''}</td>
      <td class="text-end">
        <button class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${p.id}">Sửa</button>
        <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${p.id}">Xóa</button>
      </td>
    </tr>`).join('') || '<tr><td colspan="8" class="text-center text-muted">Chưa có sản phẩm</td></tr>';
}

// ============ LOAD ============

async function loadCategories() {
  const data = await graphqlFetch(`query { getAllCategories { id name images } }`);
  cache.categories = (data.getAllCategories||[]).map(c=>{
    // chuẩn hóa url ảnh: nếu chỉ là tên file -> thêm prefix
    if (c.images && !c.images.startsWith('http') && !c.images.startsWith('/uploads/')) {
      c.images = UPLOAD_PREFIX + c.images;
    }
    return c;
  });
  renderCategoryTable();
  renderCategoryOptions();
}

async function loadUsers() {
  const data = await graphqlFetch(`query { getAllUsers { id fullname email phone role categories { id name } } }`);
  cache.users = data.getAllUsers || [];
  renderUserTable();
  renderUserOptions();
}

async function loadProducts(filterCategoryId='') {
  let products = [];
  if (filterCategoryId) {
    const d = await graphqlFetch(`
      query($id: ID!) {
        productsByCategory(categoryId: $id) {
          id title quantity description price category { id name } user { id fullname }
        }
      }`, { id: filterCategoryId });
    products = d.productsByCategory || [];
  } else {
    const d = await graphqlFetch(`
      query {
        allProductsSortedByPrice {
          id title quantity description price category { id name } user { id fullname }
        }
      }`);
    products = d.allProductsSortedByPrice || [];
  }
  cache.products = products.map(p => ({...p, price: Number(p.price||0)}));
  renderProductTable();
}

// ============ EVENTS ============

document.addEventListener('change', (e)=>{
  // preview ảnh khi chọn file
  if (e.target.id === 'category-image-file') {
    const file = e.target.files?.[0];
    const img = document.getElementById('category-image-preview');
    if (file) {
      img.src = URL.createObjectURL(file);
      img.classList.remove('d-none');
    } else {
      img.src=''; img.classList.add('d-none');
    }
  }
});

// Table buttons
document.addEventListener('click', (e)=>{
  const btn = e.target.closest('button[data-action]');
  if (!btn) return;

  // CATEGORY
  if (btn.closest('#category-table')) {
    const id = btn.dataset.id;
    const cat = cache.categories.find(c=>String(c.id)===String(id));
    if (!cat) return;
    if (btn.dataset.action==='edit') {
      document.getElementById('category-id').value = cat.id;
      document.getElementById('category-name').value = cat.name;
      document.getElementById('category-image-url').value = cat.images?.replace(UPLOAD_PREFIX,'') || '';
      const img = document.getElementById('category-image-preview');
      if (cat.images) { img.src = cat.images; img.classList.remove('d-none'); } else { img.src=''; img.classList.add('d-none'); }
    } else if (btn.dataset.action==='delete') {
      if (confirm('Bạn có chắc muốn xóa danh mục này?')) {
        graphqlFetch(`mutation($id: ID!) { deleteCategory(id: $id) }`, {id: cat.id})
          .then(()=>Promise.all([loadCategories(), loadProducts(), loadUsers()]))
          .then(()=>showAlert('success','Đã xóa danh mục.'))
          .catch(err=>showAlert('danger', err.message));
      }
    }
  }

  // USER
  if (btn.closest('#user-table')) {
    const id = btn.dataset.id;
    const u = cache.users.find(x=>String(x.id)===String(id));
    if (!u) return;
    if (btn.dataset.action==='edit') {
      document.getElementById('user-id').value = u.id;
      document.getElementById('user-fullname').value = u.fullname;
      document.getElementById('user-email').value = u.email;
      document.getElementById('user-phone').value = u.phone || '';
      document.getElementById('user-password').value = '';
      document.getElementById('user-password').required = false;
      document.getElementById('user-role').value = u.role || 'USER';
      const selected = new Set((u.categories||[]).map(c=>String(c.id)));
      for (const opt of document.getElementById('user-categories').options) opt.selected = selected.has(opt.value);
    } else if (btn.dataset.action==='delete') {
      if (confirm('Bạn có chắc muốn xóa người dùng này?')) {
        graphqlFetch(`mutation($id: ID!) { deleteUser(id: $id) }`, {id: u.id})
          .then(()=>Promise.all([loadUsers(), loadProducts()]))
          .then(()=>showAlert('success','Đã xóa người dùng.'))
          .catch(err=>showAlert('danger', err.message));
      }
    }
  }

  // PRODUCT
  if (btn.closest('#product-table')) {
    const id = btn.dataset.id;
    const p = cache.products.find(x=>String(x.id)===String(id));
    if (!p) return;
    if (btn.dataset.action==='edit') {
      document.getElementById('product-id').value = p.id;
      document.getElementById('product-title').value = p.title;
      document.getElementById('product-quantity').value = p.quantity;
      document.getElementById('product-description').value = p.description || '';
      document.getElementById('product-price').value = p.price;
      document.getElementById('product-category').value = p.category ? p.category.id : '';
      document.getElementById('product-user').value = p.user ? p.user.id : '';
    } else if (btn.dataset.action==='delete') {
      if (confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
        graphqlFetch(`mutation($id: ID!) { deleteProduct(productId: $id) }`, {id: p.id})
          .then(()=>loadProducts(document.getElementById('product-category-filter').value))
          .then(()=>showAlert('success','Đã xóa sản phẩm.'))
          .catch(err=>showAlert('danger', err.message));
      }
    }
  }
});

// Submit CATEGORY (có upload ảnh nếu có file)
document.getElementById('category-form').addEventListener('submit', async (e)=>{
  e.preventDefault();
  const form = e.target;
  if (!form.checkValidity()) { form.classList.add('was-validated'); return; }

  try {
    const id = document.getElementById('category-id').value;
    const name = document.getElementById('category-name').value.trim();

    // 1) upload file nếu có
    const file = document.getElementById('category-image-file').files?.[0];
    let imageNameOrUrl = document.getElementById('category-image-url').value || '';
    if (file) {
      const fd = new FormData();
      fd.append('file', file);
      const res = await fetch(UPLOAD_ENDPOINT, { method:'POST', body: fd });
      if (!res.ok) throw new Error('Upload ảnh thất bại');
      const json = await res.json();   // {fileName, url}
      imageNameOrUrl = json.fileName;  // lưu tên file (backend sẽ map /uploads/**)
    }

    // 2) gọi GraphQL
    const variables = { name, images: imageNameOrUrl || null };
    let mutation;
    if (id) {
      variables.id = id;
      mutation = `mutation($id: ID!, $name: String, $images: String) {
        updateCategory(id: $id, name: $name, images: $images) { id }
      }`;
    } else {
      mutation = `mutation($name: String!, $images: String) {
        createCategory(name: $name, images: $images) { id }
      }`;
    }

    await graphqlFetch(mutation, variables);
    resetCategoryForm();
    await Promise.all([loadCategories(), loadProducts(), loadUsers()]);
    showAlert('success','Lưu danh mục thành công.');
  } catch (err) {
    showAlert('danger', err.message);
  }
});

// Submit USER
document.getElementById('user-form').addEventListener('submit', async (e)=>{
  e.preventDefault();
  const form = e.target;
  if (!form.checkValidity()) { form.classList.add('was-validated'); return; }

  const id = document.getElementById('user-id').value;
  const fullname = document.getElementById('user-fullname').value.trim();
  const email = document.getElementById('user-email').value.trim();
  const passwordValue = document.getElementById('user-password').value;
  const phone = document.getElementById('user-phone').value.trim();
  const role = document.getElementById('user-role').value;
  const categoryIds = Array.from(document.getElementById('user-categories').selectedOptions).map(o=>o.value);

  const variables = { id, fullname, email, phone: phone || null, categoryIds, role };
  if (!id) {
    if (!passwordValue) { showAlert('warning','Mật khẩu là bắt buộc.'); return; }
    variables.password = passwordValue;
  } else if (passwordValue) {
    variables.password = passwordValue;
  }

  const mutation = id ? `mutation($id: ID!, $fullname: String, $email: String, $password: String, $phone: String, $categoryIds: [ID!], $role: Role) {
    updateUser(id: $id, fullname: $fullname, email: $email, password: $password, phone: $phone, categoryIds: $categoryIds, role: $role) { id }
  }` : `mutation($fullname: String!, $email: String!, $password: String!, $phone: String, $categoryIds: [ID!], $role: Role) {
    createUser(fullname: $fullname, email: $email, password: $password, phone: $phone, categoryIds: $categoryIds, role: $role) { id }
  }`;

  graphqlFetch(mutation, variables)
    .then(()=>Promise.all([loadUsers(), loadProducts()]))
    .then(()=>{ resetUserForm(); showAlert('success','Lưu người dùng thành công.'); })
    .catch(err=>showAlert('danger', err.message));
});

// Submit PRODUCT
document.getElementById('product-form').addEventListener('submit', (e)=>{
  e.preventDefault();
  const form = e.target;
  if (!form.checkValidity()) { form.classList.add('was-validated'); return; }

  const id = document.getElementById('product-id').value;
  const title = document.getElementById('product-title').value.trim();
  const quantity = Number(document.getElementById('product-quantity').value || 0);
  const description = document.getElementById('product-description').value.trim();
  const price = Number(document.getElementById('product-price').value || 0);
  const categoryId = document.getElementById('product-category').value;
  const userId = document.getElementById('product-user').value;

  if (Number.isNaN(price)) { showAlert('warning','Giá bán không hợp lệ'); return; }

  const variables = { productId: id || undefined, title, quantity, description: description||null, price, categoryId, userId };
  const mutation = id ? `mutation($productId: ID!, $title: String, $quantity: Int, $description: String, $price: Float, $categoryId: ID, $userId: ID) {
    updateProduct(productId: $productId, title: $title, quantity: $quantity, description: $description, price: $price, categoryId: $categoryId, userId: $userId) { id }
  }` : `mutation($title: String!, $quantity: Int!, $description: String, $price: Float!, $categoryId: ID!, $userId: ID!) {
    createProduct(title: $title, quantity: $quantity, description: $description, price: $price, categoryId: $categoryId, userId: $userId) { id }
  }`;

  graphqlFetch(mutation, variables)
    .then(()=> {
      resetProductForm();
      const cur = document.getElementById('product-category-filter').value;
      return loadProducts(cur);
    })
    .then(()=>showAlert('success','Lưu sản phẩm thành công.'))
    .catch(err=>showAlert('danger', err.message));
});

// filter product
document.getElementById('product-filter-reset').addEventListener('click', ()=>{
  document.getElementById('product-category-filter').value = '';
  loadProducts();
});
document.getElementById('product-category-filter').addEventListener('change', (e)=>{
  loadProducts(e.target.value);
});

// Toggle password
document.getElementById('toggle-user-password').addEventListener('click', ()=>{
  const inp = document.getElementById('user-password');
  const icon = document.querySelector('#toggle-user-password i');
  if (inp.type === 'password') { inp.type = 'text'; icon.className = 'bi bi-eye-slash'; }
  else { inp.type = 'password'; icon.className = 'bi bi-eye'; }
});

// bootstrap client validation
(function(){
  document.querySelectorAll('.needs-validation').forEach(f=>{
    f.addEventListener('submit', e=>{
      if (!f.checkValidity()) { e.preventDefault(); e.stopPropagation(); }
      f.classList.add('was-validated');
    }, false);
  });
})();

document.addEventListener('DOMContentLoaded', ()=>{
  Promise.all([loadCategories(), loadUsers()]).then(()=>loadProducts()).catch(err=>showAlert('danger', err.message));
});
