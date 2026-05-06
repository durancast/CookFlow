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

        User::updateOrCreate(
            ['email' => 'nizarad@cookflow.com'],
            [
                'name' => 'NizarAd',
                'role' => 'admin',
                'password' => Hash::make('admin'),
                'email_verified_at' => now(),
            ]
        );

        User::updateOrCreate(
            ['email' => 'nizarcam@cookflow.com'],
            [
                'name' => 'NizarCam',
                'role' => 'waiter',
                'password' => Hash::make('NizarCam'),
                'email_verified_at' => now(),
            ]
        );
    }
}
