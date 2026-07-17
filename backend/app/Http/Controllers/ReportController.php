<?php

namespace App\Http\Controllers;

use App\Models\Order;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class ReportController extends Controller
{
    public function daily(Request $request): JsonResponse
    {
        $date = $request->query('date', today()->toDateString());

        $orders = Order::where('status', 'paid')
            ->whereDate('created_at', $date)
            ->get();

        $revenue    = (float) $orders->sum('total_price');
        $orderCount = $orders->count();
        $avgTicket  = $orderCount > 0 ? round($revenue / $orderCount, 2) : 0;

        $products = DB::table('order_items')
            ->join('products', 'order_items.product_id', '=', 'products.id')
            ->join('orders', 'order_items.order_id', '=', 'orders.id')
            ->where('orders.status', 'paid')
            ->whereDate('orders.created_at', $date)
            ->select(
                'products.id',
                'products.name',
                DB::raw('SUM(order_items.quantity) as quantity'),
                DB::raw('SUM(order_items.quantity * order_items.unit_price) as total')
            )
            ->groupBy('products.id', 'products.name')
            ->orderByDesc('quantity')
            ->get()
            ->map(fn($row) => [
                'id'       => $row->id,
                'name'     => $row->name,
                'quantity' => (int) $row->quantity,
                'total'    => (float) $row->total,
            ]);

        return response()->json([
            'date'        => $date,
            'revenue'     => $revenue,
            'order_count' => $orderCount,
            'avg_ticket'  => (float) $avgTicket,
            'products'    => $products,
        ]);
    }

    public function range(Request $request): JsonResponse
    {
        $request->validate([
            'from' => ['required', 'date_format:Y-m-d'],
            'to'   => ['required', 'date_format:Y-m-d', 'after_or_equal:from'],
        ]);

        $from = $request->query('from');
        $to   = $request->query('to');

        $orders = Order::where('status', 'paid')
            ->whereDate('created_at', '>=', $from)
            ->whereDate('created_at', '<=', $to)
            ->get();

        $revenue    = (float) $orders->sum('total_price');
        $orderCount = $orders->count();
        $avgTicket  = $orderCount > 0 ? round($revenue / $orderCount, 2) : 0;

        $products = DB::table('order_items')
            ->join('products', 'order_items.product_id', '=', 'products.id')
            ->join('orders', 'order_items.order_id', '=', 'orders.id')
            ->where('orders.status', 'paid')
            ->whereDate('orders.created_at', '>=', $from)
            ->whereDate('orders.created_at', '<=', $to)
            ->select(
                'products.id',
                'products.name',
                DB::raw('SUM(order_items.quantity) as quantity'),
                DB::raw('SUM(order_items.quantity * order_items.unit_price) as total')
            )
            ->groupBy('products.id', 'products.name')
            ->orderByDesc('quantity')
            ->get()
            ->map(fn($row) => [
                'id'       => $row->id,
                'name'     => $row->name,
                'quantity' => (int) $row->quantity,
                'total'    => (float) $row->total,
            ]);

        $daily = DB::table('orders')
            ->where('status', 'paid')
            ->whereDate('created_at', '>=', $from)
            ->whereDate('created_at', '<=', $to)
            ->select(
                DB::raw('DATE(created_at) as date'),
                DB::raw('SUM(total_price) as revenue'),
                DB::raw('COUNT(*) as orders')
            )
            ->groupBy(DB::raw('DATE(created_at)'))
            ->orderBy('date')
            ->get()
            ->map(fn($row) => [
                'date'    => $row->date,
                'revenue' => (float) $row->revenue,
                'orders'  => (int) $row->orders,
            ]);

        return response()->json([
            'from'        => $from,
            'to'          => $to,
            'revenue'     => $revenue,
            'order_count' => $orderCount,
            'avg_ticket'  => (float) $avgTicket,
            'products'    => $products,
            'daily'       => $daily,
        ]);
    }
}
