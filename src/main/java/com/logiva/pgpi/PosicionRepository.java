package com.logiva.pgpi;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PosicionRepository extends JpaRepository<Posicion, Integer> {
	List<Posicion> findByIdIn(Iterable<Integer> IDs);
	List<Posicion> findByIdInAndTipo(Iterable<Integer> IDs, String tipo);
}
