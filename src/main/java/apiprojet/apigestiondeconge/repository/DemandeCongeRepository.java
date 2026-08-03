package apiprojet.apigestiondeconge.repository;

import apiprojet.apigestiondeconge.entity.DemandeConge;
import apiprojet.apigestiondeconge.entity.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long> {

    List<DemandeConge> findByEmployeId(Long employeId);

    List<DemandeConge> findByStatut(StatutDemande statut);

    List<DemandeConge> findByEmployeIdAndStatut(Long employeId, StatutDemande statut);

    // Utile pour lister les demandes des subordonnés d'un manager
    List<DemandeConge> findByEmployeManagerId(Long managerId);

    List<DemandeConge> findByTypeCongeId(Long typeCongeId);
}
