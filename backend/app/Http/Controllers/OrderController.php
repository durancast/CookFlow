<?php

namespace App\Http\Controllers;

use App\Models\Order;
use App\Models\Product;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class OrderController extends Controller
{
    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'table_id'              => 'required|integer|min:1',
            'items'                 => 'required|array|min:1',
            'items.*.product_id'    => 'required|integer|exists:products,id',
            'items.*.quantity'      => 'required|integer|min:1',
            'items.*.notes'         => 'nullable|string|max:255',
        ]);

        // Recalculate total from DB prices — never trust client-sent totals
        $productIds = collect($validated['items'])->pluck('product_id')->unique();
        $prices = Product::whereIn('id', $productIds)->pluck('price', 'id');

        $total = collect($validated['items'])->sum(
            fn($item) => $prices[$item['product_id']] * $item['quantity']
        );

        $order = DB::transaction(function () use ($validated, $prices, $total) {
            $order = Order::create([
                'table_id' => $validated['table_id'],
                'total'    => $total,
                'status'   => 'pending',
            ]);

            foreach ($validated['items'] as $item) {
                $order->items()->create([
                    'product_id' => $item['product_id'],
                    'quantity'   => $item['quantity'],
                    'unit_price' => $prices[$item['product_id']],
                    'notes'      => $item['notes'] ?? null,
                ]);
            }

            return $order->load('items.product');
        });

        return response()->json($order, 201);
    }
}
