package apiprojet.apigestiondeconge.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "solde_conge", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_employe", "annee"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoldeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solde")
    private Long id;

    @Column(nullable = false)
    private Integer annee;

    @Column(name = "jours_acquis", nullable = false)
    private Integer joursAcquis;

    @Column(name = "jours_utilises", nullable = false)
    @Builder.Default
    private Integer joursUtilises = 0;

    @Column(name = "jours_restants", nullable = false)
    private Integer joursRestants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe", nullable = false)
    private Employe employe;
}