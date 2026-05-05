<?php

namespace App\Http\Controllers;

use App\Models\Order;
use App\Models\Table;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

class DashboardController extends Controller
{
    public function stats(): JsonResponse
    {
        $revenueToday = Order::where('status', 'paid')
            ->whereDate('created_at', today())
            ->sum('total_price');

        $topProduct = DB::table('order_items')
            ->join('products', 'order_items.product_id', '=', 'products.id')
            ->select('products.id', 'products.name', DB::raw('SUM(order_items.quantity) as total_quantity'))
            ->groupBy('products.id', 'products.name')
            ->orderByDesc('total_quantity')
            ->first();

        $occupiedTables = Table::where('status', 'occupied')->count();

        return response()->json([
            'revenue_today'   => (float) $revenueToday,
            'top_product'     => $topProduct,
            'occupied_tables' => $occupiedTables,
        ]);
    }
}
