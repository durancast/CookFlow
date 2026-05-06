<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasOne;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Table extends Model
{
    public $timestamps = false; 

    protected $table = 'tables';

    protected $fillable = ['number', 'capacity', 'status', 'call_waiter'];

    protected $casts = ['call_waiter' => 'boolean'];

    public function orders(): HasMany
    {
        return $this->hasMany(Order::class);
    }


    public function activeOrder(): HasOne
    {
        return $this->hasOne(Order::class)
            ->where('status', '!=', 'paid')
            ->latestOfMany(); 
    }
}