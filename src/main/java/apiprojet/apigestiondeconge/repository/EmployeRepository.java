package apiprojet.apigestiondeconge.repository;

import apiprojet.apigestiondeconge.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {



    List<Employe> findByDepartementId(Long departementId);

    List<Employe> findByManagerId(Long managerId);
}