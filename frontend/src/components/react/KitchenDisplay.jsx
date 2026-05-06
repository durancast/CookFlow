import React, { useState, useEffect, useCallback } from 'react';

const BACKEND_URL = import.meta.env.PUBLIC_BACKEND_URL || 'http://127.0.0.1:8000';

function ElapsedTimer({ createdAt }) {
  const getElapsed = () => Math.floor((Date.now() - new Date(createdAt).getTime()) / 1000);
  const [elapsed, setElapsed] = useState(getElapsed);

  useEffect(() => {
    const id = setInterval(() => setElapsed(getElapsed()), 1000);
    return () => clearInterval(id);
  }, [createdAt]);

  const minutes = Math.floor(elapsed / 60);
  const seconds = elapsed % 60;
  const display = `${minutes}:${String(seconds).padStart(2, '0')}`;

  let cls = 'text-green-400 border-green-500/30 bg-green-500/10';
  if (minutes >= 10) cls = 'text-red-400 border-red-500/30 bg-red-500/10';
  else if (minutes >= 5) cls = 'text-yellow-400 border-yellow-500/30 bg-yellow-500/10';

  return (
    <span className={`font-mono font-black text-sm px-3 py-1.5 rounded-xl border ${cls}`}>
      {display}
    </span>
  );
}

export default function KitchenDisplay() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);

  const fetchOrders = useCallback(async () => {
    try {
      const res = await fetch(`${BACKEND_URL}/api/orders?status=pending,preparing`);
      if (res.ok) {
        setOrders(await res.json());
        setLastUpdated(new Date());
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders();
    const id = setInterval(fetchOrders, 10000);
    return () => clearInterval(id);
  }, [fetchOrders]);

  const updateStatus = async (orderId, newStatus) => {
    try {
      await fetch(`${BACKEND_URL}/api/orders/${orderId}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus }),
      });
      await fetchOrders();
    } catch (e) {}
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full text-tpv-accent animate-pulse font-black uppercase tracking-widest text-sm">
        Conectando con cocina...
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-full text-tpv-text-muted text-center gap-4">
        <svg xmlns="http://www.w3.org/2000/svg" className="h-20 w-20 opacity-20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div>
          <p className="font-black uppercase tracking-tighter text-xl">Sin pedidos pendientes</p>
          <p className="text-sm mt-1 opacity-50">La cocina está al día</p>
        </div>
        {lastUpdated && (
          <p className="text-[11px] opacity-30 font-mono">
            Actualizado {lastUpdated.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
          </p>
        )}
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto custom-scrollbar">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 p-6 auto-rows-min">
        {orders.map(order => {
          const isPreparing = order.status === 'preparing';
          return (
            <div
              key={order.id}
              className={`bg-tpv-surface border-2 rounded-3xl p-5 flex flex-col gap-4 transition-all ${
                isPreparing ? 'border-tpv-accent shadow-lg shadow-tpv-accent/10' : 'border-orange-500/40'
              }`}
            >
              <div className="flex justify-between items-start">
                <div>
                  <p className="text-[9px] font-black uppercase tracking-widest text-tpv-text-muted mb-1">Mesa</p>
                  <p className="font-mono font-black text-5xl text-tpv-text leading-none">
                    {order.table?.number ?? '?'}
                  </p>
                </div>
                <div className="flex flex-col items-end gap-2">
                  {order.created_at && <ElapsedTimer createdAt={order.created_at} />}
                  <span className={`text-[9px] font-black uppercase tracking-widest px-2 py-1 rounded-lg ${
                    isPreparing
                      ? 'bg-tpv-accent/20 text-tpv-accent'
                      : 'bg-orange-500/20 text-orange-400'
                  }`}>
                    {isPreparing ? 'Preparando' : 'Nuevo'}
                  </span>
                </div>
              </div>

              <ul className="space-y-2 flex-1 border-t border-tpv-border pt-4">
                {order.items.map((item, i) => (
                  <li key={i} className="flex justify-between items-start gap-3">
                    <span className="font-bold text-tpv-text text-sm leading-tight">
                      {item.product?.name ?? '?'}
                      {item.notes && (
                        <span className="block text-[10px] text-yellow-400 font-normal mt-0.5">
                          ↳ {item.notes}
                        </span>
                      )}
                    </span>
                    <span className="font-mono font-black text-tpv-accent shrink-0 text-lg">×{item.quantity}</span>
                  </li>
                ))}
              </ul>

              <div className="flex gap-2 pt-1">
                {!isPreparing && (
                  <button
                    onClick={() => updateStatus(order.id, 'preparing')}
                    className="flex-1 bg-orange-500 hover:bg-orange-600 text-white font-black py-3 rounded-xl text-xs uppercase tracking-widest transition-all active:scale-95 shadow-lg shadow-orange-500/20"
                  >
                    Preparando
                  </button>
                )}
                {isPreparing && (
                  <button
                    onClick={() => updateStatus(order.id, 'served')}
                    className="flex-1 bg-green-500 hover:bg-green-600 text-white font-black py-3 rounded-xl text-xs uppercase tracking-widest transition-all active:scale-95 shadow-lg shadow-green-500/20"
                  >
                    Listo ✓
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
