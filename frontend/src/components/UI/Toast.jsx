import React from 'react';
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';

export default function Toast({ toasts, onRemove }) {
  if (!toasts.length) return null;
  return (
    <div className="toast-container">
      {toasts.map(t => (
        <div key={t.id} className={`toast toast-${t.type}`}>
          {t.type === 'success' && <CheckCircle size={18} />}
          {t.type === 'error' && <XCircle size={18} />}
          {t.type === 'warning' && <AlertTriangle size={18} />}
          {t.type === 'info' && <Info size={18} />}
          <span>{t.message}</span>
          <button className="toast-close" onClick={() => onRemove(t.id)}>
            <X size={14} />
          </button>
        </div>
      ))}
    </div>
  );
}
