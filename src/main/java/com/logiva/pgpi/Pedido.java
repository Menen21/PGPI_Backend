package com.logiva.pgpi;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "pedido")
@Entity
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
    
    private String id_producto;
    private String cantidad;
    private String direccion;
    private String estado;
    private int id_cliente;
    private String nombre;
    private String tipo;
    private String agencia;
    private Date fecha_Pedido;
    private Date fecha_Entrega;
    private float peso;
    private int cod_Postal;
    

    
	public Pedido() {
	}

	public Pedido(String id_producto, String cantidad, String direccion, String estado, int id_cliente, String nombre,
			String tipo, String agencia, Date fecha_Pedido, Date fecha_Entrega, float peso, int cod_Postal) {
		this.id_producto = id_producto;
		this.cantidad = cantidad;
		this.direccion = direccion;
		this.estado = estado;
		this.id_cliente = id_cliente;
		this.nombre = nombre;
		this.tipo = tipo;
		this.agencia = agencia;
		this.fecha_Pedido = fecha_Pedido;
		this.fecha_Entrega = fecha_Entrega;
		this.peso = peso;
		this.cod_Postal = cod_Postal;
	}
	
	public String getTipo() {
		return tipo;
	}

	public Date getFecha_Pedido() {
		return fecha_Pedido;
	}
	public void setFecha_Entrega(Date fecha_Entrega) {
		this.fecha_Entrega = fecha_Entrega;
	}
	public void setPeso(float peso) {
		this.peso = peso;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public int getId() {
		return id;
	}
	public String getId_producto() {
		return id_producto;
	}
	public String getCantidad() {
		return cantidad;
	}

    
}
