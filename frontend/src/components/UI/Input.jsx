import React from 'react';

export default function Input({ label, error, success, className = '', ...props }) {
  const inputClass = ['input', error ? 'error' : '', success ? 'success' : '', className].filter(Boolean).join(' ');
  return (
    <div className="input-group">
      {label && <label>{label}</label>}
      <input className={inputClass} {...props} />
      {error && <span className="input-error">{error}</span>}
    </div>
  );
}
