export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Fetch interceptor wrapper
async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('chargeflow_token');
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config: RequestInit = {
    ...options,
    headers: headers as HeadersInit,
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

  if (response.status === 401) {
    localStorage.removeItem('chargeflow_token');
    localStorage.removeItem('chargeflow_user');
    window.location.href = '/login';
    throw new Error('Session expired. Please login again.');
  }

  if (!response.ok) {
    let errorMessage = 'An error occurred';
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch {
      errorMessage = await response.text() || errorMessage;
    }
    throw new Error(errorMessage);
  }

  // Handle empty bodies
  const text = await response.text();
  return text ? JSON.parse(text) : {} as T;
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  role: 'ROLE_DRIVER' | 'ROLE_OPERATOR' | 'ROLE_ADMIN';
  token?: string;
}

export interface ChargingStation {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
  totalChargers: number;
  availableChargers: number;
  activeSessions: number;
  queueLength: number;
  averageChargingDurationMinutes: number;
  serviceRate: number;
  arrivalRate: number;
  utilizationPercentage: number;
  expectedWaitTimeMinutes: number;
  waitProbability: number;
  status: 'ACTIVE' | 'MAINTENANCE';
  dynamicPricingPerKwh: number;
}

export interface Vehicle {
  id: number;
  model: string;
  batteryCapacityKwh: number;
  currentBatteryLevelPct: number;
  currentRangeKm: number;
}

export interface RecommendationResult {
  station: ChargingStation;
  distanceKm: number;
  travelTimeMinutes: number;
  expectedWaitTimeMinutes: number;
  chargingTimeMinutes: number;
  totalTimeMinutes: number;
  reachable: boolean;
  discountPercentage: number;
  recommendationNotes: string;
}

export interface ChargingSession {
  id: number;
  userId: number;
  username: string;
  stationId: number;
  stationName: string;
  vehicleId: number;
  vehicleModel: string;
  startTime: string;
  endTime: string | null;
  energyDeliveredKwh: number;
  cost: number;
  status: 'ACTIVE' | 'COMPLETED';
}

export interface ChargingHistory {
  id: number;
  userId: number;
  username: string;
  stationId: number;
  stationName: string;
  date: string;
  durationMinutes: number;
  energyDeliveredKwh: number;
  cost: number;
}

export interface IncentiveCampaign {
  id: number;
  sourceStationId: number;
  sourceStationName: string;
  targetStationId: number;
  targetStationName: string;
  discountPercentage: number;
  active: boolean;
  thresholdWaitTimeMinutes: number;
  description: string;
}

export interface SystemAnalytics {
  averageWaitTimeMinutes: number;
  averageUtilizationPercentage: number;
  totalChargersCount: number;
  occupiedChargersCount: number;
  totalQueueLength: number;
  totalCompletedSessions: number;
  activeCampaignsCount: number;
  driverSatisfactionScore: number;
  hourlyLoadDistribution: number[];
}

export interface SimulationStatus {
  running: boolean;
  speed: number;
  scenario: string;
  simulatedTime: string;
}

// API methods
export const api = {
  auth: {
    login: (credentials: any) => request<UserProfile>('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
    register: (details: any) => request<string>('/auth/register', { method: 'POST', body: JSON.stringify(details) }),
    getProfile: () => request<UserProfile>('/auth/profile'),
  },
  stations: {
    list: () => request<ChargingStation[]>('/stations'),
    get: (id: number) => request<ChargingStation>(`/stations/${id}`),
    create: (data: any) => request<ChargingStation>('/stations', { method: 'POST', body: JSON.stringify(data) }),
    update: (id: number, data: any) => request<ChargingStation>(`/stations/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    delete: (id: number) => request<string>(`/stations/${id}`, { method: 'DELETE' }),
  },
  driver: {
    getVehicles: () => request<Vehicle[]>('/driver/vehicles'),
    addVehicle: (data: any) => request<Vehicle>('/driver/vehicles', { method: 'POST', body: JSON.stringify(data) }),
    getHistory: () => request<ChargingHistory[]>('/driver/history'),
    getActiveSessions: () => request<ChargingSession[]>('/driver/active-sessions'),
    startCharge: (stationId: number, vehicleId: number) => 
      request<ChargingSession>('/driver/start-charge', { method: 'POST', body: JSON.stringify({ stationId, vehicleId }) }),
    stopCharge: (sessionId: number) => 
      request<ChargingSession>('/driver/stop-charge', { method: 'POST', body: JSON.stringify({ sessionId }) }),
  },
  operator: {
    listCampaigns: () => request<IncentiveCampaign[]>('/operator/campaigns'),
    createCampaign: (data: any) => request<IncentiveCampaign>('/operator/campaigns', { method: 'POST', body: JSON.stringify(data) }),
    deactivateCampaign: (id: number) => request<IncentiveCampaign>(`/operator/campaigns/${id}/deactivate`, { method: 'POST' }),
    getAnalytics: () => request<SystemAnalytics>('/operator/analytics'),
  },
  simulation: {
    getStatus: () => request<SimulationStatus>('/simulation/status'),
    toggle: () => request<boolean>('/simulation/toggle', { method: 'POST' }),
    setScenario: (scenario: string) => request<string>('/simulation/scenario', { method: 'POST', body: JSON.stringify({ scenario }) }),
    setSpeed: (speed: number) => request<number>('/simulation/speed', { method: 'POST', body: JSON.stringify({ speed }) }),
    triggerArrival: (stationId: number) => request<string>('/simulation/trigger-arrival', { method: 'POST', body: JSON.stringify({ stationId }) }),
    reset: () => request<string>('/simulation/reset', { method: 'POST' }),
  },
  recommendations: {
    get: (lat: number, lng: number, vehicleId: number) => 
      request<RecommendationResult[]>(`/recommendations?lat=${lat}&lng=${lng}&vehicleId=${vehicleId}`),
  }
};
