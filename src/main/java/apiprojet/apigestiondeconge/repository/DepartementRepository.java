package apiprojet.apigestiondeconge.repository;

import apiprojet.apigestiondeconge.entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartementRepository extends JpaRepository<Departement, Long> {
}
