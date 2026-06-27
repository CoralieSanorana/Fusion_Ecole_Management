package com.ecole.service;

import com.ecole.entity.Seance;
import com.ecole.repository.SeanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SeanceService {

    @Autowired
    private SeanceRepository seanceRepository;

    public List<Seance> findAll() {
        return seanceRepository.findAll();
    }

    public Optional<Seance> findById(Long id) {
        return seanceRepository.findById(id);
    }

    public Seance save(Seance seance) {
        return seanceRepository.save(seance);
    }

    public void deleteById(Long id) {
        seanceRepository.deleteById(id);
    }

    public Seance findOrCreateSeanceForEmploiDuTemps(Long emploiDuTempsId) {
        // Try to find existing seance for today
        LocalDate today = LocalDate.now();
        Optional<Seance> existingSeance = seanceRepository.findByEmploiDuTempsIdAndDateSeance(emploiDuTempsId.intValue(), today);
        
        if (existingSeance.isPresent()) {
            return existingSeance.get();
        }
        
        // Create new seance
        Seance seance = new Seance();
        seance.setEmploiDuTempsId(emploiDuTempsId.intValue());
        seance.setDateSeance(today);
        seance.setAEuLieu(true);
        return seanceRepository.save(seance);
    }
}
