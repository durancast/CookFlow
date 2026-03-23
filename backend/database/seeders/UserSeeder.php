<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class UserSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        User::updateOrCreate(
            ['email' => 'admin@cookflow.com'],
            [
                'name' => 'Admin CookFlow',
                'role' => 'admin',
                'password' => Hash::make('password123'),
                'email_verified_at' => now(),
            ]
        );

        User::updateOrCreate(
            ['email' => 'camarero@cookflow.com'],
            [
                'name' => 'Camarero CookFlow',
                'role' => 'waiter',
                'password' => Hash::make('password'),
                'email_verified_at' => now(),
            ]
        );
    }
}
