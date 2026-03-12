<?php
/**
 * Creado — método │ index() con Eager  
 */
namespace App\Http\Controllers;

use App\Models\Product;
use Illuminate\Http\JsonResponse;

class ProductController extends Controller
{
    public function index(): JsonResponse
    {
        $products = Product::with('category')->get();

        return response()->json($products);
    }
}