import React, { useState } from 'react';
import { useStore } from '@nanostores/react';
import { cartItems, updateQuantity, addNoteToItem, clearCart, selectedTable } from '../../store/cartStore.js';
// import { printKitchenTicket } from '../../utils/printer.js';

export default function OrderSidebar() {
  const items = useStore(cartItems);
  const currentTable = useStore(selectedTable); // 👈 Escuchamos la mesa seleccionada
  const [isSending, setIsSending] = useState(false);
  
  const total = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  const handleSendOrder = async () => {
    const backendUrl = import.meta.env.PUBLIC_BACKEND_URL || "http://127.0.0.1:8000";
    const token = localStorage.getItem('auth_token');

    if (!token) {
      if (window.showToast) window.showToast('Sesión caducada, reingresa', 'error');
      return;
    }

    // 🚨 VALIDACIÓN: Si no hay mesa, no dejamos enviar
    if (!currentTable) {
      if (window.showToast) window.showToast('⚠️ Selecciona una mesa antes de enviar', 'error');
      // Aquí podrías incluso forzar que se abra el modal de mesas
      return;
    }

    setIsSending(true);

    const orderData = {
      table_id: currentTable.id, // 👈 Ya es 100% dinámico
      items: items.map(item => ({
        product_id: item.id,
        quantity: item.quantity,
        notes: item.note || '' // 👈 Cambiado a 'notes' para Laravel
      }))
      // Hemos quitado el 'total' porque tu Laravel ya lo calcula de forma segura
    };

    console.log("📦 Datos a enviar a Laravel:", orderData);

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

      console.log("📡 Respuesta HTTP del servidor:", response.status);

      if (response.ok || response.status === 201) {
        // 🏆 ÉXITO
        if (window.showToast) window.showToast('¡Comanda enviada a cocina! 👨‍🍳');
        
        // 🖨️ Imprimir ticket (Descomenta cuando tengas el printer.js)
        // printKitchenTicket(items, currentTable.number);

        clearCart(); 
      } else {
        // 🚨 ERROR DEL SERVIDOR (Blindado contra HTML)
        const textResponse = await response.text();
        try {
          const errorData = JSON.parse(textResponse);
          console.error("🚨 Laravel rechazó el pedido:", errorData);
          throw new Error(errorData.message || 'Error al procesar comanda');
        } catch(e) {
          throw new Error('Error de servidor (posible 500 o 404)');
        }
      }

    } catch (error) {
      console.error("🚨 Error capturado en el catch:", error);
      if (window.showToast) window.showToast(error.message || 'Hubo un problema con el envío', 'error');
    } finally {
      setIsSending(false);
    }
  };

  return (
    <div className="flex flex-col h-full w-full bg-tpv-bg relative font-sans">
      {/* Cabecera */}
      <header className="shrink-0 p-6 border-b border-tpv-border bg-tpv-surface">
        <h2 className="text-xl font-bold flex items-center justify-between text-tpv-text">
          <span>Comanda Actual</span>
          {/* 👇 Mostramos el número real de la mesa */}
          <span className={`text-[10px] py-1 px-3 rounded-full font-black shadow-lg uppercase tracking-widest ${
            currentTable 
              ? 'bg-tpv-accent text-white shadow-tpv-accent/20' 
              : 'bg-red-500/20 text-red-400 border border-red-500/30 animate-pulse'
          }`}>
            {currentTable ? `Mesa ${currentTable.number}` : 'SIN MESA'}
          </span>
        </h2>
      </header>
      
      {/* 📦 LISTA DE PRODUCTOS */}
      <ul className="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar">
        {items.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center text-tpv-text-muted opacity-40 p-10">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
            <p className="font-bold uppercase tracking-tighter text-sm">Carrito vacío</p>
          </div>
        ) : (
          items.map(item => (
            <li key={item.id} className="bg-tpv-surface p-4 rounded-2xl border border-tpv-border shadow-sm flex flex-col gap-3 animate-in fade-in slide-in-from-right-2">
              <div className="flex justify-between items-start">
                <span className="font-bold text-tpv-text pr-2 leading-tight">{item.name}</span>
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
                  <button 
                    onClick={() => updateQuantity(item.id, -1)} 
                    className="px-3 py-1.5 text-tpv-accent hover:bg-tpv-surface transition-colors font-bold"
                  > – </button>
                  <span className="font-mono font-black text-tpv-text text-xs w-6 text-center">{item.quantity}</span>
                  <button 
                    onClick={() => updateQuantity(item.id, 1)} 
                    className="px-3 py-1.5 text-tpv-accent hover:bg-tpv-surface transition-colors font-bold"
                  > + </button>
                </div>
              </div>
            </li>
          ))
        )}
      </ul>

      {/* Footer Fijo */}
      <div className="shrink-0 p-6 border-t border-tpv-border bg-tpv-surface shadow-[0_-10px_30px_rgba(0,0,0,0.3)]">
        <div className="flex justify-between items-center mb-6">
          <span className="text-tpv-text-muted uppercase text-[10px] font-black tracking-[0.2em]">Total</span>
          <span className="text-3xl font-mono font-black text-tpv-text">
            {total.toFixed(2)}€
          </span>
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
    </div>
  );
}