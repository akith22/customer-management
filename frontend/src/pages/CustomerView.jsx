import React, { useState, useEffect, useContext } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getCustomerById } from '../api/api';
import Badge from '../components/UI/Badge';
import Spinner from '../components/UI/Spinner';
import { ToastContext } from '../components/Layout/Layout';

export default function CustomerView() {
  const { id } = useParams();
  const addToast = useContext(ToastContext);
  const [customer, setCustomer] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await getCustomerById(id);
        setCustomer(res.data);
      } catch (err) {
        addToast(err.response?.data?.error || 'Failed to load customer', 'error');
      } finally {
        setLoading(false);
      }
    })();
  }, [id, addToast]);

  if (loading) return <div className="loading-center"><Spinner size="lg" /></div>;
  if (!customer) return <div className="empty-state"><div className="empty-state-title">Customer not found</div></div>;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">{customer.name}</h1>
        <Link to={`/customers/${id}/edit`} className="btn btn-primary" id="btn-edit-customer">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          Edit
        </Link>
      </div>

      <div className="card">
        <div className="form-section">
          <div className="form-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            Customer Details
          </div>
          <div className="detail-grid">
            <div className="detail-item">
              <span className="detail-label">ID</span>
              <span className="detail-value">{customer.id}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Name</span>
              <span className="detail-value">{customer.name}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">NIC</span>
              <span className="detail-value"><Badge variant="ghost">{customer.nic}</Badge></span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Date of Birth</span>
              <span className="detail-value">{customer.dob}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Created At</span>
              <span className="detail-value">{customer.createdAt ? new Date(customer.createdAt).toLocaleString() : '-'}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Updated At</span>
              <span className="detail-value">{customer.updatedAt ? new Date(customer.updatedAt).toLocaleString() : '-'}</span>
            </div>
          </div>
        </div>

        {/* Mobile Numbers */}
        <div className="form-section">
          <div className="form-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>
            Mobile Numbers
          </div>
          {customer.mobiles && customer.mobiles.length > 0 ? (
            <div className="flex gap-8" style={{ flexWrap: 'wrap' }}>
              {customer.mobiles.map((m, i) => (
                <Badge key={i} variant="blue">{typeof m === 'string' ? m : m.mobile || m}</Badge>
              ))}
            </div>
          ) : (
            <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No mobile numbers</span>
          )}
        </div>

        {/* Addresses */}
        <div className="form-section">
          <div className="form-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            Addresses
          </div>
          {customer.addresses && customer.addresses.length > 0 ? (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 16 }}>
              {customer.addresses.map((a, i) => (
                <div key={i} className="address-card">
                  <div style={{ marginBottom: 8, fontWeight: 500 }}>{a.addressLine1}</div>
                  {a.addressLine2 && <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{a.addressLine2}</div>}
                  {a.city && (
                    <div style={{ marginTop: 8, fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {a.city.name}{a.city.countryName ? `, ${a.city.countryName}` : ''}{a.city.countryCode ? ` (${a.city.countryCode})` : ''}
                    </div>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No addresses</span>
          )}
        </div>

        {/* Family Members */}
        <div className="form-section">
          <div className="form-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            Family Members
          </div>
          {customer.familyMembers && customer.familyMembers.length > 0 ? (
            <div className="flex gap-8" style={{ flexWrap: 'wrap' }}>
              {customer.familyMembers.map(f => (
                <Link key={f.id} to={`/customers/${f.id}`} className="chip chip-clickable">
                  <span>{f.name} ({f.nic})</span>
                </Link>
              ))}
            </div>
          ) : (
            <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No family members linked</span>
          )}
        </div>
      </div>
    </div>
  );
}
