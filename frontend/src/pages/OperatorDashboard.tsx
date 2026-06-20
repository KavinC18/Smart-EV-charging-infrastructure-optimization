import React, { useState, useEffect } from 'react';
import { api, ChargingStation, IncentiveCampaign, SystemAnalytics } from '../services/api';
import { DashboardCharts } from '../components/DashboardCharts';
import { Zap, Clock, Users, Percent, ToggleLeft, Edit, Plus, RefreshCw, Layers, Settings } from 'lucide-react';

export const OperatorDashboard: React.FC = () => {
  const [stations, setStations] = useState<ChargingStation[]>([]);
  const [campaigns, setCampaigns] = useState<IncentiveCampaign[]>([]);
  const [analytics, setAnalytics] = useState<SystemAnalytics | null>(null);

  // Station edits form state
  const [editingStationId, setEditingStationId] = useState<number | null>(null);
  const [editPrice, setEditPrice] = useState<number>(0.3);
  const [editStatus, setEditStatus] = useState<'ACTIVE' | 'MAINTENANCE'>('ACTIVE');
  const [editChargers, setEditChargers] = useState<number>(6);

  // Campaign create form state
  const [showAddCampaign, setShowAddCampaign] = useState(false);
  const [sourceId, setSourceId] = useState<number>(0);
  const [targetId, setTargetId] = useState<number>(0);
  const [discount, setDiscount] = useState<number>(15);
  const [threshold, setThreshold] = useState<number>(15);
  const [campaignDesc, setCampaignDesc] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setError(null);
      const stationList = await api.stations.list();
      setStations(stationList);

      const campaignList = await api.operator.listCampaigns();
      setCampaigns(campaignList);

      const stats = await api.operator.getAnalytics();
      setAnalytics(stats);

      // Set default station option in campaign form
      if (stationList.length > 1) {
        if (sourceId === 0) setSourceId(stationList[0].id);
        if (targetId === 0) setTargetId(stationList[1].id);
      }
    } catch (err: any) {
      console.error('Failed to load operator stats:', err);
      setError(err.message || 'Failed to fetch network operator statistics.');
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, []);

  const handleEditStation = (station: ChargingStation) => {
    setEditingStationId(station.id);
    setEditPrice(station.dynamicPricingPerKwh);
    setEditStatus(station.status);
    setEditChargers(station.totalChargers);
  };

  const handleSaveStation = async (id: number) => {
    try {
      setError(null);
      const orig = stations.find(s => s.id === id);
      if (!orig) return;

      await api.stations.update(id, {
        ...orig,
        dynamicPricingPerKwh: editPrice,
        status: editStatus,
        totalChargers: editChargers,
      });

      setEditingStationId(null);
      setSuccess('Station parameters updated successfully!');
      setTimeout(() => setSuccess(null), 3000);
      fetchData();
    } catch (err: any) {
      setError(err.message || 'Failed to update station parameters.');
    }
  };

  const handleCreateCampaign = async (e: React.FormEvent) => {
    e.preventDefault();
    if (sourceId === targetId) {
      setError('Source and target stations must be different.');
      return;
    }

    try {
      setError(null);
      await api.operator.createCampaign({
        sourceStationId: sourceId,
        targetStationId: targetId,
        discountPercentage: discount,
        thresholdWaitTimeMinutes: threshold,
        description: campaignDesc
      });

      setShowAddCampaign(false);
      setCampaignDesc('');
      setSuccess('Redirection incentive campaign activated!');
      setTimeout(() => setSuccess(null), 3000);
      fetchData();
    } catch (err: any) {
      setError(err.message || 'Failed to create campaign.');
    }
  };

  const handleDeactivateCampaign = async (id: number) => {
    try {
      setError(null);
      await api.operator.deactivateCampaign(id);
      setSuccess('Campaign deactivated.');
      setTimeout(() => setSuccess(null), 3000);
      fetchData();
    } catch (err: any) {
      setError(err.message || 'Failed to deactivate campaign.');
    }
  };

  return (
    <div className="page-container" style={{ maxWidth: '1400px', margin: '0 auto' }}>
      
      {/* Alert Banners */}
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
          <h1 style={{ fontFamily: 'var(--font-family-title)', fontSize: '28px', color: '#fff' }}>Operator Hub</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>Real-time EV infrastructure monitoring & load management</p>
        </div>
        <button onClick={fetchData} className="btn btn-secondary" style={{ padding: '8px 16px' }}>
          <RefreshCw size={16} /> Refresh Grid
        </button>
      </div>

      {/* KPI Cards */}
      <div className="kpi-grid">
        <div className="glass-card kpi-card">
          <div>
            <Clock className="kpi-icon" size={36} color="var(--color-warning)" />
            <span style={{ display: 'block', fontSize: '12px', color: 'var(--text-secondary)', marginTop: '12px' }}>AVG GRID WAIT TIME</span>
          </div>
          <div className="kpi-val">{analytics ? `${analytics.averageWaitTimeMinutes}m` : '0.0m'}</div>
        </div>

        <div className="glass-card kpi-card">
          <div>
            <Zap className="kpi-icon" size={36} color="var(--color-primary)" />
            <span style={{ display: 'block', fontSize: '12px', color: 'var(--text-secondary)', marginTop: '12px' }}>GRID UTILIZATION RATE</span>
          </div>
          <div className="kpi-val">{analytics ? `${analytics.averageUtilizationPercentage}%` : '0%'}</div>
        </div>

        <div className="glass-card kpi-card">
          <div>
            <Users className="kpi-icon" size={36} color="var(--color-success)" />
            <span style={{ display: 'block', fontSize: '12px', color: 'var(--text-secondary)', marginTop: '12px' }}>CHARGERS IN USE</span>
          </div>
          <div className="kpi-val">{analytics ? `${analytics.occupiedChargersCount} / ${analytics.totalChargersCount}` : '0'}</div>
        </div>

        <div className="glass-card kpi-card">
          <div>
            <Percent className="kpi-icon" size={36} color="var(--color-secondary)" />
            <span style={{ display: 'block', fontSize: '12px', color: 'var(--text-secondary)', marginTop: '12px' }}>ACTIVE CAMPAIGNS</span>
          </div>
          <div className="kpi-val">{analytics ? analytics.activeCampaignsCount : 0}</div>
        </div>

        <div className="glass-card kpi-card">
          <div>
            <Settings className="kpi-icon" size={36} color="var(--color-success)" />
            <span style={{ display: 'block', fontSize: '12px', color: 'var(--text-secondary)', marginTop: '12px' }}>DRIVER SATISFACTION</span>
          </div>
          <div className="kpi-val">{analytics ? `${Math.round(analytics.driverSatisfactionScore)}%` : '100%'}</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '24px', marginBottom: '24px' }}>
        {/* Charts layer */}
        <DashboardCharts stations={stations} analytics={analytics} />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '24px' }}>
        
        {/* Charging Stations status list */}
        <div className="glass-card">
          <h3 style={{ fontSize: '18px', color: '#fff', marginBottom: '16px', fontFamily: 'Outfit' }}>Grid Node Controller</h3>
          
          <div className="glass-table-container">
            <table className="glass-table">
              <thead>
                <tr>
                  <th>Node Station</th>
                  <th>Location</th>
                  <th>Status</th>
                  <th>Total/Available</th>
                  <th>Active Sessions</th>
                  <th>Queue Length</th>
                  <th>Wait Time</th>
                  <th>Dynamic Pricing</th>
                  <th style={{ textAlign: 'center' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {stations.map(station => (
                  <tr key={station.id}>
                    <td>
                      <strong style={{ display: 'block', color: '#fff' }}>{station.name}</strong>
                      <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>ID: {station.id}</span>
                    </td>
                    <td>{station.latitude.toFixed(4)}, {station.longitude.toFixed(4)}</td>
                    <td>
                      <span className={`badge ${station.status === 'ACTIVE' ? 'badge-low' : 'badge-high'}`}>
                        {station.status}
                      </span>
                    </td>
                    <td>
                      {editingStationId === station.id ? (
                        <input
                          type="number"
                          className="glass-input"
                          style={{ width: '70px', padding: '6px' }}
                          value={editChargers}
                          onChange={e => setEditChargers(Number(e.target.value))}
                        />
                      ) : (
                        `${station.totalChargers} total`
                      )}
                    </td>
                    <td>{station.activeSessions} active</td>
                    <td>
                      <span style={{ color: station.queueLength > 0 ? 'var(--color-warning)' : 'var(--text-secondary)' }}>
                        {station.queueLength} queued
                      </span>
                    </td>
                    <td>
                      <strong style={{ color: station.expectedWaitTimeMinutes > 15 ? 'var(--color-danger)' : station.expectedWaitTimeMinutes > 5 ? 'var(--color-warning)' : 'var(--color-success)' }}>
                        {station.status === 'MAINTENANCE' ? 'N/A' : `${Math.round(station.expectedWaitTimeMinutes)}m`}
                      </strong>
                    </td>
                    <td>
                      {editingStationId === station.id ? (
                        <input
                          type="number"
                          step="0.01"
                          className="glass-input"
                          style={{ width: '80px', padding: '6px' }}
                          value={editPrice}
                          onChange={e => setEditPrice(Number(e.target.value))}
                        />
                      ) : (
                        `$${station.dynamicPricingPerKwh.toFixed(2)}/kWh`
                      )}
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      {editingStationId === station.id ? (
                        <div style={{ display: 'flex', gap: '6px', justifyContent: 'center' }}>
                          <select
                            className="glass-input"
                            style={{ width: '100px', padding: '4px', fontSize: '12px', background: 'rgba(16,22,35,0.9)' }}
                            value={editStatus}
                            onChange={e => setEditStatus(e.target.value as any)}
                          >
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="MAINTENANCE">OUTAGE</option>
                          </select>
                          <button onClick={() => handleSaveStation(station.id)} className="btn btn-primary" style={{ padding: '6px 12px', fontSize: '12px' }}>Save</button>
                          <button onClick={() => setEditingStationId(null)} className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '12px' }}>Cancel</button>
                        </div>
                      ) : (
                        <button onClick={() => handleEditStation(station)} className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '12px' }}>
                          <Edit size={12} /> Configure
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Incentive Campaigns manager */}
        <div className="glass-card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h3 style={{ fontSize: '18px', color: '#fff', fontFamily: 'Outfit', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Layers size={20} color="var(--color-secondary)" /> Dynamic Redirection Incentives
            </h3>
            <button onClick={() => setShowAddCampaign(!showAddCampaign)} className="btn btn-primary" style={{ padding: '8px 16px', fontSize: '13px' }}>
              <Plus size={16} /> Deploy Campaign
            </button>
          </div>

          {showAddCampaign ? (
            <form onSubmit={handleCreateCampaign} style={{ background: 'rgba(255,255,255,0.01)', border: '1px solid var(--border-color)', borderRadius: '12px', padding: '20px', marginBottom: '20px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '16px' }}>
                <div>
                  <label className="glass-label">Congested Source Node</label>
                  <select className="glass-input" value={sourceId} onChange={e => setSourceId(Number(e.target.value))} style={{ background: 'rgba(16,22,35,0.9)' }}>
                    {stations.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="glass-label">Target Destination Node</label>
                  <select className="glass-input" value={targetId} onChange={e => setTargetId(Number(e.target.value))} style={{ background: 'rgba(16,22,35,0.9)' }}>
                    {stations.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="glass-label">Discount % Offer</label>
                  <input type="number" min="5" max="50" className="glass-input" value={discount} onChange={e => setDiscount(Number(e.target.value))} />
                </div>
                <div>
                  <label className="glass-label">Trigger Threshold (Wait min)</label>
                  <input type="number" min="5" max="60" className="glass-input" value={threshold} onChange={e => setThreshold(Number(e.target.value))} />
                </div>
              </div>
              <div style={{ marginBottom: '16px' }}>
                <label className="glass-label">Campaign Description Banner</label>
                <input type="text" className="glass-input" placeholder="e.g. Save 20% on Gangnam station fees by routing to City Hall!" value={campaignDesc} onChange={e => setCampaignDesc(e.target.value)} />
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button type="submit" className="btn btn-primary">Deploy Campaign</button>
                <button type="button" onClick={() => setShowAddCampaign(false)} className="btn btn-secondary">Cancel</button>
              </div>
            </form>
          ) : null}

          <div className="glass-table-container">
            <table className="glass-table">
              <thead>
                <tr>
                  <th>Redirection Route</th>
                  <th>Discount</th>
                  <th>Threshold</th>
                  <th>Description Banner</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'center' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {campaigns.length === 0 ? (
                  <tr>
                    <td colSpan={6} style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '24px' }}>
                      No active incentive campaigns deployed. Add one above or trigger peak grid loads to allow auto-balancer to deploy campaigns.
                    </td>
                  </tr>
                ) : (
                  campaigns.map(c => (
                    <tr key={c.id}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <span style={{ color: 'var(--color-danger)' }}>{c.sourceStationName}</span>
                          <span>➔</span>
                          <span style={{ color: 'var(--color-success)' }}>{c.targetStationName}</span>
                        </div>
                      </td>
                      <td>
                        <strong style={{ color: 'var(--color-success)' }}>{c.discountPercentage}% off</strong>
                      </td>
                      <td>{c.thresholdWaitTimeMinutes}m wait</td>
                      <td>{c.description}</td>
                      <td>
                        <span className={`badge ${c.active ? 'badge-low' : 'badge-high'}`}>
                          {c.active ? 'ACTIVE' : 'EXPIRED'}
                        </span>
                      </td>
                      <td style={{ textAlign: 'center' }}>
                        {c.active ? (
                          <button onClick={() => handleDeactivateCampaign(c.id)} className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '11px', color: 'var(--color-danger)', borderColor: 'rgba(255,23,68,0.2)' }}>
                            Deactivate
                          </button>
                        ) : (
                          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Inactive</span>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </div>
  );
};
