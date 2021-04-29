package com.logiva.pgpi;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer>, ProductRepositoryCustom{
	Proveedor findByNombre(String proveedor);

}
