package com.logiva.pgpi;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Table(name = "posicion")
@Entity
public class Posicion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
    
    private int fila;
    private int columna;
    private String tipo;
    
	public Posicion(int i, int j, String e) {
		super();
    	this.fila = i;
    	this.columna = j;
    	this.tipo = e;
	}
	
	
	
	public Posicion() {
		// TODO Auto-generated constructor stub
	}



	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getFila() {
		return fila;
	}
	public void setFila(int fila) {
		this.fila = fila;
	}
	public int getColumna() {
		return columna;
	}
	public void setColumna(int columna) {
		this.columna = columna;
	}
    
    

}
