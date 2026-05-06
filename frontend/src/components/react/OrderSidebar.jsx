import React, { useState, useEffect } from 'react';
import { useStore } from '@nanostores/react';
import { cartItems, updateQuantity, addNoteToItem, clearCart, selectedTable, clearOrder, markItemsAsSent } from '../../store/cartStore.js';
import { printKitchenTicket, printCustomerReceipt } from '../../utils/printer.js';

export default function OrderSidebar() {
  const items = useStore(cartItems);
  const currentTable = useStore(selectedTable);
  const [isSending, setIsSending] = useState(false);
  
  // --- ESTADOS DE PAGO ---
  const [paymentMode, setPaymentMode] = useState(false);
  const [cashMode, setCashMode] = useState(false); // 👈 Activa la vista de calculadora
  const [amountGiven, setAmountGiven] = useState(''); // 👈 Guarda el billete entregado
  
  const unsentItems = items.filter(i => !i.sent);
  const hasUnsent = unsentItems.length > 0;
  const total = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  // --- 🏷️ CAMBIAR ESTADO MANUAL ---
  const handleStatusChange = async (newStatus) => {
    if (!currentTable) return;
    const backendUrl = import.meta.env.PUBLIC_BACKEND_URL || "http://127.0.0.1:8000";
    try {
      await fetch(`${backendUrl}/api/tables/${currentTable.id}/status`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: newStatus })
      });
      selectedTable.set({ ...currentTable, status: newStatus });
      const label = newStatus === 'free' ? 'Libre' : newStatus === 'occupied' ? 'En Servicio' : 'Cobrando';
      if (window.showToast) window.showToast(`Mesa ${currentTable.number} → ${label}`, 'success');
    } catch (e) {
      if (window.showToast) window.showToast('Error al cambiar estado', 'error');
    }
  };

  // --- 🚪 SALIR DE LA MESA ---
  const handleExitTable = async () => {
    if (!currentTable) return;
    
    if (items.length === 0) {
      const backendUrl = import.meta.env.PUBLIC_BACKEND_URL || "http://127.0.0.1:8000";
      try {
        await fetch(`${backendUrl}/api/tables/${currentTable.id}/status`, {
          method: 'PATCH',
          headers: { 
            'Authorization': `Bearer ${localStorage.getItem('auth_token')}`, 
            'Content-Type': 'application/json' 
          },
          body: JSON.stringify({ status: 'free' })
        });
      } catch (e) {
        console.error("Error al liberar la mesa:", e);
      }
    }
    
    clearOrder();
    setPaymentMode(false);
    setCashMode(false);
  };

  // --- 📝 ENVIAR COMANDA A COCINA ---
  const handleSendOrder = async () => {
    const backendUrl = import.meta.env.PUBLIC_BACKEND_URL || "http://127.0.0.1:8000";
    const token = localStorage.getItem('auth_token');

    if (!token) {
      if (window.showToast) window.showToast('Sesión caducada, reingresa', 'error');
      return;
    }

    if (!currentTable) {
      if (window.showToast) window.showToast('⚠️ Selecciona una mesa antes de enviar', 'error');
      return;
    }

    if (unsentItems.length === 0) {
      if (window.showToast) window.showToast('No hay platos nuevos que enviar', 'error');
      return;
    }

    setIsSending(true);

    const orderData = {
      table_id: currentTable.id,
      items: unsentItems.map(item => ({
        product_id: item.id,
        quantity: item.quantity,
        notes: item.note || ''
      }))
    };

    try {
      const response = await fetch(`${backendUrl}/api/orders`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(orderData)
      });

      if (response.ok || response.status === 201) {
        if (window.showToast) window.showToast('¡Comanda enviada a cocina! 👨‍🍳');
        printKitchenTicket(unsentItems, currentTable.number);
        markItemsAsSent(unsentItems.map(i => i.id));
        setPaymentMode(false);
        setCashMode(false);
      } else {
        const textResponse = await response.text();
        try {
          const errorData = JSON.parse(textResponse);
          throw new Error(errorData.message || 'Error al procesar comanda');
        } catch(e) {
          throw new Error('Error de servidor (posible 500 o 404)');
        }
      }

    } catch (error) {
      if (window.showToast) window.showToast(error.message || 'Hubo un problema con el envío', 'error');
    } finally {
      setIsSending(false);
    }
  };

  // --- 🧾 IMPRIMIR CUENTA PARA EL CLIENTE ---
  const handlePrintReceipt = async () => {
    if (!currentTable || items.length === 0) return;
    
    // 1. Imprime el ticket físico
    printCustomerReceipt(items, currentTable.number, total);

    // 2. Avisa a Laravel de que la mesa está pendiente de cobro
    const backendUrl = import.meta.env.PUBLIC_BACKEND_URL || "http://127.0.0.1:8000";
    try {
      await fetch(`${backendUrl}/api/tables/${currentTable.id}/status`, {
        method: 'PATCH',
        headers: { 
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`, 
          'Content-Type': 'application/json' 
        },
        body: JSON.stringify({ status: 'pending' }) // 👈 ESTADO AMARILLO
      });
      selectedTable.set({ ...currentTable, status: 'pending' });
      if (window.showToast) window.showToast('Cuenta impresa. Mesa marcada como Pendiente.', 'success');
    } catch (e) {
      console.error("Error al cambiar estado a pendiente", e);
    }
  };

  // --- 💸 COBRAR Y LIBERAR MESA ---
  const handleCheckout = async (method) => {
    if (!currentTable) return;

    // Si es con tarjeta, pide confirmación simple. Si es efectivo, ya se confirmó en la calculadora.
    if (method === 'card' && !window.confirm(`¿Seguro que deseas cobrar ${total.toFixed(2)}€ con Tarjeta?`)) return;

    const backendUrl = import.meta.env.PUBLIC_BACKEND_URL || "http://127.0.0.1:8000";
    const token = localStorage.getItem('auth_token');

    try {
      const response = await fetch(`${backendUrl}/api/tables/${currentTable.id}/checkout`, {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${token}`, 
          'Content-Type': 'application/json',
          'Accept': 'application/json' 
        },
        body: JSON.stringify({ payment_method: method })
      });

      if (response.ok) {
        const metodoText = method === 'cash' ? 'EFECTIVO 💵' : 'TARJETA 💳';
        if (window.showToast) window.showToast(`¡Mesa cobrada en ${metodoText}!`, 'success');
        
        clearOrder(); 
        setPaymentMode(false);
        setCashMode(false);
        setAmountGiven('');
      } else {
        const err = await response.json();
        throw new Error(err.message || 'Error al cerrar la cuenta de la mesa');
      }
    } catch (error) {
      if (window.showToast) window.showToast(error.message, 'error');
    }
  };

  return (
    <div className="flex flex-col h-full w-full bg-tpv-bg relative font-sans">
      <header className="shrink-0 p-5 border-b border-tpv-border bg-tpv-surface">
        <div className="flex justify-between items-center mb-3">
          <h2 className="text-xl font-bold flex items-center gap-3 text-tpv-text">
            <span>Comanda Actual</span>
            <span className={`text-[10px] py-1 px-3 rounded-full font-black shadow-lg uppercase tracking-widest ${
              currentTable
                ? 'bg-tpv-accent text-white shadow-tpv-accent/20'
                : 'bg-red-500/20 text-red-400 border border-red-500/30 animate-pulse'
            }`}>
              {currentTable ? `Mesa ${currentTable.number}` : 'SIN MESA'}
            </span>
          </h2>

          {currentTable && (
            <button
              onClick={handleExitTable}
              title="Volver a Sala"
              className="p-2 bg-tpv-bg border border-tpv-border text-tpv-text-muted hover:text-red-400 hover:border-red-400/30 rounded-xl transition-all active:scale-90"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          )}
        </div>

        {currentTable && (
          <div className="flex gap-1.5">
            {[
              { value: 'free',     label: 'Libre',      active: 'bg-tpv-text-muted/20 text-tpv-text border border-tpv-border' },
              { value: 'occupied', label: 'En Servicio', active: 'bg-red-500/15 text-red-400 border border-red-500/30' },
              { value: 'pending',  label: 'Cobrando',   active: 'bg-yellow-500/15 text-yellow-500 border border-yellow-500/30' },
            ].map(({ value, label, active }) => (
              <button
                key={value}
                onClick={() => handleStatusChange(value)}
                className={`flex-1 py-1.5 rounded-lg text-[9px] font-black uppercase tracking-wide transition-all active:scale-95 ${
                  currentTable.status === value
                    ? active
                    : 'bg-transparent text-tpv-text-muted/40 border border-transparent hover:text-tpv-text-muted hover:border-tpv-border/50'
                }`}
              >
                {label}
              </button>
            ))}
          </div>
        )}
      </header>
      
      <ul className="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar">
        {items.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center text-tpv-text-muted opacity-40 p-10">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
            <p className="font-bold uppercase tracking-tighter text-sm">Mesa libre o sin pedidos</p>
          </div>
        ) : (
          items.map(item => (
            <li key={`${item.id}-${item.sent}`} className={`p-4 rounded-2xl border shadow-sm flex flex-col gap-3 animate-in fade-in slide-in-from-right-2 ${item.sent ? 'bg-tpv-surface border-green-500/20' : 'bg-tpv-surface border-tpv-border'}`}>
              <div className="flex justify-between items-start">
                <div className="flex items-center gap-2 pr-2">
                  {item.sent && <span className="text-green-500 text-xs font-black">✓</span>}
                  <span className={`font-bold leading-tight ${item.sent ? 'text-tpv-text-muted' : 'text-tpv-text'}`}>{item.name}</span>
                </div>
                <span className="font-mono font-black text-tpv-accent shrink-0">
                  {(item.price * item.quantity).toFixed(2)}€
                </span>
              </div>
              
              <div className="flex justify-between items-center">
                <input 
                  type="text" 
                  placeholder="Notas..." 
                  value={item.note || ''}
                  onChange={(e) => addNoteToItem(item.id, e.target.value)}
                  className="w-full mr-3 text-[10px] p-2 bg-tpv-bg text-tpv-text border border-tpv-border rounded-lg focus:outline-none focus:border-tpv-accent transition-colors placeholder:text-tpv-text-muted/30"
                />

                <div className="flex items-center bg-tpv-bg border border-tpv-border rounded-xl shrink-0 overflow-hidden">
                  <button onClick={() => updateQuantity(item.id, -1)} className="px-3 py-1.5 text-tpv-accent hover:bg-tpv-surface transition-colors font-bold"> – </button>
                  <span className="font-mono font-black text-tpv-text text-xs w-6 text-center">{item.quantity}</span>
                  <button onClick={() => updateQuantity(item.id, 1)} className="px-3 py-1.5 text-tpv-accent hover:bg-tpv-surface transition-colors font-bold"> + </button>
                </div>
              </div>
            </li>
          ))
        )}
      </ul>

      <div className="shrink-0 p-6 border-t border-tpv-border bg-tpv-surface shadow-[0_-10px_30px_rgba(0,0,0,0.3)] space-y-4">
        <div className="flex justify-between items-center">
          <span className="text-tpv-text-muted uppercase text-[10px] font-black tracking-[0.2em]">Total</span>
          <span className="text-3xl font-mono font-black text-tpv-text">
            {total.toFixed(2)}€
          </span>
        </div>
        
        {/* --- LÓGICA DE RENDERING DEL FOOTER --- */}
        {cashMode ? (
          // 🟩 3. CALCULADORA DE EFECTIVO
          <div className="space-y-4 animate-in fade-in slide-in-from-bottom-2">
            <div className="bg-tpv-bg p-4 rounded-xl border border-tpv-border flex flex-col gap-2">
              <label className="text-[10px] font-black text-tpv-text-muted uppercase tracking-widest">Entregado por cliente (€)</label>
              <input 
                type="number" 
                value={amountGiven}
                onChange={(e) => setAmountGiven(e.target.value)}
                placeholder={total.toFixed(2)}
                className="w-full text-3xl font-mono font-black bg-transparent text-tpv-text outline-none placeholder:text-tpv-text-muted/30"
                autoFocus
              />
            </div>
            
            <div className="flex justify-between items-center p-4 rounded-xl bg-green-500/10 border border-green-500/30">
              <span className="font-black text-green-500 uppercase text-xs tracking-wider">A devolver:</span>
              <span className="text-3xl font-mono font-black text-green-500">
                {amountGiven && parseFloat(amountGiven) >= total 
                  ? (parseFloat(amountGiven) - total).toFixed(2) + '€' 
                  : '0.00€'}
              </span>
            </div>

            <div className="flex gap-3">
              <button 
                onClick={() => { setCashMode(false); setAmountGiven(''); }} 
                className="flex-1 py-4 bg-tpv-bg text-tpv-text border border-tpv-border rounded-xl font-black uppercase text-sm transition-all active:scale-95 hover:border-tpv-text-muted"
              >
                Volver
              </button>
              <button 
                onClick={() => handleCheckout('cash')} 
                disabled={!amountGiven || parseFloat(amountGiven) < total}
                className="flex-[2] bg-green-500 hover:bg-green-600 disabled:opacity-30 disabled:cursor-not-allowed text-white font-black py-4 rounded-xl shadow-lg shadow-green-500/20 transition-all active:scale-95 flex justify-center items-center gap-2"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                CONFIRMAR
              </button>
            </div>
          </div>

        ) : paymentMode ? (
          // 🟦 2. SELECCIÓN EFECTIVO / TARJETA
          <div className="space-y-3 animate-in fade-in slide-in-from-bottom-2">
            <div className="flex gap-3">
              <button 
                onClick={() => setCashMode(true)} // 👈 Pasa a la calculadora
                className="flex-1 bg-green-500 hover:bg-green-600 text-white font-black py-4 rounded-xl shadow-lg transition-all active:scale-95 flex flex-col items-center gap-1"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
                EFECTIVO
              </button>
              <button 
                onClick={() => handleCheckout('card')} // 👈 Tarjeta cobra directo
                className="flex-1 bg-blue-500 hover:bg-blue-600 text-white font-black py-4 rounded-xl shadow-lg transition-all active:scale-95 flex flex-col items-center gap-1"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" /></svg>
                TARJETA
              </button>
            </div>
            <button 
              onClick={() => setPaymentMode(false)} 
              className="w-full py-3 text-tpv-text-muted hover:text-tpv-text font-bold text-sm tracking-widest uppercase transition-colors"
            >
              Cancelar Cobro
            </button>
          </div>

        ) : (
          // ⬛ 1. BOTONES NORMALES (Cuenta, Cobrar, Enviar)
          <div className="space-y-4 animate-in fade-in">
            <div className="flex gap-3">
              <button 
                onClick={handlePrintReceipt}
                disabled={items.length === 0}
                className="flex-1 bg-tpv-bg border-2 border-tpv-accent text-tpv-accent hover:bg-tpv-accent hover:text-white disabled:border-tpv-border disabled:text-tpv-text-muted/30 disabled:cursor-not-allowed font-black py-3 rounded-xl transition-all flex justify-center items-center gap-2 active:scale-95"
                title="Imprimir Cuenta"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                   <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
                </svg>
                CUENTA
              </button>

              <button
                onClick={() => hasUnsent ? window.showToast?.('Envía los platos pendientes antes de cobrar', 'error') : setPaymentMode(true)}
                disabled={items.length === 0}
                className={`flex-1 border-2 text-white font-black py-3 rounded-xl shadow-lg transition-all flex justify-center items-center gap-2 active:scale-95 ${hasUnsent ? 'bg-gray-500 border-gray-500 cursor-not-allowed opacity-60' : 'bg-green-500 border-green-500 hover:bg-green-600 hover:border-green-600 shadow-green-500/20'}`}
                title={hasUnsent ? 'Hay platos sin enviar' : 'Cobrar Mesa'}
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                COBRAR
              </button>
            </div>

            <button 
              onClick={handleSendOrder}
              disabled={items.length === 0 || isSending}
              className="w-full bg-tpv-accent hover:bg-tpv-accent-hover disabled:bg-tpv-border disabled:text-tpv-text-muted/30 disabled:cursor-not-allowed text-white font-black py-4 rounded-2xl shadow-xl shadow-tpv-accent/20 transition-all flex justify-center items-center gap-3 text-lg active:scale-95"
            >
              {isSending ? (
                <span className="flex items-center gap-2">
                  <svg className="animate-spin h-5 w-5 text-white" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
                  </svg>
                  ENVIANDO...
                </span>
              ) : (
                <>
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z" />
                  </svg>
                  ENVIAR COMANDA
                </>
              )}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}