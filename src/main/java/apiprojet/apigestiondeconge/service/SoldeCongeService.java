package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.SoldeCongeDto;
import java.util.List;

/**
 * J'ai créé cette interface pour formaliser le contrat de gestion des soldes de congé annuels des employés.
 */
public interface SoldeCongeService {

    /**
     * Je crée et j'initialise un solde de congé annuel pour un employé.
     */
    SoldeCongeDto.Response creer(SoldeCongeDto.Request request);

    /**
     * Je liste les soldes de congé de tous les employés.
     */
    List<SoldeCongeDto.Response> listerTous();

    /**
     * Je récupère le solde de congé par son identifiant.
     */
    SoldeCongeDto.Response getById(Long id);

    /**
     * Je récupère le solde d'un employé pour une année donnée (en l'initialisant s'il n'existe pas encore).
     */
    SoldeCongeDto.Response getByEmployeEtAnnee(Long employeId, Integer annee);

    /**
     * Je modifie le solde de congé manuellement (ajustement administratif).
     */
    SoldeCongeDto.Response modifier(Long id, SoldeCongeDto.Request request);

    /**
     * Je débite un nombre de jours du solde d'un employé lors de l'approbation d'un congé.
     */
    void debiter(Long employeId, Integer annee, Integer nombreJours);

    /**
     * Je recrédite un nombre de jours au solde d'un employé en cas d'annulation.
     */
    void crediter(Long employeId, Integer annee, Integer nombreJours);

    /**
     * Je supprime une entrée de solde de congé.
     */
    void supprimer(Long id);
}