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

    public function show(Table $table): JsonResponse
    {
        return response()->json($table);
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
        $url = env('APP_PUBLIC_URL', 'http://localhost:4321') . '/menu?table=' . $table->id;

        $qrCode = QrCode::size(300)->margin(1)->generate($url);

        return response($qrCode)->header('Content-Type', 'image/svg+xml');
    }

    public function getActiveOrder(Table $table): JsonResponse
    {
        $orders = $table->orders()
            ->where('status', '!=', 'paid')
            ->with('items.product')
            ->get();

        if ($orders->isEmpty()) {
            return response()->json(['items' => []]);
        }

        // Merge items from all rounds: same product + same note → sum quantities
        $merged = [];
        foreach ($orders->flatMap(fn($o) => $o->items) as $item) {
            $key = $item->product_id . '|' . ($item->notes ?? '');
            if (isset($merged[$key])) {
                $merged[$key]['quantity'] += $item->quantity;
            } else {
                $merged[$key] = [
                    'id'       => $item->product->id,
                    'name'     => $item->product->name,
                    'price'    => (float) $item->unit_price,
                    'quantity' => $item->quantity,
                    'note'     => $item->notes ?? '',
                ];
            }
        }

        return response()->json(['items' => array_values($merged)]);
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

        // Mark ALL non-paid orders for this table as paid (multiple rounds)
        $table->orders()->where('status', '!=', 'paid')->update(['status' => 'paid']);

        // Liberamos la mesa
        $table->update(['status' => 'free']);

        return response()->json([
            'message' => 'Mesa cobrada y liberada correctamente'
        ]);
    }
}