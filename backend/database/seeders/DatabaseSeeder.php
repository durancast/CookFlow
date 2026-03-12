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
        // Entrantes
        Product::create([
            'category_id' => $entrants->id,
            'name' => 'Patatas Bravas',
            'price' => 6.50,
            'description' => 'Patatas crujientes por fuera y tiernas por dentro, acompañadas de nuestra salsa brava secreta ligeramente picante.',
            'image' => 'patatas_bravas.webp'
        ]);
        Product::create([
            'category_id' => $entrants->id,
            'name' => 'Tequeños',
            'price' => 8.00,
            'description' => '6 deliciosos palitos de masa crujiente rellenos de queso fundido.',
            'image' => 'tequenios.webp'
        ]);

        // Hamburguesas
        Product::create([
            'category_id' => $burgers->id,
            'name' => 'La Jefa',
            'price' => 12.90,
            'description' => 'Doble carne de ternera premium (360g), bacon ahumado crujiente y queso cheddar fundido.',
            'image' => 'hamburguesa_bacon.webp'
        ]);
        Product::create([
            'category_id' => $burgers->id,
            'name' => 'The Goat',
            'price' => 11.00,
            'description' => 'Hamburguesa de ternera con medallón de queso de cabra caramelizado, cebolla y pepinos.',
            'image' => 'hamburguesa_queso.webp'
        ]);
        Product::create([
            'category_id' => $burgers->id,
            'name' => 'Trufada',
            'price' => 14.50,
            'description' => 'Nuestra joya de la corona: ternera con crema de trufa negra, huevo campero frito y queso.',
            'image' => 'hamburguesa_trufa.webp'
        ]);

        // Bebidas 
        Product::create(['category_id' => $drinks->id, 'name' => 'Cerveza', 'price' => 3.00, 'description' => 'Tercio de cerveza nacional bien fría.', 'image' => 'cerveza.webp']);
        Product::create(['category_id' => $drinks->id, 'name' => 'Coca-Cola', 'price' => 2.50, 'description' => 'Refresco original 33cl.', 'image' => 'coca-cola.webp']);
        Product::create(['category_id' => $drinks->id, 'name' => 'Agua', 'price' => 1.50, 'description' => 'Botella de agua', 'image' => 'agua.webp']);

        // Postres
        Product::create([
            'category_id' => $desserts->id,
            'name' => 'Coulant',
            'price' => 5.50,
            'description' => 'Bizcocho tierno de chocolate con corazón de chocolate negro fundido.',
            'image' => 'coulant.webp'
        ]);
        Product::create([
            'category_id' => $desserts->id,
            'name' => 'Cheesecake',
            'price' => 6.00,
            'description' => 'Tarta de queso cremosa al estilo tradicional sobre base de galleta artesana y coulis de frutos rojos.',
            'image' => 'cheesecake.webp'
        ]);
    }
}
