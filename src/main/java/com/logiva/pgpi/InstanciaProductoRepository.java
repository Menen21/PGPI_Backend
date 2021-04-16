package com.logiva.pgpi;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanciaProductoRepository extends JpaRepository<Instancia_Producto, Integer> {
	List<Instancia_Producto> findByIdproducto(int idproducto);
}
