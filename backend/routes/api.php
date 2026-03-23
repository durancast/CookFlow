<?php

use App\Http\Controllers\AuthController;
use App\Http\Controllers\ProductController;
use App\Http\Controllers\TableController;
use Illuminate\Support\Facades\Route;

// Catálogo público
Route::get('/products', [ProductController::class, 'index']);

// Autenticación
Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/me', [AuthController::class, 'me']);

    // Gestión — sólo admin
    Route::middleware('admin')->group(function () {
        // Productos
        Route::post('/products', [ProductController::class, 'store']);
        Route::post('/products/{product}', [ProductController::class, 'update']);
        Route::delete('/products/{product}', [ProductController::class, 'destroy']);

        // Mesas
        Route::get('/tables', [TableController::class, 'index']);
        Route::post('/tables', [TableController::class, 'store']);
        Route::delete('/tables/{table}', [TableController::class, 'destroy']);
    });
});
