import { useEffect, useState } from 'react';
import { cancelBooking, getMyBookings } from '../api/client';
import type { AppointmentDto } from '../api/types';
import { formatDateTime, formatPrice } from '../utils/format';

export function MyBookingsPage() {
  const [appts, setAppts] = useState<AppointmentDto[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    getMyBookings()
      .then(setAppts)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const onCancel = async (id: number) => {
    if (!confirm('Cancel this appointment?')) return;
    try {
      await cancelBooking(id);
      load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not cancel.');
    }
  };

  const isUpcoming = (a: AppointmentDto) =>
    a.status === 'BOOKED' && new Date(a.startTime).getTime() > Date.now();

  return (
    <div>
      <h1>My bookings</h1>
      {error && <div className="alert error">{error}</div>}
      {loading ? (
        <p className="muted">Loading…</p>
      ) : appts.length === 0 ? (
        <p className="muted">You have no bookings yet.</p>
      ) : (
        <div className="card">
          <table>
            <thead>
              <tr>
                <th>When</th>
                <th>Service</th>
                <th>Stylist</th>
                <th>Price</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {appts.map((a) => (
                <tr key={a.id}>
                  <td>{formatDateTime(a.startTime)}</td>
                  <td>{a.serviceName}</td>
                  <td>{a.staffName}</td>
                  <td>{formatPrice(a.priceCents)}</td>
                  <td>
                    <span className={`badge ${a.status}`}>{a.status}</span>
                  </td>
                  <td>
                    {isUpcoming(a) && (
                      <button className="btn-danger" onClick={() => onCancel(a.id)}>
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
