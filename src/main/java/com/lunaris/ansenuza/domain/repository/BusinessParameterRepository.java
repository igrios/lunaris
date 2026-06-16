package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lunaris.ansenuza.domain.model.BusinessParameter;

@Repository
public interface BusinessParameterRepository extends JpaRepository<BusinessParameter, Long> {
    
    // Spring traduce automáticamente 'findByParameterKey' a la columna 'parameter_key' en Postgres
    Optional<BusinessParameter> findByParameterKey(String parameterKey);
}