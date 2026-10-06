import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Search, Grid2X2, Heart, ShoppingCart, User, SlidersHorizontal, ChevronDown, Sparkles, Image, Video, Presentation, ChartNoAxesCombined, LayoutDashboard, Plus, ArrowLeft, Upload, Package, Users, DollarSign, ShieldCheck, X, Trash2, CheckCircle2, LogOut } from 'lucide-react';
import './styles.css';

const API = (import.meta.env.VITE_API_URL || 'https://ai-asset-marketplace-api.onrender.com').replace(/\/$/, '');
const fallbackAssets = [
  { id: 1, title: 'Analytics Dashboard UI', category: 'Dashboard', image: 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80', price: 12, creator: 'PixelForge', description: 'Clean analytics dashboard template with KPI cards, charts and reusable UI blocks.', status: 'PUBLISHED' },
  { id: 2, title: 'Finance Admin Dashboard', category: 'UI Design', image: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=1200&q=80', price: 18, creator: 'NovaStudio', description: 'Modern finance admin interface for reporting, account monitoring and team workflows.', status: 'PUBLISHED' },
  { id: 3, title: 'Dark Metrics Dashboard', category: 'Data Analysis', image: 'https://images.unsplash.com/photo-1556155092-490a1ba16284?auto=format&fit=crop&w=1200&q=80', price: 15, creator: 'MetricLab', description: 'Dark-mode dashboard with clear charts and performance metrics.', status: 'PUBLISHED' },
  { id: 4, title: 'Purple CRM Dashboard', category: 'Project Management', image: 'https://images.unsplash.com/photo-1543286386-713bdd548da4?auto=format&fit=crop&w=1200&q=80', price: 20, creator: 'AsterUI', description: 'CRM template for pipelines, customer profiles, tasks and revenue tracking.', status: 'PUBLISHED' },
  { id: 5, title: 'Sales Overview Template', category: 'Business Growth', image: 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?auto=format&fit=crop&w=1200&q=80', price: 9, creator: 'DashWorks', description: 'Simple sales overview template for quick business reporting.', status: 'PUBLISHED' },
  { id: 6, title: 'AI Operations Console', category: 'AI', image: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', price: 25, creator: 'AgenticLab', description: 'Operations console for AI products, agents and automation workflows.', status: 'PUBLISHED' },
  { id: 7, title: 'Marketing Insight Board', category: 'Digital Marketing', image: 'https://images.unsplash.com/photo-1553484771-371a605b060b?auto=format&fit=crop&w=1200&q=80', price: 14, creator: 'OrbitStudio', description: 'Campaign and audience insight dashboard for marketing teams.', status: 'PUBLISHED' },
  { id: 8, title: 'Modern Admin Template', category: 'Dashboard', image: 'https://images.unsplash.com/photo-1558655146-9f40138edfeb?auto=format&fit=crop&w=1200&q=80', price: 17, creator: 'FrameLab', description: 'Flexible admin template with widgets, forms and management tables.', status: 'PUBLISHED' }
];

const chips = ['Vectors', 'License', 'AI-generated', 'Orientation', 'Color', 'People', 'File type', 'Style', 'Advanced'];
const categories = [
  ['Create an AI image', Image], ['Create an AI video', Video], ['Create an AI icon', Sparkles], ['Presentation slides', Presentation],
  ['Project management', LayoutDashboard], ['Data analysis', ChartNoAxesCombined], ['UI design', Grid2X2]
];

const readJson = (key, fallback) => { try { return JSON.parse(localStorage.getItem(key)) ?? fallback; } catch { return fallback; } };
const apiFetch = async (path, init) => {
  if (!API) throw new Error('API not configured');
  const r = await fetch(`${API}${path}`, { ...init, headers: { 'Content-Type': 'application/json', ...(init?.headers || {}) } });
  if (!r.ok) throw new Error(`API ${r.status}`);
  return r.json();
};

function App() {
  const [query, setQuery] = useState('Dashboard templates');
  const [favorites, setFavorites] = useState(() => new Set(readJson('market-favorites', [])));
  const [cart, setCart] = useState(() => readJson('market-cart', []));
  const [assets, setAssets] = useState(fallbackAssets);
  const [view, setView] = useState('market');
  const [selected, setSelected] = useState(null);
  const [user, setUser] = useState(() => readJson('market-user', null));
  const [authOpen, setAuthOpen] = useState(false);
  const [notice, setNotice] = useState('');

  useEffect(() => { localStorage.setItem('market-favorites', JSON.stringify([...favorites])); }, [favorites]);
  useEffect(() => { localStorage.setItem('market-cart', JSON.stringify(cart)); }, [cart]);
  useEffect(() => { user ? localStorage.setItem('market-user', JSON.stringify(user)) : localStorage.removeItem('market-user'); }, [user]);
  useEffect(() => {
    if (!API) return;
    apiFetch('/api/assets').then(remote => {
      if (Array.isArray(remote) && remote.length) setAssets(remote.map(a => ({ ...a, description: a.description || 'Premium marketplace asset.', status: a.status || 'PUBLISHED' })));
    }).catch(() => {});
  }, []);

  const filtered = useMemo(() => {
    const q = query.toLowerCase().replace('templates','').trim();
    const visible = view === 'favorites' ? assets.filter(a => favorites.has(a.id)) : assets.filter(a => a.status !== 'REJECTED');
    if (!q) return visible;
    return visible.filter(a => `${a.title} ${a.category} ${a.creator}`.toLowerCase().includes(q));
  }, [query, assets, favorites, view]);

  const toggleFavorite = (id) => setFavorites(prev => {
    const next = new Set(prev); next.has(id) ? next.delete(id) : next.add(id); return next;
  });
  const addToCart = (asset) => { setCart(prev => prev.some(x => x.id === asset.id) ? prev : [...prev, asset]); setView('cart'); };
  const openProduct = (asset) => { setSelected(asset); setView('product'); };
  const cartTotal = cart.reduce((sum, item) => sum + Number(item.price || 0), 0);
  const flash = (text) => { setNotice(text); setTimeout(() => setNotice(''), 2600); };

  const checkout = () => {
    if (!cart.length) return;
    setView('payway');
  };

  const completePaidOrder = (transactionId) => {
    const orders = readJson('market-orders', []);
    orders.unshift({ id: transactionId || Date.now(), createdAt: new Date().toISOString(), total: cartTotal, items: cart, paymentProvider: 'ABA PayWay', paymentStatus: 'PAID' });
    localStorage.setItem('market-orders', JSON.stringify(orders));
    setCart([]);
    setView('orders');
    flash('Payment confirmed. Order completed.');
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <button className="logo" onClick={()=>setView('market')}>A</button>
        <button className="pink-btn" onClick={()=>setView('seller')}><Plus size={20}/></button>
        <nav>
          <button className={view==='market'?'active':''} onClick={()=>setView('market')}><LayoutDashboard size={20}/></button>
          <button onClick={()=>setView('market')}><Search size={20}/></button>
          <button className={view==='seller'?'active':''} onClick={()=>setView('seller')}><Upload size={20}/></button>
          <button className={view==='favorites'?'active':''} onClick={()=>setView('favorites')}><Heart size={20}/></button>
          <button className={view==='cart'?'active':''} onClick={()=>setView('cart')}><ShoppingCart size={20}/><span className="badge">{cart.length}</span></button>
          <button className={view==='admin'?'active':''} onClick={()=>setView('admin')}><ShieldCheck size={20}/></button>
        </nav>
        <div className="sidebar-bottom"><button onClick={()=>user ? setView('orders') : setAuthOpen(true)}><User size={20}/></button></div>
      </aside>

      <main>
        <header className="topbar">
          <div className="searchbox"><Search size={18}/><input value={query} onChange={e=>setQuery(e.target.value)} onFocus={()=>setView('market')} /><span>⌘ K</span></div>
          <div className="auth">{user ? <><span className="signed-in">{user.name}</span><button onClick={()=>{setUser(null);flash('Signed out.')}}><LogOut size={15}/> Logout</button></> : <><a onClick={()=>setAuthOpen(true)}>Log in</a><button onClick={()=>setAuthOpen(true)}>Sign up</button></>}</div>
        </header>

        {(view === 'market' || view === 'favorites') && <Marketplace title={view==='favorites'?'Your favorites':'Dashboard templates'} filtered={filtered} favorites={favorites} toggleFavorite={toggleFavorite} openProduct={openProduct} addToCart={addToCart} />}
        {view === 'product' && selected && <Product asset={selected} onBack={()=>setView('market')} addToCart={addToCart} favorite={favorites.has(selected.id)} toggleFavorite={toggleFavorite} />}
        {view === 'cart' && <Cart items={cart} total={cartTotal} remove={(id)=>setCart(prev=>prev.filter(x=>x.id!==id))} onBack={()=>setView('market')} checkout={checkout} />}
        {view === 'payway' && <PayWayPayment items={cart} total={cartTotal} user={user} onBack={()=>setView('cart')} onPaid={completePaidOrder} />}
        {view === 'seller' && <SellerDashboard assets={assets} setAssets={setAssets} flash={flash} />}
        {view === 'admin' && <AdminDashboard assets={assets} setAssets={setAssets} flash={flash} />}
        {view === 'orders' && <Orders onBack={()=>setView('market')} />}
      </main>
      {authOpen && <AuthModal onClose={()=>setAuthOpen(false)} onLogin={(u)=>{setUser(u);setAuthOpen(false);flash(`Welcome, ${u.name}.`);}}/>}
      {notice && <div className="toast"><CheckCircle2 size={18}/>{notice}</div>}
    </div>
  );
}

function Marketplace({title, filtered, favorites, toggleFavorite, openProduct, addToCart}) {
  return <>
    <div className="category-row">{categories.map(([label, Icon]) => <button key={label}><Icon size={15}/>{label}</button>)}</div>
    <section className="content">
      <div className="title-row"><div><h2>{title}</h2><p>Premium digital assets from independent creators</p></div><button className="sort">Relevance <ChevronDown size={16}/></button></div>
      <div className="filter-row">{chips.map((c,i)=><button key={c}>{i===8 && <SlidersHorizontal size={14}/>} {c}<ChevronDown size={14}/></button>)}</div>
      <div className="promo"><div><strong>Build faster with premium design assets</strong><span> Find templates for dashboards, presentations, AI products and more.</span></div><button>Start selling</button></div>
      <div className="grid-head"><span>{filtered.length} assets</span><div><button className="icon-active"><Grid2X2 size={18}/></button><button><LayoutDashboard size={18}/></button></div></div>
      {filtered.length === 0 ? <div className="empty">No assets match this view.</div> : <div className="asset-grid">{filtered.map(asset => <article className="card" key={asset.id}>
        <div className="preview"><img src={asset.image} alt={asset.title}/><button className="fav" onClick={()=>toggleFavorite(asset.id)}><Heart size={18} fill={favorites.has(asset.id) ? 'currentColor':'none'}/></button><div className="overlay"><button onClick={()=>openProduct(asset)}>Preview</button><button className="buy" onClick={()=>addToCart(asset)}>Buy ${asset.price}</button></div></div>
        <div className="card-info"><div><h3 onClick={()=>openProduct(asset)}>{asset.title}</h3><p>{asset.creator} · {asset.category}</p></div><strong>${asset.price}</strong></div>
      </article>)}</div>}
    </section>
  </>;
}

function Product({asset,onBack,addToCart,favorite,toggleFavorite}) {
  return <section className="page"><button className="back" onClick={onBack}><ArrowLeft size={18}/> Back to marketplace</button><div className="product-layout"><div className="product-visual"><img src={asset.image} alt={asset.title}/></div><div className="product-panel"><span className="pill">{asset.category}</span><h1>{asset.title}</h1><p className="muted">by {asset.creator}</p><p className="description">{asset.description}</p><div className="price">${asset.price}</div><button className="primary wide" onClick={()=>addToCart(asset)}><ShoppingCart size={18}/> Add to cart</button><button className="secondary wide" onClick={()=>toggleFavorite(asset.id)}><Heart size={18} fill={favorite?'currentColor':'none'}/> {favorite?'Saved':'Add to favorites'}</button><div className="license-box"><strong>Commercial license included</strong><span>Use this asset in personal and commercial projects.</span></div></div></div></section>;
}

function Cart({items,total,remove,onBack,checkout}) {
  return <section className="page"><button className="back" onClick={onBack}><ArrowLeft size={18}/> Continue shopping</button><div className="dashboard-header"><div><h1>Your cart</h1><p>{items.length} item{items.length===1?'':'s'} ready for checkout</p></div></div><div className="cart-layout"><div>{items.length===0?<div className="empty">Your cart is empty.</div>:items.map(item=><div className="cart-item" key={item.id}><img src={item.image} alt=""/><div><h3>{item.title}</h3><p>{item.creator}</p></div><strong>${item.price}</strong><button onClick={()=>remove(item.id)}><Trash2 size={18}/></button></div>)}</div><aside className="checkout"><h3>Order summary</h3><div><span>Subtotal</span><strong>${total.toFixed(2)}</strong></div><div><span>Platform fee</span><strong>$0.00</strong></div><hr/><div className="checkout-total"><span>Total</span><strong>${total.toFixed(2)}</strong></div><button className="primary wide" disabled={!items.length} onClick={checkout}>Pay with ABA PayWay</button><small>Secure payment is processed by ABA PayWay. The marketplace does not collect card or ABA account credentials.</small></aside></div></section>;
}

function PayWayPayment({items,total,user,onBack,onPaid}) {
  const [state,setState]=useState({loading:true,configured:false,environment:'sandbox',checkoutUrl:'',message:''});
  const [starting,setStarting]=useState(false);
  useEffect(()=>{
    apiFetch('/api/payway/config').then(data=>setState({loading:false,...data})).catch(()=>setState({loading:false,configured:false,environment:'sandbox',checkoutUrl:'',message:'Payment API is waking up or unavailable.'}));
  },[]);
  const startPayment=async()=>{
    setStarting(true);
    try{
      const data=await apiFetch('/api/payway/checkout',{method:'POST',body:JSON.stringify({items:items.map(({id,title,price})=>({id,title,price})),customer:{name:user?.name||'',email:user?.email||''},amount:total,currency:'USD'})});
      if(data.checkoutUrl && data.configured){ window.location.href=data.checkoutUrl; return; }
      setState(prev=>({...prev,...data,loading:false}));
    }catch{ setState(prev=>({...prev,loading:false,message:'Unable to start ABA PayWay checkout. Please try again.'})); }
    finally{ setStarting(false); }
  };
  return <section className="page payway-page"><button className="back" onClick={onBack}><ArrowLeft size={18}/> Back to cart</button><div className="payway-shell"><div className="payway-card"><div className="payway-brand"><div className="aba-mark">ABA</div><div><strong>ABA PayWay</strong><span>Secure Checkout</span></div></div><div className="payway-badge">{String(state.environment||'sandbox').toUpperCase()}</div><h1>Complete your payment</h1><p className="muted">You will continue to ABA PayWay to choose ABA PAY, KHQR, card, or another enabled payment method.</p><div className="payway-summary"><div><span>Items</span><strong>{items.length}</strong></div><div><span>Currency</span><strong>USD</strong></div><div className="payway-total"><span>Total</span><strong>${Number(total).toFixed(2)}</strong></div></div>{state.loading?<div className="payway-status">Checking ABA PayWay connection…</div>:state.configured?<div className="payway-status ready">ABA PayWay sandbox is connected and ready.</div>:<div className="payway-status warning"><strong>Sandbox integration is ready for credentials.</strong><span>{state.message||'Add the ABA PayWay Merchant ID and API key to the backend environment to enable real sandbox payment.'}</span></div>}<button className="payway-pay" disabled={state.loading||starting||!state.configured} onClick={startPayment}>{starting?'Opening ABA PayWay…':state.configured?'Pay 
  const [form,setForm]=useState({title:'',category:'Dashboard',price:'12',image:'https://images.unsplash.com/photo-1558655146-9f40138edfeb?auto=format&fit=crop&w=1200&q=80',description:''});
  const submit=async(e)=>{e.preventDefault();const asset={...form,id:Date.now(),price:Number(form.price),creator:'You',status:'PENDING_REVIEW'};try{await apiFetch('/api/seller/assets',{method:'POST',body:JSON.stringify(form)});}catch{}setAssets(prev=>[asset,...prev]);setForm({...form,title:'',description:''});flash('Asset submitted for admin review.');};
  return <section className="page"><div className="dashboard-header"><div><span className="eyebrow">SELLER CENTER</span><h1>Creator dashboard</h1><p>Upload products and manage your marketplace business.</p></div></div><div className="seller-layout"><form className="panel upload-form" onSubmit={submit}><h2>Upload new asset</h2><label>Title<input required value={form.title} onChange={e=>setForm({...form,title:e.target.value})}/></label><label>Category<select value={form.category} onChange={e=>setForm({...form,category:e.target.value})}><option>Dashboard</option><option>UI Design</option><option>AI</option><option>Data Analysis</option></select></label><label>Price (USD)<input required type="number" min="0" value={form.price} onChange={e=>setForm({...form,price:e.target.value})}/></label><label>Preview image URL<input required value={form.image} onChange={e=>setForm({...form,image:e.target.value})}/></label><label>Description<textarea value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label><button className="primary" type="submit"><Upload size={18}/> Submit for review</button></form><div><div className="stat-grid"><Stat icon={Package} label="Assets" value={assets.length}/><Stat icon={DollarSign} label="Revenue" value="$1,284"/><Stat icon={ShoppingCart} label="Orders" value="96"/><Stat icon={Heart} label="Favorites" value="418"/></div><div className="panel"><div className="panel-head"><h2>Your products</h2></div><table><thead><tr><th>Product</th><th>Category</th><th>Price</th><th>Status</th></tr></thead><tbody>{assets.slice(0,8).map(a=><tr key={a.id}><td><div className="product-cell"><img src={a.image} alt=""/><span>{a.title}</span></div></td><td>{a.category}</td><td>${a.price}</td><td><span className={`status ${a.status==='PENDING_REVIEW'?'pending':''}`}>{a.status||'PUBLISHED'}</span></td></tr>)}</tbody></table></div></div></div></section>;
}

function AdminDashboard({assets,setAssets,flash}) {
  const pending=assets.filter(a=>a.status==='PENDING_REVIEW');
  const approve=async(id)=>{try{await apiFetch(`/api/admin/assets/${id}/approve`,{method:'POST'});}catch{}setAssets(prev=>prev.map(a=>a.id===id?{...a,status:'PUBLISHED'}:a));flash('Asset approved and published.');};
  const reject=(id)=>{setAssets(prev=>prev.map(a=>a.id===id?{...a,status:'REJECTED'}:a));flash('Asset rejected.');};
  return <section className="page"><div className="dashboard-header"><div><span className="eyebrow">ADMIN</span><h1>Marketplace control center</h1><p>Review products, creators and marketplace activity.</p></div></div><div className="stat-grid"><Stat icon={Package} label="Assets" value={assets.length}/><Stat icon={Users} label="Creators" value="24"/><Stat icon={ShoppingCart} label="Pending review" value={pending.length}/><Stat icon={DollarSign} label="GMV this month" value="$8,640"/></div><div className="admin-grid"><div className="panel"><div className="panel-head"><h2>Review queue</h2><span>{pending.length} pending</span></div>{pending.length===0?<div className="empty compact">No pending submissions.</div>:pending.map(a=><div className="review-row" key={a.id}><img src={a.image} alt=""/><div><strong>{a.title}</strong><span>{a.creator}</span></div><button className="approve" onClick={()=>approve(a.id)}>Approve</button><button className="reject" onClick={()=>reject(a.id)}><X size={16}/></button></div>)}</div><div className="panel"><div className="panel-head"><h2>Marketplace health</h2></div><div className="health"><div><span>Approval rate</span><strong>94%</strong></div><div><span>Refund rate</span><strong>1.8%</strong></div><div><span>Active sellers</span><strong>24</strong></div><div><span>New users</span><strong>138</strong></div></div></div></div></section>;
}

function Orders({onBack}) { const orders=readJson('market-orders',[]); return <section className="page"><button className="back" onClick={onBack}><ArrowLeft size={18}/> Back to marketplace</button><div className="dashboard-header"><div><h1>Order history</h1><p>Your completed purchases.</p></div></div>{orders.length===0?<div className="empty">No completed orders yet.</div>:<div className="panel order-list">{orders.map(o=><div className="order-row" key={o.id}><div><strong>Order #{String(o.id).slice(-6)}</strong><span>{new Date(o.createdAt).toLocaleString()}</span></div><span>{o.items.length} item{o.items.length===1?'':'s'}</span><strong>${Number(o.total).toFixed(2)}</strong></div>)}</div>}</section>; }

function AuthModal({onClose,onLogin}) { const [name,setName]=useState('Oudom'); const [email,setEmail]=useState(''); return <div className="modal-backdrop" onMouseDown={onClose}><div className="modal" onMouseDown={e=>e.stopPropagation()}><button className="modal-x" onClick={onClose}><X size={18}/></button><span className="eyebrow">ACCOUNT</span><h2>Sign in to AI Asset Marketplace</h2><p className="muted">Demo account mode. Production authentication can be connected to the Java API.</p><label>Name<input value={name} onChange={e=>setName(e.target.value)}/></label><label>Email<input type="email" required value={email} onChange={e=>setEmail(e.target.value)} placeholder="you@example.com"/></label><button className="primary wide" onClick={()=>onLogin({name:name.trim()||'User',email})}>Continue</button></div></div>; }

function Stat({icon:Icon,label,value}) { return <div className="stat"><div className="stat-icon"><Icon size={20}/></div><div><span>{label}</span><strong>{value}</strong></div></div>; }

createRoot(document.getElementById('root')).render(<App/>);
+Number(total).toFixed(2)+' with ABA PayWay':'ABA PayWay credentials required'}</button><div className="payway-methods"><span>ABA PAY</span><span>KHQR</span><span>VISA</span><span>Mastercard</span></div><small>Payment status must be confirmed by the PayWay callback before an order is marked paid.</small></div></div></section>;
}

function SellerDashboard({assets,setAssets,flash}) {
  const [form,setForm]=useState({title:'',category:'Dashboard',price:'12',image:'https://images.unsplash.com/photo-1558655146-9f40138edfeb?auto=format&fit=crop&w=1200&q=80',description:''});
  const submit=async(e)=>{e.preventDefault();const asset={...form,id:Date.now(),price:Number(form.price),creator:'You',status:'PENDING_REVIEW'};try{await apiFetch('/api/seller/assets',{method:'POST',body:JSON.stringify(form)});}catch{}setAssets(prev=>[asset,...prev]);setForm({...form,title:'',description:''});flash('Asset submitted for admin review.');};
  return <section className="page"><div className="dashboard-header"><div><span className="eyebrow">SELLER CENTER</span><h1>Creator dashboard</h1><p>Upload products and manage your marketplace business.</p></div></div><div className="seller-layout"><form className="panel upload-form" onSubmit={submit}><h2>Upload new asset</h2><label>Title<input required value={form.title} onChange={e=>setForm({...form,title:e.target.value})}/></label><label>Category<select value={form.category} onChange={e=>setForm({...form,category:e.target.value})}><option>Dashboard</option><option>UI Design</option><option>AI</option><option>Data Analysis</option></select></label><label>Price (USD)<input required type="number" min="0" value={form.price} onChange={e=>setForm({...form,price:e.target.value})}/></label><label>Preview image URL<input required value={form.image} onChange={e=>setForm({...form,image:e.target.value})}/></label><label>Description<textarea value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label><button className="primary" type="submit"><Upload size={18}/> Submit for review</button></form><div><div className="stat-grid"><Stat icon={Package} label="Assets" value={assets.length}/><Stat icon={DollarSign} label="Revenue" value="$1,284"/><Stat icon={ShoppingCart} label="Orders" value="96"/><Stat icon={Heart} label="Favorites" value="418"/></div><div className="panel"><div className="panel-head"><h2>Your products</h2></div><table><thead><tr><th>Product</th><th>Category</th><th>Price</th><th>Status</th></tr></thead><tbody>{assets.slice(0,8).map(a=><tr key={a.id}><td><div className="product-cell"><img src={a.image} alt=""/><span>{a.title}</span></div></td><td>{a.category}</td><td>${a.price}</td><td><span className={`status ${a.status==='PENDING_REVIEW'?'pending':''}`}>{a.status||'PUBLISHED'}</span></td></tr>)}</tbody></table></div></div></div></section>;
}

function AdminDashboard({assets,setAssets,flash}) {
  const pending=assets.filter(a=>a.status==='PENDING_REVIEW');
  const approve=async(id)=>{try{await apiFetch(`/api/admin/assets/${id}/approve`,{method:'POST'});}catch{}setAssets(prev=>prev.map(a=>a.id===id?{...a,status:'PUBLISHED'}:a));flash('Asset approved and published.');};
  const reject=(id)=>{setAssets(prev=>prev.map(a=>a.id===id?{...a,status:'REJECTED'}:a));flash('Asset rejected.');};
  return <section className="page"><div className="dashboard-header"><div><span className="eyebrow">ADMIN</span><h1>Marketplace control center</h1><p>Review products, creators and marketplace activity.</p></div></div><div className="stat-grid"><Stat icon={Package} label="Assets" value={assets.length}/><Stat icon={Users} label="Creators" value="24"/><Stat icon={ShoppingCart} label="Pending review" value={pending.length}/><Stat icon={DollarSign} label="GMV this month" value="$8,640"/></div><div className="admin-grid"><div className="panel"><div className="panel-head"><h2>Review queue</h2><span>{pending.length} pending</span></div>{pending.length===0?<div className="empty compact">No pending submissions.</div>:pending.map(a=><div className="review-row" key={a.id}><img src={a.image} alt=""/><div><strong>{a.title}</strong><span>{a.creator}</span></div><button className="approve" onClick={()=>approve(a.id)}>Approve</button><button className="reject" onClick={()=>reject(a.id)}><X size={16}/></button></div>)}</div><div className="panel"><div className="panel-head"><h2>Marketplace health</h2></div><div className="health"><div><span>Approval rate</span><strong>94%</strong></div><div><span>Refund rate</span><strong>1.8%</strong></div><div><span>Active sellers</span><strong>24</strong></div><div><span>New users</span><strong>138</strong></div></div></div></div></section>;
}

function Orders({onBack}) { const orders=readJson('market-orders',[]); return <section className="page"><button className="back" onClick={onBack}><ArrowLeft size={18}/> Back to marketplace</button><div className="dashboard-header"><div><h1>Order history</h1><p>Your completed demo purchases.</p></div></div>{orders.length===0?<div className="empty">No completed orders yet.</div>:<div className="panel order-list">{orders.map(o=><div className="order-row" key={o.id}><div><strong>Order #{String(o.id).slice(-6)}</strong><span>{new Date(o.createdAt).toLocaleString()}</span></div><span>{o.items.length} item{o.items.length===1?'':'s'}</span><strong>${Number(o.total).toFixed(2)}</strong></div>)}</div>}</section>; }

function AuthModal({onClose,onLogin}) { const [name,setName]=useState('Oudom'); const [email,setEmail]=useState(''); return <div className="modal-backdrop" onMouseDown={onClose}><div className="modal" onMouseDown={e=>e.stopPropagation()}><button className="modal-x" onClick={onClose}><X size={18}/></button><span className="eyebrow">ACCOUNT</span><h2>Sign in to AI Asset Marketplace</h2><p className="muted">Demo account mode. Production authentication can be connected to the Java API.</p><label>Name<input value={name} onChange={e=>setName(e.target.value)}/></label><label>Email<input type="email" required value={email} onChange={e=>setEmail(e.target.value)} placeholder="you@example.com"/></label><button className="primary wide" onClick={()=>onLogin({name:name.trim()||'User',email})}>Continue</button></div></div>; }

function Stat({icon:Icon,label,value}) { return <div className="stat"><div className="stat-icon"><Icon size={20}/></div><div><span>{label}</span><strong>{value}</strong></div></div>; }

createRoot(document.getElementById('root')).render(<App/>);
