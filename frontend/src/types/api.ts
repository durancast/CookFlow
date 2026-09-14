// Tipos alineados con la API Spring Boot (backend-spring).

export type UserRole = 'admin' | 'manager' | 'waiter' | 'kitchen';
export type TableStatus = 'free' | 'occupied' | 'pending';
export type OrderStatus = 'pending' | 'preparing' | 'served' | 'paid';

export interface Category {
  id: number;
  name: string;
  slug: string;
}

export interface Dish {
  id: number;
  name: string;
  price: number;
  available: boolean;
  imageUrl: string | null;
  category: { id: number; name: string };
}

// Compatibilidad con el código anterior que aún usa `Product`.
export type Product = Dish;

export interface DishDetail extends Dish {
  description: string | null;
  ingredients: Array<{ id: number; name: string }>;
}

export interface OrderSummary {
  id: number;
  tableNumber: number;
  status: OrderStatus;
  total: number;
  createdAt: string;
}

export interface OrderItem {
  dishId: number;
  dishName: string;
  quantity: number;
  unitPrice: number;
  notes: string | null;
}

export interface OrderDetail extends OrderSummary {
  items: OrderItem[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface Table {
  id: number;
  number: number;
  capacity: number;
  status: TableStatus;
  waiterCalled: boolean;
  waiterCalledAt: string | null;
}

export interface User {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  tenantId: number;
}

export interface DashboardStats {
  todayOrders: number;
  todayRevenue: number;
  averageTicket: number;
  openOrders: number;
  occupiedTables: number;
}

export interface DailyReportRow {
  hour: number;
  orders: number;
  revenue: number;
}

export interface RangeReportRow {
  date: string;
  orders: number;
  revenue: number;
}
