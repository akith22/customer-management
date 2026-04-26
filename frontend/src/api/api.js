import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
});

export function getCustomers(page = 0, size = 20, sortBy = 'createdAt', direction = 'desc') {
  return api.get('/api/customers', { params: { page, size, sortBy, direction } });
}

export function searchCustomers(query, size = 20) {
  const trimmed = query.trim();
  if (!trimmed) return Promise.resolve({ data: { content: [] } });
  const isId = /^\d+$/.test(trimmed);
  if (isId) {
    return api.get(`/api/customers/${trimmed}`).then(res => ({
      data: { content: [res.data] }
    })).catch(() => ({ data: { content: [] } }));
  }
  return api.get('/api/customers', {
    params: { page: 0, size, sortBy: 'name', direction: 'asc', name: trimmed }
  });
}

export function getCustomerById(id) {
  return api.get(`/api/customers/${id}`);
}

export function createCustomer(data) {
  return api.post('/api/customers', data);
}

export function updateCustomer(id, data) {
  return api.put(`/api/customers/${id}`, data);
}

export function bulkUploadCustomers(file) {
  const fd = new FormData();
  fd.append('file', file);
  return api.post('/api/customers/bulk/upload', fd);
}

export default api;
