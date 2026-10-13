import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  ShieldCheck, 
  Star, 
  Search, 
  Filter, 
  RotateCcw, 
  Lock, 
  AlertCircle, 
  Users, 
  Briefcase, 
  Sparkles,
  ArrowRight
} from 'lucide-react';
import { getBabysitters } from '../services/api';
import { useAuth } from '../context/AuthContext';
import BookingModal from '../components/BookingModal';

export default function BabysitterListPage() {
  const { isAuthenticated } = useAuth();
  const [babysitters, setBabysitters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isAuthError, setIsAuthError] = useState(false);

  // Booking modal state
  const [activeBookingSitter, setActiveBookingSitter] = useState(null);

  // Filter & Search states
  const [searchTerm, setSearchTerm] = useState('');
  const [minRating, setMinRating] = useState('');
  const [sortBy, setSortBy] = useState('rating-desc');

  const fetchProfiles = async () => {
    setLoading(true);
    setError(null);
    setIsAuthError(false);

    try {
      const response = await getBabysitters();
      if (response && response.data) {
        setBabysitters(response.data);
      } else {
        setBabysitters([]);
      }
    } catch (err) {
      const msg = err.message || '';
      if (msg.includes('401') || msg.toLowerCase().includes('authentication') || msg.toLowerCase().includes('bearer')) {
        setIsAuthError(true);
        setError('Authentication is required to browse babysitter profiles.');
      } else {
        setError(msg || 'Failed to connect to backend server. Please check if the backend is running.');
      }
      setBabysitters([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfiles();
  }, []);

  // Filter & Sort logic on real backend data
  const filteredBabysitters = babysitters
    .filter((sitter) => {
      // Search term filter (matches name, skills, bio, or experience)
      if (searchTerm.trim()) {
        const query = searchTerm.toLowerCase();
        const matchesName = sitter.name?.toLowerCase().includes(query);
        const matchesSkills = sitter.skills?.toLowerCase().includes(query);
        const matchesBio = sitter.bio?.toLowerCase().includes(query);
        const matchesExp = sitter.experience?.toLowerCase().includes(query);
        if (!matchesName && !matchesSkills && !matchesBio && !matchesExp) {
          return false;
        }
      }

      // Minimum rating filter
      if (minRating) {
        const rating = sitter.averageRating ?? 0;
        if (rating < parseFloat(minRating)) {
          return false;
        }
      }

      return true;
    })
    .sort((a, b) => {
      if (sortBy === 'rating-desc') {
        return (b.averageRating ?? 0) - (a.averageRating ?? 0);
      }
      if (sortBy === 'rate-asc') {
        return (a.hourlyRate ?? 0) - (b.hourlyRate ?? 0);
      }
      if (sortBy === 'rate-desc') {
        return (b.hourlyRate ?? 0) - (a.hourlyRate ?? 0);
      }
      if (sortBy === 'name-asc') {
        return (a.name || '').localeCompare(b.name || '');
      }
      return 0;
    });

  const getInitials = (name, email) => {
    if (name) {
      return name
        .split(' ')
        .map((part) => part[0])
        .join('')
        .toUpperCase()
        .slice(0, 2);
    }
    if (email) {
      return email.slice(0, 2).toUpperCase();
    }
    return 'BS';
  };

  return (
    <div className="page-wrapper">
      {/* Header Banner */}
      <section className="listing-hero">
        <div className="container">
          <div style={{ maxWidth: '750px' }}>
            <div className="badge badge-primary" style={{ marginBottom: '0.85rem' }}>
              <Users size={14} />
              Caregiver Directory
            </div>
            <h1 style={{ fontSize: '2.5rem', marginBottom: '0.75rem' }}>
              Find a Trusted Babysitter
            </h1>
            <p style={{ fontSize: '1.05rem', color: 'var(--text-muted)' }}>
              Browse real, background-checked babysitter profiles registered on the platform. View verified rates, skills, and parent reviews.
            </p>
          </div>
        </div>
      </section>

      {/* Main Content Area */}
      <section style={{ padding: '2rem 0 5rem' }}>
        <div className="container">
          {/* Controls & Search Toolbar */}
          <div className="filter-toolbar">
            {/* Search Input */}
            <div className="filter-search-box input-with-icon">
              <Search className="input-icon" size={18} />
              <input
                type="text"
                className="form-input"
                placeholder="Search by name, skills (e.g. CPR, infant)..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            {/* Filter Options */}
            <div className="filter-options">
              {/* Rating Filter */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <span className="filter-label">Rating:</span>
                <select
                  className="form-select"
                  style={{ width: 'auto', padding: '0.55rem 0.85rem' }}
                  value={minRating}
                  onChange={(e) => setMinRating(e.target.value)}
                >
                  <option value="">All Ratings</option>
                  <option value="4.0">4.0+ Stars</option>
                  <option value="4.5">4.5+ Stars</option>
                  <option value="4.8">4.8+ Stars</option>
                  <option value="5.0">5.0 Stars only</option>
                </select>
              </div>

              {/* Sort By Dropdown */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <span className="filter-label">Sort:</span>
                <select
                  className="form-select"
                  style={{ width: 'auto', padding: '0.55rem 0.85rem' }}
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                >
                  <option value="rating-desc">Highest Rated</option>
                  <option value="rate-asc">Price: Low to High</option>
                  <option value="rate-desc">Price: High to Low</option>
                  <option value="name-asc">Name: A to Z</option>
                </select>
              </div>

              {/* Reset button */}
              {(searchTerm || minRating) && (
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => {
                    setSearchTerm('');
                    setMinRating('');
                  }}
                  title="Reset filters"
                >
                  <RotateCcw size={14} />
                  <span>Reset</span>
                </button>
              )}
            </div>
          </div>

          {/* Loading State */}
          {loading && (
            <div className="state-container card">
              <div className="spinner" />
              <h3>Loading Babysitters...</h3>
              <p style={{ marginTop: '0.5rem' }}>
                Fetching real babysitter profiles from the KindredCare backend service.
              </p>
            </div>
          )}

          {/* Authentication Required / Error State */}
          {!loading && isAuthError && (
            <div className="state-container card">
              <div className="state-icon-wrap state-icon-warn">
                <Lock size={32} />
              </div>
              <h2 style={{ fontSize: '1.6rem', marginBottom: '0.65rem' }}>
                Authentication Required
              </h2>
              <p style={{ marginBottom: '1.75rem', lineHeight: '1.6' }}>
                The backend requires you to be logged in with a valid account before accessing the babysitter directory. Please sign in or register to browse caregiver profiles.
              </p>
              <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center', flexWrap: 'wrap' }}>
                <Link to="/login" className="btn btn-primary btn-lg">
                  Sign In
                  <ArrowRight size={16} />
                </Link>
                <Link to="/register" className="btn btn-secondary btn-lg">
                  Create an Account
                </Link>
              </div>
            </div>
          )}

          {/* General Server Error State */}
          {!loading && !isAuthError && error && (
            <div className="state-container card">
              <div className="state-icon-wrap state-icon-error">
                <AlertCircle size={32} />
              </div>
              <h2 style={{ fontSize: '1.6rem', marginBottom: '0.65rem', color: 'var(--danger)' }}>
                Unable to Load Profiles
              </h2>
              <p style={{ marginBottom: '1.5rem', color: 'var(--text-muted)' }}>
                {error}
              </p>
              <button
                type="button"
                className="btn btn-primary"
                onClick={fetchProfiles}
              >
                <RotateCcw size={16} />
                <span>Retry Request</span>
              </button>
            </div>
          )}

          {/* Empty State (Database contains 0 babysitters or filters matched 0) */}
          {!loading && !error && filteredBabysitters.length === 0 && (
            <div className="state-container card">
              <div className="state-icon-wrap state-icon-info">
                <Users size={32} />
              </div>
              <h2 style={{ fontSize: '1.6rem', marginBottom: '0.65rem' }}>
                {babysitters.length === 0
                  ? 'No Babysitter Profiles Registered'
                  : 'No Profiles Match Your Search'}
              </h2>
              <p style={{ marginBottom: '1.75rem', lineHeight: '1.6' }}>
                {babysitters.length === 0
                  ? 'There are currently no babysitter profiles stored in the backend database. Are you a babysitter? Join now to create the first verified profile!'
                  : 'Try adjusting your search keywords or rating filter to view more results.'}
              </p>

              {babysitters.length === 0 ? (
                <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center', flexWrap: 'wrap' }}>
                  <Link
                    to="/register"
                    state={{ defaultRole: 'BABYSITTER' }}
                    className="btn btn-accent btn-lg"
                  >
                    <Sparkles size={18} />
                    <span>Register as Babysitter</span>
                  </Link>
                  <button
                    type="button"
                    className="btn btn-secondary btn-lg"
                    onClick={fetchProfiles}
                  >
                    <RotateCcw size={16} />
                    <span>Refresh</span>
                  </button>
                </div>
              ) : (
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => {
                    setSearchTerm('');
                    setMinRating('');
                  }}
                >
                  <RotateCcw size={16} />
                  <span>Clear All Filters</span>
                </button>
              )}
            </div>
          )}

          {/* Loaded Profiles Grid */}
          {!loading && !error && filteredBabysitters.length > 0 && (
            <>
              <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                  Showing {filteredBabysitters.length} verified {filteredBabysitters.length === 1 ? 'caregiver' : 'caregivers'}
                </span>
                <span style={{ fontSize: '0.825rem', color: 'var(--text-light)' }}>
                  Live data from backend
                </span>
              </div>

              <div className="sitters-grid">
                {filteredBabysitters.map((sitter) => {
                  const rating = sitter.averageRating;
                  const skillsList = sitter.skills
                    ? sitter.skills.split(',').map((s) => s.trim()).filter(Boolean)
                    : [];

                  return (
                    <div key={sitter.id} className="sitter-card">
                      {/* Top Header */}
                      <div className="sitter-card-top">
                        <div
                          className="sitter-avatar"
                          style={{
                            background: 'linear-gradient(135deg, #6366f1, #4f46e5)',
                            color: '#ffffff',
                          }}
                        >
                          {getInitials(sitter.name, sitter.email)}
                        </div>
                        <div className="sitter-info">
                          <div className="sitter-name">
                            <span>{sitter.name || sitter.email || `Caregiver #${sitter.id}`}</span>
                            {sitter.isVerified && (
                              <ShieldCheck
                                size={17}
                                color="#10b981"
                                title="Verified Caregiver"
                              />
                            )}
                          </div>

                          <div className="sitter-badges-row">
                            {/* Rating */}
                            <div className="sitter-rating">
                              <Star size={14} fill="#f59e0b" color="#f59e0b" />
                              <span>
                                {rating != null
                                  ? Number(rating).toFixed(1)
                                  : 'No ratings yet'}
                              </span>
                            </div>

                            {/* Availability status */}
                            {sitter.available !== undefined && (
                              <span
                                className={`badge ${
                                  sitter.available ? 'badge-success' : 'badge-warning'
                                }`}
                                style={{ fontSize: '0.65rem' }}
                              >
                                {sitter.available ? '● Available' : '○ Busy'}
                              </span>
                            )}
                          </div>

                          {/* Hourly Rate */}
                          <div className="sitter-rate">
                            ${sitter.hourlyRate != null ? Number(sitter.hourlyRate).toFixed(2) : '--'}
                            <span> / hour</span>
                          </div>
                        </div>
                      </div>

                      {/* Experience Snippet */}
                      {sitter.experience && (
                        <div className="experience-snippet">
                          <Briefcase size={15} color="var(--primary)" style={{ flexShrink: 0 }} />
                          <span>{sitter.experience}</span>
                        </div>
                      )}

                      {/* Bio */}
                      {sitter.bio && (
                        <p className="sitter-bio" style={{ marginBottom: '1rem' }}>
                          {sitter.bio}
                        </p>
                      )}

                      {/* Skills Tags */}
                      {skillsList.length > 0 && (
                        <div className="skills-tags">
                          {skillsList.map((skill, sIdx) => (
                            <span key={sIdx} className="skill-tag">
                              {skill}
                            </span>
                          ))}
                        </div>
                      )}

                      {/* Footer CTA */}
                      <div style={{ marginTop: 'auto', paddingTop: '1rem' }}>
                        {isAuthenticated ? (
                          <button
                            type="button"
                            className="btn btn-primary btn-full btn-sm"
                            onClick={() => setActiveBookingSitter(sitter)}
                          >
                            Book Sitter
                          </button>
                        ) : (
                          <Link
                            to="/login"
                            className="btn btn-outline btn-full btn-sm"
                          >
                            Sign In to Book
                          </Link>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </>
          )}
        </div>
      </section>

      {/* Real Booking Modal */}
      <BookingModal
        babysitter={activeBookingSitter}
        isOpen={!!activeBookingSitter}
        onClose={() => setActiveBookingSitter(null)}
        onBookingSuccess={(booking) => {
          // Optional callback after booking is submitted
          console.log('Booking successfully created:', booking);
        }}
      />
    </div>
  );
}
