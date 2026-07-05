export interface ServiceDto {
  id: number;
  name: string;
  description?: string;
  durationMinutes: number;
  priceCents: number;
  active: boolean;
}

export interface StaffDto {
  id: number;
  name: string;
  email?: string;
  active: boolean;
  serviceIds: number[];
}

export interface AvailabilityDto {
  id?: number;
  dayOfWeek: string; // MONDAY..SUNDAY
  startTime: string; // HH:mm[:ss]
  endTime: string;
}

export interface SlotDto {
  start: string; // ISO instant
  end: string;
}

export interface CustomerDto {
  id: number;
  name: string;
  email: string;
  phone?: string;
}

export interface AppointmentDto {
  id: number;
  serviceId: number;
  serviceName: string;
  durationMinutes: number;
  priceCents: number;
  staffId: number;
  staffName: string;
  customerId: number;
  customerName: string;
  customerEmail: string;
  customerPhone?: string;
  startTime: string;
  endTime: string;
  status: string;
  notes?: string;
}

export interface AuthResponse {
  token: string;
  role: string;
  name: string;
  email: string;
  customer?: CustomerDto;
}

export interface AuthConfig {
  googleEnabled: boolean;
  googleClientId?: string | null;
}

export interface BookingPayload {
  serviceId: number;
  staffId: number;
  startTime: string;
  notes?: string;
  guestName?: string;
  guestEmail?: string;
  guestPhone?: string;
}
