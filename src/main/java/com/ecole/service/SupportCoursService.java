package com.ecole.service;

import com.ecole.entity.SupportCours;
import com.ecole.repository.SupportCoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class SupportCoursService {

    @Autowired
    private SupportCoursRepository supportCoursRepository;

    public List<SupportCours> findAll() {
        return supportCoursRepository.findAll();
    }

    public Optional<SupportCours> findById(Long id) {
        return supportCoursRepository.findById(id);
    }

    public SupportCours saveCours(SupportCours supportCours) {
        return supportCoursRepository.save(supportCours);
    }

    public void deleteById(Long id) {
        supportCoursRepository.deleteById(id);
    }

    public List<SupportCours> findByAffectationId(Long affectationId) {
        return supportCoursRepository.findByAffectationIdOrderByCreatedAtDesc(affectationId);
    }

    public void save(SupportCours support, MultipartFile file) throws Exception {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get("uploads/supports/" + fileName);
        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        
        support.setFichierUrl("/uploads/supports/" + fileName);
        supportCoursRepository.save(support);
    }

}
