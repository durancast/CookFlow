import { atom } from 'nanostores';

export const cartItems = atom([]);

/** @type {import('nanostores').WritableAtom<{id: number, number: number} | null>} */
export const selectedTable = atom(null);

// ➕ Añadir producto
export function addToCart(product) {
    console.log("Intentando añadir al carrito el objeto:", product);
    console.log("Intentando añadir al carrito el objeto:", product);

    const productId = product.id; 

    if (!productId) {
        console.error("🚨 ERROR: El producto no tiene un identificador válido.");
        return;
    }

    const currentItems = cartItems.get();
    const existingItem = currentItems.find(item => (item.id || item.name) === productId);

    if (existingItem) {
        cartItems.set(currentItems.map(item => 
            (item.id || item.name) === productId 
                ? { ...item, quantity: item.quantity + 1 } 
                : item
        ));
    } else {
        cartItems.set([...currentItems, { ...product, quantity: 1, note: '' }]);
    }
}

// ➖ Actualizar cantidad (+1 o -1)
export function updateQuantity(productId, delta) {
    const currentItems = cartItems.get();
    
    const updatedItems = currentItems.map(item => {
        if ((item.id || item.name) === productId) {
            return { ...item, quantity: item.quantity + delta };
        }
        return item;
    }).filter(item => item.quantity > 0); 

    cartItems.set(updatedItems);
}

// 📝 Añadir nota
export function addNoteToItem(productId, note) {
    const currentItems = cartItems.get();
    cartItems.set(currentItems.map(item => 
        (item.id || item.name) === productId ? { ...item, note } : item
    ));
}

// 🧹 Vaciar carrito
export function clearCart() {
    console.log("🧹 Vaciando el carrito en el store global...");
    cartItems.set([]); // Reseteamos a un array vacío
}

// 🗑️ Eliminar el producto de un solo clic
export function removeFromCart(productId) {
    const currentItems = cartItems.get();
    cartItems.set(currentItems.filter(item => (item.id || item.name) !== productId));
}