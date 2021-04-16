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
    
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getAgencia() {
		return agencia;
	}
	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}
	public Date getFecha_Pedido() {
		return fecha_Pedido;
	}
	public void setFecha_Pedido(Date fecha_Pedido) {
		this.fecha_Pedido = fecha_Pedido;
	}
	public Date getFecha_Entrega() {
		return fecha_Entrega;
	}
	public void setFecha_Entrega(Date fecha_Entrega) {
		this.fecha_Entrega = fecha_Entrega;
	}
	public float getPeso() {
		return peso;
	}
	public void setPeso(float peso) {
		this.peso = peso;
	}
	public int getCodPostal() {
		return cod_Postal;
	}
	public void setCodPostal(int codPostal) {
		this.cod_Postal = codPostal;
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
	public void setId(int id) {
		this.id = id;
	}
	public String getId_producto() {
		return id_producto;
	}
	public void setId_producto(String id_producto) {
		this.id_producto = id_producto;
	}
	public int getId_cliente() {
		return id_cliente;
	}
	public void setId_cliente(int id_cliente) {
		this.id_cliente = id_cliente;
	}
	public String getCantidad() {
		return cantidad;
	}
	public void setCantidad(String cantidad) {
		this.cantidad = cantidad;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
    
    
}
