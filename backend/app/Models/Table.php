<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Table extends Model
{
    public $timestamps = false;

    protected $table = 'tables';

    protected $fillable = ['number', 'capacity', 'status'];
}
