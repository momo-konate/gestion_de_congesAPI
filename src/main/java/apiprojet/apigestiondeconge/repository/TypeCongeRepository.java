package apiprojet.apigestiondeconge.repository;

import apiprojet.apigestiondeconge.entity.TypeConge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TypeCongeRepository extends JpaRepository<TypeConge,Long> {
    List<TypeConge> findByLibelle(String libelle);
}
