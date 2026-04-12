import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/shared/Sidebar';
import ExpenseTracker from '../components/trip/ExpenseTracker';
import InviteModal from '../components/trip/InviteModal';
import {
  MapPin, Calendar, Users, Sun, Cloud, Package, Clock, DollarSign,
  Share2, UserPlus, ChevronDown, ChevronUp, Info, Loader,
  Thermometer, Backpack, Star, Navigation, Activity, Utensils, Bed
} from 'lucide-react';
import './TripDetailPage.css';

const ACTIVITY_ICONS = {
  sightseeing: '🏛️', food: '🍽️', transport: '🚂', accommodation: '🏨',
  adventure: '🧗', culture: '🎭', shopping: '🛍️', default: '📍'
};

const BUDGET_COLORS = {
  accommodation: '#5b4fcf', food: '#ec4899', transport: '#f59e0b',
  activities: '#10b981', insurance: '#3b82f6', contingency: '#8b5cf6'
};

export default function TripDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [trip, setTrip] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeSection, setActiveSection] = useState('highlights');
  const [expandedDay, setExpandedDay] = useState(0);
  const [showInvite, setShowInvite] = useState(false);
  const [showShare, setShowShare] = useState(false);

  useEffect(() => { fetchTrip(); }, [id]);

  const fetchTrip = async () => {
    try {
      const res = await axios.get(`/api/trips/${id}`);
      setTrip(res.data);
    } catch { navigate('/dashboard'); }
    finally { setLoading(false); }
  };

  const isOwner = trip?.owner?._id === user?.id || trip?.owner === user?.id;
  const currency = trip?.budget?.currency === 'INR' ? '₹' : '$';

  if (loading) return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        <div className="trip-loading">
          <Loader size={40} className="spin-icon" />
          <p>Loading your trip...</p>
        </div>
      </main>
    </div>
  );

  const NAV_SECTIONS = [
    { id: 'highlights', label: 'Trip Highlights', icon: Star },
    { id: 'weather', label: 'Weather Analysis', icon: Sun },
    { id: 'itinerary', label: 'Itinerary', icon: Calendar },
    { id: 'budget', label: 'Budget Range', icon: DollarSign },
    { id: 'packing', label: 'Packing Checklist', icon: Backpack },
    { id: 'expenses', label: 'Expense Tracker', icon: Activity },
  ];

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        <div className="trip-detail-layout">
          {/* Left Sidebar */}
          <aside className="trip-sidebar">
            <div className="trip-sidebar-header">
              <div className="ts-label">Your Plan</div>
            </div>
            <nav className="trip-sidebar-nav">
              {NAV_SECTIONS.map(({ id: sid, label, icon: Icon }) => (
                <button key={sid} className={`ts-nav-item ${activeSection === sid ? 'active' : ''}`}
                  onClick={() => setActiveSection(sid)}>
                  <Icon size={16} />
                  <span>{label}</span>
                  {sid === 'itinerary' && <span className="ts-badge">{trip?.itinerary?.length || 0}</span>}
                </button>
              ))}
            </nav>

            <div className="trip-sidebar-divider"></div>
            <div className="ts-label" style={{ padding: '0 16px', fontSize: 11 }}>Control Center</div>

            {isOwner && (
              <button className="ts-nav-item" onClick={() => setShowInvite(true)}>
                <UserPlus size={16} /> <span>Collaborate</span>
              </button>
            )}
            <button className="ts-nav-item" onClick={() => setShowShare(true)}>
              <Share2 size={16} /> <span>Share Plan</span>
            </button>
            <button className="ts-nav-item" onClick={() => setActiveSection('expenses')}>
              <DollarSign size={16} /> <span>Expense Tracker</span>
            </button>

            {/* Collaborators */}
            {trip?.collaborators?.length > 0 && (
              <div className="ts-collaborators">
                <div className="ts-label" style={{ fontSize: 11 }}>Collaborators</div>
                {trip.collaborators.map(c => (
                  <div key={c.user?._id} className="ts-collab-item">
                    <div className="ts-collab-avatar">{c.user?.name?.charAt(0)}</div>
                    <div>
                      <div className="ts-collab-name">{c.user?.name}</div>
                      <div className="ts-collab-role">{c.role}</div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </aside>

          {/* Main Content */}
          <div className="trip-main">
            {/* Cover */}
            <div className="trip-cover">
              <div className="trip-cover-bg" style={{ background: `linear-gradient(135deg, #1a1a4e, #2d1b69)` }}>
                <div className="trip-cover-overlay"></div>
                <div className="trip-cover-content">
                  <h1 className="trip-cover-title">{trip?.destination}</h1>
                  <div className="trip-cover-meta">
                    <span><Calendar size={14} /> {trip?.startDate} — {trip?.endDate}</span>
                    <span><Users size={14} /> {trip?.passengers} passenger(s)</span>
                    <span><Navigation size={14} /> From {trip?.startCity}</span>
                    {trip?.duration && <span><Clock size={14} /> {trip.duration} days</span>}
                  </div>
                  {trip?.themes?.length > 0 && (
                    <div className="trip-cover-themes">
                      {trip.themes.map(t => <span key={t} className="cover-theme-tag">{t}</span>)}
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Section Content */}
            <div className="trip-section-content">
              {activeSection === 'highlights' && <HighlightsSection trip={trip} />}
              {activeSection === 'weather' && <WeatherSection trip={trip} />}
              {activeSection === 'itinerary' && <ItinerarySection trip={trip} expandedDay={expandedDay} setExpandedDay={setExpandedDay} currency={currency} />}
              {activeSection === 'budget' && <BudgetSection trip={trip} currency={currency} />}
              {activeSection === 'packing' && <PackingSection trip={trip} />}
              {activeSection === 'expenses' && <ExpenseTracker tripId={id} trip={trip} user={user} currency={currency} />}
            </div>
          </div>
        </div>
      </main>

      {showInvite && <InviteModal tripId={id} onClose={() => setShowInvite(false)} />}

      {showShare && (
        <div className="modal-overlay" onClick={() => setShowShare(false)}>
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 400 }}>
            <h3 className="modal-title">Share Your Travel Plans ✈️</h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: 20 }}>
              Help fellow travelers by sharing your travel plans! Your experiences could inspire and guide others.
            </p>
            <div style={{ background: 'var(--bg-soft)', padding: 16, borderRadius: 12, fontFamily: 'monospace', fontSize: 13, wordBreak: 'break-all', marginBottom: 16 }}>
              {window.location.href}
            </div>
            <button className="btn btn-primary btn-full" onClick={() => { navigator.clipboard.writeText(window.location.href); setShowShare(false); }}>
              Copy Link
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function HighlightsSection({ trip }) {
  return (
    <div className="section-block animate-fadeIn">
      <div className="section-block-header">
        <Star size={20} />
        <h2>Trip Highlights</h2>
      </div>
      {trip?.highlights?.length > 0 ? (
        <div className="highlights-grid">
          {trip.highlights.map((h, i) => (
            <div key={i} className="highlight-item">
              <div className="highlight-num">{String(i + 1).padStart(2, '0')}</div>
              <p>{h}</p>
            </div>
          ))}
        </div>
      ) : (
        <div className="section-empty"><Info size={24} /><p>Highlights being generated...</p></div>
      )}

      {/* Quick summary cards */}
      <div className="trip-quick-stats">
        {[
          { icon: MapPin, label: 'Destination', value: trip?.destination, color: '#5b4fcf' },
          { icon: Calendar, label: 'Duration', value: `${trip?.duration || 0} Days`, color: '#ec4899' },
          { icon: Users, label: 'Travelers', value: `${trip?.passengers || 1} Person(s)`, color: '#f59e0b' },
          { icon: Bed, label: 'Stay', value: trip?.preferences?.accommodation || 'Flexible', color: '#10b981' },
        ].map(({ icon: Icon, label, value, color }) => (
          <div key={label} className="quick-stat-card">
            <div className="quick-stat-icon" style={{ color, background: `${color}15` }}><Icon size={18} /></div>
            <div className="quick-stat-label">{label}</div>
            <div className="quick-stat-value">{value}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

function WeatherSection({ trip }) {
  const w = trip?.weatherInfo;
  return (
    <div className="section-block animate-fadeIn">
      <div className="section-block-header"><Sun size={20} /><h2>Weather Analysis</h2></div>
      {w?.summary ? (
        <>
          <div className="weather-hero-card">
            <div className="weather-icon">🌤️</div>
            <div>
              <div className="weather-temp">{w.avgTemp}</div>
              <div className="weather-cond">{w.conditions}</div>
              <p className="weather-summary">{w.summary}</p>
            </div>
          </div>
          {w.tips?.length > 0 && (
            <div className="weather-tips">
              <h4>Travel Tips</h4>
              {w.tips.map((tip, i) => (
                <div key={i} className="weather-tip"><Cloud size={14} />{tip}</div>
              ))}
            </div>
          )}
        </>
      ) : (
        <div className="section-empty"><Sun size={24} /><p>Weather analysis unavailable</p></div>
      )}
    </div>
  );
}

function ItinerarySection({ trip, expandedDay, setExpandedDay, currency }) {
  return (
    <div className="section-block animate-fadeIn">
      <div className="section-block-header"><Calendar size={20} /><h2>Itinerary</h2></div>
      {trip?.itinerary?.length > 0 ? (
        <div className="itinerary-list">
          {trip.itinerary.map((day, i) => (
            <div key={i} className="itinerary-day">
              <div className="day-header" onClick={() => setExpandedDay(expandedDay === i ? -1 : i)}>
                <div className="day-number">Day {day.day}</div>
                <div className="day-info">
                  <div className="day-title">{day.title}</div>
                  <div className="day-date">{day.date}</div>
                </div>
                <div className="day-activities-count">{day.activities?.length || 0} activities</div>
                {expandedDay === i ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
              </div>
              {expandedDay === i && (
                <div className="day-activities animate-fadeIn">
                  {day.activities?.map((act, j) => (
                    <div key={j} className="activity-item">
                      <div className="activity-time">{act.time}</div>
                      <div className="activity-icon-wrap">
                        <div className="activity-icon">{ACTIVITY_ICONS[act.type] || ACTIVITY_ICONS.default}</div>
                        {j < day.activities.length - 1 && <div className="activity-line"></div>}
                      </div>
                      <div className="activity-content">
                        <div className="activity-name">{act.name}</div>
                        <div className="activity-location"><MapPin size={11} /> {act.location}</div>
                        <p className="activity-desc">{act.description}</p>
                        {act.tips && <div className="activity-tip">💡 {act.tips}</div>}
                        <div className="activity-meta">
                          {act.duration && <span><Clock size={11} /> {act.duration}</span>}
                          {act.cost > 0 && <span><DollarSign size={11} /> {currency}{act.cost?.toLocaleString()}</span>}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <div className="section-empty"><Calendar size={24} /><p>Itinerary being generated...</p></div>
      )}
    </div>
  );
}

function BudgetSection({ trip, currency }) {
  const b = trip?.budget;
  const breakdown = b?.breakdown || {};
  const total = b?.estimatedMax || 0;

  return (
    <div className="section-block animate-fadeIn">
      <div className="section-block-header"><DollarSign size={20} /><h2>Budget Range</h2></div>
      <div className="budget-total-card">
        <div className="budget-total-label">TOTAL ESTIMATED COST</div>
        <div className="budget-total-range">
          {currency}{b?.estimated?.toLocaleString() || 0} — {currency}{b?.estimatedMax?.toLocaleString() || 0}
        </div>
        <div className="budget-currency-badge">{b?.currency || 'INR'}</div>
      </div>

      <div className="budget-tabs">
        {Object.entries(breakdown).map(([key, val]) => {
          if (!val || (!val.min && !val.max)) return null;
          const pct = total > 0 ? Math.round(((val.max || 0) / total) * 100) : 0;
          return (
            <div key={key} className="budget-breakdown-item">
              <div className="budget-item-header">
                <div className="budget-item-name">
                  <div className="budget-item-dot" style={{ background: BUDGET_COLORS[key] || '#5b4fcf' }}></div>
                  {key.charAt(0).toUpperCase() + key.slice(1)}
                  <span className="budget-item-pct">~{pct}% of total budget</span>
                </div>
                <div className="budget-item-range">{currency}{val.min?.toLocaleString()} — {currency}{val.max?.toLocaleString()}</div>
              </div>
              <div className="budget-bar">
                <div className="budget-bar-fill" style={{ width: `${pct}%`, background: BUDGET_COLORS[key] || '#5b4fcf' }}></div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function PackingSection({ trip }) {
  const [checked, setChecked] = useState({});
  const items = trip?.packingList || [];

  return (
    <div className="section-block animate-fadeIn">
      <div className="section-block-header"><Backpack size={20} /><h2>Packing Checklist</h2></div>
      {items.length > 0 ? (
        <>
          <div className="packing-progress">
            <div className="packing-bar">
              <div className="packing-bar-fill" style={{ width: `${(Object.values(checked).filter(Boolean).length / items.length) * 100}%` }}></div>
            </div>
            <span>{Object.values(checked).filter(Boolean).length}/{items.length} packed</span>
          </div>
          <div className="packing-grid">
            {items.map((item, i) => (
              <div key={i} className={`packing-item ${checked[i] ? 'checked' : ''}`} onClick={() => setChecked(c => ({ ...c, [i]: !c[i] }))}>
                <div className="packing-checkbox">{checked[i] && '✓'}</div>
                <span>{item}</span>
              </div>
            ))}
          </div>
        </>
      ) : (
        <div className="section-empty"><Package size={24} /><p>Packing list unavailable</p></div>
      )}
    </div>
  );
}
