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
    
    private int idproducto;
    private int idposicion;
    
    private transient int disponible = 1;
    
    public int getDisponible() {
		return disponible;
	}

	public void setDisponible(int disponible) {
		this.disponible = disponible;
	}

	public  Instancia_Producto() {}
    
	public Instancia_Producto(int id_producto, int id_posicion) {
		this.idproducto = id_producto;
		this.idposicion = id_posicion;
	}
	
	public int getId() {
		return id;
	}
	public int getIdproducto() {
		return idproducto;
	}
	public int getIdposicion() {
		return idposicion;
	}  

}
