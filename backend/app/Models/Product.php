<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Product extends Model
{
    /// Como no tenemos ningún timestamp en nuestra BBDD, tenemos que declararlo como null para que Laravel no dé problemas
    public $timestamps = false; 
}
