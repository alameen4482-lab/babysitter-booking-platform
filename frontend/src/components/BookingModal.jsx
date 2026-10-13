import React, { useState, useEffect } from 'react';
import { 
  X, 
  Calendar, 
  Clock, 
  DollarSign, 
  ShieldCheck, 
  AlertCircle, 
  CheckCircle2, 
  Sparkles,
  ArrowRight,
  User,
  Info
} from 'lucide-react';
import { getAvailabilitySlots, createBooking } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';

export default function BookingModal({ babysitter, isOpen, onClose, onBookingSuccess }) {
  const { user, isAuthenticated } = useAuth();
  
  const [slots, setSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [slotsError, setSlotsError] = useState(null);
  
  const [selectedSlotId, setSelectedSlotId] = useState(null);
  const [notes, setNotes] = useState('');
  
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const [bookingSuccessData, setBookingSuccessData] = useState(null);

  // Fetch real unbooked availability slots for this babysitter
  useEffect(() => {
    if (!isOpen || !babysitter) return;

    // Reset form states
    setSelectedSlotId(null);
    setNotes('');
    setSubmitError(null);
    setBookingSuccessData(null);
    setSlotsError(null);

    async function fetchSlots() {
      setLoadingSlots(true);
      try {
        const response = await getAvailabilitySlots(babysitter.id);
        if (response && response.data) {
          // Filter out slots that have already started in the past or are already booked
          const now = new Date();
          const validSlots = response.data.filter(slot => {
            if (slot.isBooked) return false;
            if (slot.startTime && new Date(slot.startTime) < now) return false;
            return true;
          });
          setSlots(validSlots);
        } else {
          setSlots([]);
        }
      } catch (err) {
        setSlotsError(err.message || 'Failed to fetch available time slots for this babysitter.');
        setSlots([]);
      } finally {
        setLoadingSlots(false);
      }
    }

    fetchSlots();
  }, [isOpen, babysitter]);

  if (!isOpen || !babysitter) return null;

  const selectedSlot = slots.find(s => s.id === selectedSlotId);

  // Format date and time helpers
  const formatSlotDateTime = (startIso, endIso) => {
    if (!startIso) return '';
    const start = new Date(startIso);
    const end = endIso ? new Date(endIso) : null;

    const dateStr = start.toLocaleDateString(undefined, {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });

    const timeStartStr = start.toLocaleTimeString(undefined, {
      hour: '2-digit',
      minute: '2-digit'
    });

    const timeEndStr = end ? end.toLocaleTimeString(undefined, {
      hour: '2-digit',
      minute: '2-digit'
    }) : '';

    return {
      date: dateStr,
      time: `${timeStartStr} - ${timeEndStr}`
    };
  };

  // Calculate estimated total based on slot duration & hourly rate
  const calculateTotal = (slot) => {
    if (!slot || !slot.startTime || !slot.endTime) return null;
    const start = new Date(slot.startTime);
    const end = new Date(slot.endTime);
    const durationMinutes = (end - start) / (1000 * 60);
    const hours = durationMinutes / 60;
    const rate = slot.hourlyRate || babysitter.hourlyRate || 0;
    const total = Math.round(hours * rate * 100) / 100;
    return {
      hours: Number(hours.toFixed(2)),
      total: Number(total.toFixed(2))
    };
  };

  const handleSubmitBooking = async (e) => {
    e.preventDefault();
    setSubmitError(null);

    if (!selectedSlotId) {
      setSubmitError('Please select an available time slot to continue.');
      return;
    }

    if (user?.role && user.role !== 'PARENT') {
      setSubmitError(`Only accounts with the PARENT role can book babysitters. Your current role is ${user.role}.`);
      return;
    }

    setSubmitting(true);
    try {
      const response = await createBooking({
        babysitterId: babysitter.id,
        slotId: selectedSlotId,
        notes: notes.trim()
      });

      if (response && response.data) {
        setBookingSuccessData(response.data);
        if (onBookingSuccess) {
          onBookingSuccess(response.data);
        }
      }
    } catch (err) {
      setSubmitError(err.message || 'Failed to submit booking request. The slot may have been taken.');
    } finally {
      setSubmitting(false);
    }
  };

  const isParent = user?.role === 'PARENT';

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div 
              className="sitter-avatar" 
              style={{ 
                width: '42px', 
                height: '42px', 
                background: 'linear-gradient(135deg, #6366f1, #4f46e5)', 
                color: '#fff',
                fontSize: '1rem'
              }}
            >
              {babysitter.name ? babysitter.name.slice(0, 2).toUpperCase() : 'BS'}
            </div>
            <div>
              <h3 style={{ fontSize: '1.25rem', margin: 0, display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                Book with {babysitter.name || 'Caregiver'}
                {babysitter.isVerified && <ShieldCheck size={18} color="#10b981" />}
              </h3>
              <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                ${babysitter.hourlyRate != null ? Number(babysitter.hourlyRate).toFixed(2) : '--'} / hr
                {babysitter.averageRating != null && ` • ★ ${Number(babysitter.averageRating).toFixed(1)}`}
              </p>
            </div>
          </div>
          <button 
            type="button" 
            className="modal-close-btn" 
            onClick={onClose} 
            aria-label="Close modal"
          >
            <X size={20} />
          </button>
        </div>

        {/* Modal Content */}
        <div className="modal-body">
          {/* SUCCESS STATE */}
          {bookingSuccessData ? (
            <div className="booking-success-box">
              <div className="success-icon-wrap">
                <CheckCircle2 size={44} color="#10b981" />
              </div>
              <h3 style={{ fontSize: '1.45rem', marginBottom: '0.5rem', color: 'var(--text-main)' }}>
                Booking Request Submitted!
              </h3>
              <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', fontSize: '0.95rem' }}>
                Your booking request has been successfully created with status{' '}
                <span className="badge badge-warning" style={{ fontSize: '0.75rem' }}>
                  {bookingSuccessData.status || 'PENDING'}
                </span>
                . The babysitter will be notified to confirm.
              </p>

              {/* Receipt details */}
              <div className="booking-receipt-card">
                <div className="receipt-row">
                  <span className="receipt-label">Booking Reference ID</span>
                  <span className="receipt-value" style={{ fontWeight: 700, color: 'var(--primary)' }}>
                    #{bookingSuccessData.id}
                  </span>
                </div>
                <div className="receipt-row">
                  <span className="receipt-label">Babysitter</span>
                  <span className="receipt-value">{bookingSuccessData.babysitterName || babysitter.name}</span>
                </div>
                {bookingSuccessData.startTime && (
                  <div className="receipt-row">
                    <span className="receipt-label">Scheduled Time</span>
                    <span className="receipt-value">
                      {formatSlotDateTime(bookingSuccessData.startTime, bookingSuccessData.endTime).date}
                      <br />
                      <small style={{ color: 'var(--text-muted)' }}>
                        {formatSlotDateTime(bookingSuccessData.startTime, bookingSuccessData.endTime).time}
                      </small>
                    </span>
                  </div>
                )}
                <div className="receipt-row">
                  <span className="receipt-label">Total Amount</span>
                  <span className="receipt-value" style={{ fontWeight: 700, fontSize: '1.05rem', color: 'var(--text-main)' }}>
                    ${bookingSuccessData.totalAmount != null ? Number(bookingSuccessData.totalAmount).toFixed(2) : '--'}
                  </span>
                </div>
                {bookingSuccessData.notes && (
                  <div className="receipt-row" style={{ borderBottom: 'none' }}>
                    <span className="receipt-label">Parent Notes</span>
                    <span className="receipt-value" style={{ fontStyle: 'italic' }}>
                      "{bookingSuccessData.notes}"
                    </span>
                  </div>
                )}
              </div>

              <div style={{ marginTop: '1.75rem', display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
                <button 
                  type="button" 
                  className="btn btn-primary" 
                  onClick={onClose}
                >
                  Done
                </button>
              </div>
            </div>
          ) : (
            <>
              {/* AUTHENTICATION / ROLE CHECK */}
              {!isAuthenticated ? (
                <div className="alert alert-info" style={{ marginBottom: '1.5rem' }}>
                  <Info size={18} style={{ flexShrink: 0 }} />
                  <div>
                    <strong>Parent account required:</strong> You must be signed in as a parent to book an appointment.
                    <div style={{ marginTop: '0.5rem' }}>
                      <Link to="/login" className="btn btn-primary btn-sm" style={{ marginRight: '0.5rem' }}>
                        Sign In
                      </Link>
                      <Link to="/register" state={{ defaultRole: 'PARENT' }} className="btn btn-secondary btn-sm">
                        Register as Parent
                      </Link>
                    </div>
                  </div>
                </div>
              ) : !isParent ? (
                <div className="alert alert-danger" style={{ marginBottom: '1.5rem' }}>
                  <AlertCircle size={18} style={{ flexShrink: 0 }} />
                  <div>
                    <strong>Role Notice:</strong> Only users registered with role <strong>PARENT</strong> can create booking requests. Your current role is <strong>{user?.role}</strong>.
                  </div>
                </div>
              ) : null}

              {/* Error Alert */}
              {submitError && (
                <div className="alert alert-danger" style={{ marginBottom: '1.25rem' }}>
                  <AlertCircle size={18} style={{ flexShrink: 0 }} />
                  <div>{submitError}</div>
                </div>
              )}

              <form onSubmit={handleSubmitBooking}>
                {/* 1. Availability Slots Selection */}
                <div className="form-group">
                  <label className="form-label" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span>Select an Available Time Slot</span>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {slots.length} available
                    </span>
                  </label>

                  {loadingSlots ? (
                    <div style={{ padding: '2rem', textAlign: 'center', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                      <div className="spinner" style={{ width: '28px', height: '28px', margin: '0 auto 0.75rem' }} />
                      <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                        Fetching real available slots from backend...
                      </p>
                    </div>
                  ) : slotsError ? (
                    <div className="alert alert-danger" style={{ fontSize: '0.85rem' }}>
                      <AlertCircle size={16} />
                      <div>{slotsError}</div>
                    </div>
                  ) : slots.length === 0 ? (
                    <div className="empty-slots-box">
                      <Calendar size={28} color="var(--text-light)" style={{ marginBottom: '0.5rem' }} />
                      <p style={{ fontWeight: 600, color: 'var(--text-main)', margin: '0 0 0.25rem' }}>
                        No Open Availability Slots
                      </p>
                      <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)', margin: 0 }}>
                        {babysitter.name || 'This babysitter'} has not posted any upcoming unbooked slots in the backend. Please check back later.
                      </p>
                    </div>
                  ) : (
                    <div className="slots-grid-selector">
                      {slots.map((slot) => {
                        const { date, time } = formatSlotDateTime(slot.startTime, slot.endTime);
                        const isSelected = selectedSlotId === slot.id;
                        const estimate = calculateTotal(slot);

                        return (
                          <div
                            key={slot.id}
                            className={`slot-option-card ${isSelected ? 'selected' : ''}`}
                            onClick={() => setSelectedSlotId(slot.id)}
                          >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700, fontSize: '0.9rem', color: 'var(--text-main)' }}>
                                <Calendar size={15} color="var(--primary)" />
                                <span>{date}</span>
                              </div>
                              <span className="badge badge-success" style={{ fontSize: '0.65rem' }}>
                                Open
                              </span>
                            </div>

                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.825rem', marginTop: '0.35rem' }}>
                              <Clock size={14} />
                              <span>{time}</span>
                            </div>

                            {estimate && (
                              <div style={{ marginTop: '0.5rem', paddingTop: '0.4rem', borderTop: '1px dashed var(--border)', display: 'flex', justifyContent: 'space-between', fontSize: '0.78rem' }}>
                                <span style={{ color: 'var(--text-muted)' }}>{estimate.hours} hrs</span>
                                <span style={{ fontWeight: 700, color: 'var(--primary)' }}>${estimate.total} est.</span>
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>

                {/* 2. Optional Notes Field */}
                <div className="form-group">
                  <label className="form-label" htmlFor="booking-notes">
                    Booking Notes <span style={{ fontWeight: 400, color: 'var(--text-light)' }}>(Optional)</span>
                  </label>
                  <textarea
                    id="booking-notes"
                    className="form-textarea"
                    rows="3"
                    placeholder="Provide any specific care requirements, number and ages of children, or emergency instructions..."
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    maxLength={500}
                  />
                  <div style={{ textAlign: 'right', fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.2rem' }}>
                    {notes.length}/500
                  </div>
                </div>

                {/* 3. Cost Summary if slot selected */}
                {selectedSlot && (
                  <div className="slot-summary-banner">
                    <div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>ESTIMATED TOTAL</div>
                      <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--primary)' }}>
                        ${calculateTotal(selectedSlot)?.total || '--'}
                      </div>
                    </div>
                    <div style={{ textAlign: 'right', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      Rate: ${babysitter.hourlyRate || selectedSlot.hourlyRate}/hr<br />
                      Duration: {calculateTotal(selectedSlot)?.hours} hours
                    </div>
                  </div>
                )}

                {/* Modal Footer Actions */}
                <div className="modal-footer">
                  <button 
                    type="button" 
                    className="btn btn-secondary" 
                    onClick={onClose}
                    disabled={submitting}
                  >
                    Cancel
                  </button>
                  <button 
                    type="submit" 
                    className="btn btn-primary"
                    disabled={submitting || !selectedSlotId || !isAuthenticated || !isParent || slots.length === 0}
                  >
                    {submitting ? 'Confirming Reservation...' : 'Confirm & Book'}
                    {!submitting && <ArrowRight size={16} />}
                  </button>
                </div>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
