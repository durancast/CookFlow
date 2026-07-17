<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
class Product extends Model
{
    // 1. Añadir esta línea para evitar el error de SQL
    public $timestamps = false;

    protected $fillable = ['name', 'description', 'price', 'image', 'category_id', 'available'];

    protected $casts = ['available' => 'boolean'];

    public function category(): BelongsTo
    {
        return $this->belongsTo(Category::class);
    }

    public function orderItems(): HasMany
    {
        return $this->hasMany(OrderItem::class);
    }

}
