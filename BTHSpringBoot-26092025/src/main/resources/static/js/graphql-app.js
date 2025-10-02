const GRAPHQL_ENDPOINT = '/graphql';

let cache = {
  categories: [],
  users: [],
  products: []
};

async function graphqlFetch(query, variables = {}) {
  const res = await fetch(GRAPHQL_ENDPOINT, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, variables })
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`HTTP ${res.status}: ${text}`);
  }
  const payload = await res.json();
  if (payload.errors) {
    throw new Error(payload.errors.map(e => e.message).join('\n'));
  }
  return payload.data;
}

function showError(err) {
  console.error(err);
  alert(err.message || 'Có lỗi xảy ra, vui lòng thử lại.');
}

function resetCategoryForm() {
  const form = document.getElementById('category-form');
  form.reset();
  form.querySelector('#category-id').value = '';
}

function resetUserForm() {
  const form = document.getElementById('user-form');
  form.reset();
  form.querySelector('#user-id').value = '';
  document.getElementById('user-password').required = true;
}

function resetProductForm() {
  const form = document.getElementById('product-form');
  form.reset();
  form.querySelector('#product-id').value = '';
  document.getElementById('product-quantity').value = 0;
}

function renderCategoryOptions() {
  const categorySelect = document.getElementById('product-category');
  const categoryFilter = document.getElementById('product-category-filter');
  const userCategorySelect = document.getElementById('user-categories');
  if (!categorySelect) return;

  const optionsHtml = cache.categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  categorySelect.innerHTML = '<option value="" disabled selected>-- Chọn danh mục --</option>' + optionsHtml;
  categoryFilter.innerHTML = '<option value="">-- Tất cả danh mục --</option>' + optionsHtml;
  userCategorySelect.innerHTML = optionsHtml;
}

function renderUserOptions() {
  const userSelect = document.getElementById('product-user');
  const optionsHtml = cache.users.map(u => `<option value="${u.id}">${u.fullname}</option>`).join('');
  userSelect.innerHTML = '<option value="" disabled selected>-- Chọn người dùng --</option>' + optionsHtml;
}

function renderCategoryTable() {
  const tbody = document.querySelector('#category-table tbody');
  tbody.innerHTML = cache.categories.map(cat => `
    <tr>
      <td>${cat.id}</td>
      <td>${cat.name}</td>
      <td>${cat.images ? `<a href="${cat.images}" target="_blank">${cat.images}</a>` : ''}</td>
      <td class="text-end">
        <button class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${cat.id}">Sửa</button>
        <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${cat.id}">Xóa</button>
      </td>
    </tr>`).join('') || '<tr><td colspan="4" class="text-center text-muted">Chưa có danh mục</td></tr>';
}

function renderUserTable() {
  const tbody = document.querySelector('#user-table tbody');
  tbody.innerHTML = cache.users.map(user => `
    <tr>
      <td>${user.id}</td>
      <td>${user.fullname}</td>
      <td>${user.email}</td>
      <td>${user.phone || ''}</td>
      <td>${(user.categories || []).map(c => `<span class="badge badge-category me-1">${c.name}</span>`).join('')}</td>
      <td class="text-end">
        <button class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${user.id}">Sửa</button>
        <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${user.id}">Xóa</button>
      </td>
    </tr>`).join('') || '<tr><td colspan="6" class="text-center text-muted">Chưa có người dùng</td></tr>';
}

function renderProductTable() {
  const tbody = document.querySelector('#product-table tbody');
  tbody.innerHTML = cache.products.map(product => `
    <tr>
      <td>${product.id}</td>
      <td>${product.title}</td>
      <td class="text-end">${product.quantity}</td>
      <td>${product.description || ''}</td>
      <td class="text-end">${product.price.toFixed(2)}</td>
      <td>${product.category ? product.category.name : ''}</td>
      <td>${product.user ? product.user.fullname : ''}</td>
      <td class="text-end">
        <button class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${product.id}">Sửa</button>
        <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${product.id}">Xóa</button>
      </td>
    </tr>`).join('') || '<tr><td colspan="8" class="text-center text-muted">Chưa có sản phẩm</td></tr>';
}

async function loadCategories() {
  const data = await graphqlFetch(`query { getAllCategories { id name images } }`);
  cache.categories = data.getAllCategories || [];
  renderCategoryTable();
  renderCategoryOptions();
}

