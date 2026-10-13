import React from 'react';
import { Link } from 'react-router-dom';
import { HeartHandshake, Shield, Clock, Award } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-grid">
          {/* Brand Info */}
          <div>
            <div className="footer-brand">
              <HeartHandshake size={22} color="#818cf8" />
              <span>KindredCare</span>
            </div>
            <p style={{ color: '#94a3b8', fontSize: '0.9rem', lineHeight: '1.7', marginBottom: '1.25rem' }}>
              The modern childcare booking ecosystem connecting loving, vetted babysitters with local families. Peace of mind, flexible schedules, and verified safety.
            </p>
            <div style={{ display: 'flex', gap: '1rem', color: '#cbd5e1' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.8rem' }}>
                <Shield size={14} color="#10b981" /> Verified Sitters
              </span>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.8rem' }}>
                <Clock size={14} color="#38bdf8" /> Instant Booking
              </span>
            </div>
          </div>

          {/* For Parents */}
          <div>
            <h4 className="footer-heading">For Parents</h4>
            <ul className="footer-links">
              <li><Link to="/babysitters">Find a Babysitter</Link></li>
              <li><a href="/#how-it-works">How It Works</a></li>
              <li><a href="/#trust-safety">Safety & Verification</a></li>
              <li><Link to="/login">Parent Login</Link></li>
            </ul>
          </div>

          {/* For Babysitters */}
          <div>
            <h4 className="footer-heading">For Babysitters</h4>
            <ul className="footer-links">
              <li><Link to="/register">Become a Sitter</Link></li>
              <li><a href="#for-sitters">Sitter Benefits</a></li>
              <li><a href="#earnings">Set Your Hourly Rate</a></li>
              <li><Link to="/login">Sitter Portal</Link></li>
            </ul>
          </div>

          {/* Support & Legal */}
          <div>
            <h4 className="footer-heading">Platform</h4>
            <ul className="footer-links">
              <li><a href="#">Help Center & FAQs</a></li>
              <li><a href="#">Community Guidelines</a></li>
              <li><a href="#">Privacy Policy</a></li>
              <li><a href="#">Terms of Service</a></li>
            </ul>
          </div>
        </div>

        <div className="footer-bottom">
          <div>
            © {new Date().getFullYear()} KindredCare Inc. All rights reserved. Built with React & Spring Boot.
          </div>
          <div style={{ display: 'flex', gap: '1.5rem' }}>
            <a href="#">Security</a>
            <a href="#">Trust Center</a>
            <a href="#">Contact</a>
          </div>
        </div>
      </div>
    </footer>
  );
}
