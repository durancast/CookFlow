<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasOne;

class Table extends Model
{
    public $timestamps = false;

    protected $table = 'tables';

    protected $fillable = ['number', 'capacity', 'status'];

    public function activeOrder(): HasOne
    {
        return $this->hasOne(Order::class)->whereNot('status', 'paid')->latest('id');
    }
}
