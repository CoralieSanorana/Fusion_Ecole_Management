package com.ecole.service;

import com.ecole.dto.Secretaire.EtudiantRechercheDTO;
import com.ecole.entity.AnneeScolaire;
import com.ecole.entity.ProfilEtudiant;
import com.ecole.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
}
