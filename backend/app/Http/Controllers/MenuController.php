<?php

namespace App\Http\Controllers;

use App\Models\Category;
use Illuminate\Http\JsonResponse;

class MenuController extends Controller
{
    public function index(): JsonResponse
    {
        $categories = Category::with(['products' => function ($query) {
            $query->where('available', true)
                  ->select('id', 'name', 'description', 'price', 'image', 'category_id', 'available');
        }])->get(['id', 'name', 'slug']);

        $categories = $categories->filter(fn($cat) => $cat->products->isNotEmpty())->values();

        return response()->json($categories);
    }
}
