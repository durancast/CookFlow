<?php

namespace App\Http\Controllers;

use App\Models\Order;
use App\Models\Product;
use App\Models\Table;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Auth;

class OrderController extends Controller
{
    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'table_id'          => 'required|integer|exists:tables,id',
            'items'             => 'required|array|min:1',
            'items.*.product_id' => 'required|integer|exists:products,id',
            'items.*.quantity'   => 'required|integer|min:1',
            'items.*.notes'      => 'nullable|string|max:255',
        ]);

        $productIds = collect($validated['items'])->pluck('product_id')->unique();
        $prices = Product::whereIn('id', $productIds)->pluck('price', 'id');

        $total = collect($validated['items'])->sum(
            fn($item) => $prices[$item['product_id']] * $item['quantity']
        );

        $order = DB::transaction(function () use ($validated, $prices, $total) {
            $order = Order::create([
                'table_id'    => $validated['table_id'],
                'waiter_id' => Auth::id() ?? 1,
                'total_price' => $total,       
                'status'      => 'pending',
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

    public function updateStatus(Request $request, Order $order): JsonResponse
    {
        $validated = $request->validate([
            'status' => ['required', 'in:pending,preparing,served,paid'],
        ]);

        $order->update(['status' => $validated['status']]);

        if ($validated['status'] === 'paid') {
            Table::where('id', $order->table_id)->update(['status' => 'free']);
        }

        return response()->json($order);
    }
}
