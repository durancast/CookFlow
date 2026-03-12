<?php
/**
 * Creado — modelo con | belogsTo(Category)
 * 
 * GET /api/products
 *   → ProductController@index
 *   → Product::with('category')->get()
 *   → JsonResponse con todos los platos y su categoría embebida
 */
namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Product extends Model
{
    protected $fillable = ['name', 'description', 'price', 'image', 'category_id'];

    public function category(): BelongsTo
    {
        return $this->belongsTo(Category::class);
    }
}
