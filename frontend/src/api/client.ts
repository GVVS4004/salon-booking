import type {
  AppointmentDto,
  AuthConfig,
  AuthResponse,
  AvailabilityDto,
  BookingPayload,
  ServiceDto,
  StaffDto,
  SlotDto,
} from './types';

// In dev this is empty, so requests hit the Vite proxy at /api. In production (split
// hosting) set VITE_API_BASE_URL to the backend's origin, e.g. https://salon-api.onrender.com
const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

const TOKEN_KEY = 'salon.token';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body) headers.set('Content-Type', 'application/json');
  const token = getToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const res = await fetch(API_BASE + path, { ...options, headers });

  if (res.status === 204) return undefined as T;

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    const message = (data && (data.message || data.error)) || `Request failed (${res.status})`;
    throw new ApiError(res.status, message);
  }
  return data as T;
}

// ---- Auth ----
export const getAuthConfig = () => request<AuthConfig>('/api/auth/config');
export const googleLogin = (credential: string) =>
  request<AuthResponse>('/api/auth/google', { method: 'POST', body: JSON.stringify({ credential }) });
export const adminLogin = (email: string, password: string) =>
  request<AuthResponse>('/api/auth/admin/login', { method: 'POST', body: JSON.stringify({ email, password }) });

// ---- Public catalogue + booking ----
export const getServices = () => request<ServiceDto[]>('/api/services');
export const getStaff = (serviceId?: number) =>
  request<StaffDto[]>(`/api/staff${serviceId ? `?serviceId=${serviceId}` : ''}`);
export const getSlots = (staffId: number, serviceId: number, date: string) =>
  request<SlotDto[]>(`/api/availability?staffId=${staffId}&serviceId=${serviceId}&date=${date}`);
export const createBooking = (payload: BookingPayload) =>
  request<AppointmentDto>('/api/bookings', { method: 'POST', body: JSON.stringify(payload) });
export const getMyBookings = () => request<AppointmentDto[]>('/api/bookings/me');
export const cancelBooking = (id: number) =>
  request<AppointmentDto>(`/api/bookings/${id}/cancel`, { method: 'POST' });

// ---- Admin ----
export const adminGetServices = () => request<ServiceDto[]>('/api/admin/services');
export const adminCreateService = (body: Partial<ServiceDto>) =>
  request<ServiceDto>('/api/admin/services', { method: 'POST', body: JSON.stringify(body) });
export const adminUpdateService = (id: number, body: Partial<ServiceDto>) =>
  request<ServiceDto>(`/api/admin/services/${id}`, { method: 'PUT', body: JSON.stringify(body) });
export const adminDeleteService = (id: number) =>
  request<void>(`/api/admin/services/${id}`, { method: 'DELETE' });

export const adminGetStaff = () => request<StaffDto[]>('/api/admin/staff');
export const adminCreateStaff = (body: Partial<StaffDto>) =>
  request<StaffDto>('/api/admin/staff', { method: 'POST', body: JSON.stringify(body) });
export const adminUpdateStaff = (id: number, body: Partial<StaffDto>) =>
  request<StaffDto>(`/api/admin/staff/${id}`, { method: 'PUT', body: JSON.stringify(body) });
export const adminDeleteStaff = (id: number) =>
  request<void>(`/api/admin/staff/${id}`, { method: 'DELETE' });

export const adminGetAvailability = (staffId: number) =>
  request<AvailabilityDto[]>(`/api/admin/staff/${staffId}/availability`);
export const adminSetAvailability = (staffId: number, windows: AvailabilityDto[]) =>
  request<AvailabilityDto[]>(`/api/admin/staff/${staffId}/availability`, {
    method: 'PUT',
    body: JSON.stringify(windows),
  });

export const adminGetAppointments = (from: string, to: string, staffId?: number) =>
  request<AppointmentDto[]>(
    `/api/admin/appointments?from=${from}&to=${to}${staffId ? `&staffId=${staffId}` : ''}`,
  );
export const adminSetStatus = (id: number, status: string) =>
  request<AppointmentDto>(`/api/admin/appointments/${id}/status`, {
    method: 'POST',
    body: JSON.stringify({ status }),
  });
export const adminCancelAppointment = (id: number) =>
  request<AppointmentDto>(`/api/admin/appointments/${id}/cancel`, { method: 'POST' });
