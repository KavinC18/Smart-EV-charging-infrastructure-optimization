import React, { useState } from 'react';
import { api } from '../services/api';
import { Shield, Mail, Lock, UserCheck, Zap } from 'lucide-react';

interface RegisterProps {
  onRegisterSuccess: () => void;
  onNavigateToLogin: () => void;
}

export const Register: React.FC<RegisterProps> = ({ onRegisterSuccess, onNavigateToLogin }) => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('ROLE_DRIVER');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !email || !password || !role) {
      setError('Please fill in all fields');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      await api.auth.register({ username, email, password, role });
      setSuccess(true);
      setTimeout(() => {
        onRegisterSuccess();
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Registration failed. Try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      padding: '20px',
      background: 'radial-gradient(circle at center, #0f172a 0%, #020617 100%)'
    }}>
      <div className="glass-card" style={{ width: '100%', maxWidth: '440px', padding: '40px 30px' }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div className="glow-active" style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '64px',
            height: '64px',
            borderRadius: '16px',
            background: 'linear-gradient(135deg, var(--color-primary), var(--color-secondary))',
            marginBottom: '16px'
          }}>
            <Zap size={32} color="#040814" />
          </div>
          <h2 style={{ fontSize: '28px', color: '#fff', marginBottom: '8px' }}>Join ChargeFlow</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>Register to balance and manage the EV Grid</p>
        </div>

        {error && (
          <div style={{
            background: 'rgba(255, 23, 68, 0.1)',
            border: '1px solid rgba(255, 23, 68, 0.3)',
            borderRadius: '8px',
            color: 'var(--color-danger)',
            padding: '12px',
            fontSize: '14px',
            marginBottom: '20px',
            textAlign: 'center'
          }}>
            {error}
          </div>
        )}

        {success && (
          <div style={{
            background: 'rgba(0, 230, 118, 0.1)',
            border: '1px solid rgba(0, 230, 118, 0.3)',
            borderRadius: '8px',
            color: 'var(--color-success)',
            padding: '12px',
            fontSize: '14px',
            marginBottom: '20px',
            textAlign: 'center'
          }}>
            Account created successfully! Redirecting to login...
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '20px' }}>
            <label className="glass-label">Username</label>
            <div style={{ position: 'relative' }}>
              <Shield size={18} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '14px' }} />
              <input
                type="text"
                className="glass-input"
                placeholder="driver_kavin"
                style={{ paddingLeft: '45px' }}
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                disabled={loading || success}
              />
            </div>
          </div>

          <div style={{ marginBottom: '20px' }}>
            <label className="glass-label">Email address</label>
            <div style={{ position: 'relative' }}>
              <Mail size={18} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '14px' }} />
              <input
                type="email"
                className="glass-input"
                placeholder="kavin@chargeflow.io"
                style={{ paddingLeft: '45px' }}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={loading || success}
              />
            </div>
          </div>

          <div style={{ marginBottom: '20px' }}>
            <label className="glass-label">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={18} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '14px' }} />
              <input
                type="password"
                className="glass-input"
                placeholder="At least 6 characters"
                style={{ paddingLeft: '45px' }}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading || success}
              />
            </div>
          </div>

          <div style={{ marginBottom: '28px' }}>
            <label className="glass-label">Select Account Type</label>
            <div style={{ position: 'relative' }}>
              <UserCheck size={18} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '14px' }} />
              <select
                className="glass-input"
                style={{ paddingLeft: '45px', appearance: 'none', background: 'rgba(16, 22, 35, 0.9)' }}
                value={role}
                onChange={(e) => setRole(e.target.value)}
                disabled={loading || success}
              >
                <option value="ROLE_DRIVER">EV Driver (Find & Start Charges)</option>
                <option value="ROLE_OPERATOR">Station Operator (Manage Chargers & Pricing)</option>
                <option value="ROLE_ADMIN">System Administrator (Global Dashboard & Settings)</option>
              </select>
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginBottom: '20px' }}
            disabled={loading || success}
          >
            {loading ? 'Creating Account...' : 'Register'}
          </button>
        </form>

        <div style={{ textAlign: 'center', fontSize: '14px', color: 'var(--text-secondary)' }}>
          Already have an account?{' '}
          <span
            onClick={onNavigateToLogin}
            style={{ color: 'var(--color-primary)', cursor: 'pointer', fontWeight: 600 }}
          >
            Sign In
          </span>
        </div>
      </div>
    </div>
  );
};
