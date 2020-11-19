package com.homework.domain.coordenador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecadoCoordenadorRepository extends JpaRepository<RecadoCoordenador, Long> {

}
