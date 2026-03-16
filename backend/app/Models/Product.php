<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Product extends Model
{
    // 1. Añadir esta línea para evitar el error de SQL
    public $timestamps = false; 

    protected $fillable = ['name', 'description', 'price', 'image', 'category_id'];

    public function category(): BelongsTo
    {
        return $this->belongsTo(Category::class);
    }
}