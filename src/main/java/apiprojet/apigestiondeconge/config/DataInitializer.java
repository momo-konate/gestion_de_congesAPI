package apiprojet.apigestiondeconge.config;

import apiprojet.apigestiondeconge.entity.Role;
import apiprojet.apigestiondeconge.entity.Utilisateur;
import apiprojet.apigestiondeconge.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Crée l'administrateur par défaut au démarrage s'il n'existe pas
        if (utilisateurRepository.findByEmail("admin@test.com").isEmpty()) {
            Utilisateur admin = Utilisateur.builder()
                    .nom("System")
                    .prenom("Admin")
                    .email("admin@test.com")
                    .motDePasse(passwordEncoder.encode("Admin1234"))
                    .telephone("770000000")
                    .role(Role.ADMIN)
                    .actif(true)
                    .build();

            utilisateurRepository.save(admin);
            System.out.println("✅ Compte Administrateur initial créé avec succès ! (email: admin@test.com)");
        }
    }
}