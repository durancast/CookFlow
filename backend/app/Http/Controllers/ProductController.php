<?php

namespace App\Http\Controllers;

use App\Models\Product;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ProductController extends Controller
{
    public function index(): JsonResponse
    {
        return response()->json(Product::with('category')->get());
    }

    public function store(Request $request): JsonResponse
    {
        $data = $request->validate([
            'name'        => ['required', 'string', 'max:255'],
            'description' => ['nullable', 'string'],
            'price'       => ['required', 'numeric', 'min:0'],
            'category_id' => ['required', 'integer', 'exists:categories,id'],
            'image'       => ['nullable', 'string', 'max:255'],
        ]);

        $product = Product::create($data);
        $product->load('category');

        return response()->json($product, 201);
    }

    public function update(Request $request, Product $product): JsonResponse
    {
        $data = $request->validate([
            'name'        => ['sometimes', 'string', 'max:255'],
            'description' => ['nullable', 'string'],
            'price'       => ['sometimes', 'numeric', 'min:0'],
            'category_id' => ['sometimes', 'integer', 'exists:categories,id'],
            'image'       => ['nullable', 'string', 'max:255'],
        ]);

        $product->update($data);
        $product->load('category');

        return response()->json($product);
    }

    public function destroy(Product $product): JsonResponse
    {
        $product->delete();

        return response()->json(null, 204);
    }
}
