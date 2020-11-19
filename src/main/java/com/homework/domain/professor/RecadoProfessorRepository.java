package com.homework.domain.professor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecadoProfessorRepository extends JpaRepository<RecadoProfessor, Long> {

}
