<?php

namespace App\Http\Controllers;

use App\Models\Table;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use SimpleSoftwareIO\QrCode\Facades\QrCode;

class TableController extends Controller
{
    public function index(): JsonResponse
    {
        $tables = Table::with('activeOrder')->orderBy('number')->get();

        $tables->transform(function ($table) {
            $table->qr_url = url("/api/tables/{$table->id}/qr");
            return $table;
        });

        return response()->json($tables);
    }

    public function store(Request $request): JsonResponse
    {
        $data = $request->validate([
            'number'   => ['required', 'integer', 'min:1', 'unique:tables,number'],
            'capacity' => ['required', 'integer', 'min:1'],
            'status'   => ['sometimes', 'in:free,occupied,pending'],
        ]);

        $table = Table::create($data);

        return response()->json($table, 201);
    }

    public function update(Request $request, Table $table): JsonResponse
    {
        $data = $request->validate([
            // Validamos que el número sea único, pero ignoramos el ID de la mesa actual
            'number'   => ['required', 'integer', 'min:1', 'unique:tables,number,' . $table->id],
            'capacity' => ['required', 'integer', 'min:1'],
            'status'   => ['sometimes', 'in:free,occupied,pending'],
        ]);

        $table->update($data);

        return response()->json([
            'message' => 'Mesa actualizada correctamente',
            'table' => $table
        ]);
    }

    public function destroy(Table $table): JsonResponse
    {
        $table->delete();

        // Cambiado a 200 con mensaje para que el fetch de Astro no piense que está vacío
        return response()->json(['message' => 'Mesa eliminada'], 200);
    }

    public function updateStatus(Request $request, Table $table): JsonResponse
    {
        $validated = $request->validate([
            'status' => ['required', 'in:free,occupied,pending'],
        ]);

        $table->update(['status' => $validated['status']]);

        return response()->json($table->fresh());
    }

    public function generateQr($id)
    {
        $table = Table::findOrFail($id);
        // Ajusta esta URL a la de tu frontend real de clientes
        $url = env('APP_PUBLIC_URL', 'http://localhost:4321') . '/menu?table=' . $table->number;

        $qrCode = QrCode::size(300)->margin(1)->generate($url);

        return response($qrCode)->header('Content-Type', 'image/svg+xml');
    }

    // 🔥 NUEVO: Obtener la comanda activa de la mesa para el TPV
    public function getActiveOrder(Table $table): JsonResponse
    {
        // 1. Usamos la excelente relación activeOrder() que ya tienes en tu modelo Table
        // y le pedimos que nos traiga también los 'items' y el 'product' de cada item.
        $order = $table->activeOrder()->with('items.product')->first();

        if (!$order || $order->items->isEmpty()) {
            return response()->json(['items' => []]);
        }

        // 2. Mapeamos los items usando tu estructura real ($orderItem->product)
        $cartItems = $order->items->map(function ($orderItem) {
            return [
                // Es vital pasar el ID del producto, NO el ID del order_item
                'id'       => $orderItem->product->id, 
                'name'     => $orderItem->product->name,
                'price'    => (float) $orderItem->unit_price, 
                'quantity' => $orderItem->quantity,
                'note'     => $orderItem->notes ?? '', 
            ];
        });

        return response()->json(['items' => $cartItems]);
    }

    public function callWaiter(Table $table): JsonResponse
    {
        $table->update(['call_waiter' => true]);

        return response()->json(['message' => 'Camarero llamado']);
    }

    public function clearWaiter(Table $table): JsonResponse
    {
        $table->update(['call_waiter' => false]);

        return response()->json(['message' => 'Llamada cancelada']);
    }

    // 💸 COBRAR Y LIBERAR MESA
    public function checkout(Request $request, Table $table): JsonResponse
    {
        // Recibimos si es cash (efectivo) o card (tarjeta)
        $validated = $request->validate([
            'payment_method' => ['sometimes', 'in:cash,card']
        ]);

        $order = $table->activeOrder()->first();

        if ($order) {
            // Actualizamos la orden a pagada
            $order->update([
                'status' => 'paid'
            ]);
            
            // 💡 NOTA: Si en el futuro añades una columna "payment_method" a tu tabla de orders,
            // puedes guardarlo así:
            // $order->update(['status' => 'paid', 'payment_method' => $validated['payment_method']]);
        }

        // Liberamos la mesa
        $table->update(['status' => 'free']);

        return response()->json([
            'message' => 'Mesa cobrada y liberada correctamente'
        ]);
    }
}