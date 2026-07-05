import { type ReactNode } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function Layout({ children }: { children: ReactNode }) {
  const { user, isAdmin, isCustomer, logout } = useAuth();
  const navigate = useNavigate();

  const onLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className="app">
      <header className="topbar">
        <Link to="/" className="brand">
          ✂️ Luxe Salon
        </Link>
        <nav>
          <Link to="/">Book</Link>
          {isCustomer && <Link to="/my-bookings">My bookings</Link>}
          {isAdmin && (
            <>
              <Link to="/admin">Schedule</Link>
              <Link to="/admin/services">Services</Link>
              <Link to="/admin/staff">Stylists</Link>
            </>
          )}
          {user ? (
            <>
              <span className="who">{user.name}</span>
              <button className="link-btn" onClick={onLogout}>
                Sign out
              </button>
            </>
          ) : (
            <Link to="/admin/login">Admin</Link>
          )}
        </nav>
      </header>
      <main className="content">{children}</main>
      <footer className="footer">Luxe Salon · Booking demo</footer>
    </div>
  );
}
