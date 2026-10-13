import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  HeartHandshake, 
  Menu, 
  X, 
  User as UserIcon, 
  LogOut, 
  ShieldCheck 
} from 'lucide-react';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/');
    setMobileOpen(false);
  };

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="navbar" id="main-navbar">
      <div className="container">
        <div className="nav-inner">
          {/* Logo */}
          <Link to="/" className="brand" onClick={() => setMobileOpen(false)}>
            <div className="brand-icon">
              <HeartHandshake size={22} />
            </div>
            <span>Kindred<span className="brand-accent">Care</span></span>
          </Link>

          {/* Desktop Nav Links */}
          <ul className="nav-links">
            <li>
              <Link to="/" className={`nav-link ${isActive('/') ? 'active' : ''}`}>
                Home
              </Link>
            </li>
            <li>
              <Link to="/babysitters" className={`nav-link ${isActive('/babysitters') ? 'active' : ''}`}>
                Find Babysitters
              </Link>
            </li>
            <li>
              <a href="/#how-it-works" className="nav-link">
                How It Works
              </a>
            </li>
            <li>
              <a href="/#for-sitters" className="nav-link">
                For Babysitters
              </a>
            </li>
            <li>
              <a href="/#trust-safety" className="nav-link">
                Safety & Trust
              </a>
            </li>
          </ul>

          {/* Auth Actions */}
          <div className="nav-actions">
            {isAuthenticated && user ? (
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div className="user-badge">
                  <div className="user-avatar">
                    {user.name ? user.name.charAt(0).toUpperCase() : 'U'}
                  </div>
                  <div>
                    <span className="user-name">{user.name}</span>
                    <span 
                      className={`badge ${user.role === 'BABYSITTER' ? 'badge-accent' : 'badge-primary'}`}
                      style={{ marginLeft: '0.4rem', fontSize: '0.65rem' }}
                    >
                      {user.role}
                    </span>
                  </div>
                </div>
                <button 
                  onClick={handleLogout} 
                  className="btn btn-secondary btn-sm"
                  title="Sign Out"
                >
                  <LogOut size={16} />
                  <span>Logout</span>
                </button>
              </div>
            ) : (
              <>
                <Link to="/login" className="btn btn-secondary btn-sm">
                  Sign In
                </Link>
                <Link to="/register" className="btn btn-primary btn-sm">
                  Join Now
                </Link>
              </>
            )}

            {/* Mobile Menu Button */}
            <button 
              className="mobile-toggle"
              onClick={() => setMobileOpen(!mobileOpen)}
              aria-label="Toggle navigation"
            >
              {mobileOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>
        </div>

        {/* Mobile Dropdown Menu */}
        {mobileOpen && (
          <div className="nav-menu-mobile">
            <Link 
              to="/" 
              className="nav-link"
              onClick={() => setMobileOpen(false)}
            >
              Home
            </Link>
            <Link 
              to="/babysitters" 
              className="nav-link"
              onClick={() => setMobileOpen(false)}
            >
              Find Babysitters
            </Link>
            <a 
              href="/#how-it-works" 
              className="nav-link"
              onClick={() => setMobileOpen(false)}
            >
              How It Works
            </a>
            <a 
              href="/#for-sitters" 
              className="nav-link"
              onClick={() => setMobileOpen(false)}
            >
              For Babysitters
            </a>
            <a 
              href="/#trust-safety" 
              className="nav-link"
              onClick={() => setMobileOpen(false)}
            >
              Safety & Trust
            </a>
            <div style={{ paddingTop: '1rem', borderTop: '1px solid var(--border)', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {!isAuthenticated ? (
                <>
                  <Link 
                    to="/login" 
                    className="btn btn-secondary btn-full"
                    onClick={() => setMobileOpen(false)}
                  >
                    Sign In
                  </Link>
                  <Link 
                    to="/register" 
                    className="btn btn-primary btn-full"
                    onClick={() => setMobileOpen(false)}
                  >
                    Create Account
                  </Link>
                </>
              ) : (
                <button 
                  onClick={handleLogout} 
                  className="btn btn-secondary btn-full"
                >
                  <LogOut size={16} />
                  <span>Logout ({user?.name})</span>
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </nav>
  );
}
