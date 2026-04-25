import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { createCustomer, getCustomerById } from '../api/api';
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
  const [familySearch, setFamilySearch] = useState({ show: false, id: '', loading: false, result: null, error: '' });

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

  const lookupFamily = async () => {
    if (!familySearch.id) return;
    const numId = Number(familySearch.id);
    if (familyMembers.find(f => f.id === numId)) {
      setFamilySearch(s => ({ ...s, error: 'Already linked' }));
      return;
    }
    setFamilySearch(s => ({ ...s, loading: true, result: null, error: '' }));
    try {
      const res = await getCustomerById(numId);
      setFamilySearch(s => ({ ...s, loading: false, result: res.data }));
    } catch {
      setFamilySearch(s => ({ ...s, loading: false, error: 'Customer not found' }));
    }
  };

  const confirmFamily = () => {
    if (familySearch.result) {
      setFamilyMembers([...familyMembers, { id: familySearch.result.id, name: familySearch.result.name, nic: familySearch.result.nic }]);
      setFamilySearch({ show: false, id: '', loading: false, result: null, error: '' });
    }
  };

  const cancelFamilySearch = () => {
    setFamilySearch({ show: false, id: '', loading: false, result: null, error: '' });
  };

  const removeFamily = (id) => setFamilyMembers(familyMembers.filter(f => f.id !== id));

  const nicValid = form.nic && NIC_REGEX.test(form.nic);
  const nicInvalid = form.nic && !NIC_REGEX.test(form.nic);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">New Customer</h1>
      </div>

      <form onSubmit={handleSubmit} className="card" style={{ maxWidth: 800 }}>
        {/* SECTION 1: Core Info */}
        <div className="form-section">
          <div className="form-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
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
            <Input
              id="input-dob"
              label="Date of Birth"
              type="date"
              value={form.dob}
              onChange={e => setForm({ ...form, dob: e.target.value })}
              error={errors.dob}
            />
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
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--success)" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
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
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>
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
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
              </button>
            </div>
          ))}
          <Button type="button" variant="ghost" size="sm" onClick={addMobile} disabled={mobiles.length >= 10}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            Add Mobile {mobiles.length > 0 && `(${mobiles.length}/10)`}
          </Button>
        </div>

        {/* SECTION 3: Addresses */}
        <div className="form-section">
          <div className="form-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            Addresses
          </div>
          {addresses.map((a, i) => (
            <div key={i} className="address-card mb-16">
              <button type="button" className="btn-icon danger remove-btn" onClick={() => removeAddress(i)} title="Remove address">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
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
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            Add Address {addresses.length > 0 && `(${addresses.length}/5)`}
          </Button>
        </div>

        {/* SECTION 4: Family Members */}
        <div className="form-section">
          <div className="form-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            Link Family Members
          </div>

          {familyMembers.map(f => (
            <div key={f.id} className="chip mb-8" style={{ marginRight: 8 }}>
              <span>{f.name} ({f.nic})</span>
              <button type="button" className="chip-remove" onClick={() => removeFamily(f.id)}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
          ))}

          {familySearch.show && (
            <div className="input-row mb-8" style={{ alignItems: 'flex-start' }}>
              <Input
                id="input-family-id"
                placeholder="Enter Customer ID"
                type="number"
                value={familySearch.id}
                onChange={e => setFamilySearch(s => ({ ...s, id: e.target.value, error: '', result: null }))}
                min="1"
              />
              <Button type="button" variant="ghost" size="sm" onClick={lookupFamily} disabled={familySearch.loading || !familySearch.id}>
                {familySearch.loading ? <Spinner size="sm" /> : 'Lookup'}
              </Button>
              <button type="button" className="btn-icon" onClick={cancelFamilySearch} title="Cancel">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
          )}

          {familySearch.error && (
            <div className="input-error mb-8">{familySearch.error}</div>
          )}

          {familySearch.result && (
            <div className="chip mb-8" style={{ background: 'rgba(34,197,94,0.1)', borderColor: 'rgba(34,197,94,0.2)' }}>
              <span>{familySearch.result.name} ({familySearch.result.nic})</span>
              <button type="button" className="chip-remove" onClick={confirmFamily} style={{ color: 'var(--success)' }} title="Confirm">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
              </button>
              <button type="button" className="chip-remove" onClick={cancelFamilySearch} title="Cancel">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
          )}

          {!familySearch.show && (
            <Button type="button" variant="ghost" size="sm" onClick={() => setFamilySearch(s => ({ ...s, show: true }))}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
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
          <Link to="/" style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Cancel</Link>
        </div>
      </form>
    </div>
  );
}
