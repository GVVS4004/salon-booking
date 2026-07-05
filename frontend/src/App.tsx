import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { useAuth } from './auth/AuthContext';
import { BookingPage } from './pages/BookingPage';
import { MyBookingsPage } from './pages/MyBookingsPage';
import { AdminLoginPage } from './pages/admin/AdminLoginPage';
import { AdminSchedulePage } from './pages/admin/AdminSchedulePage';
import { AdminServicesPage } from './pages/admin/AdminServicesPage';
import { AdminStaffPage } from './pages/admin/AdminStaffPage';
import type { ReactElement } from 'react';

function RequireAdmin({ children }: { children: ReactElement }) {
  const { isAdmin } = useAuth();
  return isAdmin ? children : <Navigate to="/admin/login" replace />;
}

function RequireCustomer({ children }: { children: ReactElement }) {
  const { isCustomer } = useAuth();
  return isCustomer ? children : <Navigate to="/" replace />;
}

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<BookingPage />} />
        <Route
          path="/my-bookings"
          element={
            <RequireCustomer>
              <MyBookingsPage />
            </RequireCustomer>
          }
        />
        <Route path="/admin/login" element={<AdminLoginPage />} />
        <Route
          path="/admin"
          element={
            <RequireAdmin>
              <AdminSchedulePage />
            </RequireAdmin>
          }
        />
        <Route
          path="/admin/services"
          element={
            <RequireAdmin>
              <AdminServicesPage />
            </RequireAdmin>
          }
        />
        <Route
          path="/admin/staff"
          element={
            <RequireAdmin>
              <AdminStaffPage />
            </RequireAdmin>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  );
}
