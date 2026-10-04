import React, { useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Search, Grid2X2, Heart, ShoppingCart, User, SlidersHorizontal, ChevronDown, Sparkles, Image, Video, Presentation, ChartNoAxesCombined, LayoutDashboard, Plus } from 'lucide-react';
import './styles.css';

const fallbackAssets = [
  { id: 1, title: 'Analytics Dashboard UI', category: 'Dashboard', image: 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80', price: 12, creator: 'PixelForge' },
  { id: 2, title: 'Finance Admin Dashboard', category: 'UI Design', image: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=1200&q=80', price: 18, creator: 'NovaStudio' },
  { id: 3, title: 'Dark Metrics Dashboard', category: 'Data Analysis', image: 'https://images.unsplash.com/photo-1556155092-490a1ba16284?auto=format&fit=crop&w=1200&q=80', price: 15, creator: 'MetricLab' },
  { id: 4, title: 'Purple CRM Dashboard', category: 'Project Management', image: 'https://images.unsplash.com/photo-1543286386-713bdd548da4?auto=format&fit=crop&w=1200&q=80', price: 20, creator: 'AsterUI' },
  { id: 5, title: 'Sales Overview Template', category: 'Business Growth', image: 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?auto=format&fit=crop&w=1200&q=80', price: 9, creator: 'DashWorks' },
  { id: 6, title: 'AI Operations Console', category: 'AI', image: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', price: 25, creator: 'AgenticLab' },
  { id: 7, title: 'Marketing Insight Board', category: 'Digital Marketing', image: 'https://images.unsplash.com/photo-1553484771-371a605b060b?auto=format&fit=crop&w=1200&q=80', price: 14, creator: 'OrbitStudio' },
  { id: 8, title: 'Modern Admin Template', category: 'Dashboard', image: 'https://images.unsplash.com/photo-1558655146-9f40138edfeb?auto=format&fit=crop&w=1200&q=80', price: 17, creator: 'FrameLab' }
];

const chips = ['Vectors', 'License', 'AI-generated', 'Orientation', 'Color', 'People', 'File type', 'Style', 'Advanced'];
const categories = [
  ['Create an AI image', Image], ['Create an AI video', Video], ['Create an AI icon', Sparkles], ['Presentation slides', Presentation],
  ['Project management', LayoutDashboard], ['Data analysis', ChartNoAxesCombined], ['UI design', Grid2X2]
];

function App() {
  const [query, setQuery] = useState('Dashboard templates');
  const [favorites, setFavorites] = useState(new Set());
  const assets = fallbackAssets;

  const filtered = useMemo(() => {
    const q = query.toLowerCase().replace('templates','').trim();
    if (!q) return assets;
    return assets.filter(a => `${a.title} ${a.category} ${a.creator}`.toLowerCase().includes(q));
  }, [query]);

  const toggleFavorite = (id) => {
    setFavorites(prev => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="logo">A</div>
        <button className="pink-btn"><Plus size={20}/></button>
        <nav>
          <button><LayoutDashboard size={20}/></button>
          <button><Search size={20}/></button>
          <button className="active"><Grid2X2 size={20}/></button>
          <button><Heart size={20}/></button>
          <button><ShoppingCart size={20}/></button>
        </nav>
        <div className="sidebar-bottom"><button><User size={20}/></button></div>
      </aside>

      <main>
        <header className="topbar">
          <div className="searchbox"><Search size={18}/><input value={query} onChange={e=>setQuery(e.target.value)} /><span>⌘ K</span></div>
          <div className="auth"><a>Pricing</a><a>Log in</a><button>Sign up</button></div>
        </header>

        <div className="category-row">
          {categories.map(([label, Icon]) => <button key={label}><Icon size={15}/>{label}</button>)}
        </div>

        <section className="content">
          <div className="title-row"><div><h2>Dashboard templates</h2><p>Premium digital assets from independent creators</p></div><button className="sort">Relevance <ChevronDown size={16}/></button></div>
          <div className="filter-row">
            {chips.map((c,i)=><button key={c}>{i===8 && <SlidersHorizontal size={14}/>} {c}<ChevronDown size={14}/></button>)}
          </div>

          <div className="promo"><div><strong>Build faster with premium design assets</strong><span> Find templates for dashboards, presentations, AI products and more.</span></div><button>Start selling</button></div>

          <div className="grid-head"><span>{filtered.length} assets</span><div><button className="icon-active"><Grid2X2 size={18}/></button><button><LayoutDashboard size={18}/></button></div></div>

          <div className="asset-grid">
            {filtered.map(asset => (
              <article className="card" key={asset.id}>
                <div className="preview">
                  <img src={asset.image} alt={asset.title}/>
                  <button className="fav" onClick={()=>toggleFavorite(asset.id)}><Heart size={18} fill={favorites.has(asset.id) ? 'currentColor':'none'}/></button>
                  <div className="overlay"><button>Preview</button><button className="buy">Buy ${asset.price}</button></div>
                </div>
                <div className="card-info"><div><h3>{asset.title}</h3><p>{asset.creator} · {asset.category}</p></div><strong>${asset.price}</strong></div>
              </article>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}

createRoot(document.getElementById('root')).render(<App/>);
