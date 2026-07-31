package apiprojet.apigestiondeconge.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type")
    private Long id;

    @Column(nullable = false, length = 100)
    private String libelle;

    // Nombre de jours attribués par défaut pour ce type de congé
    @Column(name = "nombre_jours", nullable = false)
    private Integer nombreJours;

    @Column(columnDefinition = "TEXT")
    private String description;
}
