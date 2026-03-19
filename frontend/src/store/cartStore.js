import { atom } from 'nanostores';

export const cartItems = atom([]);

// ➕ Añadir producto
export function addToCart(product) {
    // 1. Chivato para la consola: Vamos a ver qué nos manda Laravel realmente
    console.log("Intentando añadir al carrito el objeto:", product);

    // 2. Extraemos el ID. Si Luis lo llamó distinto, cámbialo aquí (ej. product.id_producto)
    // Usamos el nombre como plan B por si el ID viene nulo temporalmente
    const productId = product.id 

    if (!productId) {
        console.error("🚨 ERROR: El producto no tiene un identificador válido.");
        return;
    }

    const currentItems = cartItems.get();
    const existingItem = currentItems.find(item => (item.id || item.name) === productId);

    if (existingItem) {
        // Si existe, le sumamos 1
        cartItems.set(currentItems.map(item => 
            (item.id || item.name) === productId 
                ? { ...item, quantity: item.quantity + 1 } 
                : item
        ));
    } else {
        // Si no existe, lo creamos con cantidad 1
        cartItems.set([...currentItems, { ...product, quantity: 1, note: '' }]);
    }
}

// ➖ Actualizar cantidad (+1 o -1)
export function updateQuantity(productId, delta) {
    const currentItems = cartItems.get();
    
    const updatedItems = currentItems.map(item => {
        // Comparamos usando el identificador seguro
        if ((item.id || item.name) === productId) {
            return { ...item, quantity: item.quantity + delta };
        }
        return item;
    }).filter(item => item.quantity > 0); // Eliminamos si baja a 0

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
    cartItems.set([]);
}