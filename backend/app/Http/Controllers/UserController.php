<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Auth; // 👈 ¡Añade esta línea!
use Illuminate\Http\JsonResponse;

class UserController extends Controller
{
    public function index(): JsonResponse
    {
        return response()->json(User::orderBy('name')->get());
    }

    public function store(Request $request): JsonResponse
    {
        $data = $request->validate([
            'name'     => 'required|string|max:255',
            'password' => 'required|string|min:4',
            'role'     => 'required|in:admin,manager,waiter',
        ]);

        $email = strtolower(str_replace(' ', '', $data['name'])) . '@cookflow.com';

        $user = User::create([
            'name'     => $data['name'],
            'email'    => $email,
            'password' => Hash::make($data['password']),
            'role'     => $data['role'],
        ]);

        return response()->json($user, 201);
    }

    public function destroy(User $user): JsonResponse
    {
        // 🟢 Usamos Auth::id() que es más robusto
        if (Auth::id() === $user->id) {
            return response()->json(['message' => 'No puedes borrar tu propio usuario'], 403);
        }

        $user->delete();
        return response()->json(['message' => 'Empleado eliminado']);
    }
}