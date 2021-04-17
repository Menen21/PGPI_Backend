package com.logiva.pgpi;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer>{
	List<Pedido> findByEstado(String estado);
	Pedido findbyId(int id);
}
