<?php

return [
    /*
    | Aquí permitimos que las rutas de la API acepten peticiones externas
    */
    'paths' => ['api/*', 'sanctum/csrf-cookie'],

    'allowed_methods' => ['*'], // Permite GET, POST, PUT, DELETE, etc.

    // 🚩 AQUÍ ESTÁ LA CLAVE: 
    // Debes poner la URL exacta de tu frontend de Astro
    'allowed_origins' => ['http://localhost:4321'], 

    'allowed_origins_patterns' => [],

    'allowed_headers' => ['*'], // Permite todas las cabeceras (Authorization, Content-Type, etc.)

    'exposed_headers' => [],

    'max_age' => 0,

    'supports_credentials' => true, // 👈 IMPORTANTE para que funcionen las Cookies/Tokens
];