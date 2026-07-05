import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ApiError,
  createBooking,
  getAuthConfig,
  getServices,
  getSlots,
  getStaff,
  googleLogin,
} from '../api/client';
import type { AppointmentDto, AuthConfig, ServiceDto, SlotDto, StaffDto } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { GoogleSignIn } from '../auth/GoogleSignIn';
import { formatDateTime, formatDuration, formatPrice, formatTime, todayIso } from '../utils/format';

type Step = 'service' | 'stylist' | 'time' | 'confirm' | 'done';
const STEP_LABELS: Record<Step, string> = {
  service: 'Service',
  stylist: 'Stylist',
  time: 'Date & time',
  confirm: 'Confirm',
  done: 'Done',
};
const ORDER: Step[] = ['service', 'stylist', 'time', 'confirm'];

export function BookingPage() {
  const { user, isCustomer, login } = useAuth();
  const [step, setStep] = useState<Step>('service');
  const [error, setError] = useState<string | null>(null);

  const [services, setServices] = useState<ServiceDto[]>([]);
  const [service, setService] = useState<ServiceDto | null>(null);

  const [staff, setStaff] = useState<StaffDto[]>([]);
  const [stylist, setStylist] = useState<StaffDto | null>(null);

  const [date, setDate] = useState<string>(todayIso());
  const [slots, setSlots] = useState<SlotDto[]>([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [slot, setSlot] = useState<SlotDto | null>(null);

  const [authConfig, setAuthConfig] = useState<AuthConfig | null>(null);
  const [guest, setGuest] = useState({ name: '', email: '', phone: '' });
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [confirmed, setConfirmed] = useState<AppointmentDto | null>(null);

  useEffect(() => {
    getServices().then(setServices).catch((e) => setError(e.message));
    getAuthConfig().then(setAuthConfig).catch(() => setAuthConfig({ googleEnabled: false }));
  }, []);

  // Load stylists that can perform the chosen service.
  useEffect(() => {
    if (!service) return;
    getStaff(service.id).then(setStaff).catch((e) => setError(e.message));
  }, [service]);

  // Load slots whenever stylist/service/date change.
  const refreshSlots = useCallback(() => {
    if (!service || !stylist) return;
    setLoadingSlots(true);
    setSlot(null);
    getSlots(stylist.id, service.id, date)
      .then(setSlots)
      .catch((e) => setError(e.message))
      .finally(() => setLoadingSlots(false));
  }, [service, stylist, date]);

  useEffect(() => {
    if (step === 'time') refreshSlots();
  }, [step, refreshSlots]);

  const go = (s: Step) => {
    setError(null);
    setStep(s);
  };

  const doBooking = async () => {
    if (!service || !stylist || !slot) return;
    setSubmitting(true);
    setError(null);
    try {
      const payload = {
        serviceId: service.id,
        staffId: stylist.id,
        startTime: slot.start,
        notes: notes || undefined,
        ...(isCustomer
          ? {}
          : { guestName: guest.name, guestEmail: guest.email, guestPhone: guest.phone || undefined }),
      };
      const appt = await createBooking(payload);
      setConfirmed(appt);
      go('done');
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : 'Something went wrong. Please try again.';
      setError(msg);
      // If the slot was taken, refresh the list.
      if (e instanceof ApiError && e.status === 409) {
        go('time');
        refreshSlots();
      }
    } finally {
      setSubmitting(false);
    }
  };

  const onGoogle = async (credential: string) => {
    try {
      const res = await googleLogin(credential);
      login(res);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Google sign-in failed.');
    }
  };

  const canBookGuest = guest.name.trim() && guest.email.trim();

  return (
    <div>
      <h1>Book an appointment</h1>

      {step !== 'done' && (
        <div className="steps">
          {ORDER.map((s) => (
            <span
              key={s}
              className={`step ${s === step ? 'active' : ''} ${ORDER.indexOf(s) < ORDER.indexOf(step) ? 'done' : ''}`}
            >
              {STEP_LABELS[s]}
            </span>
          ))}
        </div>
      )}

      {error && <div className="alert error">{error}</div>}

      {/* Step 1: service */}
      {step === 'service' && (
        <div className="card">
          <h2>Choose a service</h2>
          <div className="option-grid">
            {services.map((s) => (
              <button
                key={s.id}
                className={`option ${service?.id === s.id ? 'selected' : ''}`}
                onClick={() => {
                  setService(s);
                  setStylist(null);
                  go('stylist');
                }}
              >
                <div className="name">{s.name}</div>
                <div className="meta">
                  {formatDuration(s.durationMinutes)} · {formatPrice(s.priceCents)}
                </div>
                {s.description && <div className="meta">{s.description}</div>}
              </button>
            ))}
            {services.length === 0 && <p className="muted">No services available.</p>}
          </div>
        </div>
      )}

      {/* Step 2: stylist */}
      {step === 'stylist' && service && (
        <div className="card">
          <h2>Choose a stylist for {service.name}</h2>
          <div className="option-grid">
            {staff.map((st) => (
              <button
                key={st.id}
                className={`option ${stylist?.id === st.id ? 'selected' : ''}`}
                onClick={() => {
                  setStylist(st);
                  go('time');
                }}
              >
                <div className="name">{st.name}</div>
              </button>
            ))}
            {staff.length === 0 && <p className="muted">No stylists offer this service yet.</p>}
          </div>
          <p>
            <button className="link-btn" onClick={() => go('service')}>
              ← Back to services
            </button>
          </p>
        </div>
      )}

      {/* Step 3: date & time */}
      {step === 'time' && service && stylist && (
        <div className="card">
          <h2>
            Pick a time with {stylist.name}
          </h2>
          <div style={{ maxWidth: 220 }}>
            <label htmlFor="date">Date</label>
            <input
              id="date"
              type="date"
              value={date}
              min={todayIso()}
              onChange={(e) => setDate(e.target.value)}
            />
          </div>

          {loadingSlots ? (
            <p className="muted">Loading available times…</p>
          ) : slots.length === 0 ? (
            <p className="muted">No open slots on this date. Try another day.</p>
          ) : (
            <div className="slots">
              {slots.map((s) => (
                <button
                  key={s.start}
                  className={`slot ${slot?.start === s.start ? 'selected' : ''}`}
                  onClick={() => setSlot(s)}
                >
                  {formatTime(s.start)}
                </button>
              ))}
            </div>
          )}

          <div style={{ marginTop: '1rem' }} className="row-actions">
            <button className="link-btn" onClick={() => go('stylist')}>
              ← Back
            </button>
            <button className="btn" disabled={!slot} onClick={() => go('confirm')}>
              Continue
            </button>
          </div>
        </div>
      )}

      {/* Step 4: confirm */}
      {step === 'confirm' && service && stylist && slot && (
        <div className="card">
          <h2>Confirm your booking</h2>
          <table>
            <tbody>
              <tr>
                <th>Service</th>
                <td>
                  {service.name} ({formatDuration(service.durationMinutes)})
                </td>
              </tr>
              <tr>
                <th>Stylist</th>
                <td>{stylist.name}</td>
              </tr>
              <tr>
                <th>When</th>
                <td>{formatDateTime(slot.start)}</td>
              </tr>
              <tr>
                <th>Price</th>
                <td>{formatPrice(service.priceCents)}</td>
              </tr>
            </tbody>
          </table>

          <div style={{ marginTop: '1rem' }}>
            <label htmlFor="notes">Notes (optional)</label>
            <textarea id="notes" rows={2} value={notes} onChange={(e) => setNotes(e.target.value)} />
          </div>

          {isCustomer ? (
            <>
              <p className="muted">Booking as {user?.name} ({user?.email})</p>
              <div className="row-actions">
                <button className="link-btn" onClick={() => go('time')}>
                  ← Back
                </button>
                <button className="btn" onClick={doBooking} disabled={submitting}>
                  {submitting ? 'Booking…' : 'Confirm booking'}
                </button>
              </div>
            </>
          ) : (
            <>
              <h2 style={{ marginTop: '1.25rem' }}>Your details</h2>
              {authConfig?.googleEnabled && authConfig.googleClientId && (
                <div style={{ marginBottom: '1rem' }}>
                  <p className="muted">Sign in for faster booking and to manage your appointments:</p>
                  <GoogleSignIn clientId={authConfig.googleClientId} onCredential={onGoogle} />
                  <p className="muted" style={{ margin: '0.6rem 0' }}>
                    — or continue as a guest —
                  </p>
                </div>
              )}
              <div className="field-row">
                <div>
                  <label htmlFor="gname">Name</label>
                  <input
                    id="gname"
                    value={guest.name}
                    onChange={(e) => setGuest({ ...guest, name: e.target.value })}
                  />
                </div>
                <div>
                  <label htmlFor="gemail">Email</label>
                  <input
                    id="gemail"
                    type="email"
                    value={guest.email}
                    onChange={(e) => setGuest({ ...guest, email: e.target.value })}
                  />
                </div>
              </div>
              <label htmlFor="gphone">Phone (optional)</label>
              <input
                id="gphone"
                value={guest.phone}
                onChange={(e) => setGuest({ ...guest, phone: e.target.value })}
              />
              <div className="row-actions">
                <button className="link-btn" onClick={() => go('time')}>
                  ← Back
                </button>
                <button className="btn" onClick={doBooking} disabled={submitting || !canBookGuest}>
                  {submitting ? 'Booking…' : 'Book as guest'}
                </button>
              </div>
            </>
          )}
        </div>
      )}

      {/* Done */}
      {step === 'done' && confirmed && (
        <div className="card">
          <div className="alert success">Your appointment is confirmed! A confirmation has been sent.</div>
          <table>
            <tbody>
              <tr>
                <th>Service</th>
                <td>{confirmed.serviceName}</td>
              </tr>
              <tr>
                <th>Stylist</th>
                <td>{confirmed.staffName}</td>
              </tr>
              <tr>
                <th>When</th>
                <td>{formatDateTime(confirmed.startTime)}</td>
              </tr>
            </tbody>
          </table>
          <div className="row-actions" style={{ marginTop: '1rem' }}>
            <button
              className="btn-secondary"
              onClick={() => {
                setService(null);
                setStylist(null);
                setSlot(null);
                setConfirmed(null);
                setNotes('');
                go('service');
              }}
            >
              Book another
            </button>
            {isCustomer && <Link to="/my-bookings">View my bookings →</Link>}
          </div>
        </div>
      )}
    </div>
  );
}
