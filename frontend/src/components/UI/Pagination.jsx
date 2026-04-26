import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export default function Pagination({ page, totalPages, totalElements, size, onPageChange, onSizeChange }) {
  const sizes = [10, 20, 50];
  const from = totalElements === 0 ? 0 : page * size + 1;
  const to = Math.min((page + 1) * size, totalElements);

  return (
    <div className="pagination-bar">
      <span className="pagination-summary">
        Showing <strong>{from}–{to}</strong> of <strong>{totalElements}</strong> customers
      </span>

      <div className="pagination-right">
        <div className="pagination-size-group">
          <span className="pagination-size-label">Rows per page:</span>
          {sizes.map(s => (
            <button
              key={s}
              className={`page-size-pill ${size === s ? 'active' : ''}`}
              onClick={() => onSizeChange(s)}
            >
              {s}
            </button>
          ))}
        </div>

        <div className="pagination-nav">
          <button
            className="pagination-btn"
            disabled={page === 0}
            onClick={() => onPageChange(page - 1)}
            title="Previous page"
          >
            <ChevronLeft size={16} />
          </button>
          <span className="pagination-page-indicator">
            {page + 1} / {totalPages || 1}
          </span>
          <button
            className="pagination-btn"
            disabled={page >= totalPages - 1}
            onClick={() => onPageChange(page + 1)}
            title="Next page"
          >
            <ChevronRight size={16} />
          </button>
        </div>
      </div>
    </div>
  );
}
