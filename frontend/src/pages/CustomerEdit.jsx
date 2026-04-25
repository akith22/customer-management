import React, { useState, useEffect, useContext } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { User, Phone, MapPin, Link as LinkIcon, Plus, X, Check } from 'lucide-react';
import { getCustomerById, updateCustomer } from '../api/api';
import Input from '../components/UI/Input';
import Button from '../components/UI/Button';
import Spinner from '../components/UI/Spinner';
import { ToastContext } from '../components/Layout/Layout';

const NIC_REGEX = /^(\d{12}|\d{9}[Vv])$/;

export default function CustomerEdit() {
  const { id } = useParams();
  const navigate = useNavigate();
  const addToast = useContext(ToastContext);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const [form, setForm] = useState({ name: '', dob: '', nic: '' });
  const [errors, setErrors] = useState({});
  const [mobiles, setMobiles] = useState([]);
  const [addresses, setAddresses] = useState([]);
  const [familyMembers, setFamilyMembers] = useState([]);
  const [familySearch, setFamilySearch] = useState({ show: false, id: '', loading: false, result: null, error: '' });

  useEffect(() => {
    (async () => {
      try {
        const res = await getCustomerById(id);
        const c = res.data;
        setForm({ name: c.name || '', dob: c.dob || '', nic: c.nic || '' });
        setMobiles(c.mobiles || []);
        setAddresses((c.addresses || []).map(a => ({
          addressLine1: a.addressLine1 || '',
          addressLine2: a.addressLine2 || '',
          cityName: a.city?.name || '',
          countryName: a.city?.countryName || '',
        })));
        setFamilyMembers((c.familyMembers || []).map(f => ({ id: f.id, name: f.name, nic: f.nic })));
      } catch (err) {
        addToast(err.response?.data?.error || 'Failed to load customer', 'error');
      } finally {
        setLoading(false);
      }
    })();
  }, [id, addToast]);

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
      await updateCustomer(id, payload);
      addToast('Customer updated', 'success');
      navigate(`/customers/${id}`);
    } catch (err) {
      addToast(err.response?.data?.error || 'Failed to update customer', 'error');
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
    if (numId === Number(id)) {
      setFamilySearch(s => ({ ...s, error: 'Cannot link to self' }));
      return;
    }
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

  const cancelFamilySearch = () => setFamilySearch({ show: false, id: '', loading: false, result: null, error: '' });
  const removeFamily = (fid) => setFamilyMembers(familyMembers.filter(f => f.id !== fid));

  const nicValid = form.nic && NIC_REGEX.test(form.nic);
  const nicInvalid = form.nic && !NIC_REGEX.test(form.nic);

  if (loading) return <div className="loading-center"><Spinner size="lg" /></div>;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Edit Customer #{id}</h1>
      </div>

      <form onSubmit={handleSubmit} className="card" style={{ maxWidth: 800 }}>
        {/* Core Info */}
        <div className="form-section">
          <div className="form-section-title">
            <User size={18} />
            Core Information
          </div>
          <div className="form-grid">
            <Input id="input-name" label="Name" placeholder="Enter full name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} error={errors.name} />
            <Input id="input-dob" label="Date of Birth" type="date" value={form.dob} onChange={e => setForm({ ...form, dob: e.target.value })} error={errors.dob} />
            <div className="input-group">
              <label>NIC Number</label>
              <div style={{ position: 'relative' }}>
                <input id="input-nic" className={`input ${nicInvalid ? 'error' : ''} ${nicValid ? 'success' : ''}`} placeholder="e.g. 200012345678 or 901234567V" value={form.nic} onChange={e => setForm({ ...form, nic: e.target.value })} style={{ width: '100%' }} />
                {nicValid && <span style={{ position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)' }}><Check size={18} className="text-success" strokeWidth={2.5} /></span>}
              </div>
              {errors.nic && <span className="input-error">{errors.nic}</span>}
              {nicInvalid && !errors.nic && <span className="input-error">NIC must be 12 digits or 9 digits + V</span>}
            </div>
          </div>
        </div>

        {/* Mobiles */}
        <div className="form-section">
          <div className="form-section-title">
            <Phone size={18} />
            Mobile Numbers
          </div>
          {mobiles.map((m, i) => (
            <div key={i} className="input-row mb-8">
              <Input id={`input-mobile-${i}`} placeholder="Enter mobile number" value={m} onChange={e => updateMobile(i, e.target.value)} error={errors[`mobile_${i}`]} />
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

        {/* Addresses */}
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
                <Input id={`input-addr1-${i}`} label="Address Line 1" placeholder="Street address" value={a.addressLine1} onChange={e => updateAddress(i, 'addressLine1', e.target.value)} />
                <Input id={`input-addr2-${i}`} label="Address Line 2" placeholder="Apartment, suite, etc." value={a.addressLine2} onChange={e => updateAddress(i, 'addressLine2', e.target.value)} />
                <Input id={`input-city-${i}`} label="City" placeholder="City" value={a.cityName} onChange={e => updateAddress(i, 'cityName', e.target.value)} />
                <Input id={`input-country-${i}`} label="Country" placeholder="Country" value={a.countryName} onChange={e => updateAddress(i, 'countryName', e.target.value)} />
              </div>
            </div>
          ))}
          <Button type="button" variant="ghost" size="sm" onClick={addAddress} disabled={addresses.length >= 5}>
            <Plus size={14} />
            Add Address {addresses.length > 0 && `(${addresses.length}/5)`}
          </Button>
        </div>

        {/* Family Members */}
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
            <div className="input-row mb-8" style={{ alignItems: 'flex-start' }}>
              <Input id="input-family-id" placeholder="Enter Customer ID" type="number" value={familySearch.id} onChange={e => setFamilySearch(s => ({ ...s, id: e.target.value, error: '', result: null }))} min="1" />
              <Button type="button" variant="ghost" size="sm" onClick={lookupFamily} disabled={familySearch.loading || !familySearch.id}>
                {familySearch.loading ? <Spinner size="sm" /> : 'Lookup'}
              </Button>
              <button type="button" className="btn-icon" onClick={cancelFamilySearch} title="Cancel">
                <X size={16} />
              </button>
            </div>
          )}
          {familySearch.error && <div className="input-error mb-8">{familySearch.error}</div>}
          {familySearch.result && (
            <div className="chip mb-8" style={{ background: 'rgba(34,197,94,0.1)', borderColor: 'rgba(34,197,94,0.2)' }}>
              <span>{familySearch.result.name} ({familySearch.result.nic})</span>
              <button type="button" className="chip-remove" onClick={confirmFamily} style={{ color: 'var(--success)' }} title="Confirm"><Check size={16} strokeWidth={2.5} /></button>
              <button type="button" className="chip-remove" onClick={cancelFamilySearch} title="Cancel"><X size={14} /></button>
            </div>
          )}
          {!familySearch.show && (
            <Button type="button" variant="ghost" size="sm" onClick={() => setFamilySearch(s => ({ ...s, show: true }))}>
              <Plus size={14} />
              Add Member
            </Button>
          )}
        </div>

        {/* Actions */}
        <div style={{ display: 'flex', gap: 16, marginTop: 24 }}>
          <Button type="submit" full disabled={submitting} id="btn-update-customer">
            {submitting ? <><Spinner size="sm" /> Updating...</> : 'Update Customer'}
          </Button>
        </div>
        <div style={{ textAlign: 'center', marginTop: 12 }}>
          <Link to={`/customers/${id}`} style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Cancel</Link>
        </div>
      </form>
    </div>
  );
}
