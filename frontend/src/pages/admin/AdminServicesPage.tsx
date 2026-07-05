import { useEffect, useState, type FormEvent } from 'react';
import {
  adminCreateService,
  adminDeleteService,
  adminGetServices,
  adminUpdateService,
} from '../../api/client';
import type { ServiceDto } from '../../api/types';
import { formatDuration, formatPrice } from '../../utils/format';

interface FormState {
  id?: number;
  name: string;
  description: string;
  durationMinutes: number;
  priceDollars: number;
  active: boolean;
}

const EMPTY: FormState = { name: '', description: '', durationMinutes: 30, priceDollars: 30, active: true };

export function AdminServicesPage() {
  const [services, setServices] = useState<ServiceDto[]>([]);
  const [form, setForm] = useState<FormState>(EMPTY);
  const [error, setError] = useState<string | null>(null);

  const load = () => adminGetServices().then(setServices).catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, []);

  const edit = (s: ServiceDto) =>
    setForm({
      id: s.id,
      name: s.name,
      description: s.description ?? '',
      durationMinutes: s.durationMinutes,
      priceDollars: s.priceCents / 100,
      active: s.active,
    });

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    const body = {
      name: form.name,
      description: form.description,
      durationMinutes: Number(form.durationMinutes),
      priceCents: Math.round(Number(form.priceDollars) * 100),
      active: form.active,
    };
    try {
      if (form.id) await adminUpdateService(form.id, body);
      else await adminCreateService(body);
      setForm(EMPTY);
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed.');
    }
  };

  const deactivate = async (id: number) => {
    if (!confirm('Deactivate this service?')) return;
    await adminDeleteService(id);
    load();
  };

  return (
    <div>
      <h1>Services</h1>
      {error && <div className="alert error">{error}</div>}

      <div className="card">
        <h2>{form.id ? 'Edit service' : 'New service'}</h2>
        <form onSubmit={submit}>
          <label>Name</label>
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <label>Description</label>
          <input
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
          <div className="field-row">
            <div>
              <label>Duration (minutes)</label>
              <input
                type="number"
                min={1}
                value={form.durationMinutes}
                onChange={(e) => setForm({ ...form, durationMinutes: Number(e.target.value) })}
                required
              />
            </div>
            <div>
              <label>Price (USD)</label>
              <input
                type="number"
                min={0}
                step="0.01"
                value={form.priceDollars}
                onChange={(e) => setForm({ ...form, priceDollars: Number(e.target.value) })}
                required
              />
            </div>
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
              {form.id ? 'Save changes' : 'Create service'}
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
              <th>Duration</th>
              <th>Price</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {services.map((s) => (
              <tr key={s.id}>
                <td>{s.name}</td>
                <td>{formatDuration(s.durationMinutes)}</td>
                <td>{formatPrice(s.priceCents)}</td>
                <td>{s.active ? <span className="badge COMPLETED">Active</span> : <span className="badge CANCELLED">Inactive</span>}</td>
                <td>
                  <div className="row-actions">
                    <button className="link-btn" onClick={() => edit(s)}>
                      Edit
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
    </div>
  );
}
