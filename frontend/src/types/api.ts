export interface Category {
  id: number;
  name: string;
}

export interface Product {
  id: number;
  category_id: number;
  name: string;
  description: string;
  price: string;
  category: Category;
  image?: string;
}