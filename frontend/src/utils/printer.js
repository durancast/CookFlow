function printHtml(content) {
  const iframe = document.createElement('iframe');
  iframe.style.position = 'fixed';
  iframe.style.right = '0';
  iframe.style.bottom = '0';
  iframe.style.width = '0';
  iframe.style.height = '0';
  iframe.style.border = '0';
  document.body.appendChild(iframe);

  const doc = iframe.contentWindow.document;
  doc.open();
  doc.write(`
    <html>
      <head>
        <style>
          @page { margin: 0; } 
          body { 
            font-family: 'Courier New', Courier, monospace;
            width: 80mm;
            margin: 0; 
            padding: 5mm; 
            color: #000;
            font-size: 14px;
          }
          .text-center { text-align: center; }
          .text-right { text-align: right; }
          .font-bold { font-weight: bold; }
          .text-xl { font-size: 20px; }
          .text-2xl { font-size: 26px; }
          .divider { border-top: 1px dashed #000; margin: 10px 0; }
          table { width: 100%; border-collapse: collapse; }
          th, td { text-align: left; vertical-align: top; padding: 2px 0; }
          .qty { width: 15%; font-weight: bold; }
          .item { width: 60%; }
          .price { width: 25%; text-align: right; }
          .notes { font-size: 13px; font-style: italic; display: block; padding-left: 15%; }
        </style>
      </head>
      <body>
        ${content}
      </body>
    </html>
  `);
  doc.close();

  iframe.contentWindow.focus();
  setTimeout(() => {
    iframe.contentWindow.print();
    setTimeout(() => {
      document.body.removeChild(iframe);
    }, 1000);
  }, 250);
}

export function printKitchenTicket(items, tableNumber) {
  const date = new Date().toLocaleString('es-ES');
  const itemsHtml = items.map(item => `
    <tr>
      <td class="qty text-xl">${item.quantity}x</td>
      <td class="item font-bold text-xl">${item.name}</td>
    </tr>
    ${item.note ? `<tr><td colspan="2"><span class="notes">NOTA: ${item.note}</span></td></tr>` : ''}
  `).join('');

  const html = `
    <div class="text-center font-bold text-2xl">COMANDA COCINA</div>
    <div class="text-center font-bold text-2xl" style="margin-top: 5px; border: 2px solid #000; padding: 5px;">MESA ${tableNumber}</div>
    <div class="divider"></div>
    <div>Fecha: ${date}</div>
    <div class="divider"></div>
    <table>${itemsHtml}</table>
    <div class="divider"></div>
    <div class="text-center" style="margin-top: 20px;">.</div>
  `;
  printHtml(html);
}

export function printCustomerReceipt(items, tableNumber, total) {
  const date = new Date().toLocaleString('es-ES');
  const ivaRate = 0.10;
  const subtotal = total / (1 + ivaRate);
  const ivaAmount = total - subtotal;

  const itemsHtml = items.map(item => `
    <tr>
      <td class="qty">${item.quantity}x</td>
      <td class="item">${item.name}</td>
      <td class="price">${(item.price * item.quantity).toFixed(2)}€</td>
    </tr>
  `).join('');

  const html = `
    <div class="text-center font-bold text-2xl">CookFlow</div>
    <div class="text-center">Restaurante Demo, Local 1</div>
    <div class="divider"></div>
    <div><b>MESA: ${tableNumber}</b></div>
    <div>Fecha: ${date}</div>
    <div class="divider"></div>
    <table>${itemsHtml}</table>
    <div class="divider"></div>
    <div style="display: flex; justify-content: space-between;">
      <span>Subtotal Base:</span>
      <span class="text-right">${subtotal.toFixed(2)}€</span>
    </div>
    <div style="display: flex; justify-content: space-between;">
      <span>IVA (10%):</span>
      <span class="text-right">${ivaAmount.toFixed(2)}€</span>
    </div>
    <div class="divider"></div>
    <div style="display: flex; justify-content: space-between;" class="font-bold text-2xl">
      <span>TOTAL:</span>
      <span class="text-right">${total.toFixed(2)}€</span>
    </div>
    <div class="divider"></div>
    <div class="text-center font-bold">¡Gracias por su visita!</div>
    <div class="text-center" style="margin-top: 20px;">.</div>
  `;
  printHtml(html);
}