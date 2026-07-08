package com.ecole.service;

import com.ecole.dto.Secretaire.AjoutEleveDTO;
import com.ecole.dto.Secretaire.EtudiantRechercheDTO;
import com.ecole.entity.AnneeScolaire;
import com.ecole.entity.Classe;
import com.ecole.entity.Inscription;
import com.ecole.entity.ProfilEtudiant;
import com.ecole.entity.ProfilParent;
import com.ecole.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EleveServiceTest {

    @Mock private ProfilEtudiantRepository etudiantRepo;
    @Mock private InscriptionRepository inscriptionRepo;
    @Mock private ClasseRepository classeRepo;
    @Mock private PaiementRepository paiementRepo;
    @Mock private ProfilParentRepository parentRepo;
    @Mock private AnneeScolaireRepository anneeScolaireRepo;
    @Mock private JdbcTemplate jdbc;
    @Mock private HistoriqueReinscriptionRepository historiqueReinscriptionRepo;

    @InjectMocks private EleveService eleveService;

    @Test
    void rechercherEtudiantsPourReinscription_shouldReturnMappedDtos() {
        ProfilEtudiant etudiant = new ProfilEtudiant();
        etudiant.setId(1L);
        etudiant.setMatricule("ETU2024-001");
        etudiant.setNom("Rakoto");
        etudiant.setPrenom("Jean");
        etudiant.setTelephone("0341234567");

        when(etudiantRepo.searchByNomOrPrenomOrMatricule("rak")).thenReturn(List.of(etudiant));
        when(inscriptionRepo.findActiveByEtudiantId(1L)).thenReturn(Optional.empty());

        List<EtudiantRechercheDTO> result = eleveService.rechercherEtudiantsPourReinscription("rak");

        assertEquals(1, result.size());
        assertEquals("ETU2024-001", result.get(0).getMatricule());
        assertEquals("Jean", result.get(0).getPrenom());
    }

    @Test
    void ensureAnneeScolaireDisponible_shouldCreateNextYearWhenOnlyOneYearExists() {
        AnneeScolaire currentYear = new AnneeScolaire();
        currentYear.setId(1L);
        currentYear.setLibelle("2025-2026");
        currentYear.setDateDebut(LocalDate.of(2025, 9, 1));
        currentYear.setDateFin(LocalDate.of(2026, 7, 31));
        currentYear.setEstActive(true);

        when(anneeScolaireRepo.findAll()).thenReturn(List.of(currentYear));

        eleveService.ensureAnneeScolaireDisponible();

        verify(anneeScolaireRepo).save(any(AnneeScolaire.class));
    }

    @Test
    void ajouterEleve_shouldLinkInscriptionToSavedStudent() {
        AjoutEleveDTO dto = new AjoutEleveDTO();
        dto.setNom("Rakoto");
        dto.setPrenom("Jean");
        dto.setClasseId("10");
        dto.setNomParent("Rakoto");
        dto.setPrenomParent("Aline");
        dto.setTelephoneParent("0341234567");

        ProfilEtudiant savedEtudiant = new ProfilEtudiant();
        savedEtudiant.setId(77L);
        savedEtudiant.setNom("Rakoto");
        savedEtudiant.setPrenom("Jean");

        Classe classe = new Classe();
        classe.setId(10L);
        classe.setNom("Seconde A");

        AnneeScolaire annee = new AnneeScolaire();
        annee.setId(3L);
        annee.setLibelle("2025-2026");
        annee.setEstActive(true);

        ProfilParent savedParent = new ProfilParent();
        savedParent.setId(15);

        when(etudiantRepo.save(any(ProfilEtudiant.class))).thenReturn(savedEtudiant);
        when(parentRepo.save(any(ProfilParent.class))).thenReturn(savedParent);
        when(classeRepo.findById(10L)).thenReturn(Optional.of(classe));
        when(anneeScolaireRepo.findByEstActiveTrue()).thenReturn(Optional.of(annee));

        eleveService.ajouterEleve(dto);

        ArgumentCaptor<Inscription> inscriptionCaptor = ArgumentCaptor.forClass(Inscription.class);
        verify(inscriptionRepo).save(inscriptionCaptor.capture());
        verify(jdbc).update(anyString(), eq(77L), eq(15));

        Inscription savedInscription = inscriptionCaptor.getValue();
        assertNotNull(savedInscription.getEtudiant());
        assertEquals(77L, savedInscription.getEtudiant().getId());
        assertEquals(10L, savedInscription.getClasseId());
    }
}
