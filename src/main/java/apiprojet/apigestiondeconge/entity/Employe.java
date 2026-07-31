package apiprojet.apigestiondeconge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employe")
    private Long id;

    @Column(length = 100)
    private String poste;

    @Column(length = 10)
    private String sexe;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    // --- Relations ---

    @OneToOne
    @JoinColumn(name = "id_utilisateur", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;

    // Auto-référence : le manager est lui-même un Employe
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_manager")
    private Employe manager;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Employe> subordonnes = new ArrayList<>();

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SoldeConge> soldesConge = new ArrayList<>();

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    @Builder.Default
    private List<DemandeConge> demandesConge = new ArrayList<>();
}
