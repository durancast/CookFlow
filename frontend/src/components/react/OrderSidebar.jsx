import React from 'react';
import { useStore } from '@nanostores/react';
import { cartItems, updateQuantity, addNoteToItem } from '../../store/cartStore.js';

export default function OrderSidebar() {
  // Conectamos React con el Nano Store
  const items = useStore(cartItems);

  // Calculamos el total
  const total = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  const handleSendOrder = () => {
    // Aquí irá el fetch en el Paso 4 del Sprint 3
    console.log("Listo para enviar a Laravel:", items);
  };

  return (
    <div className="flex flex-col h-full w-full bg-tpv-bg relative">
      {/* Cabecera del Sidebar */}
      <header className="shrink-0 p-6 border-b border-tpv-border bg-tpv-surface">
        <h2 className="text-xl font-bold flex items-center justify-between">
          <span>Comanda Actual</span>
          <span className="bg-tpv-accent text-white text-xs py-1 px-3 rounded-full font-mono">
            MESA --
          </span>
        </h2>
      </header>
      
      {/* Lista de Productos (Scrollable) */}
      <ul className="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar">
        {items.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center opacity-50">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
            <p>Selecciona productos<br/>para empezar la comanda</p>
          </div>
        ) : (
          items.map(item => (
            <li key={item.id} className="bg-tpv-surface p-3 rounded-xl border border-tpv-border shadow-sm flex flex-col gap-2">
              <div className="flex justify-between items-start">
                <span className="font-semibold text-sm pr-2 leading-tight">{item.name}</span>
                <span className="font-mono font-bold text-sm shrink-0">
                  {(item.price * item.quantity).toFixed(2)}€
                </span>
              </div>
              
              <div className="flex justify-between items-center mt-1">
                {/* Input de notas */}
                <input 
                  type="text" 
                  placeholder="Notas (ej. sin salsa)" 
                  value={item.note}
                  onChange={(e) => addNoteToItem(item.id, e.target.value)}
                  className="w-full mr-3 text-xs p-1.5 bg-tpv-bg border border-tpv-border rounded focus:outline-none focus:border-tpv-accent transition-colors"
                />

                {/* Controles de cantidad */}
                <div className="flex items-center bg-tpv-bg border border-tpv-border rounded-lg shrink-0">
                  <button 
                    onClick={() => updateQuantity(item.id, -1)} 
                    className="px-2 py-1 text-tpv-accent hover:bg-tpv-surface rounded-l-lg transition-colors"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 12H4" /></svg>
                  </button>
                  <span className="font-mono text-sm w-6 text-center">{item.quantity}</span>
                  <button 
                    onClick={() => updateQuantity(item.id, 1)} 
                    className="px-2 py-1 text-tpv-accent hover:bg-tpv-surface rounded-r-lg transition-colors"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4" /></svg>
                  </button>
                </div>
              </div>
            </li>
          ))
        )}
      </ul>

      {/* Footer Fijo con Total y Botón */}
      <div className="shrink-0 p-4 md:p-6 border-t border-tpv-border bg-tpv-surface">
        <div className="flex justify-between items-center mb-4">
          <span className="text-gray-500 uppercase text-sm font-bold tracking-wider">Total</span>
          <span className="text-2xl font-mono font-bold">
            {total.toFixed(2)}€
          </span>
        </div>
        
        <button 
          onClick={handleSendOrder}
          disabled={items.length === 0}
          className="w-full bg-tpv-accent hover:bg-tpv-accent-hover disabled:bg-tpv-border disabled:text-gray-400 disabled:cursor-not-allowed text-white font-bold py-4 rounded-xl shadow-lg transition-all flex justify-center items-center gap-2"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
            <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z" />
          </svg>
          Enviar a Cocina
        </button>
      </div>
    </div>
  );
}