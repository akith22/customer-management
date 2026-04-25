import React from 'react';

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
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
          Prev
        </button>
        <button
          className="btn btn-ghost btn-sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
        >
          Next
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
      </div>
    </div>
  );
}
