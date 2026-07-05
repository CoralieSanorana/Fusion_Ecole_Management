package com.ecole.service;

import com.ecole.entity.ProfilsProfesseurs;
import com.ecole.repository.ProfilsProfesseursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfilsProfesseursService {

    @Autowired
    private ProfilsProfesseursRepository ProfilsProfesseursRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/photo/professeurs/";

    public List<ProfilsProfesseurs> findAll() {
        return ProfilsProfesseursRepository.findAll();
    }

    public Optional<ProfilsProfesseurs> findById(Long id) {
        return ProfilsProfesseursRepository.findById(id);
    }

    public ProfilsProfesseurs save(ProfilsProfesseurs ProfilsProfesseurs) {
        return ProfilsProfesseursRepository.save(ProfilsProfesseurs);
    }

    public void deleteById(Long id) {
        ProfilsProfesseursRepository.deleteById(id);
    }

    public String uploadProfessorPhoto(Long professeurId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String newFilename = "professeur_" + professeurId + "_" + UUID.randomUUID().toString() + fileExtension;

        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        return "/photo/professeurs/" + newFilename;
    }
}
