import React from 'react';
import { Line, Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  ChartOptions
} from 'chart.js';
import { ChargingStation, SystemAnalytics } from '../services/api';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

interface DashboardChartsProps {
  stations: ChargingStation[];
  analytics: SystemAnalytics | null;
}

export const DashboardCharts: React.FC<DashboardChartsProps> = ({ stations, analytics }) => {
  // Chart 1: Hourly Load Distribution
  const loadData = {
    labels: Array.from({ length: 24 }, (_, i) => `${i.toString().padStart(2, '0')}:00`),
    datasets: [
      {
        label: 'Grid Charging Load (kW)',
        data: analytics?.hourlyLoadDistribution || Array.from({ length: 24 }, () => 10 + Math.random() * 80),
        borderColor: '#00f2fe',
        backgroundColor: 'rgba(0, 242, 254, 0.12)',
        fill: true,
        tension: 0.4,
        borderWidth: 2,
        pointBackgroundColor: '#00f2fe',
        pointHoverRadius: 6,
      }
    ]
  };

  const loadOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        backgroundColor: '#101623',
        titleFont: { family: 'Outfit' },
        bodyFont: { family: 'Inter' },
        borderColor: 'rgba(255,255,255,0.08)',
        borderWidth: 1
      }
    },
    scales: {
      x: {
        grid: {
          display: false,
        },
        ticks: {
          color: '#64748b',
          font: { family: 'Inter', size: 10 }
        }
      },
      y: {
        grid: {
          color: 'rgba(255, 255, 255, 0.04)',
        },
        ticks: {
          color: '#64748b',
          font: { family: 'Inter', size: 10 }
        }
      }
    }
  };

  // Chart 2: Station Utilization rates
  const utilizationLabels = stations.map(s => s.name.split(' ')[0]);
  const utilizationRates = stations.map(s => s.utilizationPercentage);

  const utilizationData = {
    labels: utilizationLabels,
    datasets: [
      {
        label: 'Station Utilization %',
        data: utilizationRates,
        backgroundColor: stations.map(s => {
          if (s.status === 'MAINTENANCE') return 'rgba(100, 116, 139, 0.4)';
          const u = s.utilizationPercentage;
          if (u < 50) return 'rgba(0, 230, 118, 0.65)'; // success green
          if (u < 85) return 'rgba(255, 179, 0, 0.65)'; // warning orange
          return 'rgba(255, 23, 68, 0.65)'; // danger red
        }),
        borderColor: stations.map(s => {
          if (s.status === 'MAINTENANCE') return '#64748b';
          const u = s.utilizationPercentage;
          if (u < 50) return '#00e676';
          if (u < 85) return '#ffb300';
          return '#ff1744';
        }),
        borderWidth: 1.5,
        borderRadius: 6,
      }
    ]
  };

  const utilizationOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        backgroundColor: '#101623',
        titleFont: { family: 'Outfit' },
        bodyFont: { family: 'Inter' },
        borderColor: 'rgba(255,255,255,0.08)',
        borderWidth: 1
      }
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: '#64748b', font: { family: 'Inter', size: 10 } }
      },
      y: {
        max: 100,
        grid: { color: 'rgba(255, 255, 255, 0.04)' },
        ticks: { color: '#64748b', font: { family: 'Inter', size: 10 } }
      }
    }
  };

  // Chart 3: Wait Time Comparison
  const waitTimes = stations.map(s => s.status === 'MAINTENANCE' ? 0 : s.expectedWaitTimeMinutes);
  const queueLengths = stations.map(s => s.queueLength);

  const waitTimeData = {
    labels: utilizationLabels,
    datasets: [
      {
        label: 'Wait Time (mins)',
        data: waitTimes,
        backgroundColor: 'rgba(155, 81, 224, 0.5)',
        borderColor: '#9b51e0',
        borderWidth: 1.5,
        borderRadius: 4,
      },
      {
        label: 'Queue (vehicles)',
        data: queueLengths,
        backgroundColor: 'rgba(0, 242, 254, 0.5)',
        borderColor: '#00f2fe',
        borderWidth: 1.5,
        borderRadius: 4,
      }
    ]
  };

  const waitTimeOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          color: '#94a3b8',
          font: { family: 'Outfit', size: 10 }
        }
      },
      tooltip: {
        backgroundColor: '#101623',
        titleFont: { family: 'Outfit' },
        bodyFont: { family: 'Inter' }
      }
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: '#64748b', font: { family: 'Inter', size: 10 } }
      },
      y: {
        grid: { color: 'rgba(255, 255, 255, 0.04)' },
        ticks: { color: '#64748b', font: { family: 'Inter', size: 10 } }
      }
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* 24h Grid Demand */}
      <div className="glass-card">
        <h3 style={{ fontSize: '16px', fontFamily: 'Outfit', color: '#fff', marginBottom: '16px' }}>
          Hourly Grid Charging Load Profile (kW)
        </h3>
        <div style={{ height: '220px' }}>
          <Line data={loadData} options={loadOptions} />
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '20px' }}>
        {/* Utilization Rate */}
        <div className="glass-card">
          <h3 style={{ fontSize: '16px', fontFamily: 'Outfit', color: '#fff', marginBottom: '16px' }}>
            Station Active Utilization (%)
          </h3>
          <div style={{ height: '200px' }}>
            <Bar data={utilizationData} options={utilizationOptions} />
          </div>
        </div>

        {/* Wait Times & Queues */}
        <div className="glass-card">
          <h3 style={{ fontSize: '16px', fontFamily: 'Outfit', color: '#fff', marginBottom: '16px' }}>
            Station Congestion Parameters
          </h3>
          <div style={{ height: '200px' }}>
            <Bar data={waitTimeData} options={waitTimeOptions} />
          </div>
        </div>
      </div>
    </div>
  );
};
