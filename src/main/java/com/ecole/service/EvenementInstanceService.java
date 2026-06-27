package com.ecole.service;

import com.ecole.entity.EvenementInstance;
import com.ecole.repository.EvenementInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EvenementInstanceService {

    @Autowired
    private EvenementInstanceRepository evenementInstanceRepository;

    public List<EvenementInstance> findAll() {
        return evenementInstanceRepository.findAll();
    }

    public Optional<EvenementInstance> findById(Long id) {
        return evenementInstanceRepository.findById(id);
    }

    public EvenementInstance save(EvenementInstance evenementInstance) {
        return evenementInstanceRepository.save(evenementInstance);
    }

    public void deleteById(Long id) {
        evenementInstanceRepository.deleteById(id);
    }
}
