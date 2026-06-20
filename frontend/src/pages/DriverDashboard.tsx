import React, { useState, useEffect } from 'react';
import { api, ChargingStation, Vehicle, RecommendationResult, ChargingSession, ChargingHistory } from '../services/api';
import { InteractiveMap } from '../components/InteractiveMap';
import { Zap, Clock, Navigation, Plus, Database, Power, CheckCircle, HelpCircle, RefreshCw } from 'lucide-react';

export const DriverDashboard: React.FC = () => {
  const [stations, setStations] = useState<ChargingStation[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState<number | null>(null);
  const [recommendations, setRecommendations] = useState<RecommendationResult[]>([]);
  const [activeSession, setActiveSession] = useState<ChargingSession | null>(null);
  const [history, setHistory] = useState<ChargingHistory[]>([]);
  
  // Mock User Location (downtown Coimbatore grid center as fallback)
  const [userLat, setUserLat] = useState<number>(11.0180);
  const [userLng, setUserLng] = useState<number>(76.9700);

  // Retrieve browser live location on component mount
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setUserLat(position.coords.latitude);
          setUserLng(position.coords.longitude);
          setInfoMessage("Recommendations customized to your browser live location!");
          setTimeout(() => setInfoMessage(null), 4000);
        },
        (error) => {
          console.warn("Browser Geolocation permission denied/failed. Falling back to Coimbatore.", error);
        }
      );
    }
  }, []);

  const handleUseLiveLocation = () => {
    if (!navigator.geolocation) {
      setError("Geolocation is not supported by your browser.");
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setUserLat(position.coords.latitude);
        setUserLng(position.coords.longitude);
        setInfoMessage("Coordinates synced with browser live geolocation!");
        setTimeout(() => setInfoMessage(null), 3000);
      },
      (error) => {
        setError(`Failed to retrieve live location: ${error.message}`);
      }
    );
  };
  
  // Selected station from list/map
  const [selectedStationId, setSelectedStationId] = useState<number | null>(null);
  const [routeTarget, setRouteTarget] = useState<[number, number] | null>(null);

  // Modal / Form state for new vehicle
  const [showAddVehicle, setShowAddVehicle] = useState(false);
  const [newModel, setNewModel] = useState('');
  const [newCapacity, setNewCapacity] = useState(65);
  const [newLevel, setNewLevel] = useState(30);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [infoMessage, setInfoMessage] = useState<string | null>(null);

  // Fetch initial profile data
  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const stationList = await api.stations.list();
      setStations(stationList);

      const vehicleList = await api.driver.getVehicles();
      setVehicles(vehicleList);

      if (vehicleList.length > 0 && !selectedVehicleId) {
        setSelectedVehicleId(vehicleList[0].id);
      }

      const activeSess = await api.driver.getActiveSessions();
      if (activeSess.length > 0) {
        setActiveSession(activeSess[0]);
      } else {
        setActiveSession(null);
      }

      const hist = await api.driver.getHistory();
      setHistory(hist.reverse()); // latest first
    } catch (err: any) {
      setError(err.message || 'Failed to load driver console data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    // Poll updates every 6 seconds
    const interval = setInterval(fetchData, 6000);
    return () => clearInterval(interval);
  }, []);

  // Fetch recommendations whenever vehicle, location, or stations change
  useEffect(() => {
    if (selectedVehicleId && stations.length > 0) {
      getRecommendations();
    }
  }, [selectedVehicleId, userLat, userLng, stations]);

  const getRecommendations = async () => {
    if (!selectedVehicleId) return;
    try {
      const recs = await api.recommendations.get(userLat, userLng, selectedVehicleId);
      setRecommendations(recs);
      // Select the top recommended station by default
      if (recs.length > 0 && !selectedStationId) {
        const top = recs[0].station;
        setSelectedStationId(top.id);
        setRouteTarget([top.latitude, top.longitude]);
      }
    } catch (err: any) {
      console.error('Failed to load routing recommendations:', err);
    }
  };

  const handleAddVehicle = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newModel) return;
    try {
      const range = (newCapacity * 5) * (newLevel / 100.0); // estimated simple range
      const saved = await api.driver.addVehicle({
        model: newModel,
        batteryCapacityKwh: newCapacity,
        currentBatteryLevelPct: newLevel,
        currentRangeKm: Math.round(range),
      });
      setVehicles([...vehicles, saved]);
      setSelectedVehicleId(saved.id);
      setShowAddVehicle(false);
      setNewModel('');
      setInfoMessage('Vehicle added successfully!');
      setTimeout(() => setInfoMessage(null), 3000);
    } catch (err: any) {
      const errMsg = err.message || 'Failed to add vehicle.';
      setError(errMsg);
      document.querySelector('.page-container')?.scrollTo({ top: 0, behavior: 'smooth' });
      alert(`Error adding vehicle: ${errMsg}`);
    }
  };

  const handleStartCharge = async (stationId: number) => {
    if (!selectedVehicleId) {
      const errMsg = 'Please select or add a vehicle first.';
      setError(errMsg);
      document.querySelector('.page-container')?.scrollTo({ top: 0, behavior: 'smooth' });
      alert(errMsg);
      return;
    }
    setError(null);
    try {
      const session = await api.driver.startCharge(stationId, selectedVehicleId);
      setActiveSession(session);
      setInfoMessage('Charging session started successfully!');
      setTimeout(() => setInfoMessage(null), 3000);
      fetchData();
    } catch (err: any) {
      const errMsg = err.message || 'Failed to start charging session.';
      setError(errMsg);
      document.querySelector('.page-container')?.scrollTo({ top: 0, behavior: 'smooth' });
      alert(`Error starting charging session: ${errMsg}`);
    }
  };

  const handleStopCharge = async () => {
    if (!activeSession) return;
    try {
      const session = await api.driver.stopCharge(activeSession.id);
      setActiveSession(null);
      setInfoMessage(`Charging completed! Energy: ${session.energyDeliveredKwh} kWh, Cost: $${session.cost}`);
      fetchData();
    } catch (err: any) {
      const errMsg = err.message || 'Failed to stop session.';
      setError(errMsg);
      document.querySelector('.page-container')?.scrollTo({ top: 0, behavior: 'smooth' });
      alert(`Error stopping charging session: ${errMsg}`);
    }
  };

  const handleSelectStation = (id: number) => {
    setSelectedStationId(id);
    const station = stations.find(s => s.id === id);
    if (station) {
      setRouteTarget([station.latitude, station.longitude]);
    }
  };

  const activeVehicle = vehicles.find(v => v.id === selectedVehicleId);

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
          marginBottom: '20px',
          display: 'flex',
          justifyContent: 'between',
          alignItems: 'center'
        }}>
          <span>{error}</span>
          <button onClick={() => setError(null)} style={{ background: 'none', border: 'none', color: '#fff', cursor: 'pointer' }}>✕</button>
        </div>
      )}

      {infoMessage && (
        <div style={{
          background: 'rgba(0, 230, 118, 0.1)',
          border: '1px solid rgba(0, 230, 118, 0.3)',
          borderRadius: '8px',
          color: 'var(--color-success)',
          padding: '16px',
          fontSize: '14px',
          marginBottom: '20px'
        }}>
          {infoMessage}
        </div>
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontFamily: 'var(--font-family-title)', fontSize: '28px', color: '#fff' }}>Driver Console</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>Optimize your route to the smartest charging node</p>
        </div>
        <button onClick={fetchData} className="btn btn-secondary" style={{ padding: '8px 16px' }}>
          <RefreshCw size={16} /> Refresh
        </button>
      </div>

      <div className="dashboard-grid">
        
        {/* Left Column: Map & Routing */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          {/* Map display */}
          <div className="glass-card" style={{ padding: '8px', height: '480px' }}>
            <InteractiveMap
              stations={stations}
              userLocation={[userLat, userLng]}
              selectedStationId={selectedStationId}
              onSelectStation={handleSelectStation}
              drawRouteTo={routeTarget}
            />
          </div>

          {/* Recommendations list */}
          <div className="glass-card">
            <h3 style={{ fontSize: '18px', color: '#fff', marginBottom: '16px', fontFamily: 'Outfit', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Navigation size={20} color="var(--color-primary)" /> Smart Charging Routing Recommendations
            </h3>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {recommendations.length === 0 ? (
                <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                  Add a vehicle and select your location to compute grid routing recommendations.
                </div>
              ) : (
                recommendations.map((rec, index) => (
                  <div
                    key={rec.station.id}
                    onClick={() => handleSelectStation(rec.station.id)}
                    style={{
                      background: selectedStationId === rec.station.id ? 'rgba(0, 242, 254, 0.05)' : 'rgba(255, 255, 255, 0.02)',
                      border: `1px solid ${selectedStationId === rec.station.id ? 'var(--color-primary)' : 'var(--border-color)'}`,
                      borderRadius: '12px',
                      padding: '16px',
                      cursor: 'pointer',
                      transition: 'all 0.2s ease',
                      position: 'relative'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap', marginBottom: '4px' }}>
                          <h4 style={{ color: '#fff', fontSize: '16px', fontFamily: 'Outfit', margin: 0 }}>
                            {rec.station.name}
                          </h4>
                          {index === 0 && (
                            <span className="badge badge-info">
                              ★ Recommended Optimal
                            </span>
                          )}
                        </div>
                        <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                          Distance: {rec.distanceKm} km ({rec.travelTimeMinutes} mins travel)
                        </span>
                      </div>
                      
                      <div style={{ textAlign: 'right' }}>
                        <div style={{ fontSize: '20px', fontFamily: 'Outfit', fontWeight: 800, color: '#fff' }}>
                          {rec.totalTimeMinutes} <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>mins total delay</span>
                        </div>
                        <span style={{ fontSize: '12px', color: 'var(--color-success)' }}>
                          ${rec.station.dynamicPricingPerKwh.toFixed(2)}/kWh
                        </span>
                      </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '12px', fontSize: '13px', background: 'rgba(0,0,0,0.15)', padding: '10px', borderRadius: '8px' }}>
                      <div>
                        <span style={{ display: 'block', color: 'var(--text-muted)' }}>Travel Time:</span>
                        <strong>{rec.travelTimeMinutes} mins</strong>
                      </div>
                      <div>
                        <span style={{ display: 'block', color: 'var(--text-muted)' }}>Wait Time:</span>
                        <strong style={{ color: rec.expectedWaitTimeMinutes > 15 ? 'var(--color-danger)' : rec.expectedWaitTimeMinutes > 5 ? 'var(--color-warning)' : 'var(--color-success)' }}>
                          {rec.expectedWaitTimeMinutes} mins
                        </strong>
                      </div>
                      <div>
                        <span style={{ display: 'block', color: 'var(--text-muted)' }}>Charging Time:</span>
                        <strong>{rec.chargingTimeMinutes} mins</strong>
                      </div>
                    </div>

                    <div style={{ marginTop: '12px', fontSize: '13px', color: rec.discountPercentage > 0 ? 'var(--color-success)' : 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <CheckCircle size={14} />
                      <span>{rec.recommendationNotes}</span>
                    </div>

                    {selectedStationId === rec.station.id && !activeSession && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleStartCharge(rec.station.id);
                        }}
                        className="btn btn-primary"
                        style={{ width: '100%', marginTop: '14px', padding: '8px' }}
                      >
                        Start Charging Session
                      </button>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        {/* Right Column: Driver profile, battery indicator, active charge, and history */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Active Vehicle & Battery Level Card */}
          <div className="glass-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '18px', color: '#fff', fontFamily: 'Outfit' }}>Active Vehicle</h3>
              <button onClick={() => setShowAddVehicle(!showAddVehicle)} className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '12px' }}>
                <Plus size={14} /> Add Vehicle
              </button>
            </div>

            {showAddVehicle ? (
              <form onSubmit={handleAddVehicle} style={{ background: 'rgba(255,255,255,0.01)', padding: '16px', borderRadius: '12px', border: '1px solid var(--border-color)', marginBottom: '20px' }}>
                <div style={{ marginBottom: '12px' }}>
                  <label className="glass-label">Model Name</label>
                  <input type="text" className="glass-input" placeholder="e.g. Kia EV6" value={newModel} onChange={e => setNewModel(e.target.value)} required />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '16px' }}>
                  <div>
                    <label className="glass-label">Battery Cap. (kWh)</label>
                    <input type="number" className="glass-input" value={newCapacity} onChange={e => setNewCapacity(Number(e.target.value))} required />
                  </div>
                  <div>
                    <label className="glass-label">Current Battery %</label>
                    <input type="number" className="glass-input" min="0" max="100" value={newLevel} onChange={e => setNewLevel(Number(e.target.value))} required />
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <button type="submit" className="btn btn-primary" style={{ padding: '8px 16px', fontSize: '12px' }}>Save</button>
                  <button type="button" onClick={() => setShowAddVehicle(false)} className="btn btn-secondary" style={{ padding: '8px 16px', fontSize: '12px' }}>Cancel</button>
                </div>
              </form>
            ) : null}

            {vehicles.length === 0 ? (
              <div style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '16px' }}>
                No vehicles configured. Add one above.
              </div>
            ) : (
              <div>
                <select
                  className="glass-input"
                  value={selectedVehicleId || ''}
                  onChange={e => setSelectedVehicleId(Number(e.target.value))}
                  style={{ marginBottom: '20px', background: 'rgba(16,22,35,0.9)' }}
                >
                  {vehicles.map(v => (
                    <option key={v.id} value={v.id}>{v.model} ({v.batteryCapacityKwh} kWh)</option>
                  ))}
                </select>

                {activeVehicle && (
                  <div>
                    {/* Battery gauge visualizer */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '16px' }}>
                      <div style={{
                        flex: 1,
                        height: '24px',
                        background: 'rgba(255,255,255,0.05)',
                        border: '1px solid var(--border-color)',
                        borderRadius: '6px',
                        overflow: 'hidden',
                        position: 'relative'
                      }}>
                        <div style={{
                          width: `${activeVehicle.currentBatteryLevelPct}%`,
                          height: '100%',
                          background: activeVehicle.currentBatteryLevelPct < 25 ? 'var(--color-danger)' : activeVehicle.currentBatteryLevelPct < 60 ? 'var(--color-warning)' : 'var(--color-success)',
                          boxShadow: '0 0 10px rgba(0, 230, 118, 0.4)',
                          transition: 'width 0.5s ease'
                        }}></div>
                        <span style={{
                          position: 'absolute',
                          left: '50%',
                          top: '50%',
                          transform: 'translate(-50%, -50%)',
                          fontSize: '12px',
                          fontWeight: 700,
                          color: '#fff',
                          textShadow: '0 1px 2px rgba(0,0,0,0.8)'
                        }}>
                          {Math.round(activeVehicle.currentBatteryLevelPct)}%
                        </span>
                      </div>
                      
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-secondary)' }}>RANGE EST.</span>
                        <strong style={{ fontSize: '18px', color: '#fff', fontFamily: 'Outfit' }}>{activeVehicle.currentRangeKm} km</strong>
                      </div>
                    </div>

                    {/* Mock driver coordinates slider & Live Geolocation */}
                    <div style={{ marginTop: '16px', background: 'rgba(0,0,0,0.1)', padding: '12px', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.02)' }}>
                      <span className="glass-label" style={{ marginBottom: '4px' }}>Driver Grid Location (Coimbatore / Live GPS)</span>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: 'var(--text-secondary)' }}>
                          <span>Latitude: {userLat.toFixed(4)}</span>
                          <span>Longitude: {userLng.toFixed(4)}</span>
                        </div>
                        <input
                          type="range"
                          min="10.95"
                          max="11.10"
                          step="0.0001"
                          value={userLat}
                          onChange={e => setUserLat(Number(e.target.value))}
                          style={{ accentColor: 'var(--color-primary)', cursor: 'ew-resize', width: '100%' }}
                        />
                        <input
                          type="range"
                          min="76.90"
                          max="77.05"
                          step="0.0001"
                          value={userLng}
                          onChange={e => setUserLng(Number(e.target.value))}
                          style={{ accentColor: 'var(--color-primary)', cursor: 'ew-resize', width: '100%' }}
                        />
                        <button
                          type="button"
                          onClick={handleUseLiveLocation}
                          className="btn btn-secondary"
                          style={{ padding: '8px', fontSize: '11px', width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '4px', marginTop: '4px' }}
                        >
                          <Navigation size={12} /> Sync Live Browser GPS
                        </button>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Active Charging Session Card */}
          <div className="glass-card" style={{ border: activeSession ? '1px solid var(--color-success)' : '1px solid var(--border-color)' }}>
            <h3 style={{ fontSize: '18px', color: '#fff', marginBottom: '16px', fontFamily: 'Outfit', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Power size={20} color={activeSession ? 'var(--color-success)' : 'var(--text-muted)'} /> Active Charging Status
            </h3>

            {activeSession ? (
              <div>
                <div className="glow-active" style={{
                  background: 'rgba(0, 230, 118, 0.05)',
                  border: '1px solid rgba(0, 230, 118, 0.2)',
                  borderRadius: '12px',
                  padding: '16px',
                  marginBottom: '16px'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                    <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>STATION</span>
                    <strong style={{ color: '#fff' }}>{activeSession.stationName}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                    <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>VEHICLE</span>
                    <strong style={{ color: '#fff' }}>{activeSession.vehicleModel}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                    <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>START TIME</span>
                    <strong style={{ color: '#fff' }}>{new Date(activeSession.startTime).toLocaleTimeString()}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>RATE</span>
                    <strong style={{ color: 'var(--color-success)' }}>120 kW (DC Fast)</strong>
                  </div>
                </div>

                <button onClick={handleStopCharge} className="btn btn-primary" style={{ width: '100%', background: 'var(--color-danger)', boxShadow: '0 4px 15px rgba(255, 23, 68, 0.3)', color: '#fff' }}>
                  Stop Charge & Complete Bill
                </button>
              </div>
            ) : (
              <div style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '24px' }}>
                No active charging session. Choose an optimal node from the recommendation list to start.
              </div>
            )}
          </div>

          {/* Charging History Card */}
          <div className="glass-card" style={{ flex: 1 }}>
            <h3 style={{ fontSize: '18px', color: '#fff', marginBottom: '16px', fontFamily: 'Outfit', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Database size={20} color="var(--color-secondary)" /> Charging Logs
            </h3>

            <div style={{ maxHeight: '280px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {history.length === 0 ? (
                <div style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '24px' }}>
                  No charging sessions completed yet.
                </div>
              ) : (
                history.map(item => (
                  <div key={item.id} style={{ background: 'rgba(255,255,255,0.01)', border: '1px solid var(--border-color)', borderRadius: '10px', padding: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <strong style={{ display: 'block', fontSize: '13px', color: '#fff' }}>{item.stationName}</strong>
                      <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{new Date(item.date).toLocaleDateString()} • {Math.round(item.durationMinutes)} mins</span>
                    </div>
                    
                    <div style={{ textAlign: 'right' }}>
                      <strong style={{ display: 'block', fontSize: '14px', color: 'var(--color-success)' }}>${item.cost.toFixed(2)}</strong>
                      <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>{item.energyDeliveredKwh} kWh</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};
