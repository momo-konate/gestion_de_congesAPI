package apiprojet.apigestiondeconge.repository;

import apiprojet.apigestiondeconge.entity.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, Long> {

    Optional<Validation> findByDemandeCongeId(Long demandeId);
}