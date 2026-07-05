import { useCallback, useEffect, useState } from 'react';
import {
  adminCancelAppointment,
  adminGetAppointments,
  adminGetStaff,
  adminSetStatus,
} from '../../api/client';
import type { AppointmentDto, StaffDto } from '../../api/types';
import { formatDateTime, formatPrice } from '../../utils/format';

function addDays(iso: string, days: number): string {
  const d = new Date(iso + 'T00:00:00');
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0, 10);
}

export function AdminSchedulePage() {
  const today = new Date().toISOString().slice(0, 10);
  const [from, setFrom] = useState(today);
  const [to, setTo] = useState(addDays(today, 7));
  const [staffId, setStaffId] = useState<number | ''>('');
  const [staff, setStaff] = useState<StaffDto[]>([]);
  const [appts, setAppts] = useState<AppointmentDto[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    adminGetStaff().then(setStaff).catch(() => {});
  }, []);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    adminGetAppointments(from, to, staffId === '' ? undefined : staffId)
      .then(setAppts)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [from, to, staffId]);

  useEffect(load, [load]);

  const act = async (fn: () => Promise<unknown>) => {
    try {
      await fn();
      load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Action failed.');
    }
  };

  return (
    <div>
      <h1>Schedule</h1>
      {error && <div className="alert error">{error}</div>}

      <div className="toolbar">
        <div>
          <label>From</label>
          <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div>
          <label>To</label>
          <input type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <div>
          <label>Stylist</label>
          <select value={staffId} onChange={(e) => setStaffId(e.target.value === '' ? '' : Number(e.target.value))}>
            <option value="">All</option>
            {staff.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <p className="muted">Loading…</p>
        ) : appts.length === 0 ? (
          <p className="muted">No appointments in this range.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>When</th>
                <th>Customer</th>
                <th>Service</th>
                <th>Stylist</th>
                <th>Price</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {appts.map((a) => (
                <tr key={a.id}>
                  <td>{formatDateTime(a.startTime)}</td>
                  <td>
                    {a.customerName}
                    <div className="muted" style={{ fontSize: '0.8rem' }}>
                      {a.customerEmail}
                      {a.customerPhone ? ` · ${a.customerPhone}` : ''}
                    </div>
                  </td>
                  <td>{a.serviceName}</td>
                  <td>{a.staffName}</td>
                  <td>{formatPrice(a.priceCents)}</td>
                  <td>
                    <span className={`badge ${a.status}`}>{a.status}</span>
                  </td>
                  <td>
                    <div className="row-actions">
                      {a.status === 'BOOKED' && (
                        <>
                          <button
                            className="btn-secondary"
                            onClick={() => act(() => adminSetStatus(a.id, 'COMPLETED'))}
                          >
                            Complete
                          </button>
                          <button className="btn-danger" onClick={() => act(() => adminCancelAppointment(a.id))}>
                            Cancel
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
