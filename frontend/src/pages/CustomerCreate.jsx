import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { User, Phone, MapPin, Link as LinkIcon, Plus, X, Check } from 'lucide-react';
import { createCustomer, getCustomerById, searchCustomers } from '../api/api';
import Input from '../components/UI/Input';
import Button from '../components/UI/Button';
import Spinner from '../components/UI/Spinner';
import { ToastContext } from '../components/Layout/Layout';

const NIC_REGEX = /^(\d{12}|\d{9}[Vv])$/;

export default function CustomerCreate() {
  const navigate = useNavigate();
  const addToast = useContext(ToastContext);
  const [submitting, setSubmitting] = useState(false);

  const [form, setForm] = useState({ name: '', dob: '', nic: '' });
  const [errors, setErrors] = useState({});
  const [mobiles, setMobiles] = useState([]);
  const [addresses, setAddresses] = useState([]);

  // Family members
  const [familyMembers, setFamilyMembers] = useState([]);
  const [familySearch, setFamilySearch] = useState({
    show: false,
    query: '',
    loading: false,
    results: [],
    error: '',
  });

  const validate = () => {
    const e = {};
    if (!form.name || form.name.trim().length < 2) e.name = 'Name is required (min 2 characters)';
    if (!form.dob) e.dob = 'Date of birth is required';
    else if (new Date(form.dob) >= new Date()) e.dob = 'Date of birth must be in the past';
    if (!form.nic) e.nic = 'NIC is required';
    else if (!NIC_REGEX.test(form.nic)) e.nic = 'NIC must be 12 digits or 9 digits + V';
    mobiles.forEach((m, i) => { if (!m.trim()) e[`mobile_${i}`] = 'Mobile number cannot be empty'; });
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setSubmitting(true);
    try {
      const payload = {
        name: form.name.trim(),
        dob: form.dob,
        nic: form.nic.trim(),
        mobiles: mobiles.filter(m => m.trim()).map(m => ({ mobile: m.trim() })),
        addresses: addresses
          .filter(a => a.addressLine1.trim())
          .map(a => ({
            addressLine1: a.addressLine1,
            addressLine2: a.addressLine2,
            cityName: a.cityName,
            countryName: a.countryName,
          })),
        familyMemberIds: familyMembers.map(f => f.id),
      };
      const res = await createCustomer(payload);
      addToast('Customer created successfully', 'success');
      navigate(`/customers/${res.data.id}`);
    } catch (err) {
      addToast(err.response?.data?.error || 'Failed to create customer', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const addMobile = () => { if (mobiles.length < 10) setMobiles([...mobiles, '']); };
  const removeMobile = (i) => setMobiles(mobiles.filter((_, idx) => idx !== i));
  const updateMobile = (i, val) => { const m = [...mobiles]; m[i] = val; setMobiles(m); };

  const addAddress = () => {
    if (addresses.length < 5) setAddresses([...addresses, { addressLine1: '', addressLine2: '', cityName: '', countryName: '' }]);
  };
  const removeAddress = (i) => setAddresses(addresses.filter((_, idx) => idx !== i));
  const updateAddress = (i, field, val) => {
    const a = [...addresses]; a[i] = { ...a[i], [field]: val }; setAddresses(a);
  };

  const searchFamily = async (query) => {
    if (!query || query.trim().length < 1) {
      setFamilySearch(s => ({ ...s, results: [], error: '' }));
      return;
    }
    setFamilySearch(s => ({ ...s, loading: true, results: [], error: '' }));
    try {
      const res = await searchCustomers(query.trim(), 8);
      const customers = res.data?.content || [];
      setFamilySearch(s => ({
        ...s,
        loading: false,
        results: customers,
        error: customers.length === 0 ? 'No customers found' : '',
      }));
    } catch {
      setFamilySearch(s => ({ ...s, loading: false, results: [], error: 'Search failed' }));
    }
  };

  const confirmFamily = (customer) => {
    if (familyMembers.find(f => f.id === customer.id)) {
      setFamilySearch(s => ({ ...s, error: 'Already linked' }));
      return;
    }
    setFamilyMembers([...familyMembers, { id: customer.id, name: customer.name, nic: customer.nic }]);
    setFamilySearch({ show: false, query: '', loading: false, results: [], error: '' });
  };

  const cancelFamilySearch = () => {
    setFamilySearch({ show: false, query: '', loading: false, results: [], error: '' });
  };

  const removeFamily = (id) => setFamilyMembers(familyMembers.filter(f => f.id !== id));

  const nicValid = form.nic && NIC_REGEX.test(form.nic);
  const nicInvalid = form.nic && !NIC_REGEX.test(form.nic);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">New Customer</h1>
      </div>

      <div className="create-form-wrapper">
        <form onSubmit={handleSubmit} className="card">
          {/* SECTION 1: Core Info */}
          <div className="form-section">
            <div className="form-section-title">
              <User size={18} />
              Core Information
            </div>
            <div className="form-grid">
              <Input
                id="input-name"
                label="Name"
                placeholder="Enter full name"
                value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })}
                error={errors.name}
              />
              <div className="input-group">
                <label htmlFor="input-dob">Date of Birth</label>
                <input
                  id="input-dob"
                  className={`input${errors.dob ? ' error' : ''}`}
                  type="date"
                  value={form.dob}
                  max={new Date().toISOString().split('T')[0]}
                  min="1900-01-01"
                  onChange={e => setForm({ ...form, dob: e.target.value })}
                />
                {errors.dob && <span className="input-error">{errors.dob}</span>}
              </div>
            <div className="input-group">
              <label>NIC Number</label>
              <div style={{ position: 'relative' }}>
                <input
                  id="input-nic"
                  className={`input ${nicInvalid ? 'error' : ''} ${nicValid ? 'success' : ''}`}
                  placeholder="e.g. 200012345678 or 901234567V"
                  value={form.nic}
                  onChange={e => setForm({ ...form, nic: e.target.value })}
                  style={{ width: '100%' }}
                />
                {nicValid && (
                  <span style={{ position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)' }}>
                    <Check size={18} className="text-success" strokeWidth={2.5} />
                  </span>
                )}
              </div>
              {errors.nic && <span className="input-error">{errors.nic}</span>}
              {nicInvalid && !errors.nic && <span className="input-error">NIC must be 12 digits or 9 digits + V</span>}
            </div>
          </div>
        </div>

        {/* SECTION 2: Mobile Numbers */}
        <div className="form-section">
          <div className="form-section-title">
            <Phone size={18} />
            Mobile Numbers
          </div>
          {mobiles.map((m, i) => (
            <div key={i} className="input-row mb-8">
              <Input
                id={`input-mobile-${i}`}
                placeholder="Enter mobile number"
                value={m}
                onChange={e => updateMobile(i, e.target.value)}
                error={errors[`mobile_${i}`]}
              />
              <button type="button" className="btn-icon danger" onClick={() => removeMobile(i)} title="Remove">
                <X size={18} />
              </button>
            </div>
          ))}
          <Button type="button" variant="ghost" size="sm" onClick={addMobile} disabled={mobiles.length >= 10}>
            <Plus size={14} />
            Add Mobile {mobiles.length > 0 && `(${mobiles.length}/10)`}
          </Button>
        </div>

        {/* SECTION 3: Addresses */}
        <div className="form-section">
          <div className="form-section-title">
            <MapPin size={18} />
            Addresses
          </div>
          {addresses.map((a, i) => (
            <div key={i} className="address-card mb-16">
              <button type="button" className="btn-icon danger remove-btn" onClick={() => removeAddress(i)} title="Remove address">
                <X size={16} />
              </button>
              <div className="form-grid">
                <Input
                  id={`input-addr1-${i}`}
                  label="Address Line 1"
                  placeholder="Street address"
                  value={a.addressLine1}
                  onChange={e => updateAddress(i, 'addressLine1', e.target.value)}
                />
                <Input
                  id={`input-addr2-${i}`}
                  label="Address Line 2"
                  placeholder="Apartment, suite, etc."
                  value={a.addressLine2}
                  onChange={e => updateAddress(i, 'addressLine2', e.target.value)}
                />
                <Input
                  id={`input-city-${i}`}
                  label="City"
                  placeholder="City"
                  value={a.cityName}
                  onChange={e => updateAddress(i, 'cityName', e.target.value)}
                />
                <Input
                  id={`input-country-${i}`}
                  label="Country"
                  placeholder="Country"
                  value={a.countryName}
                  onChange={e => updateAddress(i, 'countryName', e.target.value)}
                />
              </div>
            </div>
          ))}
          <Button type="button" variant="ghost" size="sm" onClick={addAddress} disabled={addresses.length >= 5}>
            <Plus size={14} />
            Add Address {addresses.length > 0 && `(${addresses.length}/5)`}
          </Button>
        </div>

        {/* SECTION 4: Family Members */}
        <div className="form-section">
          <div className="form-section-title">
            <LinkIcon size={18} />
            Link Family Members
          </div>

          {familyMembers.map(f => (
            <div key={f.id} className="chip mb-8" style={{ marginRight: 8 }}>
              <span>{f.name} ({f.nic})</span>
              <button type="button" className="chip-remove" onClick={() => removeFamily(f.id)}>
                <X size={14} />
              </button>
            </div>
          ))}

          {familySearch.show && (
            <div className="family-search-wrapper">
              <div className="input-row mb-8" style={{ alignItems: 'flex-start' }}>
                <div className="input-group" style={{ flex: 1 }}>
                  <input
                    id="input-family-search"
                    className="input"
                    placeholder="Search by name or ID..."
                    value={familySearch.query}
                    onChange={e => {
                      const q = e.target.value;
                      setFamilySearch(s => ({ ...s, query: q }));
                      searchFamily(q);
                    }}
                    autoComplete="off"
                  />
                </div>
                <button type="button" className="btn-icon" onClick={cancelFamilySearch} title="Cancel">
                  <X size={16} />
                </button>
              </div>

              {familySearch.loading && (
                <div style={{ padding: '8px 0', color: 'var(--text-muted)', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Spinner size="sm" /> Searching...
                </div>
              )}

              {familySearch.error && !familySearch.loading && (
                <div className="input-error mb-8">{familySearch.error}</div>
              )}

              {!familySearch.loading && familySearch.results.length > 0 && (
                <div className="family-search-results">
                  {familySearch.results.map(customer => (
                    <button
                      key={customer.id}
                      type="button"
                      className="family-search-result-item"
                      onClick={() => confirmFamily(customer)}
                      disabled={!!familyMembers.find(f => f.id === customer.id)}
                    >
                      <div className="family-result-info">
                        <span className="family-result-name">{customer.name}</span>
                        <span className="family-result-meta">ID: {customer.id} · {customer.nic}</span>
                      </div>
                      {familyMembers.find(f => f.id === customer.id) ? (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Added</span>
                      ) : (
                        <Check size={14} style={{ color: 'var(--accent-blue)' }} />
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {!familySearch.show && (
            <Button type="button" variant="ghost" size="sm" onClick={() => setFamilySearch(s => ({ ...s, show: true }))}>
              <Plus size={14} />
              Add Member
            </Button>
          )}
        </div>

        {/* SECTION 5: Actions */}
        <div style={{ display: 'flex', gap: 16, marginTop: 24 }}>
          <Button type="submit" full disabled={submitting} id="btn-create-customer">
            {submitting ? <><Spinner size="sm" /> Creating...</> : 'Create Customer'}
          </Button>
        </div>
          <div style={{ textAlign: 'center', marginTop: 12 }}>
            <Link to="/dashboard" style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Cancel</Link>
          </div>
        </form>

        <aside className="create-form-tips">
          <h4>💡 Tips</h4>
          <ul>
            <li>NIC format: 12 digits (e.g. 200012345678) or 9 digits ending in V (e.g. 901234567V)</li>
            <li>Date of birth must be in the past — use the calendar picker</li>
            <li>You can add up to 10 mobile numbers and 5 addresses</li>
            <li>Link family members by their existing Customer ID</li>
            <li>All mandatory fields (Name, DOB, NIC) are required before saving</li>
          </ul>
        </aside>
      </div>
    </div>
  );
}
