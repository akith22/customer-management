import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout/Layout';
import CustomerList from './pages/CustomerList';
import CustomerCreate from './pages/CustomerCreate';
import CustomerView from './pages/CustomerView';
import CustomerEdit from './pages/CustomerEdit';
import BulkUpload from './pages/BulkUpload';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<CustomerList />} />
          <Route path="/customers/new" element={<CustomerCreate />} />
          <Route path="/customers/:id" element={<CustomerView />} />
          <Route path="/customers/:id/edit" element={<CustomerEdit />} />
          <Route path="/bulk-upload" element={<BulkUpload />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
