import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, BarChart2, Users, UploadCloud, Shield, CheckCircle2 } from 'lucide-react';

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
          <a href="#pricing">Pricing</a>
        </div>
        <div className="landing-nav-actions">
          <Link to="/dashboard" className="btn btn-ghost">Sign In</Link>
          <Link to="/dashboard" className="btn btn-primary">Get Started</Link>
        </div>
      </nav>

      <header className="hero-section">
        <div className="hero-content">
          <div className="hero-badge">New: AI-Powered Insights v2.0</div>
          <h1 className="hero-title">Manage your customers with <span>unrivaled precision.</span></h1>
          <p className="hero-subtitle">
            ClientSphere is the premium CRM for modern SaaS teams. Track clients, bulk upload data, and scale your business securely with our intelligent dashboard.
          </p>
          <div className="hero-actions">
            <Link to="/dashboard" className="btn btn-primary btn-lg">Start Free Trial <ArrowRight size={18} /></Link>
            <a href="#features" className="btn btn-ghost btn-lg">Explore Features</a>
          </div>
        </div>
        <div className="hero-image-wrapper">
          <div className="hero-image-glow"></div>
          <div className="hero-image">
            <img src="https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80" alt="CRM Dashboard Preview" />
          </div>
        </div>
      </header>

      <section id="features" className="features-section">
        <div className="section-header">
          <h2>Everything you need to grow</h2>
          <p>Powerful features designed for performance and scale.</p>
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

      <section id="pricing" className="pricing-section">
         <div className="section-header">
          <h2>Simple, transparent pricing</h2>
          <p>No hidden fees. Scale as you grow.</p>
        </div>
        <div className="pricing-grid">
          <div className="pricing-card">
            <h3>Starter</h3>
            <div className="price"><span>$</span>29<span>/mo</span></div>
            <ul className="pricing-features">
              <li><CheckCircle2 size={16} className="text-success" /> Up to 5,000 customers</li>
              <li><CheckCircle2 size={16} className="text-success" /> Standard support</li>
              <li><CheckCircle2 size={16} className="text-success" /> Basic analytics</li>
            </ul>
            <Link to="/dashboard" className="btn btn-ghost btn-full">Start Trial</Link>
          </div>
          <div className="pricing-card premium">
            <div className="pricing-badge">Most Popular</div>
            <h3>Professional</h3>
            <div className="price"><span>$</span>99<span>/mo</span></div>
            <ul className="pricing-features">
              <li><CheckCircle2 size={16} className="text-accent" /> Up to 100,000 customers</li>
              <li><CheckCircle2 size={16} className="text-accent" /> Priority 24/7 support</li>
              <li><CheckCircle2 size={16} className="text-accent" /> Advanced analytics</li>
              <li><CheckCircle2 size={16} className="text-accent" /> Bulk Excel uploads</li>
            </ul>
            <Link to="/dashboard" className="btn btn-primary btn-full">Start Trial</Link>
          </div>
          <div className="pricing-card">
            <h3>Enterprise</h3>
            <div className="price"><span>$</span>299<span>/mo</span></div>
            <ul className="pricing-features">
              <li><CheckCircle2 size={16} className="text-success" /> Unlimited customers</li>
              <li><CheckCircle2 size={16} className="text-success" /> Dedicated success manager</li>
              <li><CheckCircle2 size={16} className="text-success" /> Custom integrations</li>
            </ul>
            <Link to="/dashboard" className="btn btn-ghost btn-full">Contact Sales</Link>
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
          <div className="footer-links">
            <div className="footer-col">
              <h4>Product</h4>
              <a href="#">Features</a>
              <a href="#">Pricing</a>
              <a href="#">Changelog</a>
            </div>
            <div className="footer-col">
              <h4>Company</h4>
              <a href="#">About</a>
              <a href="#">Blog</a>
              <a href="#">Careers</a>
            </div>
            <div className="footer-col">
              <h4>Legal</h4>
              <a href="#">Privacy</a>
              <a href="#">Terms</a>
            </div>
          </div>
        </div>
        <div className="footer-bottom">
          &copy; {new Date().getFullYear()} ClientSphere. All rights reserved.
        </div>
      </footer>
    </div>
  );
}
