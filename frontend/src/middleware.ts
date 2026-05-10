import { defineMiddleware } from 'astro:middleware';

const ROUTE_PERMISSIONS: Array<{
  match: (path: string) => boolean;
  roles: string[];
}> = [
  { match: (p) => p === '/tpv',             roles: ['admin', 'waiter'] },
  { match: (p) => p === '/cocina',           roles: ['admin', 'manager', 'waiter', 'cook'] },
  { match: (p) => p === '/admin/productos',  roles: ['admin', 'manager'] },
  { match: (p) => p === '/admin/categorias', roles: ['admin', 'manager'] },
  { match: (p) => p === '/admin/ventas',     roles: ['admin', 'manager'] },
  { match: (p) => p.startsWith('/admin'),    roles: ['admin'] },
];

export const onRequest = defineMiddleware((context, next) => {
  const raw  = new URL(context.request.url).pathname;
  const path = raw.length > 1 ? raw.replace(/\/$/, '') : raw;

  const rule = ROUTE_PERMISSIONS.find((r) => r.match(path));
  if (!rule) return next();

  const token = context.cookies.get('auth_token')?.value;
  const role  = context.cookies.get('user_role')?.value;

  if (!token || !role) {
    return context.redirect('/');
  }

  if (!rule.roles.includes(role)) {
    return context.redirect('/403');
  }

  return next();
});
