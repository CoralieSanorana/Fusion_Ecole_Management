package com.ecole.service;

import com.ecole.entity.HoraireEdt;
import com.ecole.repository.HoraireEdtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HoraireEdtService {

    @Autowired
    private HoraireEdtRepository horaireEdtRepository;

    public List<HoraireEdt> findAll() {
        return horaireEdtRepository.findAll();
    }

    public Optional<HoraireEdt> findById(Long id) {
        return horaireEdtRepository.findById(id);
    }

    public HoraireEdt save(HoraireEdt horaireEdt) {
        return horaireEdtRepository.save(horaireEdt);
    }

    public void deleteById(Long id) {
        horaireEdtRepository.deleteById(id);
    }
}
