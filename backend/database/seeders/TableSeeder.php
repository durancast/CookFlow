<?php

namespace Database\Seeders;

use App\Models\Table;
use Illuminate\Database\Seeder;

class TableSeeder extends Seeder
{
    public function run(): void
    {
        // Mesas pequeñas
        for ($i = 1; $i <= 5; $i++) {
            Table::updateOrCreate(
                ['number' => $i],
                ['capacity' => 2, 'status' => 'free']
            );
        }

        // Mesas medianas
        for ($i = 6; $i <= 10; $i++) {
            Table::updateOrCreate(
                ['number' => $i],
                ['capacity' => 4, 'status' => 'free']
            );
        }
        
        // Una mesa grande 
        Table::updateOrCreate(
            ['number' => 11],
            ['capacity' => 8, 'status' => 'free']
        );
    }
}