import React, { useState } from 'react';
import { api } from '../services/api';
import { Shield, Mail, Lock, Zap } from 'lucide-react';

interface LoginProps {
  onLoginSuccess: (user: any) => void;
  onNavigateToRegister: () => void;
}

export const Login: React.FC<LoginProps> = ({ onLoginSuccess, onNavigateToRegister }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) {
      setError('Please fill in all fields');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      const data = await api.auth.login({ username, password });
      localStorage.setItem('chargeflow_token', data.token || '');
      localStorage.setItem('chargeflow_user', JSON.stringify(data));
      onLoginSuccess(data);
    } catch (err: any) {
      setError(err.message || 'Invalid username or password');
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
      <div className="glass-card" style={{ width: '100%', maxWidth: '420px', padding: '40px 30px' }}>
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
          <h2 style={{ fontSize: '28px', color: '#fff', marginBottom: '8px' }}>ChargeFlow</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>AI-Powered EV Charging Grid Balancer</p>
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

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '20px' }}>
            <label className="glass-label">Username</label>
            <div style={{ position: 'relative' }}>
              <Shield size={18} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '14px' }} />
              <input
                type="text"
                className="glass-input"
                placeholder="Enter your username"
                style={{ paddingLeft: '45px' }}
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                disabled={loading}
              />
            </div>
          </div>

          <div style={{ marginBottom: '28px' }}>
            <label className="glass-label">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={18} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '14px' }} />
              <input
                type="password"
                className="glass-input"
                placeholder="••••••••"
                style={{ paddingLeft: '45px' }}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
              />
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginBottom: '20px' }}
            disabled={loading}
          >
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>

        <div style={{ textAlign: 'center', fontSize: '14px', color: 'var(--text-secondary)' }}>
          Don't have an account?{' '}
          <span
            onClick={onNavigateToRegister}
            style={{ color: 'var(--color-primary)', cursor: 'pointer', fontWeight: 600 }}
          >
            Register Here
          </span>
        </div>
      </div>
    </div>
  );
};
