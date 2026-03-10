<?php

namespace Database\Seeders;

use App\Models\User;
use App\Models\Category; // Importamos el category
use App\Models\Product;  // Importamos el product
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        // 1. Creamos el usuario de prueba 
        User::factory()->create([
            'name' => 'Admin CookFlow',
            'email' => 'admin@cookflow.com',
        ]);

        // 2. 4 categorías 
        $entrants = Category::create(['name' => 'Entrantes', 'slug' => 'entrantes']);
        $burgers = Category::create(['name' => 'Hamburguesas', 'slug' => 'hamburguesas']);
        $drinks = Category::create(['name' => 'Bebidas', 'slug' => 'bebidas']);
        $desserts = Category::create(['name' => 'Postres', 'slug' => 'postres']);

        // 3. 10 productos reales
        // Entrantes
        Product::create(['category_id' => $entrants->id, 'name' => 'Patatas Bravas', 'price' => 6.50, 'description' => 'Con salsa casera.', 'image' => 'bravas.jpg']);
        Product::create(['category_id' => $entrants->id, 'name' => 'Tequeños', 'price' => 8.00, 'description' => '6 palitos de queso.', 'image' => 'tequenos.jpg']);

        // Hamburguesas
        Product::create(['category_id' => $burgers->id, 'name' => 'La Jefa', 'price' => 12.90, 'description' => 'Doble carne y bacon.', 'image' => 'jefa.jpg']);
        Product::create(['category_id' => $burgers->id, 'name' => 'Veggie Mix', 'price' => 11.00, 'description' => 'Heura y aguacate.', 'image' => 'veggie.jpg']);
        Product::create(['category_id' => $burgers->id, 'name' => 'Trufada', 'price' => 14.50, 'description' => 'Salsa de trufa y huevo.', 'image' => 'trufada.jpg']);

        // Bebidas
        Product::create(['category_id' => $drinks->id, 'name' => 'Cerveza', 'price' => 3.00, 'image' => 'beer.jpg']);
        Product::create(['category_id' => $drinks->id, 'name' => 'Refresco', 'price' => 2.50, 'image' => 'soda.jpg']);
        Product::create(['category_id' => $drinks->id, 'name' => 'Agua', 'price' => 1.50, 'image' => 'water.jpg']);

        // Postres
        Product::create(['category_id' => $desserts->id, 'name' => 'Coulant', 'price' => 5.50, 'description' => 'Chocolate fundido.', 'image' => 'coulant.jpg']);
        Product::create(['category_id' => $desserts->id, 'name' => 'Cheesecake', 'price' => 6.00, 'description' => 'Receta de la abuela.', 'image' => 'cheese.jpg']);
    }
}
