import React, { useState, useEffect, useContext, useCallback } from 'react';
import { Link } from 'react-router-dom';
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
    if (sortBy !== col) return <span className="sort-icon">&uarr;</span>;
    return <span className="sort-icon">{direction === 'asc' ? '\u2191' : '\u2193'}</span>;
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Customers</h1>
        <div className="flex gap-12">
          <Link to="/bulk-upload" className="btn btn-ghost" id="btn-bulk-upload">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            Bulk Upload
          </Link>
          <Link to="/customers/new" className="btn btn-primary" id="btn-new-customer">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            New Customer
          </Link>
        </div>
      </div>

      <div className="table-wrapper">
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
                      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></svg>
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
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                      </Link>
                      <Link to={`/customers/${c.id}/edit`} className="btn-icon" title="Edit">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
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
