package com.logiva.pgpi;

import java.util.List;

public interface ProductRepositoryCustom {
	List<Integer> availableProducts(Pedido pedido);
	List<Producto> findProductsByIdString(String products);
	List<Producto_cantidades> findProductoCantidades();
	List<Object> saveProducto(Producto producto, int cantidad);
	int countProductosPosicion(int id, String tipo);
}
