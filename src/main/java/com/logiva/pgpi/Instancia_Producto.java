package com.logiva.pgpi;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;



@Table(name = "instancia_producto")
@Entity
public class Instancia_Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
    
    private int id_producto;
    private int id_posicion;
    
    private transient int disponible = 1;
    
    public int getDisponible() {
		return disponible;
	}

	public void setDisponible(int disponible) {
		this.disponible = disponible;
	}

	public  Instancia_Producto() {}
    
	public Instancia_Producto(int id_producto, int id_posicion) {
		this.id_producto = id_producto;
		this.id_posicion = id_posicion;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId_producto() {
		return id_producto;
	}
	public void setId_producto(int id_producto) {
		this.id_producto = id_producto;
	}
	public int getId_posicion() {
		return id_posicion;
	}
	public void setId_posicion(int id_posicion) {
		this.id_posicion = id_posicion;
	}
    
    

}