async function loadUsers() {
  const data = await graphqlFetch(`query { getAllUsers { id fullname email phone categories { id name } } }`);
  cache.users = data.getAllUsers || [];
  renderUserTable();
  renderUserOptions();
}

async function loadProducts(filterCategoryId = '') {
  let products = [];
  if (filterCategoryId) {
    const data = await graphqlFetch(`query($id: ID!) {
      productsByCategory(categoryId: $id) {
        id title quantity description price category { id name } user { id fullname }
      }
    }`, { id: filterCategoryId });
    products = data.productsByCategory || [];
  } else {
    const data = await graphqlFetch(`query {
      allProductsSortedByPrice {
        id title quantity description price category { id name } user { id fullname }
      }
    }`);
    products = data.allProductsSortedByPrice || [];
  }
  cache.products = products.map(p => ({ ...p, price: Number(p.price || 0) }));
  renderProductTable();
}

function handleCategoryTableClick(event) {
  const button = event.target.closest('button[data-action]');
  if (!button) return;
  const id = button.dataset.id;
  const action = button.dataset.action;
  const category = cache.categories.find(c => String(c.id) === String(id));
  if (!category) return;
  if (action === 'edit') {
    const form = document.getElementById('category-form');
    form.querySelector('#category-id').value = category.id;
    form.querySelector('#category-name').value = category.name;
    form.querySelector('#category-images').value = category.images || '';
  } else if (action === 'delete') {
    if (confirm('Bạn có chắc muốn xóa danh mục này?')) {
      graphqlFetch(`mutation($id: ID!) { deleteCategory(id: $id) }`, { id: category.id })
        .then(() => Promise.all([loadCategories(), loadProducts(), loadUsers()]))
        .catch(showError);
    }
  }
}

function handleUserTableClick(event) {
  const button = event.target.closest('button[data-action]');
  if (!button) return;
  const id = button.dataset.id;
  const action = button.dataset.action;
  const user = cache.users.find(u => String(u.id) === String(id));
  if (!user) return;
  if (action === 'edit') {
    const form = document.getElementById('user-form');
    form.querySelector('#user-id').value = user.id;
    form.querySelector('#user-fullname').value = user.fullname;
    form.querySelector('#user-email').value = user.email;
    form.querySelector('#user-phone').value = user.phone || '';
    form.querySelector('#user-password').value = '';
    document.getElementById('user-password').required = false;
    const selectedIds = new Set((user.categories || []).map(c => String(c.id)));
    for (const option of document.getElementById('user-categories').options) {
      option.selected = selectedIds.has(option.value);
    }
  } else if (action === 'delete') {
    if (confirm('Bạn có chắc muốn xóa người dùng này?')) {
      graphqlFetch(`mutation($id: ID!) { deleteUser(id: $id) }`, { id: user.id })
        .then(() => Promise.all([loadUsers(), loadProducts()]))
        .catch(showError);
    }
  }
}

function handleProductTableClick(event) {
  const button = event.target.closest('button[data-action]');
  if (!button) return;
  const id = button.dataset.id;
  const action = button.dataset.action;
  const product = cache.products.find(p => String(p.id) === String(id));
  if (!product) return;
  if (action === 'edit') {
    const form = document.getElementById('product-form');
    form.querySelector('#product-id').value = product.id;
    form.querySelector('#product-title').value = product.title;
    form.querySelector('#product-quantity').value = product.quantity;
    form.querySelector('#product-description').value = product.description || '';
    form.querySelector('#product-price').value = product.price;
    form.querySelector('#product-category').value = product.category ? product.category.id : '';
    form.querySelector('#product-user').value = product.user ? product.user.id : '';
  } else if (action === 'delete') {
    if (confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
      graphqlFetch(`mutation($id: ID!) { deleteProduct(productId: $id) }`, { id: product.id })
        .then(() => loadProducts(document.getElementById('product-category-filter').value))
        .catch(showError);
    }
  }
}

async function handleCategorySubmit(event) {
  event.preventDefault();
  const form = event.target;
  if (!form.checkValidity()) {
    form.classList.add('was-validated');
    return;
  }
  const id = form.querySelector('#category-id').value;
  const name = form.querySelector('#category-name').value.trim();
  const images = form.querySelector('#category-images').value.trim();
  const variables = { name, images: images || null };
  const mutation = id ? `mutation($id: ID!, $name: String, $images: String) {
    updateCategory(id: $id, name: $name, images: $images) { id }
  }` : `mutation($name: String!, $images: String) {
    createCategory(name: $name, images: $images) { id }
  }`;
  if (id) variables.id = id;
  graphqlFetch(mutation, variables)
    .then(() => {
      resetCategoryForm();
      return Promise.all([loadCategories(), loadProducts(), loadUsers()]);
    })
    .catch(showError);
}

