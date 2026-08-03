package apiprojet.apigestiondeconge.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "demande_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Long id;

    @Column(name = "date_demande", nullable = false)
    @Builder.Default
    private LocalDate dateDemande = LocalDate.now();

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    // Calculé en service (dateFin_dateDebut), stocké pour faciliter les requêtes
    @Column(name = "nombre_jours", nullable = false)
    private Integer nombreJours;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type", nullable = false)
    private TypeConge typeConge;

    @OneToOne(mappedBy = "demandeConge", cascade = CascadeType.ALL)
    private Validation validation;

    // Pièce jointe (justificatif)
    @Column(name = "justificatif_nom")
    private String justificatifNom;

    @Column(name = "justificatif_type")
    private String justificatifType;

    @Lob
    @Column(name = "justificatif_data", columnDefinition = "LONGBLOB")
    private byte[] justificatifData;
}
