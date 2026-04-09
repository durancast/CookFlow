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

    public function destroy(Table $table): JsonResponse
    {
        $table->delete();

        return response()->json(null, 204);
    }

    public function generateQr($id)
    {
        $table = Table::findOrFail($id);
        $url = "https://cookflow.com/menu?table=" . $table->number;

        $qrCode = QrCode::size(300)->margin(1)->generate($url);

        return response($qrCode)->header('Content-Type', 'image/svg+xml');
    }
}
