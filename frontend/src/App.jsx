import React, { useState, useEffect } from 'react';
import { 
  BarChart3, 
  History, 
  LayoutDashboard, 
  LogOut, 
  PlusCircle, 
  Settings, 
  ShieldCheck, 
  Timer, 
  TrendingUp, 
  User as UserIcon,
  Sparkles
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

const App = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [loading, setLoading] = useState(false);
  const [history, setHistory] = useState([]);
  const [user, setUser] = useState({
    name: 'Kaviya',
    safeLimit: 120,
    sleepStart: 22,
    sleepEnd: 6
  });
  const [lastScore, setLastScore] = useState(85);
  const [showLogForm, setShowLogForm] = useState(false);

  // Form states
  const [logData, setLogData] = useState({
    study: 0,
    social: 0,
    entertainment: 0,
    peakHour: 18
  });

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      const resp = await fetch(`/api/history?name=${user.name}`);
      const data = await resp.json();
      setHistory(data);
    } catch (e) {
      console.error('Failed to fetch history:', e);
    }
  };

  const handleLogSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const resp = await fetch('/api/log', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...user,
          ...logData
        })
      });
      const result = await resp.json();
      setLastScore(result.score);
      setShowLogForm(false);
      fetchHistory();
    } catch (e) {
      console.error('Failed to log session:', e);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateProfile = (e) => {
    e.preventDefault();
    setActiveTab('dashboard');
  };

  return (
    <div className="app-container">
      {/* Sidebar Navigation */}
      <aside className="sidebar glass-panel">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '0 1rem 1rem' }}>
          <ShieldCheck size={32} color="var(--accent-color)" />
          <h2 style={{ fontSize: '1.2rem', color: 'white' }}>DETOX PRO</h2>
        </div>

        <nav style={{ flex: 1 }}>
          <div className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`} onClick={() => setActiveTab('dashboard')}>
            <LayoutDashboard size={20} /> Dashboard
          </div>
          <div className={`nav-item ${activeTab === 'history' ? 'active' : ''}`} onClick={() => setActiveTab('history')}>
            <History size={20} /> History
          </div>
          <div className={`nav-item ${activeTab === 'settings' ? 'active' : ''}`} onClick={() => setActiveTab('settings')}>
            <Settings size={20} /> Settings
          </div>
        </nav>

        <div className="nav-item">
          <LogOut size={20} /> Logout
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="main-content">
        <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 style={{ fontSize: '2.2rem', fontWeight: 700 }}>Welcome back, {user.name}</h1>
            <p style={{ color: 'var(--text-secondary)' }}>Track your digital well-being today.</p>
          </div>
          <button onClick={() => setShowLogForm(true)} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <PlusCircle size={20} /> Log Today's Time
          </button>
        </header>

        <AnimatePresence mode="wait">
          {activeTab === 'dashboard' && (
            <motion.div 
              key="dashboard"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="dashboard-grid"
            >
              <div className="glass-panel stat-card">
                <span className="stat-label"><Sparkles size={14} /> Current Score</span>
                <span className="stat-value">{lastScore}</span>
                <span style={{ color: lastScore > 70 ? 'var(--success)' : 'var(--warning)', fontWeight: 600 }}>
                  {lastScore >= 85 ? 'Excellent 🌟' : lastScore >= 70 ? 'Good ✅' : 'Needs attention ⚠️'}
                </span>
              </div>
              
              <div className="glass-panel stat-card">
                <span className="stat-label"><Timer size={14} /> Daily Limit</span>
                <span className="stat-value">{user.safeLimit}m</span>
                <span style={{ color: 'var(--text-secondary)' }}>Target: Stay under limit</span>
              </div>

              <div className="glass-panel stat-card">
                <span className="stat-label"><TrendingUp size={14} /> Active Streak</span>
                <span className="stat-value">5 Days</span>
                <span style={{ color: 'var(--success)' }}>Personal record!</span>
              </div>

              {/* Recent Activity Mini-List */}
              <div className="glass-panel" style={{ gridColumn: '1 / -1' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                  <h3>Recent Historical Entries</h3>
                  <BarChart3 size={20} color="var(--accent-color)" />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {history.slice(0, 3).map((entry, idx) => (
                    <div key={idx} style={{ 
                      padding: '12px', 
                      borderRadius: '12px', 
                      background: 'rgba(255,255,255,0.03)',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                        <div style={{ padding: '8px', background: 'rgba(0,210,255,0.1)', borderRadius: '10px' }}>
                          <Timer size={18} color="var(--accent-color)" />
                        </div>
                        <div>
                          <div style={{ fontWeight: 600 }}>{entry.date}</div>
                          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Total: {entry.totalTime} mins</div>
                        </div>
                      </div>
                      <div style={{ fontWeight: 700 }}>{Math.max(0, 100 - (entry.totalTime/2))} Score</div>
                    </div>
                  ))}
                  {history.length === 0 && <p style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '2rem' }}>No history found. Try logging your first session!</p>}
                </div>
              </div>
            </motion.div>
          )}

          {activeTab === 'history' && (
            <motion.div 
              key="history"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              className="glass-panel"
            >
              <h2 style={{ marginBottom: '1.5rem' }}>Your Progress History</h2>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ textAlign: 'left', borderBottom: '1px solid var(--glass-border)' }}>
                    <th style={{ padding: '12px' }}>Date</th>
                    <th style={{ padding: '12px' }}>Study</th>
                    <th style={{ padding: '12px' }}>Social</th>
                    <th style={{ padding: '12px' }}>Entertainment</th>
                    <th style={{ padding: '12px' }}>Total</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((entry, idx) => (
                    <tr key={idx} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                      <td style={{ padding: '12px' }}>{entry.date}</td>
                      <td style={{ padding: '12px' }}>{entry.studyTime}m</td>
                      <td style={{ padding: '12px' }}>{entry.socialTime}m</td>
                      <td style={{ padding: '12px' }}>{entry.entertainmentTime}m</td>
                      <td style={{ padding: '12px', fontWeight: 600 }}>{entry.totalTime}m</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </motion.div>
          )}

          {activeTab === 'settings' && (
            <motion.div 
              key="settings"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="glass-panel"
              style={{ maxWidth: '600px' }}
            >
              <h2 style={{ marginBottom: '1.5rem' }}>Profile Settings</h2>
              <form onSubmit={handleUpdateProfile} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
                <div>
                  <label className="stat-label">Full Name</label>
                  <input value={user.name} onChange={(e) => setUser({...user, name: e.target.value})} />
                </div>
                <div>
                  <label className="stat-label">Daily Safe Limit (mins)</label>
                  <input type="number" value={user.safeLimit} onChange={(e) => setUser({...user, safeLimit: parseInt(e.target.value)})} />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div>
                    <label className="stat-label">Sleep Start (24h)</label>
                    <input type="number" value={user.sleepStart} onChange={(e) => setUser({...user, sleepStart: parseInt(e.target.value)})} />
                  </div>
                  <div>
                    <label className="stat-label">Wake Up Hour</label>
                    <input type="number" value={user.sleepEnd} onChange={(e) => setUser({...user, sleepEnd: parseInt(e.target.value)})} />
                  </div>
                </div>
                <button style={{ marginTop: '1rem' }}>Save Preferences</button>
              </form>
            </motion.div>
          )}
        </AnimatePresence>
      </main>

      {/* Log Session Modal */}
      <AnimatePresence>
        {showLogForm && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            style={{ 
              position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', 
              display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
              backdropFilter: 'blur(5px)'
            }}
          >
            <motion.div 
              initial={{ scale: 0.9, y: 20 }}
              animate={{ scale: 1, y: 0 }}
              exit={{ scale: 0.9, y: 20 }}
              className="glass-panel" 
              style={{ width: '90%', maxWidth: '450px' }}
            >
              <h2 style={{ marginBottom: '1rem' }}>Log Daily Usage</h2>
              <form onSubmit={handleLogSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label className="stat-label">Study / Work (mins)</label>
                  <input type="number" value={logData.study} onChange={(e) => setLogData({...logData, study: parseInt(e.target.value)})} />
                </div>
                <div>
                  <label className="stat-label">Social Media (mins)</label>
                  <input type="number" value={logData.social} onChange={(e) => setLogData({...logData, social: parseInt(e.target.value)})} />
                </div>
                <div>
                  <label className="stat-label">Entertainment (mins)</label>
                  <input type="number" value={logData.entertainment} onChange={(e) => setLogData({...logData, entertainment: parseInt(e.target.value)})} />
                </div>
                <div>
                  <label className="stat-label">Peak Usage Hour (0-23)</label>
                  <input type="number" value={logData.peakHour} onChange={(e) => setLogData({...logData, peakHour: parseInt(e.target.value)})} />
                </div>
                <div style={{ display: 'flex', gap: '10px', marginTop: '1rem' }}>
                  <button type="submit" style={{ flex: 1 }}>{loading ? 'Processing...' : 'Submit Log'}</button>
                  <button type="button" onClick={() => setShowLogForm(false)} className="btn-secondary">Cancel</button>
                </div>
              </form>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default App;
