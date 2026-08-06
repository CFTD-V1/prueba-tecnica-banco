import { Routes } from '@angular/router';

/**
 * Enrutamiento de la aplicación de una sola página (SPA). Cada ruta carga su
 * componente de forma diferida (lazy loading): el navegador solo descarga el
 * código de la pantalla cuando el usuario entra en ella.
 */
export const routes: Routes = [
  { path: '', redirectTo: 'clientes', pathMatch: 'full' },

  {
    path: 'clientes',
    title: 'Clientes',
    loadComponent: () =>
      import('./paginas/clientes/lista-clientes/lista-clientes').then((m) => m.ListaClientes),
  },
  {
    path: 'clientes/nuevo',
    title: 'Nuevo cliente',
    loadComponent: () =>
      import('./paginas/clientes/formulario-cliente/formulario-cliente').then(
        (m) => m.FormularioCliente,
      ),
  },
  {
    path: 'clientes/:id/editar',
    title: 'Editar cliente',
    loadComponent: () =>
      import('./paginas/clientes/formulario-cliente/formulario-cliente').then(
        (m) => m.FormularioCliente,
      ),
  },

  {
    path: 'productos',
    title: 'Productos',
    loadComponent: () =>
      import('./paginas/productos/lista-productos/lista-productos').then((m) => m.ListaProductos),
  },
  {
    path: 'productos/nuevo',
    title: 'Nueva cuenta',
    loadComponent: () =>
      import('./paginas/productos/formulario-producto/formulario-producto').then(
        (m) => m.FormularioProducto,
      ),
  },

  {
    path: 'transacciones',
    title: 'Movimientos',
    loadComponent: () =>
      import('./paginas/transacciones/lista-movimientos/lista-movimientos').then(
        (m) => m.ListaMovimientos,
      ),
  },
  {
    path: 'transacciones/nueva',
    title: 'Nuevo movimiento',
    loadComponent: () =>
      import('./paginas/transacciones/formulario-movimiento/formulario-movimiento').then(
        (m) => m.FormularioMovimiento,
      ),
  },

  { path: '**', redirectTo: 'clientes' },
];
