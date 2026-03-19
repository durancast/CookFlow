import React from 'react';
import { useStore } from '@nanostores/react';
import { cartItems, updateQuantity, addNoteToItem } from '../../store/cartStore.js';

export default function OrderSidebar() {
  const items = useStore(cartItems);
  const total = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  const handleSendOrder = () => {
    console.log("Listo para enviar a Laravel:", items);
  };

  return (
    <div className="flex flex-col h-full w-full bg-tpv-bg relative font-sans">
      {/* Cabecera: Usamos tpv-surface y tpv-text */}
      <header className="shrink-0 p-6 border-b border-tpv-border bg-tpv-surface">
        <h2 className="text-xl font-bold flex items-center justify-between text-tpv-text">
          <span>Comanda Actual</span>
          <span className="bg-tpv-accent text-white text-xs py-1 px-3 rounded-full font-mono shadow-lg shadow-tpv-accent/20">
            MESA --
          </span>
        </h2>
      </header>
      
      {/* Lista de Productos */}
      <ul className="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar">
        {items.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center text-tpv-text-muted opacity-40">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
            <p className="font-medium">Selecciona productos<br/>para empezar la comanda</p>
          </div>
        ) : (
          items.map(item => (
            <li key={item.id} className="bg-tpv-surface p-4 rounded-2xl border border-tpv-border shadow-sm flex flex-col gap-3">
              <div className="flex justify-between items-start">
                {/* Nombre del plato en BLANCO */}
                <span className="font-bold text-tpv-text pr-2 leading-tight">{item.name}</span>
                {/* Precio en ACENTO (Índigo) */}
                <span className="font-mono font-black text-tpv-accent shrink-0">
                  {(item.price * item.quantity).toFixed(2)}€
                </span>
              </div>
              
              <div className="flex justify-between items-center">
                {/* Input de notas: texto blanco sobre fondo oscuro */}
                <input 
                  type="text" 
                  placeholder="Notas (ej. sin salsa)" 
                  value={item.note}
                  onChange={(e) => addNoteToItem(item.id, e.target.value)}
                  className="w-full mr-3 text-xs p-2 bg-tpv-bg text-tpv-text border border-tpv-border rounded-lg focus:outline-none focus:border-tpv-accent transition-colors placeholder:text-tpv-text-muted/50"
                />

                {/* Controles de cantidad */}
                <div className="flex items-center bg-tpv-bg border border-tpv-border rounded-xl shrink-0 overflow-hidden">
                  <button 
                    onClick={() => updateQuantity(item.id, -1)} 
                    className="px-3 py-1.5 text-tpv-accent hover:bg-tpv-surface transition-colors"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 12H4" /></svg>
                  </button>
                  <span className="font-mono font-bold text-tpv-text text-sm w-8 text-center">{item.quantity}</span>
                  <button 
                    onClick={() => updateQuantity(item.id, 1)} 
                    className="px-3 py-1.5 text-tpv-accent hover:bg-tpv-surface transition-colors"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4" /></svg>
                  </button>
                </div>
              </div>
            </li>
          ))
        )}
      </ul>

      {/* Footer Fijo */}
      <div className="shrink-0 p-6 border-t border-tpv-border bg-tpv-surface shadow-[0_-10px_30px_rgba(0,0,0,0.3)]">
        <div className="flex justify-between items-center mb-6">
          <span className="text-tpv-text-muted uppercase text-xs font-black tracking-widest">Total</span>
          <span className="text-3xl font-mono font-black text-tpv-text">
            {total.toFixed(2)}€
          </span>
        </div>
        
        <button 
          onClick={handleSendOrder}
          disabled={items.length === 0}
          className="w-full bg-tpv-accent hover:bg-tpv-accent-hover disabled:bg-tpv-border disabled:text-tpv-text-muted/30 disabled:cursor-not-allowed text-white font-black py-4 rounded-2xl shadow-xl shadow-tpv-accent/20 transition-all flex justify-center items-center gap-3 text-lg"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" viewBox="0 0 20 20" fill="currentColor">
            <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z" />
          </svg>
          ENVIAR COMANDA
        </button>
      </div>
    </div>
  );
}