package com.ecole.service;

import com.ecole.entity.Periode;
import com.ecole.repository.PeriodeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PeriodeService {

    private final PeriodeRepository periodeRepository;

    public PeriodeService(PeriodeRepository periodeRepository) {
        this.periodeRepository = periodeRepository;
    }

    public List<Periode> findAll() {
        return periodeRepository.findAll();
    }

    public Optional<Periode> findById(Integer id) {
        return periodeRepository.findById(id);
    }

    public List<Periode> findByAnneeScolaireId(Long anneeScolaireId) {
        return periodeRepository.findByAnneeScolaireId(anneeScolaireId);
    }

    public Periode save(Periode periode) {
        return periodeRepository.save(periode);
    }

    public void delete(Integer id) {
        periodeRepository.deleteById(id);
    }
}