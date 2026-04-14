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
                'name' => 'Admin',
                'role' => 'admin',
                'password' => Hash::make('admin'),
                'email_verified_at' => now(),
            ]
        );

        User::updateOrCreate(
            ['email' => 'camarero@cookflow.com'],
            [
                'name' => 'Camarero',
                'role' => 'waiter',
                'password' => Hash::make('camarero'),
                'email_verified_at' => now(),
            ]
        );
    }
}
