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
Route::get('/products', [ProductController::class, 'index']);
Route::get('/products/{product}', [ProductController::class, 'show']);
Route::middleware(['auth:sanctum', 'role:admin,manager'])->group(function () {
    Route::post('/products', [ProductController::class, 'store']);
    Route::put('/products/{product}', [ProductController::class, 'update']);
    Route::patch('/products/{product}', [ProductController::class, 'update']);
    Route::delete('/products/{product}', [ProductController::class, 'destroy']);
});

// --- CATEGORÍAS ---
Route::get('/categories', [CategoryController::class, 'index']);
Route::middleware(['auth:sanctum', 'role:admin,manager'])->group(function () {
    Route::post('/categories', [CategoryController::class, 'store']);
    Route::put('/categories/{category}', [CategoryController::class, 'update']);
    Route::patch('/categories/{category}', [CategoryController::class, 'update']);
    Route::delete('/categories/{category}', [CategoryController::class, 'destroy']);
});

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
    Route::get('/reports/range', [ReportController::class, 'range']);
});
