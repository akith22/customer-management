import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import TopBar from './TopBar';
import Toast from '../UI/Toast';
import useToast from '../../hooks/useToast';

export const ToastContext = React.createContext(null);

export default function Layout() {
  const [collapsed, setCollapsed] = useState(false);
  const { toasts, addToast, removeToast } = useToast();

  return (
    <ToastContext.Provider value={addToast}>
      <div className="app-layout">
        <Sidebar collapsed={collapsed} onToggle={() => setCollapsed(c => !c)} />
        <main className={`app-main ${collapsed ? 'collapsed' : ''}`}>
          <TopBar />
          <div className="page-content">
            <Outlet />
          </div>
        </main>
        <Toast toasts={toasts} onRemove={removeToast} />
      </div>
    </ToastContext.Provider>
  );
}
