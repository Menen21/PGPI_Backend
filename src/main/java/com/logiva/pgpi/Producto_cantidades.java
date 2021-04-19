package com.logiva.pgpi;

public class Producto_cantidades extends Producto{
	private int stock;
	private int preparacion;
	boolean restock;

	public Producto_cantidades() {}
	
	public Producto_cantidades(Producto producto, int stock, int preparacion, boolean restock) {
		this.setId(producto.getId());
		this.setCantidad(producto.getCantidad());
		this.setNombre(producto.getNombre());
		this.setStock(stock);
		this.setPreparacion(preparacion);
		this.setCantidad_minima_restock(producto.getCantidad_minima_restock());
		this.setProveedor(producto.getProveedor());
		this.restock = restock;
	}
	
	
	public Producto_cantidades(Producto producto, int stock, int preparacion) {
		this.setId(producto.getId());
		this.setCantidad(producto.getCantidad());
		this.setNombre(producto.getNombre());
		this.setStock(stock);
		this.setPreparacion(preparacion);
		this.setCantidad_minima_restock(producto.getCantidad_minima_restock());
		this.setProveedor(producto.getProveedor());
	}
	
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	public int getPreparacion() {
		return preparacion;
	}
	public void setPreparacion(int preparacion) {
		this.preparacion = preparacion;
	} 
	
	public boolean isRestock() {
		return restock;
	}

	public void setRestock(boolean restock) {
		this.restock = restock;
	}

	

}
