import React, { useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Search, Grid2X2, Heart, ShoppingCart, User, SlidersHorizontal, ChevronDown, Sparkles, Image, Video, Presentation, ChartNoAxesCombined, LayoutDashboard, Plus, ArrowLeft, Upload, Package, Users, DollarSign, ShieldCheck, X, Trash2 } from 'lucide-react';
import './styles.css';

const fallbackAssets = [
  { id: 1, title: 'Analytics Dashboard UI', category: 'Dashboard', image: 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80', price: 12, creator: 'PixelForge', description: 'Clean analytics dashboard template with KPI cards, charts and reusable UI blocks.' },
  { id: 2, title: 'Finance Admin Dashboard', category: 'UI Design', image: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=1200&q=80', price: 18, creator: 'NovaStudio', description: 'Modern finance admin interface for reporting, account monitoring and team workflows.' },
  { id: 3, title: 'Dark Metrics Dashboard', category: 'Data Analysis', image: 'https://images.unsplash.com/photo-1556155092-490a1ba16284?auto=format&fit=crop&w=1200&q=80', price: 15, creator: 'MetricLab', description: 'Dark-mode dashboard with clear charts and performance metrics.' },
  { id: 4, title: 'Purple CRM Dashboard', category: 'Project Management', image: 'https://images.unsplash.com/photo-1543286386-713bdd548da4?auto=format&fit=crop&w=1200&q=80', price: 20, creator: 'AsterUI', description: 'CRM template for pipelines, customer profiles, tasks and revenue tracking.' },
  { id: 5, title: 'Sales Overview Template', category: 'Business Growth', image: 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?auto=format&fit=crop&w=1200&q=80', price: 9, creator: 'DashWorks', description: 'Simple sales overview template for quick business reporting.' },
  { id: 6, title: 'AI Operations Console', category: 'AI', image: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', price: 25, creator: 'AgenticLab', description: 'Operations console for AI products, agents and automation workflows.' },
  { id: 7, title: 'Marketing Insight Board', category: 'Digital Marketing', image: 'https://images.unsplash.com/photo-1553484771-371a605b060b?auto=format&fit=crop&w=1200&q=80', price: 14, creator: 'OrbitStudio', description: 'Campaign and audience insight dashboard for marketing teams.' },
  { id: 8, title: 'Modern Admin Template', category: 'Dashboard', image: 'https://images.unsplash.com/photo-1558655146-9f40138edfeb?auto=format&fit=crop&w=1200&q=80', price: 17, creator: 'FrameLab', description: 'Flexible admin template with widgets, forms and management tables.' }
];

const chips = ['Vectors', 'License', 'AI-generated', 'Orientation', 'Color', 'People', 'File type', 'Style', 'Advanced'];
const categories = [
  ['Create an AI image', Image], ['Create an AI video', Video], ['Create an AI icon', Sparkles], ['Presentation slides', Presentation],
  ['Project management', LayoutDashboard], ['Data analysis', ChartNoAxesCombined], ['UI design', Grid2X2]
];

function App() {
  const [query, setQuery] = useState('Dashboard templates');
  const [favorites, setFavorites] = useState(new Set());
  const [cart, setCart] = useState([]);
  const [view, setView] = useState('market');
  const [selected, setSelected] = useState(null);
  const assets = fallbackAssets;

  const filtered = useMemo(() => {
    const q = query.toLowerCase().replace('templates','').trim();
    if (!q) return assets;
    return assets.filter(a => `${a.title} ${a.category} ${a.creator}`.toLowerCase().includes(q));
  }, [query]);

  const toggleFavorite = (id) => setFavorites(prev => {
    const next = new Set(prev);
    next.has(id) ? next.delete(id) : next.add(id);
    return next;
  });

  const addToCart = (asset) => {
    setCart(prev => prev.some(x => x.id === asset.id) ? prev : [...prev, asset]);
    setView('cart');
  };

  const openProduct = (asset) => {
    setSelected(asset);
    setView('product');
  };

  const cartTotal = cart.reduce((sum, item) => sum + item.price, 0);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <button className="logo" onClick={()=>setView('market')}>A</button>
        <button className="pink-btn" onClick={()=>setView('seller')}><Plus size={20}/></button>
        <nav>
          <button className={view==='market'?'active':''} onClick={()=>setView('market')}><LayoutDashboard size={20}/></button>
          <button onClick={()=>setView('market')}><Search size={20}/></button>
          <button className={view==='seller'?'active':''} onClick={()=>setView('seller')}><Upload size={20}/></button>
          <button><Heart size={20}/></button>
          <button className={view==='cart'?'active':''} onClick={()=>setView('cart')}><ShoppingCart size={20}/><span className="badge">{cart.length}</span></button>
          <button className={view==='admin'?'active':''} onClick={()=>setView('admin')}><ShieldCheck size={20}/></button>
        </nav>
        <div className="sidebar-bottom"><button><User size={20}/></button></div>
      </aside>

      <main>
        <header className="topbar">
          <div className="searchbox"><Search size={18}/><input value={query} onChange={e=>setQuery(e.target.value)} onFocus={()=>setView('market')} /><span>⌘ K</span></div>
          <div className="auth"><a>Pricing</a><a>Log in</a><button>Sign up</button></div>
        </header>

        {view === 'market' && <Marketplace filtered={filtered} favorites={favorites} toggleFavorite={toggleFavorite} openProduct={openProduct} addToCart={addToCart} />}
        {view === 'product' && selected && <Product asset={selected} onBack={()=>setView('market')} addToCart={addToCart} favorite={favorites.has(selected.id)} toggleFavorite={toggleFavorite} />}
        {view === 'cart' && <Cart items={cart} total={cartTotal} remove={(id)=>setCart(prev=>prev.filter(x=>x.id!==id))} onBack={()=>setView('market')} />}
        {view === 'seller' && <SellerDashboard assets={assets} />}
        {view === 'admin' && <AdminDashboard assets={assets} />}
      </main>
    </div>
  );
}

function Marketplace({filtered, favorites, toggleFavorite, openProduct, addToCart}) {
  return <>
    <div className="category-row">{categories.map(([label, Icon]) => <button key={label}><Icon size={15}/>{label}</button>)}</div>
    <section className="content">
      <div className="title-row"><div><h2>Dashboard templates</h2><p>Premium digital assets from independent creators</p></div><button className="sort">Relevance <ChevronDown size={16}/></button></div>
      <div className="filter-row">{chips.map((c,i)=><button key={c}>{i===8 && <SlidersHorizontal size={14}/>} {c}<ChevronDown size={14}/></button>)}</div>
      <div className="promo"><div><strong>Build faster with premium design assets</strong><span> Find templates for dashboards, presentations, AI products and more.</span></div><button>Start selling</button></div>
      <div className="grid-head"><span>{filtered.length} assets</span><div><button className="icon-active"><Grid2X2 size={18}/></button><button><LayoutDashboard size={18}/></button></div></div>
      <div className="asset-grid">{filtered.map(asset => <article className="card" key={asset.id}>
        <div className="preview"><img src={asset.image} alt={asset.title}/><button className="fav" onClick={()=>toggleFavorite(asset.id)}><Heart size={18} fill={favorites.has(asset.id) ? 'currentColor':'none'}/></button><div className="overlay"><button onClick={()=>openProduct(asset)}>Preview</button><button className="buy" onClick={()=>addToCart(asset)}>Buy ${asset.price}</button></div></div>
        <div className="card-info"><div><h3 onClick={()=>openProduct(asset)}>{asset.title}</h3><p>{asset.creator} · {asset.category}</p></div><strong>${asset.price}</strong></div>
      </article>)}</div>
    </section>
  </>;
}

function Product({asset,onBack,addToCart,favorite,toggleFavorite}) {
  return <section className="page"><button className="back" onClick={onBack}><ArrowLeft size={18}/> Back to marketplace</button><div className="product-layout"><div className="product-visual"><img src={asset.image} alt={asset.title}/></div><div className="product-panel"><span className="pill">{asset.category}</span><h1>{asset.title}</h1><p className="muted">by {asset.creator}</p><p className="description">{asset.description}</p><div className="price">${asset.price}</div><button className="primary wide" onClick={()=>addToCart(asset)}><ShoppingCart size={18}/> Add to cart</button><button className="secondary wide" onClick={()=>toggleFavorite(asset.id)}><Heart size={18} fill={favorite?'currentColor':'none'}/> {favorite?'Saved':'Add to favorites'}</button><div className="license-box"><strong>Commercial license included</strong><span>Use this asset in personal and commercial projects.</span></div></div></div></section>;
}

function Cart({items,total,remove,onBack}) {
  return <section className="page"><button className="back" onClick={onBack}><ArrowLeft size={18}/> Continue shopping</button><div className="dashboard-header"><div><h1>Your cart</h1><p>{items.length} item{items.length===1?'':'s'} ready for checkout</p></div></div><div className="cart-layout"><div>{items.length===0?<div className="empty">Your cart is empty.</div>:items.map(item=><div className="cart-item" key={item.id}><img src={item.image}/><div><h3>{item.title}</h3><p>{item.creator}</p></div><strong>${item.price}</strong><button onClick={()=>remove(item.id)}><Trash2 size={18}/></button></div>)}</div><aside className="checkout"><h3>Order summary</h3><div><span>Subtotal</span><strong>${total.toFixed(2)}</strong></div><div><span>Platform fee</span><strong>$0.00</strong></div><hr/><div className="checkout-total"><span>Total</span><strong>${total.toFixed(2)}</strong></div><button className="primary wide" disabled={!items.length}>Proceed to checkout</button></aside></div></section>;
}

function SellerDashboard({assets}) {
  return <section className="page"><div className="dashboard-header"><div><span className="eyebrow">SELLER CENTER</span><h1>Creator dashboard</h1><p>Upload products and manage your marketplace business.</p></div><button className="primary"><Upload size={18}/> Upload new asset</button></div><div className="stat-grid"><Stat icon={Package} label="Published assets" value="12"/><Stat icon={DollarSign} label="Revenue" value="$1,284"/><Stat icon={ShoppingCart} label="Orders" value="96"/><Stat icon={Heart} label="Favorites" value="418"/></div><div className="panel"><div className="panel-head"><h2>Your products</h2><button>View all</button></div><table><thead><tr><th>Product</th><th>Category</th><th>Price</th><th>Status</th></tr></thead><tbody>{assets.slice(0,5).map(a=><tr key={a.id}><td><div className="product-cell"><img src={a.image}/><span>{a.title}</span></div></td><td>{a.category}</td><td>${a.price}</td><td><span className="status">Published</span></td></tr>)}</tbody></table></div></section>;
}

function AdminDashboard({assets}) {
  return <section className="page"><div className="dashboard-header"><div><span className="eyebrow">ADMIN</span><h1>Marketplace control center</h1><p>Review products, creators and marketplace activity.</p></div></div><div className="stat-grid"><Stat icon={Package} label="Assets" value={assets.length}/><Stat icon={Users} label="Creators" value="24"/><Stat icon={ShoppingCart} label="Orders today" value="31"/><Stat icon={DollarSign} label="GMV this month" value="$8,640"/></div><div className="admin-grid"><div className="panel"><div className="panel-head"><h2>Recent submissions</h2><button>Review queue</button></div>{assets.slice(0,4).map(a=><div className="review-row" key={a.id}><img src={a.image}/><div><strong>{a.title}</strong><span>{a.creator}</span></div><button className="approve">Approve</button><button className="reject"><X size={16}/></button></div>)}</div><div className="panel"><div className="panel-head"><h2>Marketplace health</h2></div><div className="health"><div><span>Approval rate</span><strong>94%</strong></div><div><span>Refund rate</span><strong>1.8%</strong></div><div><span>Active sellers</span><strong>24</strong></div><div><span>New users</span><strong>138</strong></div></div></div></div></section>;
}

function Stat({icon:Icon,label,value}) { return <div className="stat"><div className="stat-icon"><Icon size={20}/></div><div><span>{label}</span><strong>{value}</strong></div></div>; }

createRoot(document.getElementById('root')).render(<App/>);
