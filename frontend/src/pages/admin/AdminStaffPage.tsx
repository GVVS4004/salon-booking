import { useEffect, useState, type FormEvent } from 'react';
import {
  adminCreateStaff,
  adminDeleteStaff,
  adminGetAvailability,
  adminGetServices,
  adminGetStaff,
  adminSetAvailability,
  adminUpdateStaff,
} from '../../api/client';
import type { AvailabilityDto, ServiceDto, StaffDto } from '../../api/types';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

interface StaffForm {
  id?: number;
  name: string;
  email: string;
  active: boolean;
  serviceIds: number[];
}
const EMPTY: StaffForm = { name: '', email: '', active: true, serviceIds: [] };

export function AdminStaffPage() {
  const [staff, setStaff] = useState<StaffDto[]>([]);
  const [services, setServices] = useState<ServiceDto[]>([]);
  const [form, setForm] = useState<StaffForm>(EMPTY);
  const [error, setError] = useState<string | null>(null);

  // Availability editor state
  const [hoursFor, setHoursFor] = useState<StaffDto | null>(null);
  const [windows, setWindows] = useState<AvailabilityDto[]>([]);
  const [savedMsg, setSavedMsg] = useState<string | null>(null);

  const loadStaff = () => adminGetStaff().then(setStaff).catch((e) => setError(e.message));
  useEffect(() => {
    loadStaff();
    adminGetServices().then(setServices).catch(() => {});
  }, []);

  const edit = (s: StaffDto) =>
    setForm({ id: s.id, name: s.name, email: s.email ?? '', active: s.active, serviceIds: [...s.serviceIds] });

  const toggleService = (id: number) =>
    setForm((f) => ({
      ...f,
      serviceIds: f.serviceIds.includes(id) ? f.serviceIds.filter((x) => x !== id) : [...f.serviceIds, id],
    }));

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    const body = { name: form.name, email: form.email || undefined, active: form.active, serviceIds: form.serviceIds };
    try {
      if (form.id) await adminUpdateStaff(form.id, body);
      else await adminCreateStaff(body);
      setForm(EMPTY);
      loadStaff();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed.');
    }
  };

  const deactivate = async (id: number) => {
    if (!confirm('Deactivate this stylist?')) return;
    await adminDeleteStaff(id);
    loadStaff();
  };

  const openHours = async (s: StaffDto) => {
    setHoursFor(s);
    setSavedMsg(null);
    const w = await adminGetAvailability(s.id);
    setWindows(w.map((x) => ({ dayOfWeek: x.dayOfWeek, startTime: x.startTime.slice(0, 5), endTime: x.endTime.slice(0, 5) })));
  };

  const addWindow = () =>
    setWindows((w) => [...w, { dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00' }]);

  const updateWindow = (i: number, patch: Partial<AvailabilityDto>) =>
    setWindows((w) => w.map((x, idx) => (idx === i ? { ...x, ...patch } : x)));

  const removeWindow = (i: number) => setWindows((w) => w.filter((_, idx) => idx !== i));

  const saveHours = async () => {
    if (!hoursFor) return;
    setError(null);
    try {
      await adminSetAvailability(hoursFor.id, windows);
      setSavedMsg('Working hours saved.');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save hours.');
    }
  };

  const serviceName = (id: number) => services.find((s) => s.id === id)?.name ?? `#${id}`;

  return (
    <div>
      <h1>Stylists</h1>
      {error && <div className="alert error">{error}</div>}

      <div className="card">
        <h2>{form.id ? 'Edit stylist' : 'New stylist'}</h2>
        <form onSubmit={submit}>
          <div className="field-row">
            <div>
              <label>Name</label>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div>
              <label>Email</label>
              <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </div>
          </div>
          <label>Services offered</label>
          <div className="option-grid" style={{ marginBottom: '0.8rem' }}>
            {services.map((s) => (
              <label key={s.id} className={`option ${form.serviceIds.includes(s.id) ? 'selected' : ''}`}>
                <input
                  type="checkbox"
                  style={{ width: 'auto', marginRight: '0.5rem' }}
                  checked={form.serviceIds.includes(s.id)}
                  onChange={() => toggleService(s.id)}
                />
                {s.name}
              </label>
            ))}
          </div>
          <label>
            <input
              type="checkbox"
              style={{ width: 'auto', marginRight: '0.5rem' }}
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Active
          </label>
          <div className="row-actions" style={{ marginTop: '0.5rem' }}>
            <button className="btn" type="submit">
              {form.id ? 'Save changes' : 'Create stylist'}
            </button>
            {form.id && (
              <button type="button" className="btn-secondary" onClick={() => setForm(EMPTY)}>
                Cancel
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Services</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {staff.map((s) => (
              <tr key={s.id}>
                <td>{s.name}</td>
                <td className="muted" style={{ fontSize: '0.85rem' }}>
                  {s.serviceIds.map(serviceName).join(', ') || '—'}
                </td>
                <td>{s.active ? <span className="badge COMPLETED">Active</span> : <span className="badge CANCELLED">Inactive</span>}</td>
                <td>
                  <div className="row-actions">
                    <button className="link-btn" onClick={() => edit(s)}>
                      Edit
                    </button>
                    <button className="link-btn" onClick={() => openHours(s)}>
                      Hours
                    </button>
                    {s.active && (
                      <button className="btn-danger" onClick={() => deactivate(s.id)}>
                        Deactivate
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {hoursFor && (
        <div className="card">
          <h2>Working hours · {hoursFor.name}</h2>
          {savedMsg && <div className="alert success">{savedMsg}</div>}
          {windows.length === 0 && <p className="muted">No hours set. Add a window below.</p>}
          {windows.map((w, i) => (
            <div className="field-row" key={i} style={{ alignItems: 'flex-end' }}>
              <div>
                <label>Day</label>
                <select value={w.dayOfWeek} onChange={(e) => updateWindow(i, { dayOfWeek: e.target.value })}>
                  {DAYS.map((d) => (
                    <option key={d} value={d}>
                      {d.charAt(0) + d.slice(1).toLowerCase()}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label>Start</label>
                <input type="time" value={w.startTime} onChange={(e) => updateWindow(i, { startTime: e.target.value })} />
              </div>
              <div>
                <label>End</label>
                <input type="time" value={w.endTime} onChange={(e) => updateWindow(i, { endTime: e.target.value })} />
              </div>
              <div style={{ flex: '0 0 auto' }}>
                <button className="btn-danger" onClick={() => removeWindow(i)}>
                  Remove
                </button>
              </div>
            </div>
          ))}
          <div className="row-actions" style={{ marginTop: '0.5rem' }}>
            <button className="btn-secondary" onClick={addWindow}>
              + Add window
            </button>
            <button className="btn" onClick={saveHours}>
              Save hours
            </button>
            <button className="link-btn" onClick={() => setHoursFor(null)}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
