package apiprojet.apigestiondeconge.repository;

import apiprojet.apigestiondeconge.entity.SoldeConge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoldeCongeRepository extends JpaRepository<SoldeConge, Long> {

    Optional<SoldeConge> findByEmployeIdAndAnnee(Long employeId, Integer annee);
}
