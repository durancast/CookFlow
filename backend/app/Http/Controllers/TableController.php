<?php

namespace App\Http\Controllers;

use App\Models\Table;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use SimpleSoftwareIO\QrCode\Facades\QrCode;

class TableController extends Controller
{
    public function index(): JsonResponse
    {
        $tables = Table::with('activeOrder')->orderBy('number')->get();

        $tables->transform(function ($table) {
            $table->qr_url = url("/api/tables/{$table->id}/qr");
            return $table;
        });

        return response()->json($tables);
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

    // 🟢 NUEVO MÉTODO: Actualizar mesa
    public function update(Request $request, Table $table): JsonResponse
    {
        $data = $request->validate([
            // Validamos que el número sea único, pero ignoramos el ID de la mesa actual
            'number'   => ['required', 'integer', 'min:1', 'unique:tables,number,' . $table->id],
            'capacity' => ['required', 'integer', 'min:1'],
            'status'   => ['sometimes', 'in:free,occupied,pending'],
        ]);

        $table->update($data);

        return response()->json([
            'message' => 'Mesa actualizada correctamente',
            'table' => $table
        ]);
    }

    // 🔴 AJUSTADO: Eliminar mesa
    public function destroy(Table $table): JsonResponse
    {
        $table->delete();

        // Cambiado a 200 con mensaje para que el fetch de Astro no piense que está vacío
        return response()->json(['message' => 'Mesa eliminada'], 200);
    }

    public function updateStatus(Request $request, Table $table): JsonResponse
    {
        $validated = $request->validate([
            'status' => ['required', 'in:free,occupied,pending'],
        ]);

        $table->update(['status' => $validated['status']]);

        return response()->json($table->fresh());
    }

    public function generateQr($id)
    {
        $table = Table::findOrFail($id);
        // Ajusta esta URL a la de tu frontend real de clientes
        $url = "https://cookflow.com/menu?table=" . $table->number;

        $qrCode = QrCode::size(300)->margin(1)->generate($url);

        return response($qrCode)->header('Content-Type', 'image/svg+xml');
    }
}