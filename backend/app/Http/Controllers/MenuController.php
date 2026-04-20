<?php

namespace App\Http\Controllers;

use App\Models\Category;
use Illuminate\Http\JsonResponse;

class MenuController extends Controller
{
    public function index(): JsonResponse
    {
        $categories = Category::with(['products' => function ($query) {
            $query->select('id', 'name', 'description', 'price', 'image', 'category_id');
        }])->get(['id', 'name', 'slug']);

        return response()->json($categories);
    }
}