async function handleUserSubmit(event) {
  event.preventDefault();
  const form = event.target;
  if (!form.checkValidity()) {
    form.classList.add('was-validated');
    return;
  }
  const id = form.querySelector('#user-id').value;
  const fullname = form.querySelector('#user-fullname').value.trim();
  const email = form.querySelector('#user-email').value.trim();
  const passwordValue = form.querySelector('#user-password').value;
  const phone = form.querySelector('#user-phone').value.trim();
  const categoryIds = Array.from(form.querySelector('#user-categories').selectedOptions).map(o => o.value);

  const variables = { id, fullname, email, phone: phone || null, categoryIds };
  if (!id) {
    variables.password = passwordValue;
    if (!passwordValue) {
      alert('Mật khẩu là bắt buộc khi tạo mới.');
      return;
    }
  } else if (passwordValue) {
    variables.password = passwordValue;
  }

  const mutation = id ? `mutation($id: ID!, $fullname: String, $email: String, $password: String, $phone: String, $categoryIds: [ID!]) {
    updateUser(id: $id, fullname: $fullname, email: $email, password: $password, phone: $phone, categoryIds: $categoryIds) { id }
  }` : `mutation($fullname: String!, $email: String!, $password: String!, $phone: String, $categoryIds: [ID!]) {
    createUser(fullname: $fullname, email: $email, password: $password, phone: $phone, categoryIds: $categoryIds) { id }
  }`;

  graphqlFetch(mutation, variables)
    .then(() => {
      resetUserForm();
      return Promise.all([loadUsers(), loadProducts()]);
    })
    .catch(showError);
}

async function handleProductSubmit(event) {
  event.preventDefault();
  const form = event.target;
  if (!form.checkValidity()) {
    form.classList.add('was-validated');
    return;
  }
  const id = form.querySelector('#product-id').value;
  const title = form.querySelector('#product-title').value.trim();
  const quantity = Number(form.querySelector('#product-quantity').value || 0);
  const description = form.querySelector('#product-description').value.trim();
  const price = Number(form.querySelector('#product-price').value || 0);
  const categoryId = form.querySelector('#product-category').value;
  const userId = form.querySelector('#product-user').value;

  const variables = { productId: id || undefined, title, quantity, description: description || null, price, categoryId, userId };

  const mutation = id ? `mutation($productId: ID!, $title: String, $quantity: Int, $description: String, $price: Float, $categoryId: ID, $userId: ID) {
    updateProduct(productId: $productId, title: $title, quantity: $quantity, description: $description, price: $price, categoryId: $categoryId, userId: $userId) { id }
  }` : `mutation($title: String!, $quantity: Int!, $description: String, $price: Float!, $categoryId: ID!, $userId: ID!) {
    createProduct(title: $title, quantity: $quantity, description: $description, price: $price, categoryId: $categoryId, userId: $userId) { id }
  }`;

  graphqlFetch(mutation, variables)
    .then(() => {
      resetProductForm();
      const currentFilter = document.getElementById('product-category-filter').value;
      return loadProducts(currentFilter);
    })
    .catch(showError);
}

function setupEventListeners() {
  document.getElementById('category-form').addEventListener('submit', handleCategorySubmit);
  document.getElementById('category-table').addEventListener('click', handleCategoryTableClick);
  document.getElementById('category-reset').addEventListener('click', resetCategoryForm);

  document.getElementById('user-form').addEventListener('submit', handleUserSubmit);
  document.getElementById('user-table').addEventListener('click', handleUserTableClick);
  document.getElementById('user-reset').addEventListener('click', resetUserForm);

  document.getElementById('product-form').addEventListener('submit', handleProductSubmit);
  document.getElementById('product-table').addEventListener('click', handleProductTableClick);
  document.getElementById('product-filter-reset').addEventListener('click', () => {
    document.getElementById('product-category-filter').value = '';
    loadProducts();
  });
  document.getElementById('product-category-filter').addEventListener('change', event => {
    loadProducts(event.target.value);
  });
}

document.addEventListener('DOMContentLoaded', () => {
  setupEventListeners();
  Promise.all([loadCategories(), loadUsers()])
    .then(() => loadProducts())
    .catch(showError);
});
