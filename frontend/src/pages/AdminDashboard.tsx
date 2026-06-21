import React, { useState, useEffect } from 'react';
import { api, ChargingStation, UserProfile, API_BASE_URL } from '../services/api';
import { Settings, Users, PlusCircle, Trash, RefreshCw, Cpu, Database, ToggleRight } from 'lucide-react';

export const AdminDashboard: React.FC = () => {
  const [users, setUsers] = useState<any[]>([]);
  const [stations, setStations] = useState<ChargingStation[]>([]);
  
  // Settings state
  const [routingFactor, setRoutingFactor] = useState(1.0);
  const [autoBalance, setAutoBalance] = useState(true);
  const [sysMetrics, setSysMetrics] = useState<any>(null);

  // New Station form state
  const [showAddStation, setShowAddStation] = useState(false);
  const [name, setName] = useState('');
  const [lat, setLat] = useState(37.55);
  const [lng, setLng] = useState(126.98);
  const [chargers, setChargers] = useState(6);
  const [avgDuration, setAvgDuration] = useState(45);
  const [basePrice, setBasePrice] = useState(0.35);

  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setError(null);
      
      const userList = await api.auth.getProfile().then(() => 
        // Direct admin API call for user list
        fetch(`${API_BASE_URL}/admin/users`, {
          headers: { 'Authorization': `Bearer ${localStorage.getItem('chargeflow_token')}` }
        }).then(res => res.json())
      );
      setUsers(userList);

      const stationList = await api.stations.list();
      setStations(stationList);

      const settings = await fetch(`${API_BASE_URL}/admin/settings`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('chargeflow_token')}` }
      }).then(res => res.json());

      setRoutingFactor(settings.routingOptimizationFactor);
      setAutoBalance(settings.autoLoadBalancingEnabled);
      setSysMetrics(settings);
    } catch (err: any) {
      console.error('Failed to load admin logs:', err);
      setError(err.message || 'Failed to fetch admin system data.');
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 6000);
    return () => clearInterval(interval);
  }, []);

  const handleSaveSettings = async () => {
    try {
      setError(null);
      await fetch(`${API_BASE_URL}/admin/settings`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('chargeflow_token')}`
        },
        body: JSON.stringify({
          routingOptimizationFactor: routingFactor,
          autoLoadBalancingEnabled: autoBalance
        })
      });
      setSuccess('Optimization parameters deployed to routing engines.');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err: any) {
      setError(err.message || 'Failed to apply parameters.');
    }
  };

  const handleAddStation = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name) return;

    try {
      setError(null);
      await api.stations.create({
        name,
        latitude: lat,
        longitude: lng,
        totalChargers: chargers,
        averageChargingDurationMinutes: avgDuration,
        dynamicPricingPerKwh: basePrice,
      });

      setShowAddStation(false);
      setName('');
      setSuccess('New charging station node provisioned successfully!');
      setTimeout(() => setSuccess(null), 3000);
      fetchData();
    } catch (err: any) {
      setError(err.message || 'Failed to provision station.');
    }
  };

  const handleDeleteStation = async (id: number) => {
    if (!window.confirm('Are you sure you want to decommission this charging station?')) return;
    try {
      setError(null);
      await api.stations.delete(id);
      setSuccess('Station node decommissioned.');
      setTimeout(() => setSuccess(null), 3000);
      fetchData();
    } catch (err: any) {
      setError(err.message || 'Failed to decommission station.');
    }
  };

  const handleDeleteUser = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this user profile?')) return;
    try {
      setError(null);
      await fetch(`${API_BASE_URL}/admin/users/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${localStorage.getItem('chargeflow_token')}` }
      });
      setSuccess('User profile deleted.');
      setTimeout(() => setSuccess(null), 3000);
      fetchData();
    } catch (err: any) {
      setError(err.message || 'Failed to delete user.');
    }
  };

  return (
    <div className="page-container" style={{ maxWidth: '1400px', margin: '0 auto' }}>
      
      {/* Messages */}
      {error && (
        <div style={{
          background: 'rgba(255, 23, 68, 0.1)',
          border: '1px solid rgba(255, 23, 68, 0.3)',
          borderRadius: '8px',
          color: 'var(--color-danger)',
          padding: '16px',
          fontSize: '14px',
          marginBottom: '20px'
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
          padding: '16px',
          fontSize: '14px',
          marginBottom: '20px'
        }}>
          {success}
        </div>
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontFamily: 'var(--font-family-title)', fontSize: '28px', color: '#fff' }}>Admin Console</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>Global optimization constants, infrastructure deployments & credentials</p>
        </div>
        <button onClick={fetchData} className="btn btn-secondary" style={{ padding: '8px 16px' }}>
          <RefreshCw size={16} /> Sync Configuration
        </button>
      </div>

      <div className="dashboard-grid">
        
        {/* Left Column: Station Provisioning & User Directories */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Station Provisioner */}
          <div className="glass-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h3 style={{ fontSize: '18px', color: '#fff', fontFamily: 'Outfit', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <PlusCircle size={20} color="var(--color-primary)" /> Grid Node Deployments
              </h3>
              <button onClick={() => setShowAddStation(!showAddStation)} className="btn btn-primary" style={{ padding: '8px 16px', fontSize: '13px' }}>
                Deploy Node
              </button>
            </div>

            {showAddStation ? (
              <form onSubmit={handleAddStation} style={{ background: 'rgba(255,255,255,0.01)', border: '1px solid var(--border-color)', borderRadius: '12px', padding: '20px', marginBottom: '20px' }}>
                <div style={{ marginBottom: '12px' }}>
                  <label className="glass-label">Station Node Name</label>
                  <input type="text" className="glass-input" placeholder="e.g. Incheon Express Charger" value={name} onChange={e => setName(e.target.value)} required />
                </div>
                
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '12px' }}>
                  <div>
                    <label className="glass-label">Latitude</label>
                    <input type="number" step="0.0001" className="glass-input" value={lat} onChange={e => setLat(Number(e.target.value))} required />
                  </div>
                  <div>
                    <label className="glass-label">Longitude</label>
                    <input type="number" step="0.0001" className="glass-input" value={lng} onChange={e => setLng(Number(e.target.value))} required />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '12px', marginBottom: '16px' }}>
                  <div>
                    <label className="glass-label">Total Chargers</label>
                    <input type="number" className="glass-input" value={chargers} onChange={e => setChargers(Number(e.target.value))} required />
                  </div>
                  <div>
                    <label className="glass-label">Avg Duration (min)</label>
                    <input type="number" className="glass-input" value={avgDuration} onChange={e => setAvgDuration(Number(e.target.value))} required />
                  </div>
                  <div>
                    <label className="glass-label">Base Cost ($)</label>
                    <input type="number" step="0.01" className="glass-input" value={basePrice} onChange={e => setBasePrice(Number(e.target.value))} required />
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '8px' }}>
                  <button type="submit" className="btn btn-primary">Provision Node</button>
                  <button type="button" onClick={() => setShowAddStation(false)} className="btn btn-secondary">Cancel</button>
                </div>
              </form>
            ) : null}

            <div className="glass-table-container">
              <table className="glass-table">
                <thead>
                  <tr>
                    <th>Node Station</th>
                    <th>Location</th>
                    <th>Chargers</th>
                    <th>Base Price</th>
                    <th style={{ textAlign: 'center' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {stations.map(station => (
                    <tr key={station.id}>
                      <td>
                        <strong style={{ color: '#fff' }}>{station.name}</strong>
                      </td>
                      <td>{station.latitude.toFixed(4)}, {station.longitude.toFixed(4)}</td>
                      <td>{station.totalChargers} bays</td>
                      <td>${station.dynamicPricingPerKwh.toFixed(2)}/kWh</td>
                      <td style={{ textAlign: 'center' }}>
                        <button onClick={() => handleDeleteStation(station.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--color-danger)' }}>
                          <Trash size={16} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* User Directory Manager */}
          <div className="glass-card">
            <h3 style={{ fontSize: '18px', color: '#fff', marginBottom: '16px', fontFamily: 'Outfit', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Users size={20} color="var(--color-secondary)" /> System User Directory
            </h3>

            <div className="glass-table-container">
              <table className="glass-table">
                <thead>
                  <tr>
                    <th>Username</th>
                    <th>Email Address</th>
                    <th>Security Role</th>
                    <th style={{ textAlign: 'center' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(u => (
                    <tr key={u.id}>
                      <td><strong style={{ color: '#fff' }}>{u.username}</strong></td>
                      <td>{u.email}</td>
                      <td>
                        <span className={`badge ${u.role === 'ROLE_ADMIN' ? 'badge-info' : u.role === 'ROLE_OPERATOR' ? 'badge-mod' : 'badge-low'}`}>
                          {u.role.replace('ROLE_', '')}
                        </span>
                      </td>
                      <td style={{ textAlign: 'center' }}>
                        <button
                          onClick={() => handleDeleteUser(u.id)}
                          style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--color-danger)' }}
                          disabled={u.username === 'admin'}
                          className={u.username === 'admin' ? 'btn-disabled' : ''}
                        >
                          <Trash size={16} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

        </div>

        {/* Right Column: Settings Panel & Hardware Diagnostics */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Optimization parameters */}
          <div className="glass-card">
            <h3 style={{ fontSize: '18px', color: '#fff', marginBottom: '20px', fontFamily: 'Outfit', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Settings size={20} color="var(--color-primary)" /> Smart Routing Weights
            </h3>

            <div style={{ marginBottom: '20px' }}>
              <label className="glass-label" style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>Travel vs Wait Ratio</span>
                <strong>{routingFactor}x</strong>
              </label>
              <input
                type="range"
                min="0.2"
                max="2.5"
                step="0.1"
                className="glass-input"
                style={{ width: '100%', height: '6px', appearance: 'none', background: 'rgba(255,255,255,0.08)', borderRadius: '3px', cursor: 'pointer' }}
                value={routingFactor}
                onChange={e => setRoutingFactor(Number(e.target.value))}
              />
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '10px', color: 'var(--text-muted)', marginTop: '6px' }}>
                <span>Prioritize Travel Time (0.2x)</span>
                <span>Prioritize Wait (2.5x)</span>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', background: 'rgba(0,0,0,0.15)', padding: '16px', borderRadius: '10px', border: '1px solid var(--border-color)' }}>
              <div>
                <strong style={{ display: 'block', fontSize: '14px', color: '#fff' }}>Auto load-balancing</strong>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>Triggers incentive offers during load spikes</span>
              </div>
              <input
                type="checkbox"
                checked={autoBalance}
                onChange={e => setAutoBalance(e.target.checked)}
                style={{
                  width: '40px',
                  height: '20px',
                  appearance: 'none',
                  background: autoBalance ? 'var(--color-primary)' : 'rgba(255,255,255,0.05)',
                  border: '1px solid var(--border-color)',
                  borderRadius: '10px',
                  cursor: 'pointer',
                  position: 'relative',
                  outline: 'none',
                  transition: 'all 0.3s ease'
                }}
              />
            </div>

            <button onClick={handleSaveSettings} className="btn btn-primary" style={{ width: '100%' }}>
              Save Settings
            </button>
          </div>

          {/* Grid Engine Diagnostics */}
          <div className="glass-card">
            <h3 style={{ fontSize: '18px', color: '#fff', marginBottom: '16px', fontFamily: 'Outfit', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Cpu size={20} color="var(--color-secondary)" /> Hardware Diagnostics
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', marginBottom: '6px' }}>
                  <span style={{ color: 'var(--text-secondary)' }}>CPU Core Temperature</span>
                  <strong style={{ color: '#fff' }}>{sysMetrics ? `${sysMetrics.systemCpuUsagePercentage}%` : '24%'}</strong>
                </div>
                <div style={{ height: '6px', background: 'rgba(255,255,255,0.05)', borderRadius: '3px', overflow: 'hidden' }}>
                  <div style={{
                    width: sysMetrics ? `${sysMetrics.systemCpuUsagePercentage}%` : '24%',
                    height: '100%',
                    background: 'var(--color-primary)',
                    borderRadius: '3px'
                  }}></div>
                </div>
              </div>

              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', marginBottom: '6px' }}>
                  <span style={{ color: 'var(--text-secondary)' }}>Virtual Memory Usage</span>
                  <strong style={{ color: '#fff' }}>{sysMetrics ? `${sysMetrics.systemMemoryUsagePercentage}%` : '42%'}</strong>
                </div>
                <div style={{ height: '6px', background: 'rgba(255,255,255,0.05)', borderRadius: '3px', overflow: 'hidden' }}>
                  <div style={{
                    width: sysMetrics ? `${sysMetrics.systemMemoryUsagePercentage}%` : '42%',
                    height: '100%',
                    background: 'var(--color-secondary)',
                    borderRadius: '3px'
                  }}></div>
                </div>
              </div>

              <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '14px', marginTop: '6px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div style={{ background: 'rgba(0,0,0,0.1)', padding: '12px', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-secondary)' }}>DATABASE STATUS</span>
                  <strong style={{ color: 'var(--color-success)', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '4px', marginTop: '4px' }}>
                    <Database size={12} /> {sysMetrics ? sysMetrics.databaseStatus : 'CONNECTED'}
                  </strong>
                </div>
                
                <div style={{ background: 'rgba(0,0,0,0.1)', padding: '12px', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-secondary)' }}>REDIS CACHE</span>
                  <strong style={{ color: 'var(--color-success)', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '4px', marginTop: '4px' }}>
                    <Database size={12} /> SYNCED
                  </strong>
                </div>
              </div>

            </div>
          </div>

        </div>

      </div>
    </div>
  );
};
