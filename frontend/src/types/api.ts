// Puedes poner esto en un archivo src/types/api.ts o arriba en tu index.astro
export interface Category {
  id: number;
  name: string;
}

export interface Product {
  id: number;
  category_id: number;
  name: string;
  description: string;
  price: string; // Ojo: viene como string
  category: Category;
  image?: string; // Marcado como opcional porque aún no está en el back
}