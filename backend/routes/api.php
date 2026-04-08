use App\Http\Controllers\AuthController;
use App\Http\Controllers\OrderController;
use App\Http\Controllers\ProductController;
use Illuminate\Support\Facades\Route;

// Catálogo público
Route::get('/products', [ProductController::class, 'index']);

// Pedidos — público (el camarero envía desde la tablet sin login)
Route::post('/orders', [OrderController::class, 'store']);

// Autenticación de camareros
Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/me', [AuthController::class, 'me']);
});