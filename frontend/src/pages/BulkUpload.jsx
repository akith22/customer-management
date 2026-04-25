import React, { useState, useRef, useContext } from 'react';
import { UploadCloud, Download, FileSpreadsheet, X, ChevronDown } from 'lucide-react';
import { bulkUploadCustomers } from '../api/api';
import Button from '../components/UI/Button';
import Spinner from '../components/UI/Spinner';
import { ToastContext } from '../components/Layout/Layout';
import * as XLSX from 'xlsx';

export default function BulkUpload() {
  const addToast = useContext(ToastContext);
  const fileRef = useRef(null);
  const [file, setFile] = useState(null);
  const [dragover, setDragover] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [showErrors, setShowErrors] = useState(false);
  const [fileError, setFileError] = useState('');

  const validTypes = [
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-excel',
  ];
  const validExtensions = ['.xlsx', '.xls'];

  const validateFile = (f) => {
    if (!f) return false;
    const ext = f.name.substring(f.name.lastIndexOf('.')).toLowerCase();
    if (!validExtensions.includes(ext) && !validTypes.includes(f.type)) {
      setFileError('Only .xlsx and .xls files are accepted');
      return false;
    }
    setFileError('');
    return true;
  };

  const handleFileSelect = (f) => {
    if (validateFile(f)) {
      setFile(f);
      setResult(null);
    } else {
      setFile(null);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragover(false);
    if (e.dataTransfer.files.length) handleFileSelect(e.dataTransfer.files[0]);
  };

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    setResult(null);
    try {
      const res = await bulkUploadCustomers(file);
      setResult(res.data);
      addToast('Upload completed', 'success');
    } catch (err) {
      addToast(err.response?.data?.error || 'Upload failed', 'error');
    } finally {
      setUploading(false);
    }
  };

  const downloadTemplate = () => {
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([['Name', 'DOB', 'NIC'], ['John Doe', '1990-05-15', '901234567V']]);
    XLSX.utils.book_append_sheet(wb, ws, 'Customers');
    XLSX.writeFile(wb, 'customer_template.xlsx');
  };

  const formatSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Bulk Upload</h1>
        <Button variant="ghost" onClick={downloadTemplate} id="btn-download-template">
          <Download size={16} />
          Download Template
        </Button>
      </div>

      <div className="card" style={{ maxWidth: 700 }}>
        <div
          className={`upload-zone ${dragover ? 'dragover' : ''}`}
          onClick={() => fileRef.current?.click()}
          onDragOver={e => { e.preventDefault(); setDragover(true); }}
          onDragLeave={() => setDragover(false)}
          onDrop={handleDrop}
        >
          <div className="upload-zone-icon">
            <UploadCloud size={48} strokeWidth={1.5} />
          </div>
          <div className="upload-zone-text">Drag and drop your Excel file here</div>
          <div className="upload-zone-hint">or click to browse -- .xlsx, .xls only</div>
          <input
            ref={fileRef}
            type="file"
            accept=".xlsx,.xls"
            style={{ display: 'none' }}
            onChange={e => { if (e.target.files.length) handleFileSelect(e.target.files[0]); }}
          />
        </div>

        {fileError && <div className="input-error mt-8">{fileError}</div>}

        {file && (
          <div className="file-info">
            <FileSpreadsheet size={20} className="text-success" />
            <div>
              <div className="file-name">{file.name}</div>
              <div className="file-size">{formatSize(file.size)}</div>
            </div>
            <button className="btn-icon" style={{ marginLeft: 'auto' }} onClick={() => { setFile(null); setResult(null); }}>
              <X size={16} />
            </button>
          </div>
        )}

        {file && !uploading && !result && (
          <div className="mt-24">
            <Button full onClick={handleUpload} id="btn-upload">
              <UploadCloud size={16} />
              Upload
            </Button>
          </div>
        )}

        {uploading && (
          <div className="loading-center" style={{ padding: 40 }}>
            <div style={{ textAlign: 'center' }}>
              <Spinner size="lg" />
              <div className="mt-16" style={{ color: 'var(--text-secondary)' }}>Processing file...</div>
            </div>
          </div>
        )}

        {result && (
          <div className="mt-24">
            <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: 16 }}>Upload Results</h3>
            <div className="summary-grid">
              <div className="summary-item" style={{ background: 'rgba(67,97,238,0.1)' }}>
                <div className="summary-value" style={{ color: 'var(--accent-blue)' }}>{result.totalRows}</div>
                <div className="summary-label">Total Rows</div>
              </div>
              <div className="summary-item" style={{ background: 'rgba(34,197,94,0.1)' }}>
                <div className="summary-value" style={{ color: 'var(--success)' }}>{result.successCount}</div>
                <div className="summary-label">Created Successfully</div>
              </div>
              <div className="summary-item" style={{ background: 'rgba(67,97,238,0.08)' }}>
                <div className="summary-value" style={{ color: 'var(--accent-blue)' }}>{result.updatedCount}</div>
                <div className="summary-label">Updated</div>
              </div>
              <div className="summary-item" style={{ background: 'rgba(239,68,68,0.1)' }}>
                <div className="summary-value" style={{ color: 'var(--danger)' }}>{result.failedCount}</div>
                <div className="summary-label">Failed</div>
              </div>
            </div>

            {result.errors && result.errors.length > 0 && (
              <div className="mt-16">
                <button className="collapsible-toggle" onClick={() => setShowErrors(!showErrors)}>
                  {showErrors ? 'Hide' : 'Show'} Errors ({result.errors.length})
                  <ChevronDown size={14} style={{ transform: showErrors ? 'rotate(180deg)' : '', transition: '0.2s' }} />
                </button>
                {showErrors && (
                  <div className="collapsible-content">
                    <div className="error-list">
                      {result.errors.map((err, i) => (
                        <div key={i} className="error-list-item">{err}</div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
