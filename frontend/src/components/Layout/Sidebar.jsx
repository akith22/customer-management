import React from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { LayoutDashboard, Users, UploadCloud, ChevronRight, ChevronLeft } from 'lucide-react';

const navItems = [
  {
    label: 'Dashboard',
    path: '/dashboard',
    icon: <LayoutDashboard size={20} />,
  },
  {
    label: 'Customers',
    path: '/dashboard',
    icon: <Users size={20} />,
  },
  {
    label: 'Bulk Upload',
    path: '/bulk-upload',
    icon: <UploadCloud size={20} />,
  },
];

export default function Sidebar({ collapsed, onToggle }) {
  const location = useLocation();

  const isActive = (path, label) => {
    if (label === 'Bulk Upload') return location.pathname === '/bulk-upload';
    if (label === 'Dashboard' || label === 'Customers') {
      return location.pathname === '/dashboard' || (location.pathname.startsWith('/customers') && label === 'Customers');
    }
    return false;
  };

  return (
    <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      <div className="sidebar-logo">
        <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
          <rect width="32" height="32" rx="8" fill="#4361ee"/>
          <path d="M18 6L12 18h6l-2 8 8-14h-7l3-6z" fill="#fff"/>
        </svg>
        <span>ClientSphere</span>
      </div>

      <nav className="sidebar-nav">
        {navItems.map((item) => (
          <NavLink
            key={item.label}
            to={item.path}
            className={`sidebar-link ${isActive(item.path, item.label) ? 'active' : ''}`}
          >
            {item.icon}
            <span className="link-text">{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <button className="sidebar-toggle" onClick={onToggle} title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}>
        {collapsed ? <ChevronRight size={20} /> : <ChevronLeft size={20} />}
      </button>
    </aside>
  );
}
