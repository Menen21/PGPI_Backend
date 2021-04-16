package com.logiva.pgpi;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Producto, Integer>, ProductRepositoryCustom{
	Producto findByNombre(String nombre);
	Producto findById(int id);
	List<Producto> findByIdIn(List<Integer> id);
}
