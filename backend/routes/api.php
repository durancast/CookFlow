<?php
use App\Http\Controllers\AuthController;
use App\Http\Controllers\CategoryController;
use App\Http\Controllers\DashboardController;
use App\Http\Controllers\MenuController;
use App\Http\Controllers\OrderController;
use App\Http\Controllers\ProductController;
use App\Http\Controllers\ReportController;
use App\Http\Controllers\TableController;
use App\Http\Controllers\UserController;
use Illuminate\Support\Facades\Route;

// --- MENÚ PÚBLICO (sin token) ---
Route::get('/public/menu', [MenuController::class, 'index']);

// --- PRODUCTOS ---
Route::apiResource('products', ProductController::class);

// --- CATEGORÍAS ---
Route::apiResource('categories', CategoryController::class)->only(['index', 'store', 'update', 'destroy']);

// --- MESAS ---
Route::apiResource('tables', TableController::class);
Route::get('/tables/{id}/qr', [TableController::class, 'generateQr']);
Route::patch('/tables/{table}/status', [TableController::class, 'updateStatus'])->middleware('auth:sanctum');
Route::get('/tables/{table}/active-order', [TableController::class, 'getActiveOrder'])->middleware('auth:sanctum');
Route::post('/tables/{table}/checkout', [TableController::class, 'checkout'])->middleware('auth:sanctum');
Route::post('/tables/{table}/call-waiter', [TableController::class, 'callWaiter']);
Route::post('/tables/{table}/clear-waiter', [TableController::class, 'clearWaiter'])->middleware('auth:sanctum');

// --- PEDIDOS ---
Route::get('/orders', [OrderController::class, 'index']);
Route::post('/orders', [OrderController::class, 'store']);
Route::patch('/orders/{order}/status', [OrderController::class, 'updateStatus']);

// --- AUTENTICACIÓN ---
Route::post('/login', [AuthController::class, 'login']);

// Ruta pública para que el login pueda mostrar la lista de nombres
Route::get('/users-list', function() {
    return \App\Models\User::select('id', 'name', 'role')->get();
});

Route::middleware('auth:sanctum')->group(function () {
    Route::apiResource('users', UserController::class);
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/me', [AuthController::class, 'me']);
    Route::get('/dashboard/stats', [DashboardController::class, 'stats']);
    Route::get('/reports/daily', [ReportController::class, 'daily']);
});