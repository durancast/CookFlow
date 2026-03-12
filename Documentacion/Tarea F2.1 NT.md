###  Flujo de la petición:
___
  GET /api/products
    → ProductController@index
    → Product::with('category')->get()
    → JsonResponse con todos los platos + su categoría embebida

  #### **Ejemplo de respuesta JSON:**
```JSON
  [
    {
      "id": 1,
      "name": "Paella",
      "price": 12.50,
      "category_id": 2,
      "category": {
        "id": 2,
        "name": "Arroces"
      }
    }
  ]
```
### Archivos creados/modificados:
___

| Archivo                                    | Acción                                     |
| ------------------------------------------ | ------------------------------------------ |
| app/Models/Category.php                    | Creado — modelo con `hasMany(Products)`    |
| app/Models/Product.php                     | Creado — modelo con  `belongsTo(Category)` |
| app/Http/Controllers/ProductController.php | Creado — método index() con Eager Loading  |
| routes/api.php                             | Creado — ruta GET /products                |
| bootstrap/app.php                          | Modificado — registra api.php              |

![[Screenshot from 2026-03-12 22-17-47.png]]
### Commandos para mis pruebas:
`php artisan migrate:rollback --step=2
`php artisan migrate

**Luego vuelve a tinker y ya funcionará:**

`php artisan tinker

```
  $cat = App\Models\Category::create(['name' => 'Arroces']);
  App\Models\Product::create([
      'name' => 'Paella',
      'description' => 'Paella valenciana',
      'price' => 12.50,
      'category_id' => $cat->id
  ]);
```
