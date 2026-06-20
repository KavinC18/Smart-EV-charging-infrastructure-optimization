import React, { useState, useEffect } from 'react';
import { api, UserProfile, SimulationStatus } from './services/api';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { DriverDashboard } from './pages/DriverDashboard';
import { OperatorDashboard } from './pages/OperatorDashboard';
import { AdminDashboard } from './pages/AdminDashboard';
import { Zap, LogOut, Shield, MapPin, BarChart2, Laptop, Play, Pause, FastForward, Activity } from 'lucide-react';
import './App.css';

function App() {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [authView, setAuthView] = useState<'login' | 'register'>('login');
  const [activeTab, setActiveTab] = useState<'driver' | 'operator' | 'admin'>('driver');
  const [simStatus, setSimStatus] = useState<SimulationStatus | null>(null);

  // Load user session on startup
  useEffect(() => {
    const cachedUser = localStorage.getItem('chargeflow_user');
    const token = localStorage.getItem('chargeflow_token');
    
    if (cachedUser && token) {
      const parsed = JSON.parse(cachedUser);
      setUser(parsed);
      
      // Select appropriate default tab based on role
      if (parsed.role === 'ROLE_ADMIN') {
        setActiveTab('admin');
      } else if (parsed.role === 'ROLE_OPERATOR') {
        setActiveTab('operator');
      } else {
        setActiveTab('driver');
      }
    }
  }, []);

  // Fetch simulation status periodically
  const fetchSimStatus = async () => {
    try {
      const status = await api.simulation.getStatus();
      setSimStatus(status);
    } catch (err) {
      console.error('Failed to sync simulation state:', err);
    }
  };

  useEffect(() => {
    fetchSimStatus();
    const interval = setInterval(fetchSimStatus, 4000);
    return () => clearInterval(interval);
  }, []);

  const handleLoginSuccess = (profile: UserProfile) => {
    setUser(profile);
    if (profile.role === 'ROLE_ADMIN') {
      setActiveTab('admin');
    } else if (profile.role === 'ROLE_OPERATOR') {
      setActiveTab('operator');
    } else {
      setActiveTab('driver');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('chargeflow_token');
    localStorage.removeItem('chargeflow_user');
    setUser(null);
    setAuthView('login');
  };

  // Simulation controls
  const handleToggleSim = async () => {
    try {
      const isRunning = await api.simulation.toggle();
      if (simStatus) {
        setSimStatus({ ...simStatus, running: isRunning });
      }
    } catch (err) {
      alert('Error triggering simulation state');
    }
  };

  const handleScenarioChange = async (scenario: string) => {
    try {
      const updated = await api.simulation.setScenario(scenario);
      if (simStatus) {
        setSimStatus({ ...simStatus, scenario: updated });
      }
    } catch (err) {
      alert('Error updating scenario');
    }
  };

  const handleSpeedChange = async (speed: number) => {
    try {
      const updated = await api.simulation.setSpeed(speed);
      if (simStatus) {
        setSimStatus({ ...simStatus, speed: updated });
      }
    } catch (err) {
      alert('Error updating simulation speed');
    }
  };

  const handleResetGrid = async () => {
    if (!window.confirm('Reset grid simulation to morning defaults? This deletes historical logs.')) return;
    try {
      await api.simulation.reset();
      fetchSimStatus();
      alert('Grid simulation reset successfully.');
      window.location.reload();
    } catch (err) {
      alert('Error resetting grid');
    }
  };

  // If user is not logged in, show auth screens
  if (!user) {
    if (authView === 'register') {
      return (
        <Register
          onRegisterSuccess={() => setAuthView('login')}
          onNavigateToLogin={() => setAuthView('login')}
        />
      );
    }
    return (
      <Login
        onLoginSuccess={handleLoginSuccess}
        onNavigateToRegister={() => setAuthView('register')}
      />
    );
  }

  return (
    <div className="app-container">
      
      {/* Dynamic Global Simulation Banner */}
      <div style={{
        background: 'rgba(2, 6, 23, 0.95)',
        borderBottom: '1px solid var(--border-color)',
        padding: '10px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '12px',
        zIndex: 110
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Activity size={16} color="var(--color-primary)" className="glow-active" style={{ borderRadius: '50%' }} />
          <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Grid Simulation:
          </span>
          <span className={`badge ${simStatus?.running ? 'badge-low' : 'badge-high'}`} style={{ fontSize: '10px' }}>
            {simStatus?.running ? 'LIVE RUNNING' : 'PAUSED'}
          </span>
        </div>

        {/* Simulator controls */}
        {simStatus && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', flexWrap: 'wrap' }}>
            {/* Clock */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px' }}>
              <span style={{ color: 'var(--text-muted)' }}>Sim Time:</span>
              <strong style={{ color: '#fff', fontSize: '15px', fontFamily: 'Outfit' }}>{simStatus.simulatedTime}</strong>
            </div>

            {/* Play/Pause Button */}
            <button
              onClick={handleToggleSim}
              className="btn btn-secondary"
              style={{ padding: '6px 12px', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}
            >
              {simStatus.running ? <Pause size={12} /> : <Play size={12} />}
              {simStatus.running ? 'Pause' : 'Resume'}
            </button>

            {/* Speed selection */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Speed:</span>
              <select
                className="glass-input"
                style={{ padding: '4px 8px', fontSize: '12px', width: '70px', height: '28px', background: 'rgba(16,22,35,0.9)' }}
                value={simStatus.speed}
                onChange={(e) => handleSpeedChange(Number(e.target.value))}
              >
                <option value="12">1x</option>
                <option value="60">5x</option>
                <option value="120">10x</option>
              </select>
            </div>

            {/* Scenario Selection */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Scenario:</span>
              <select
                className="glass-input"
                style={{ padding: '4px 8px', fontSize: '12px', width: '130px', height: '28px', background: 'rgba(16,22,35,0.9)' }}
                value={simStatus.scenario}
                onChange={(e) => handleScenarioChange(e.target.value)}
              >
                <option value="NORMAL">Normal Load</option>
                <option value="MORNING_PEAK">Morning Peak (8-10 AM)</option>
                <option value="EVENING_PEAK">Evening Peak (5-7 PM)</option>
                <option value="WEEKEND_LOAD">Weekend Load</option>
                <option value="OUTAGE">Mapo Outage Out</option>
              </select>
            </div>

            {/* Reset Grid */}
            <button
              onClick={handleResetGrid}
              className="btn btn-secondary"
              style={{ padding: '6px 12px', fontSize: '12px', color: 'var(--color-danger)', borderColor: 'rgba(255,23,68,0.2)' }}
            >
              Reset Simulator
            </button>
          </div>
        )}
      </div>

      {/* Main Navbar */}
      <nav className="navbar">
        <div className="nav-brand">
          <Zap size={22} fill="var(--color-primary)" stroke="var(--color-primary)" />
          <span>ChargeFlow</span>
        </div>

        <div className="nav-links">
          {/* Active profile card */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginRight: '16px' }}>
            <div style={{
              width: '32px',
              height: '32px',
              borderRadius: '50%',
              background: 'linear-gradient(135deg, var(--color-primary), var(--color-secondary))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#040814',
              fontWeight: 800,
              fontSize: '14px',
              fontFamily: 'Outfit'
            }}>
              {user.username.charAt(0).toUpperCase()}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span style={{ fontSize: '13px', fontWeight: 600, color: '#fff' }}>{user.username}</span>
              <span style={{ fontSize: '10px', color: 'var(--text-secondary)' }}>{user.role.replace('ROLE_', '')}</span>
            </div>
          </div>

          <button onClick={handleLogout} className="btn btn-secondary" style={{ padding: '8px 16px', fontSize: '13px', gap: '6px' }}>
            <LogOut size={14} /> Sign Out
          </button>
        </div>
      </nav>

      {/* Navigation Layout with Sidebar + Viewports */}
      <div className="main-content">
        <aside className="sidebar">
          <div className="sidebar-menu">
            {/* Always visible to drivers, operators, and admins */}
            <div
              className={`sidebar-item ${activeTab === 'driver' ? 'active' : ''}`}
              onClick={() => setActiveTab('driver')}
            >
              <MapPin size={18} />
              <span>EV Driver Console</span>
            </div>

            {/* Visible to Operators and Admins */}
            {(user.role === 'ROLE_OPERATOR' || user.role === 'ROLE_ADMIN') && (
              <div
                className={`sidebar-item ${activeTab === 'operator' ? 'active' : ''}`}
                onClick={() => setActiveTab('operator')}
              >
                <BarChart2 size={18} />
                <span>Station Operator Hub</span>
              </div>
            )}

            {/* Only visible to Admins */}
            {user.role === 'ROLE_ADMIN' && (
              <div
                className={`sidebar-item ${activeTab === 'admin' ? 'active' : ''}`}
                onClick={() => setActiveTab('admin')}
              >
                <Shield size={18} />
                <span>Grid Administrator</span>
              </div>
            )}
          </div>
          
          <div style={{ marginTop: 'auto', padding: '16px', background: 'rgba(0,0,0,0.15)', borderRadius: '10px', border: '1px solid var(--border-color)', fontSize: '11px', color: 'var(--text-muted)' }}>
            <strong>Grid balancer active</strong><br />
            Erlang-C load predictions computed dynamically.
          </div>
        </aside>

        {/* Dynamic Panel Mounting */}
        <main style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          {activeTab === 'driver' && <DriverDashboard />}
          {activeTab === 'operator' && <OperatorDashboard />}
          {activeTab === 'admin' && <AdminDashboard />}
        </main>
      </div>

    </div>
  );
}

export default App;
