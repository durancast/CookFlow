export type CategorySlug = 
  | "bebidas" 
  | "entrantes" 
  | "ensaladas" 
  | "principales" 
  | "hamburguesas" 
  | "postres" 
  | "cafes";

export interface Product {
    id: number;
    name: string;
    price: number;
    description: string;
    category: CategorySlug;
    image: string;
}