import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  Mail, 
  Lock, 
  ArrowRight, 
  AlertCircle, 
  HeartHandshake, 
  Users, 
  Award, 
  ShieldCheck 
} from 'lucide-react';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [roleContext, setRoleContext] = useState('PARENT'); // PARENT | BABYSITTER | ADMIN
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Please provide both email and password.');
      return;
    }

    setLoading(true);
    try {
      await login(email.trim(), password);
      // Successfully authenticated
      navigate('/');
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = (demoEmail, demoRole) => {
    setEmail(demoEmail);
    setPassword('password123');
    setRoleContext(demoRole);
    setError('');
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        {/* Header */}
        <div className="auth-header">
          <Link to="/" className="brand" style={{ justifyContent: 'center', marginBottom: '1.25rem' }}>
            <div className="brand-icon">
              <HeartHandshake size={20} />
            </div>
            <span>Kindred<span className="brand-accent">Care</span></span>
          </Link>
          <h2>Welcome Back</h2>
          <p>Sign in to manage your bookings, schedule, or account.</p>
        </div>

        {/* Role Selector Context */}
        <div className="role-selector">
          <button
            type="button"
            className={`role-tab ${roleContext === 'PARENT' ? 'active' : ''}`}
            onClick={() => setRoleContext('PARENT')}
          >
            <Users size={16} />
            <span>Parent</span>
          </button>
          <button
            type="button"
            className={`role-tab ${roleContext === 'BABYSITTER' ? 'active' : ''}`}
            onClick={() => setRoleContext('BABYSITTER')}
          >
            <Award size={16} />
            <span>Babysitter</span>
          </button>
          <button
            type="button"
            className={`role-tab ${roleContext === 'ADMIN' ? 'active' : ''}`}
            onClick={() => setRoleContext('ADMIN')}
          >
            <ShieldCheck size={16} />
            <span>Admin</span>
          </button>
        </div>

        {/* Role Helper Info */}
        <div className="role-helper-box">
          {roleContext === 'PARENT' && (
            <span>Signing in as a <strong>Parent</strong> lets you search caregivers and book availability slots.</span>
          )}
          {roleContext === 'BABYSITTER' && (
            <span>Signing in as a <strong>Babysitter</strong> lets you publish open slots and respond to booking requests.</span>
          )}
          {roleContext === 'ADMIN' && (
            <span>Signing in as an <strong>Admin</strong> lets you inspect platform audit logs and moderate users.</span>
          )}
        </div>

        {/* Error Alert */}
        {error && (
          <div className="alert alert-danger" role="alert">
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <div>{error}</div>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="login-email">Email Address</label>
            <div className="input-with-icon">
              <Mail className="input-icon" size={18} />
              <input
                id="login-email"
                type="email"
                className="form-input"
                placeholder="name@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </div>
          </div>

          <div className="form-group">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <label className="form-label" htmlFor="login-password">Password</label>
              <a href="#" style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Forgot password?</a>
            </div>
            <div className="input-with-icon">
              <Lock className="input-icon" size={18} />
              <input
                id="login-password"
                type="password"
                className="form-input"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
              />
            </div>
          </div>

          <button 
            type="submit" 
            className="btn btn-primary btn-full btn-lg" 
            disabled={loading}
            style={{ marginTop: '0.5rem' }}
          >
            {loading ? 'Signing In...' : 'Sign In'}
            {!loading && <ArrowRight size={18} />}
          </button>
        </form>

        {/* Quick Demo Helper */}
        <div style={{ marginTop: '1.25rem', padding: '0.75rem', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
          <div style={{ fontWeight: 600, marginBottom: '0.4rem', color: 'var(--text-main)' }}>Demo Quick-Fill:</div>
          <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
            <button 
              type="button" 
              onClick={() => handleQuickFill('parent@example.com', 'PARENT')}
              style={{ padding: '0.2rem 0.5rem', background: '#fff', border: '1px solid var(--border)', borderRadius: '4px', cursor: 'pointer', fontSize: '0.75rem' }}
            >
              parent@example.com
            </button>
            <button 
              type="button" 
              onClick={() => handleQuickFill('sitter@example.com', 'BABYSITTER')}
              style={{ padding: '0.2rem 0.5rem', background: '#fff', border: '1px solid var(--border)', borderRadius: '4px', cursor: 'pointer', fontSize: '0.75rem' }}
            >
              sitter@example.com
            </button>
          </div>
        </div>

        {/* Footer */}
        <div className="auth-footer">
          Don't have an account yet?{' '}
          <Link to="/register" style={{ fontWeight: 600 }}>Create an account</Link>
        </div>
      </div>
    </div>
  );
}
