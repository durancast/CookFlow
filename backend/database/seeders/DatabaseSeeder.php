<?php

namespace Database\Seeders;

use App\Models\Category; // Importamos el category
use App\Models\Product;  // Importamos el product
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        $this->call(UserSeeder::class);

        // 2. 4 categorías
        $entrants = Category::updateOrCreate(['slug' => 'entrantes'], ['name' => 'Entrantes']);
        $burgers = Category::updateOrCreate(['slug' => 'hamburguesas'], ['name' => 'Hamburguesas']);
        $drinks = Category::updateOrCreate(['slug' => 'bebidas'], ['name' => 'Bebidas']);
        $desserts = Category::updateOrCreate(['slug' => 'postres'], ['name' => 'Postres']);

        // 3. 10 productos reales
        // Entrantes
        // Entrantes
        Product::updateOrCreate(
            ['name' => 'Patatas Bravas'],
            [
                'category_id' => $entrants->id,
                'price' => 6.50,
                'description' => 'Patatas crujientes por fuera y tiernas por dentro, acompañadas de nuestra salsa brava secreta ligeramente picante.',
                'image' => 'patatas_bravas.webp',
            ]
        );
        Product::updateOrCreate(
            ['name' => 'Tequeños'],
            [
                'category_id' => $entrants->id,
                'price' => 8.00,
                'description' => '6 deliciosos palitos de masa crujiente rellenos de queso fundido.',
                'image' => 'tequenios.webp',
            ]
        );

        // Hamburguesas
        Product::updateOrCreate(
            ['name' => 'La Jefa'],
            [
                'category_id' => $burgers->id,
                'price' => 12.90,
                'description' => 'Doble carne de ternera premium (360g), bacon ahumado crujiente y queso cheddar fundido.',
                'image' => 'hamburguesa_bacon.webp',
            ]
        );
        Product::updateOrCreate(
            ['name' => 'The Goat'],
            [
                'category_id' => $burgers->id,
                'price' => 11.00,
                'description' => 'Hamburguesa de ternera con medallón de queso de cabra caramelizado, cebolla y pepinos.',
                'image' => 'hamburguesa_queso.webp',
            ]
        );
        Product::updateOrCreate(
            ['name' => 'Trufada'],
            [
                'category_id' => $burgers->id,
                'price' => 14.50,
                'description' => 'Nuestra joya de la corona: ternera con crema de trufa negra, huevo campero frito y queso.',
                'image' => 'hamburguesa_trufa.webp',
            ]
        );

        // Bebidas
        Product::updateOrCreate(['name' => 'Cerveza'], ['category_id' => $drinks->id, 'price' => 3.00, 'description' => 'Tercio de cerveza nacional bien fría.', 'image' => 'cerveza.webp']);
        Product::updateOrCreate(['name' => 'Coca-Cola'], ['category_id' => $drinks->id, 'price' => 2.50, 'description' => 'Refresco original 33cl.', 'image' => 'coca_cola.webp']);
        Product::updateOrCreate(['name' => 'Agua'], ['category_id' => $drinks->id, 'price' => 1.50, 'description' => 'Botella de agua', 'image' => 'agua.webp']);

        // Postres
        Product::updateOrCreate(
            ['name' => 'Coulant'],
            [
                'category_id' => $desserts->id,
                'price' => 5.50,
                'description' => 'Bizcocho tierno de chocolate con corazón de chocolate negro fundido.',
                'image' => 'coulant.webp',
            ]
        );
        Product::updateOrCreate(
            ['name' => 'Cheesecake'],
            [
                'category_id' => $desserts->id,
                'price' => 6.00,
                'description' => 'Tarta de queso cremosa al estilo tradicional sobre base de galleta artesana y coulis de frutos rojos.',
                'image' => 'cheesecake.webp',
            ]
        );
    }
}

