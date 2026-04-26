import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, BarChart2, Users, UploadCloud, Shield } from 'lucide-react';

export default function LandingPage() {
  return (
    <div className="landing-page">
      <nav className="landing-nav">
        <div className="landing-logo">
          <div className="landing-logo-icon">
            <svg width="24" height="24" viewBox="0 0 32 32" fill="none">
              <rect width="32" height="32" rx="8" fill="var(--accent-blue)"/>
              <path d="M18 6L12 18h6l-2 8 8-14h-7l3-6z" fill="#fff"/>
            </svg>
          </div>
          <span>ClientSphere</span>
        </div>
        <div className="landing-nav-links">
          <a href="#features">Features</a>
        </div>
        <div className="landing-nav-actions">
          <Link to="/dashboard" className="btn btn-primary">Go to App</Link>
        </div>
      </nav>

      <header className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">Your Customers. <span>Managed Perfectly.</span></h1>
          <p className="hero-subtitle">
            ClientSphere gives you a powerful, fast interface to create, view, and manage your customers — with bulk Excel import for massive datasets.
          </p>
          <div className="hero-actions">
            <Link to="/dashboard" className="btn btn-primary btn-lg">Open Dashboard <ArrowRight size={18} /></Link>
            <a href="#features" className="btn btn-ghost btn-lg">See Features</a>
          </div>
        </div>
      </header>

      <section id="features" className="features-section">
        <div className="section-header">
          <h2>Built for real customer management</h2>
          <p>Everything you need to manage customers at any scale.</p>
        </div>
        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-icon"><Users size={24} /></div>
            <h3>Smart Customer Profiles</h3>
            <p>Maintain detailed records with complex family trees and multiple data points seamlessly.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon"><UploadCloud size={24} /></div>
            <h3>Lightning Bulk Uploads</h3>
            <p>Upload up to 1,000,000 records in seconds with our optimized chunk processing architecture.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon"><Shield size={24} /></div>
            <h3>Enterprise-grade Security</h3>
            <p>Bank-level encryption and strict access controls ensure your customer data remains private.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon"><BarChart2 size={24} /></div>
            <h3>Advanced Analytics</h3>
            <p>Get instant insights into your customer base with real-time dynamic reporting.</p>
          </div>
        </div>
      </section>

      <footer className="landing-footer">
        <div className="footer-content">
          <div className="footer-brand">
            <div className="landing-logo">
              <div className="landing-logo-icon">
                <svg width="24" height="24" viewBox="0 0 32 32" fill="none">
                  <rect width="32" height="32" rx="8" fill="var(--accent-blue)"/>
                  <path d="M18 6L12 18h6l-2 8 8-14h-7l3-6z" fill="#fff"/>
                </svg>
              </div>
              <span>ClientSphere</span>
            </div>
            <p>The premium CRM solution for modern businesses scaling to the next level.</p>
          </div>
        </div>
        <div className="footer-bottom">
          &copy; {new Date().getFullYear()} ClientSphere. All rights reserved.
        </div>
      </footer>
    </div>
  );
}
