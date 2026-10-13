import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  ShieldCheck, 
  Star, 
  Search, 
  Calendar, 
  Clock, 
  Heart, 
  Award, 
  CheckCircle2, 
  Users, 
  ArrowRight,
  Sparkles
} from 'lucide-react';
import { getBabysitters } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function HomePage() {
  const { isAuthenticated } = useAuth();
  const [sitters, setSitters] = useState([]);
  const [loadingSitters, setLoadingSitters] = useState(true);
  const [minRatingFilter, setMinRatingFilter] = useState('');

  useEffect(() => {
    async function fetchSitters() {
      try {
        const response = await getBabysitters();
        if (response && response.data && Array.isArray(response.data)) {
          setSitters(response.data);
        } else {
          setSitters([]);
        }
      } catch (err) {
        // Backend returned error (e.g. 401 unauthenticated or empty)
        setSitters([]);
      } finally {
        setLoadingSitters(false);
      }
    }

    fetchSitters();
  }, []);

  const filteredSitters = sitters.filter(sitter => {
    if (!minRatingFilter) return true;
    const rating = sitter.averageRating ?? 0;
    return rating >= parseFloat(minRatingFilter);
  });

  return (
    <div className="page-wrapper">
      {/* Hero Section */}
      <section className="hero">
        <div className="container">
          <div className="hero-grid">
            <div>
              <div className="hero-pill">
                <Sparkles size={16} />
                <span>#1 Trusted Family Childcare Network</span>
              </div>
              <h1 className="hero-title">
                Loving, Verified <span>Babysitters</span> for Your Little Ones.
              </h1>
              <p className="hero-subtitle">
                Find certified, background-checked caregivers in your neighborhood. Simple scheduling, instant availability slots, and genuine peace of mind.
              </p>

              <div className="hero-cta">
                <Link to="/babysitters" className="btn btn-primary btn-lg">
                  Find a Babysitter
                  <ArrowRight size={18} />
                </Link>
                <Link to="/register" state={{ defaultRole: 'BABYSITTER' }} className="btn btn-secondary btn-lg">
                  Join as Babysitter
                </Link>
              </div>

              <div className="trust-stats">
                <div className="stat-item">
                  <span className="stat-number">100%</span>
                  <span className="stat-label">ID & Background Checked</span>
                </div>
                <div className="stat-item">
                  <span className="stat-number">4.9 / 5</span>
                  <span className="stat-label">Average Parent Rating</span>
                </div>
                <div className="stat-item">
                  <span className="stat-number">Instant</span>
                  <span className="stat-label">Slot Booking System</span>
                </div>
              </div>
            </div>

            {/* Hero Visual Showcase */}
            <div className="hero-visual">
              <div className="hero-main-card">
                <div className="floating-badge">
                  <ShieldCheck size={18} />
                  <span>Verified Platform</span>
                </div>

                <div className="sitter-preview-header">
                  <div className="avatar-large" style={{ background: 'linear-gradient(135deg, #818cf8, #4f46e5)', color: '#fff' }}>
                    <Users size={32} />
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1.25rem', marginBottom: '0.2rem' }}>Caregiver Directory</h3>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#d97706', fontSize: '0.9rem', fontWeight: 600 }}>
                      <Star size={16} fill="#f59e0b" color="#f59e0b" />
                      <span>Verified parent reviews</span>
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.9rem', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', marginBottom: '1.25rem' }}>
                  <div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>AVAILABILITY</div>
                    <div style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--primary)' }}>Real-Time</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>SECURITY</div>
                    <div style={{ fontSize: '1.05rem', fontWeight: 600 }}>Locked Slots</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>RATINGS</div>
                    <div style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--success)' }}>100% Verified</div>
                  </div>
                </div>

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginBottom: '1.25rem' }}>
                  <span className="badge badge-primary">Background Checked</span>
                  <span className="badge badge-primary">First Aid / CPR</span>
                  <span className="badge badge-primary">Flexible Hours</span>
                </div>

                <Link to="/babysitters" className="btn btn-primary btn-full">
                  Explore All Babysitters
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Pathways: Parents vs Babysitters */}
      <section className="pathways-section" id="for-sitters">
        <div className="container">
          <div className="section-header">
            <h2>Built for Families and Trusted Sitters</h2>
            <p className="section-subtitle">
              Whether you need dependable childcare for an evening or want to earn flexibly doing what you love.
            </p>
          </div>

          <div className="pathways-grid">
            {/* For Parents */}
            <div className="pathway-card pathway-parent">
              <div>
                <div className="pathway-icon-wrap">
                  <Heart size={26} />
                </div>
                <h3 className="pathway-title">For Parents & Families</h3>
                <p className="pathway-desc">
                  Enjoy date nights, handle urgent work meetings, and give your children caring supervision with zero stress.
                </p>
                <ul className="pathway-list">
                  <li><CheckCircle2 size={18} color="#4f46e5" /> Browse profiles with genuine ratings & verified reviews</li>
                  <li><CheckCircle2 size={18} color="#4f46e5" /> Reserve real-time availability slots seamlessly</li>
                  <li><CheckCircle2 size={18} color="#4f46e5" /> Transparent hourly rates with no hidden surprises</li>
                  <li><CheckCircle2 size={18} color="#4f46e5" /> Instant booking confirmation & status updates</li>
                </ul>
              </div>
              <Link to="/babysitters" className="btn btn-primary">
                Find a Babysitter
              </Link>
            </div>

            {/* For Babysitters */}
            <div className="pathway-card pathway-sitter">
              <div>
                <div className="pathway-icon-wrap">
                  <Award size={26} />
                </div>
                <h3 className="pathway-title">For Babysitters & Nannies</h3>
                <p className="pathway-desc">
                  Be your own boss. Build a loyal client base in your neighborhood and get rewarded for your childcare talents.
                </p>
                <ul className="pathway-list">
                  <li><CheckCircle2 size={18} color="#ea580c" /> Set your custom hourly rate and update it anytime</li>
                  <li><CheckCircle2 size={18} color="#ea580c" /> Post open availability slots that fit your weekly schedule</li>
                  <li><CheckCircle2 size={18} color="#ea580c" /> Review booking requests and choose families you love</li>
                  <li><CheckCircle2 size={18} color="#ea580c" /> Build a standout profile with verified badges</li>
                </ul>
              </div>
              <Link to="/register" state={{ defaultRole: 'BABYSITTER' }} className="btn btn-accent">
                Register as a Babysitter
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Babysitters Section (Connected to Real Backend) */}
      <section className="sitters-section" id="featured-sitters">
        <div className="container">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '2.5rem', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <h2>Caregiver Directory Preview</h2>
              <p className="section-subtitle">
                Explore registered babysitters from our platform database.
              </p>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <Link to="/babysitters" className="btn btn-secondary btn-sm">
                View Full Directory
                <ArrowRight size={14} />
              </Link>
            </div>
          </div>

          {loadingSitters ? (
            <div className="state-container card" style={{ padding: '2.5rem' }}>
              <div className="spinner" />
              <p>Loading registered babysitters...</p>
            </div>
          ) : filteredSitters.length > 0 ? (
            <div className="sitters-grid">
              {filteredSitters.slice(0, 4).map((sitter) => {
                const rating = sitter.averageRating;
                const rate = sitter.hourlyRate;
                const initials = sitter.name
                  ? sitter.name.split(' ').map((n) => n[0]).join('').slice(0, 2).toUpperCase()
                  : 'BS';
                const skillsList = sitter.skills
                  ? sitter.skills.split(',').map((s) => s.trim()).filter(Boolean)
                  : [];

                return (
                  <div key={sitter.id} className="sitter-card">
                    <div className="sitter-card-top">
                      <div
                        className="sitter-avatar"
                        style={{
                          background: 'linear-gradient(135deg, #818cf8, #4f46e5)',
                          color: '#ffffff',
                        }}
                      >
                        {initials}
                      </div>
                      <div className="sitter-info">
                        <div className="sitter-name">
                          {sitter.name || sitter.email || `Caregiver #${sitter.id}`}
                          {sitter.isVerified && (
                            <ShieldCheck size={16} color="#10b981" title="Verified Babysitter" />
                          )}
                        </div>
                        <div className="sitter-rating">
                          <Star size={14} fill="#f59e0b" color="#f59e0b" />
                          <span>{rating != null ? Number(rating).toFixed(1) : 'New'}</span>
                        </div>
                        <div className="sitter-rate">
                          ${rate != null ? Number(rate).toFixed(2) : '--'} <span>/ hour</span>
                        </div>
                      </div>
                    </div>

                    <p className="sitter-bio">
                      {sitter.bio || sitter.experience || 'Registered platform caregiver.'}
                    </p>

                    {skillsList.length > 0 && (
                      <div className="skills-tags">
                        {skillsList.slice(0, 3).map((skill, sIdx) => (
                          <span key={sIdx} className="skill-tag">{skill}</span>
                        ))}
                      </div>
                    )}

                    <div style={{ marginTop: 'auto' }}>
                      <Link to="/babysitters" className="btn btn-outline btn-full btn-sm">
                        View Profile & Slots
                      </Link>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="card" style={{ textAlign: 'center', padding: '3rem 2rem' }}>
              <div className="state-icon-wrap state-icon-info">
                <Users size={32} />
              </div>
              <h3 style={{ marginBottom: '0.5rem' }}>Explore All Babysitters</h3>
              <p style={{ maxWidth: '520px', margin: '0 auto 1.5rem', color: 'var(--text-muted)' }}>
                View all registered caregivers, check their ratings, hourly rates, and reserve availability slots.
              </p>
              <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center', flexWrap: 'wrap' }}>
                <Link to="/babysitters" className="btn btn-primary">
                  Open Babysitter Directory
                  <ArrowRight size={16} />
                </Link>
                <Link to="/register" state={{ defaultRole: 'BABYSITTER' }} className="btn btn-secondary">
                  Join as Babysitter
                </Link>
              </div>
            </div>
          )}
        </div>
      </section>

      {/* How It Works */}
      <section className="steps-section" id="how-it-works">
        <div className="container">
          <div className="section-header">
            <h2>How KindredCare Works</h2>
            <p className="section-subtitle">
              Book quality childcare in 3 simple, transparent steps.
            </p>
          </div>

          <div className="steps-grid">
            <div className="step-card">
              <div className="step-number">1</div>
              <h3 className="step-title">Discover & Match</h3>
              <p>
                Browse sitter profiles with verified experience, skills, parent reviews, and hourly rates upfront.
              </p>
            </div>

            <div className="step-card">
              <div className="step-number">2</div>
              <h3 className="step-title">Select an Open Slot</h3>
              <p>
                Pick a designated availability slot that matches your exact schedule with locked reservations.
              </p>
            </div>

            <div className="step-card">
              <div className="step-number">3</div>
              <h3 className="step-title">Care & Peace of Mind</h3>
              <p>
                Relax knowing your children are in attentive, background-checked hands. Pay securely and leave a review.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Safety & Trust Section */}
      <section className="pathways-section" id="trust-safety" style={{ background: '#f8fafc' }}>
        <div className="container">
          <div className="card" style={{ background: '#ffffff', padding: '3.5rem 2.5rem', textAlign: 'center' }}>
            <div style={{ maxWidth: '700px', margin: '0 auto' }}>
              <div className="badge badge-success" style={{ marginBottom: '1rem' }}>
                <ShieldCheck size={14} />
                Safety Guarantee
              </div>
              <h2 style={{ marginBottom: '1rem' }}>Safety Is Our First and Foremost Priority</h2>
              <p style={{ marginBottom: '2.5rem', fontSize: '1.05rem' }}>
                Every babysitter on KindredCare passes strict ID verification and community vetting before taking bookings.
              </p>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem', textAlign: 'left' }}>
                <div style={{ padding: '1rem', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <h4 style={{ fontSize: '1rem', marginBottom: '0.4rem', color: 'var(--primary)' }}>Strict Vetting</h4>
                  <p style={{ fontSize: '0.85rem' }}>Identity checks and profile moderation keep our community safe.</p>
                </div>
                <div style={{ padding: '1rem', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <h4 style={{ fontSize: '1rem', marginBottom: '0.4rem', color: 'var(--primary)' }}>Pessimistic Locking</h4>
                  <p style={{ fontSize: '0.85rem' }}>Our backend engine prevents double-booking so slots are guaranteed.</p>
                </div>
                <div style={{ padding: '1rem', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <h4 style={{ fontSize: '1rem', marginBottom: '0.4rem', color: 'var(--primary)' }}>Authentic Reviews</h4>
                  <p style={{ fontSize: '0.85rem' }}>Only parents who completed actual bookings can leave reviews.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Bottom Call to Action */}
      <section style={{ padding: '5rem 0', background: 'linear-gradient(135deg, #4f46e5 0%, #3730a3 100%)', color: '#ffffff', textAlign: 'center' }}>
        <div className="container">
          <h2 style={{ color: '#ffffff', fontSize: '2.4rem', marginBottom: '1rem' }}>
            Ready to find the ideal babysitter for your family?
          </h2>
          <p style={{ color: '#e0e7ff', fontSize: '1.15rem', maxWidth: '600px', margin: '0 auto 2.5rem' }}>
            Browse verified babysitters in your neighborhood or register as an approved caregiver.
          </p>
          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
            <Link to="/babysitters" className="btn btn-secondary btn-lg" style={{ color: 'var(--primary)', fontWeight: 700 }}>
              Find a Babysitter
            </Link>
            <Link to="/register" state={{ defaultRole: 'BABYSITTER' }} className="btn btn-outline btn-lg" style={{ borderColor: '#ffffff', color: '#ffffff' }}>
              Become a Babysitter
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
