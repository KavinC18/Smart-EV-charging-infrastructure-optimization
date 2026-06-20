import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';
import { ChargingStation } from '../services/api';
import { Zap, Clock, Navigation, DollarSign } from 'lucide-react';

interface InteractiveMapProps {
  stations: ChargingStation[];
  userLocation: [number, number];
  selectedStationId: number | null;
  onSelectStation: (id: number) => void;
  drawRouteTo?: [number, number] | null;
}

// Recenters the map when user location or route changes
const MapController: React.FC<{ center: [number, number]; target?: [number, number] | null }> = ({ center, target }) => {
  const map = useMap();
  
  useEffect(() => {
    if (target) {
      // Fit bounds to show both user and target station
      const bounds = L.latLngBounds([center, target]);
      map.fitBounds(bounds, { padding: [50, 50] });
    } else {
      map.setView(center, map.getZoom());
    }
  }, [center, target, map]);

  return null;
};

export const InteractiveMap: React.FC<InteractiveMapProps> = ({
  stations,
  userLocation,
  selectedStationId,
  onSelectStation,
  drawRouteTo,
}) => {

  const getStationColor = (station: ChargingStation) => {
    if (station.status === 'MAINTENANCE') return '#64748b'; // Slate gray
    const wait = station.expectedWaitTimeMinutes;
    if (wait <= 5) return '#00e676'; // Charging Green
    if (wait <= 15) return '#ffb300'; // Alert Orange
    return '#ff1744'; // Congestion Red
  };

  const createStationIcon = (station: ChargingStation) => {
    const color = getStationColor(station);
    const label = station.status === 'MAINTENANCE' ? '🔧' : Math.round(station.expectedWaitTimeMinutes);
    const isSelected = selectedStationId === station.id;

    return L.divIcon({
      className: 'custom-station-marker',
      html: `
        <div style="
          position: relative;
          width: ${isSelected ? '38px' : '30px'};
          height: ${isSelected ? '38px' : '30px'};
          background: ${color};
          border: 2.5px solid ${isSelected ? '#00f2fe' : '#ffffff'};
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 0 ${isSelected ? '25px' : '12px'} ${color}, inset 0 0 5px rgba(0,0,0,0.4);
          transition: all 0.25s ease;
          cursor: pointer;
        ">
          <span style="font-size: ${isSelected ? '12px' : '10px'}; font-weight: 800; color: #0c0f1d; font-family: 'Outfit';">
            ${label}
          </span>
          ${station.status === 'ACTIVE' ? `
            <div style="
              position: absolute;
              width: 100%;
              height: 100%;
              border-radius: 50%;
              background: ${color};
              opacity: 0.35;
              animation: marker-pulse 1.8s infinite;
              top: 0;
              left: 0;
              pointer-events: none;
            "></div>
          ` : ''}
        </div>
      `,
      iconSize: isSelected ? [38, 38] : [30, 30],
      iconAnchor: isSelected ? [19, 19] : [15, 15]
    });
  };

  // User position icon
  const userIcon = L.divIcon({
    className: 'custom-user-marker',
    html: `
      <div style="
        position: relative;
        width: 24px;
        height: 24px;
        background: #00f2fe;
        border: 2.5px solid #ffffff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 0 20px #00f2fe, inset 0 0 4px rgba(0,0,0,0.5);
      ">
        <div style="
          position: absolute;
          width: 100%;
          height: 100%;
          border-radius: 50%;
          background: #00f2fe;
          opacity: 0.4;
          animation: marker-pulse 2s infinite;
          top: 0;
          left: 0;
        "></div>
      </div>
    `,
    iconSize: [24, 24],
    iconAnchor: [12, 12]
  });

  return (
    <div style={{ height: '100%', position: 'relative' }}>
      <MapContainer
        center={userLocation}
        zoom={13}
        style={{ height: '100%', width: '100%', borderRadius: '16px' }}
      >
        {/* CartoDB Dark Matter map tile style */}
        <TileLayer
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
        />

        {/* User Location Marker */}
        <Marker position={userLocation} icon={userIcon}>
          <Popup>
            <div style={{ padding: '4px' }}>
              <strong style={{ display: 'block', fontSize: '14px', fontFamily: 'Outfit' }}>Your Location</strong>
              <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Seoul Grid Center</span>
            </div>
          </Popup>
        </Marker>

        {/* Charging Stations Markers */}
        {stations.map(station => (
          <Marker
            key={station.id}
            position={[station.latitude, station.longitude]}
            icon={createStationIcon(station)}
            eventHandlers={{
              click: () => onSelectStation(station.id)
            }}
          >
            <Popup>
              <div style={{ padding: '8px', minWidth: '180px' }}>
                <h4 style={{ fontFamily: 'Outfit', color: '#fff', fontSize: '15px', marginBottom: '8px' }}>
                  {station.name}
                </h4>
                
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Zap size={14} color="var(--color-primary)" />
                    <span>Chargers: <strong>{station.availableChargers} / {station.totalChargers}</strong></span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Clock size={14} color="var(--color-warning)" />
                    <span>Est. Wait: <strong>{station.status === 'MAINTENANCE' ? 'Out of Service' : `${Math.round(station.expectedWaitTimeMinutes)} mins`}</strong></span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <DollarSign size={14} color="var(--color-success)" />
                    <span>Pricing: <strong>${station.dynamicPricingPerKwh.toFixed(2)}/kWh</strong></span>
                  </div>
                </div>

                <div style={{ marginTop: '12px', display: 'flex', gap: '6px' }}>
                  <span className={`badge ${
                    station.status === 'MAINTENANCE' ? 'badge-high' :
                    station.expectedWaitTimeMinutes <= 5 ? 'badge-low' :
                    station.expectedWaitTimeMinutes <= 15 ? 'badge-mod' : 'badge-high'
                  }`}>
                    {station.status === 'MAINTENANCE' ? 'MAINTENANCE' :
                     station.expectedWaitTimeMinutes <= 5 ? 'Low Congestion' :
                     station.expectedWaitTimeMinutes <= 15 ? 'Moderate' : 'High Congestion'}
                  </span>
                </div>
              </div>
            </Popup>
          </Marker>
        ))}

        {/* Draw Route Polyline if path selected */}
        {drawRouteTo && (
          <Polyline
            positions={[userLocation, drawRouteTo]}
            pathOptions={{
              color: '#00f2fe',
              weight: 4,
              opacity: 0.8,
              dashArray: '8, 8',
              lineCap: 'round',
            }}
          />
        )}

        <MapController center={userLocation} target={drawRouteTo} />
      </MapContainer>
    </div>
  );
};
