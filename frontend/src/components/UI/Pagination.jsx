import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export default function Pagination({ page, totalPages, totalElements, size, onPageChange, onSizeChange }) {
  const sizes = [10, 20, 50];
  return (
    <div className="pagination">
      <div className="pagination-info">
        Page {page + 1} of {totalPages || 1} &mdash; {totalElements} customers
      </div>
      <div className="pagination-controls">
        <div className="page-size-pills">
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
        <button
          className="btn btn-ghost btn-sm"
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
        >
          <ChevronLeft size={16} />
          Prev
        </button>
        <button
          className="btn btn-ghost btn-sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
        >
          Next
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  );
}
