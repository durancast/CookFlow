<?php

namespace App\Http\Controllers;

use App\Models\Table;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class TableController extends Controller
{
    public function index(): JsonResponse
    {
        return response()->json(Table::with('activeOrder')->orderBy('number')->get());
    }

    public function store(Request $request): JsonResponse
    {
        $data = $request->validate([
            'number'   => ['required', 'integer', 'min:1', 'unique:tables,number'],
            'capacity' => ['required', 'integer', 'min:1'],
            'status'   => ['sometimes', 'in:free,occupied,pending'],
        ]);

        $table = Table::create($data);

        return response()->json($table, 201);
    }

    public function destroy(Table $table): JsonResponse
    {
        $table->delete();

        return response()->json(null, 204);
    }
}
