import React from 'react';

export default function Spinner({ size = 'md' }) {
  return (
    <div className={`spinner spinner-${size}`}>
      <div className="spinner-circle" />
    </div>
  );
}
