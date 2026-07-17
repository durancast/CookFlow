<?php

namespace Database\Seeders;

use App\Models\Category;
use App\Models\Product;
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        $this->call([
            UserSeeder::class,
            TableSeeder::class,
        ]);

        // Categorías
        $entrants  = Category::updateOrCreate(['slug' => 'entrantes'],   ['name' => 'Entrantes']);
        $burgers   = Category::updateOrCreate(['slug' => 'hamburguesas'],['name' => 'Hamburguesas']);
        $drinks    = Category::updateOrCreate(['slug' => 'bebidas'],     ['name' => 'Bebidas']);
        $desserts  = Category::updateOrCreate(['slug' => 'postres'],     ['name' => 'Postres']);
        $salads    = Category::updateOrCreate(['slug' => 'ensaladas'],   ['name' => 'Ensaladas']);
        $mains     = Category::updateOrCreate(['slug' => 'principales'], ['name' => 'Principales']);
        $coffees   = Category::updateOrCreate(['slug' => 'cafes'],       ['name' => 'Cafés']);

        // Entrantes
        Product::updateOrCreate(
            ['name' => 'Patatas Bravas'],
            ['category_id' => $entrants->id, 'price' => 6.50, 'image' => 'patatas_bravas.webp',
             'description' => 'Patatas crujientes por fuera y tiernas por dentro, acompañadas de nuestra salsa brava secreta ligeramente picante.']
        );
        Product::updateOrCreate(
            ['name' => 'Tequeños'],
            ['category_id' => $entrants->id, 'price' => 8.00, 'image' => 'tequenios.webp',
             'description' => '6 deliciosos palitos de masa crujiente rellenos de queso fundido.']
        );

        // Hamburguesas
        Product::updateOrCreate(
            ['name' => 'La Jefa'],
            ['category_id' => $burgers->id, 'price' => 12.90, 'image' => 'hamburguesa_bacon.webp',
             'description' => 'Doble carne de ternera premium (360g), bacon ahumado crujiente y queso cheddar fundido.']
        );
        Product::updateOrCreate(
            ['name' => 'The Goat'],
            ['category_id' => $burgers->id, 'price' => 11.00, 'image' => 'hamburguesa_queso.webp',
             'description' => 'Hamburguesa de ternera con medallón de queso de cabra caramelizado, cebolla y pepinos.']
        );
        Product::updateOrCreate(
            ['name' => 'Trufada'],
            ['category_id' => $burgers->id, 'price' => 14.50, 'image' => 'hamburguesa_trufa.webp',
             'description' => 'Nuestra joya de la corona: ternera con crema de trufa negra, huevo campero frito y queso.']
        );

        // Bebidas
        Product::updateOrCreate(
            ['name' => 'Cerveza'],
            ['category_id' => $drinks->id, 'price' => 3.00, 'image' => 'cerveza.webp',
             'description' => 'Tercio de cerveza nacional bien fría.']
        );
        Product::updateOrCreate(
            ['name' => 'Coca-Cola'],
            ['category_id' => $drinks->id, 'price' => 2.50, 'image' => 'coca_cola.webp',
             'description' => 'Refresco original 33cl.']
        );
        Product::updateOrCreate(
            ['name' => 'Agua'],
            ['category_id' => $drinks->id, 'price' => 1.50, 'image' => 'agua.webp',
             'description' => 'Botella de agua mineral 50cl.']
        );

        // Postres
        Product::updateOrCreate(
            ['name' => 'Coulant'],
            ['category_id' => $desserts->id, 'price' => 5.50, 'image' => 'coulant.webp',
             'description' => 'Bizcocho tierno de chocolate con corazón de chocolate negro fundido.']
        );
        Product::updateOrCreate(
            ['name' => 'Cheesecake'],
            ['category_id' => $desserts->id, 'price' => 6.00, 'image' => 'cheesecake.webp',
             'description' => 'Tarta de queso cremosa al estilo tradicional sobre base de galleta artesana y coulis de frutos rojos.']
        );

        // Ensaladas
        Product::updateOrCreate(
            ['name' => 'Ensalada César'],
            ['category_id' => $salads->id, 'price' => 9.50, 'image' => null,
             'description' => 'Lechuga romana, pollo a la plancha, picatostes artesanos, parmesano y salsa César de la casa.']
        );
        Product::updateOrCreate(
            ['name' => 'Ensalada de la Casa'],
            ['category_id' => $salads->id, 'price' => 7.50, 'image' => null,
             'description' => 'Mix de lechugas, tomate cherry, cebolla morada, zanahoria rallada y vinagreta de mostaza.']
        );

        // Principales
        Product::updateOrCreate(
            ['name' => 'Costillas BBQ'],
            ['category_id' => $mains->id, 'price' => 16.90, 'image' => null,
             'description' => 'Medio rack de costillas de cerdo cocinadas 12h a baja temperatura, glaseadas con salsa BBQ ahumada.']
        );
        Product::updateOrCreate(
            ['name' => 'Pollo a la Parrilla'],
            ['category_id' => $mains->id, 'price' => 13.50, 'image' => null,
             'description' => 'Medio pollo de corral marinado con hierbas mediterráneas, asado a la parrilla con patatas.']
        );

        // Cafés
        Product::updateOrCreate(
            ['name' => 'Café Solo'],
            ['category_id' => $coffees->id, 'price' => 1.50, 'image' => null,
             'description' => 'Espresso corto de mezcla natural.']
        );
        Product::updateOrCreate(
            ['name' => 'Café con Leche'],
            ['category_id' => $coffees->id, 'price' => 2.00, 'image' => null,
             'description' => 'Espresso con leche vaporizada al gusto.']
        );

        $this->call([
            OrderSeeder::class,
        ]);
    }
}
