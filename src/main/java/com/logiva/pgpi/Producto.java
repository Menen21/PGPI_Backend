package com.logiva.pgpi;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "producto")
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private int cantidad;
	private String nombre;
	private String proveedor;
	private int cantidad_minima_restock;
	
	public Producto() {
		
	}
	
	public Producto(int cantidad, String nombre, String proveedor, int cantidad_minima_restock) {
		this.cantidad = cantidad;
		this.nombre = nombre;
		this.proveedor = proveedor;
		this.cantidad_minima_restock = cantidad_minima_restock;
	}



	public Producto(String nombre, int cantidad) {
		this.nombre = nombre;
		this.cantidad = cantidad;
	}
	

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCantidad() {
		return cantidad;
	}
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getProveedor() {
		return proveedor;
	}
	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}
	public int getCantidad_minima_restock() {
		return cantidad_minima_restock;
	}
	public void setCantidad_minima_restock(int cantidad_minima_restock) {
		this.cantidad_minima_restock = cantidad_minima_restock;
	}
	
	
	

}
