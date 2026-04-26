import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ChevronLeft, Home } from 'lucide-react';

function buildBreadcrumbs(pathname) {
  if (pathname === '/dashboard') return [{ label: 'Customers', path: '/dashboard' }];
  if (pathname === '/bulk-upload') return [{ label: 'Bulk Upload', path: '/bulk-upload' }];
  if (pathname === '/customers/new') return [
    { label: 'Customers', path: '/dashboard' },
    { label: 'New Customer', path: null },
  ];

  const editMatch = pathname.match(/^\/customers\/(\d+)\/edit$/);
  if (editMatch) return [
    { label: 'Customers', path: '/dashboard' },
    { label: `Customer #${editMatch[1]}`, path: `/customers/${editMatch[1]}` },
    { label: 'Edit', path: null },
  ];

  const viewMatch = pathname.match(/^\/customers\/(\d+)$/);
  if (viewMatch) return [
    { label: 'Customers', path: '/dashboard' },
    { label: `Customer #${viewMatch[1]}`, path: null },
  ];

  return [{ label: 'Home', path: '/dashboard' }];
}

export default function TopBar({ title }) {
  const location = useLocation();
  const navigate = useNavigate();
  const breadcrumbs = buildBreadcrumbs(location.pathname);
  const showBack = location.pathname !== '/dashboard';

  return (
    <header className="topbar">
      <div className="topbar-left">
        {showBack && (
          <button className="topbar-back-btn" onClick={() => navigate(-1)} title="Go back">
            <ChevronLeft size={18} />
            <span>Back</span>
          </button>
        )}
        <div className="topbar-breadcrumb">
          {breadcrumbs.map((crumb, i) => (
            <React.Fragment key={i}>
              {i > 0 && <span className="separator">/</span>}
              {crumb.path ? (
                <Link to={crumb.path}>{crumb.label}</Link>
              ) : (
                <span className="current">{title || crumb.label}</span>
              )}
            </React.Fragment>
          ))}
        </div>
      </div>

      <div className="topbar-actions">
        <Link to="/" className="topbar-home-btn" title="Go to landing page">
          <Home size={16} />
          <span>Home</span>
        </Link>
      </div>
    </header>
  );
}
