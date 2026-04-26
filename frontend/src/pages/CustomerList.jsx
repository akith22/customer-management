import React, { useState, useEffect, useContext, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { UploadCloud, Plus, Eye, Edit2, SearchX, ArrowUp, ArrowDown, ArrowUpDown } from 'lucide-react';
import { getCustomers } from '../api/api';
import Pagination from '../components/UI/Pagination';
import { ToastContext } from '../components/Layout/Layout';

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Name' },
  { key: 'nic', label: 'NIC' },
  { key: 'dob', label: 'Date of Birth' },
  { key: 'createdAt', label: 'Created At' },
];

function SkeletonRows({ count = 5 }) {
  return Array.from({ length: count }).map((_, i) => (
    <tr key={i}>
      {[...columns, { key: 'actions' }].map((c, j) => (
        <td key={j}><div className="skeleton skeleton-row" style={{ width: j === 0 ? 40 : j === 5 ? 80 : '80%' }} /></td>
      ))}
    </tr>
  ));
}

export default function CustomerList() {
  const addToast = useContext(ToastContext);
  const [data, setData] = useState({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20 });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [sortBy, setSortBy] = useState('createdAt');
  const [direction, setDirection] = useState('desc');
  const [loading, setLoading] = useState(true);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const res = await getCustomers(page, size, sortBy, direction);
      setData(res.data);
    } catch (err) {
      addToast(err.response?.data?.error || 'Failed to fetch customers', 'error');
    } finally {
      setLoading(false);
    }
  }, [page, size, sortBy, direction, addToast]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleSort = (key) => {
    if (sortBy === key) {
      setDirection(d => d === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(key);
      setDirection('asc');
    }
    setPage(0);
  };

  const handleSizeChange = (newSize) => {
    setSize(newSize);
    setPage(0);
  };

  const SortArrow = ({ col }) => {
    if (sortBy !== col) return <span className="sort-icon"><ArrowUpDown size={14} /></span>;
    return <span className="sort-icon">{direction === 'asc' ? <ArrowUp size={14} /> : <ArrowDown size={14} />}</span>;
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Customers</h1>
        <div className="flex gap-12">
          <Link to="/bulk-upload" className="btn btn-ghost" id="btn-bulk-upload">
            <UploadCloud size={16} />
            Bulk Upload
          </Link>
          <Link to="/customers/new" className="btn btn-primary" id="btn-new-customer">
            <Plus size={16} />
            New Customer
          </Link>
        </div>
      </div>

      <div className="table-wrapper" style={{ borderRadius: 'var(--radius-lg) var(--radius-lg) 0 0' }}>
        <table className="table" id="customer-table">
          <thead>
            <tr>
              {columns.map(col => (
                <th
                  key={col.key}
                  className={sortBy === col.key ? 'sorted' : ''}
                  onClick={() => handleSort(col.key)}
                >
                  {col.label} <SortArrow col={col.key} />
                </th>
              ))}
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <SkeletonRows count={size > 10 ? 10 : size} />
            ) : data.content.length === 0 ? (
              <tr>
                <td colSpan={6}>
                  <div className="empty-state">
                    <div className="empty-state-icon">
                      <SearchX size={48} strokeWidth={1.5} />
                    </div>
                    <div className="empty-state-title">No customers found</div>
                    <div className="empty-state-text">Get started by creating your first customer.</div>
                  </div>
                </td>
              </tr>
            ) : (
              data.content.map(c => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td style={{ color: 'var(--text-primary)', fontWeight: 500 }}>{c.name}</td>
                  <td><span className="badge badge-ghost">{c.nic}</span></td>
                  <td>{c.dob}</td>
                  <td>{c.createdAt ? new Date(c.createdAt).toLocaleDateString() : '-'}</td>
                  <td>
                    <div className="actions">
                      <Link to={`/customers/${c.id}`} className="btn-icon" title="View">
                        <Eye size={16} />
                      </Link>
                      <Link to={`/customers/${c.id}/edit`} className="btn-icon" title="Edit">
                        <Edit2 size={16} />
                      </Link>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {!loading && data.content.length > 0 && (
        <Pagination
          page={data.number}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          size={size}
          onPageChange={setPage}
          onSizeChange={handleSizeChange}
        />
      )}
    </div>
  );
}
