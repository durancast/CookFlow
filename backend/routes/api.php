<?php
use App\Http\Controllers\AuthController;
use App\Http\Controllers\OrderController;
use App\Http\Controllers\ProductController;
use App\Http\Controllers\TableController;
use Illuminate\Support\Facades\Route;

// --- PRODUCTOS ---
Route::apiResource('products', ProductController::class);

// --- MESAS ---
Route::apiResource('tables', TableController::class);
Route::get('/tables/{id}/qr', [TableController::class, 'generateQr']);

// --- PEDIDOS ---
Route::post('/orders', [OrderController::class, 'store']);
Route::patch('/orders/{order}/status', [OrderController::class, 'updateStatus']);

// --- AUTENTICACIÓN ---
Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/me', [AuthController::class, 'me']);
});