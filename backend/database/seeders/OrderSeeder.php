<?php

namespace Database\Seeders;

use App\Models\Order;
use App\Models\Product;
use App\Models\User;
use Illuminate\Database\Seeder;

class OrderSeeder extends Seeder
{
    public function run(): void
    {
        if (Order::count() > 0) {
            return;
        }

        $waiter = User::where('role', 'waiter')->first();
        $waiterId = $waiter?->id ?? 1;

        $prices = Product::pluck('price', 'id');

        $orders = [
            [
                'table_id' => 3, 'status' => 'paid',
                'items' => [
                    ['product_id' => 1, 'quantity' => 2, 'notes' => null],
                    ['product_id' => 6, 'quantity' => 2, 'notes' => null],
                ],
            ],
            [
                'table_id' => 7, 'status' => 'paid',
                'items' => [
                    ['product_id' => 3, 'quantity' => 1, 'notes' => 'Sin pepinillos'],
                    ['product_id' => 7, 'quantity' => 1, 'notes' => null],
                    ['product_id' => 9, 'quantity' => 1, 'notes' => null],
                ],
            ],
            [
                'table_id' => 1, 'status' => 'served',
                'items' => [
                    ['product_id' => 5, 'quantity' => 2, 'notes' => null],
                    ['product_id' => 2, 'quantity' => 1, 'notes' => null],
                    ['product_id' => 8, 'quantity' => 2, 'notes' => null],
                ],
            ],
            [
                'table_id' => 5, 'status' => 'preparing',
                'items' => [
                    ['product_id' => 4, 'quantity' => 1, 'notes' => 'Punto de cocción medio'],
                    ['product_id' => 6, 'quantity' => 2, 'notes' => null],
                ],
            ],
            [
                'table_id' => 9, 'status' => 'pending',
                'items' => [
                    ['product_id' => 3, 'quantity' => 2, 'notes' => null],
                    ['product_id' => 1, 'quantity' => 1, 'notes' => 'Extra salsa'],
                    ['product_id' => 7, 'quantity' => 2, 'notes' => null],
                ],
            ],
        ];

        foreach ($orders as $data) {
            $total = collect($data['items'])->sum(
                fn($item) => ($prices[$item['product_id']] ?? 0) * $item['quantity']
            );

            $order = Order::create([
                'table_id'    => $data['table_id'],
                'waiter_id'   => $waiterId,
                'status'      => $data['status'],
                'total_price' => $total,
            ]);

            foreach ($data['items'] as $item) {
                $order->items()->create([
                    'product_id' => $item['product_id'],
                    'quantity'   => $item['quantity'],
                    'unit_price' => $prices[$item['product_id']] ?? 0,
                    'notes'      => $item['notes'],
                ]);
            }
        }
    }
}
