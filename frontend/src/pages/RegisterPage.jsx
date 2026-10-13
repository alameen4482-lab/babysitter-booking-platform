import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  User, 
  Mail, 
  Lock, 
  ArrowRight, 
  AlertCircle, 
  CheckCircle2, 
  HeartHandshake, 
  Users, 
  Award,
  DollarSign,
  Sparkles
} from 'lucide-react';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Pick initial role from navigation state if provided, default to PARENT
  const initialRole = location.state?.defaultRole || 'PARENT';
  const [role, setRole] = useState(initialRole);

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Validations
    if (!name.trim()) {
      setError('Please enter your full name.');
      return;
    }
    if (!email.trim()) {
      setError('Please enter a valid email address.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      await register({
        name: name.trim(),
        email: email.trim(),
        password,
        role, // 'PARENT' | 'BABYSITTER'
      });
      // Redirect to home page upon registration
      navigate('/');
    } catch (err) {
      setError(err.message || 'Registration failed. Please verify your details.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: '480px' }}>
        {/* Header */}
        <div className="auth-header">
          <Link to="/" className="brand" style={{ justifyContent: 'center', marginBottom: '1.25rem' }}>
            <div className="brand-icon">
              <HeartHandshake size={20} />
            </div>
            <span>Kindred<span className="brand-accent">Care</span></span>
          </Link>
          <h2>Create Your Account</h2>
          <p>Join our trusted childcare platform as a Parent or Babysitter.</p>
        </div>

        {/* Role Picker */}
        <div className="role-selector">
          <button
            type="button"
            className={`role-tab ${role === 'PARENT' ? 'active' : ''}`}
            onClick={() => { setRole('PARENT'); setError(''); }}
          >
            <Users size={16} />
            <span>I'm a Parent</span>
          </button>
          <button
            type="button"
            className={`role-tab ${role === 'BABYSITTER' ? 'active' : ''}`}
            onClick={() => { setRole('BABYSITTER'); setError(''); }}
          >
            <Award size={16} />
            <span>I'm a Babysitter</span>
          </button>
        </div>

        {/* Role Helper Banner */}
        <div className="role-helper-box">
          {role === 'PARENT' ? (
            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-start' }}>
              <HeartHandshake size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
              <div>
                <strong>Parent Account:</strong> Search verified babysitters, review background clearances, and book available slots effortlessly.
              </div>
            </div>
          ) : (
            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-start' }}>
              <Sparkles size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
              <div>
                <strong>Babysitter Account:</strong> Publish open availability slots, define your hourly rates, and connect with respectful families.
              </div>
            </div>
          )}
        </div>

        {/* Error Notification */}
        {error && (
          <div className="alert alert-danger" role="alert">
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <div>{error}</div>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit}>
          {/* Full Name */}
          <div className="form-group">
            <label className="form-label" htmlFor="reg-name">Full Name</label>
            <div className="input-with-icon">
              <User className="input-icon" size={18} />
              <input
                id="reg-name"
                type="text"
                className="form-input"
                placeholder={role === 'PARENT' ? 'e.g. Sarah Jenkins' : 'e.g. Maya Lin'}
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                autoComplete="name"
              />
            </div>
          </div>

          {/* Email */}
          <div className="form-group">
            <label className="form-label" htmlFor="reg-email">Email Address</label>
            <div className="input-with-icon">
              <Mail className="input-icon" size={18} />
              <input
                id="reg-email"
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

          {/* Password */}
          <div className="form-group">
            <label className="form-label" htmlFor="reg-password">Password</label>
            <div className="input-with-icon">
              <Lock className="input-icon" size={18} />
              <input
                id="reg-password"
                type="password"
                className="form-input"
                placeholder="At least 6 characters"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={6}
                autoComplete="new-password"
              />
            </div>
          </div>

          {/* Confirm Password */}
          <div className="form-group">
            <label className="form-label" htmlFor="reg-confirm-password">Confirm Password</label>
            <div className="input-with-icon">
              <Lock className="input-icon" size={18} />
              <input
                id="reg-confirm-password"
                type="password"
                className="form-input"
                placeholder="Repeat password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                minLength={6}
                autoComplete="new-password"
              />
            </div>
          </div>

          {/* Babysitter Perk Note */}
          {role === 'BABYSITTER' && (
            <div style={{ marginBottom: '1.25rem', padding: '0.75rem', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              ✓ You will be able to customize your bio, skills, CPR certifications, and hourly rates in your sitter profile right after signup.
            </div>
          )}

          {/* Submit Button */}
          <button 
            type="submit" 
            className="btn btn-primary btn-full btn-lg" 
            disabled={loading}
          >
            {loading ? 'Creating Account...' : `Register as ${role === 'PARENT' ? 'Parent' : 'Babysitter'}`}
            {!loading && <ArrowRight size={18} />}
          </button>
        </form>

        {/* Footer */}
        <div className="auth-footer">
          Already have an account?{' '}
          <Link to="/login" style={{ fontWeight: 600 }}>Sign In here</Link>
        </div>
      </div>
    </div>
  );
}
