package com.ecole.service;

import com.ecole.entity.ContratsEmployes;
import com.ecole.entity.ProfilsComptables;
import com.ecole.entity.ProfilsDirecteurs;
import com.ecole.entity.ProfilsProfesseurs;
import com.ecole.entity.ProfilsSecretariat;
import com.ecole.entity.VueEmployesDetail;
import com.ecole.entity.User;
import com.ecole.entity.Role;
import com.ecole.entity.UserRole;
import com.ecole.entity.UserRoleId;
import com.ecole.repository.ContratsEmployesRepository;
import com.ecole.repository.ProfilsComptablesRepository;
import com.ecole.repository.ProfilsDirecteursRepository;
import com.ecole.repository.ProfilsProfesseursRepository;
import com.ecole.repository.ProfilsSecretariatRepository;
import com.ecole.repository.VueEmployesDetailRepository;
import com.ecole.repository.UserRepository;
import com.ecole.repository.RoleRepository;
import com.ecole.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeService {

    private final VueEmployesDetailRepository vueEmployesDetailRepository;
    private final ProfilsProfesseursRepository profilsProfesseursRepository;
    private final ProfilsSecretariatRepository profilsSecretariatRepository;
    private final ProfilsDirecteursRepository profilsDirecteursRepository;
    private final ProfilsComptablesRepository profilsComptablesRepository;
    private final ContratsEmployesRepository contratsEmployesRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private static final String PHOTO_DIR = "src/main/resources/static/photo/photo-employes";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public List<VueEmployesDetail> getAllEmployes() {
        try {
            return vueEmployesDetailRepository.findByIsActiveTrue();
        } catch (Exception e) {
            log.error("Error fetching all employes: {}", e.getMessage());
            return List.of();
        }
    }

    public List<VueEmployesDetail> getProfesseurs() {
        try {
            return vueEmployesDetailRepository.findByRoleNomIn(List.of("professeur"));
        } catch (Exception e) {
            log.error("Error fetching professeurs: {}", e.getMessage());
            return List.of();
        }
    }

    public List<VueEmployesDetail> getSecretaires() {
        try {
            return vueEmployesDetailRepository.findByRoleNomIn(List.of("secretariat"));
        } catch (Exception e) {
            log.error("Error fetching secretaires: {}", e.getMessage());
            return List.of();
        }
    }

    public List<VueEmployesDetail> getProfesseursEtSecretaires() {
        try {
            return vueEmployesDetailRepository.findByRoleNomIn(List.of("professeur", "secretariat"));
        } catch (Exception e) {
            log.error("Error fetching professeurs et secretaires: {}", e.getMessage());
            return List.of();
        }
    }

    public List<VueEmployesDetail> filterEmployes(String keyword, String roleNom, Long matiereId, BigDecimal salaireMin, BigDecimal salaireMax) {
        try {
            return vueEmployesDetailRepository.filterEmployes(keyword, roleNom, matiereId, salaireMin, salaireMax);
        } catch (Exception e) {
            log.error("Error filtering employes: {}", e.getMessage());
            return List.of();
        }
    }

    public Optional<VueEmployesDetail> getEmployeById(Long userId) {
        try {
            return vueEmployesDetailRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("Error fetching employe by id: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public String calculateRemainingDaysText(VueEmployesDetail employe) {
        if (employe.getDateFin() == null) {
            return "Contrat sans date de fin";
        }
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = employe.getDateFin();
        long daysRemaining = ChronoUnit.DAYS.between(today, endDate);
        
        if (daysRemaining > 0) {
            return daysRemaining + " jour(s) restants";
        } else {
            return "Contrat arrivé à échéance";
        }
    }

    public String getPhotoUrl(VueEmployesDetail employe) {
        if (employe.getPhotoUrl() != null && !employe.getPhotoUrl().isEmpty()) {
            if (employe.getPhotoUrl().contains("DefaultIMG")) {
                return "/photo/" + employe.getPhotoUrl();
            }
            return "/photo/photo-employes/" + employe.getPhotoUrl();
        }
        
        String sexe = employe.getSexe() != null ? employe.getSexe().toUpperCase() : "H";
        return sexe.equals("F") ? "/photo/DefaultIMG_Femme.png" : "/photo/DefaultIMG_Homme.png";
    }

    public String uploadEmployeePhoto(Long userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Le fichier dépasse la taille maximale de 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && 
            !contentType.equals("image/jpeg") && !contentType.equals("image/png") && 
            !contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Le fichier doit être une image (jpg, png, webp)");
        }

        Path uploadDir = Paths.get(PHOTO_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = "employe_" + userId + "_" + UUID.randomUUID().toString() + extension;
        Path filePath = uploadDir.resolve(filename);

        Files.copy(file.getInputStream(), filePath);

        String photoFilename = filename;
        
        // Update photo URL in the appropriate profile table
        Optional<VueEmployesDetail> employeOpt = getEmployeById(userId);
        if (employeOpt.isPresent()) {
            VueEmployesDetail employe = employeOpt.get();
            String roleNom = employe.getRoleNom();
            
            // Delete old photo if exists
            String oldPhotoFilename = employe.getPhotoUrl();
            if (oldPhotoFilename != null && !oldPhotoFilename.isEmpty() && !oldPhotoFilename.contains("DefaultIMG")) {
                try {
                    Path oldFilePath = Paths.get(PHOTO_DIR + "/" + oldPhotoFilename);
                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                    }
                } catch (IOException e) {
                    log.error("Error deleting old photo file: {}", e.getMessage());
                }
            }
            
            // Update database based on role
            if ("professeur".equals(roleNom)) {
                profilsProfesseursRepository.findByUserId(userId).ifPresent(profile -> {
                    profile.setPhotoUrl(photoFilename);
                    profilsProfesseursRepository.save(profile);
                });
            } else if ("secretariat".equals(roleNom)) {
                profilsSecretariatRepository.findByUserId(userId).ifPresent(profile -> {
                    profile.setPhotoUrl(photoFilename);
                    profilsSecretariatRepository.save(profile);
                });
            } else if ("directeur".equals(roleNom)) {
                profilsDirecteursRepository.findByUserId(userId).ifPresent(profile -> {
                    profile.setPhotoUrl(photoFilename);
                    profilsDirecteursRepository.save(profile);
                });
            } else if ("comptable".equals(roleNom)) {
                profilsComptablesRepository.findByUserId(userId).ifPresent(profile -> {
                    profile.setPhotoUrl(photoFilename);
                    profilsComptablesRepository.save(profile);
                });
            }
        }
        
        return photoFilename;
    }

    public void deleteEmployeePhoto(Long userId) {
        Optional<VueEmployesDetail> employeOpt = getEmployeById(userId);
        if (employeOpt.isPresent()) {
            VueEmployesDetail employe = employeOpt.get();
            String currentPhotoFilename = employe.getPhotoUrl();
            String roleNom = employe.getRoleNom();
            
            // Delete photo file if exists
            if (currentPhotoFilename != null && !currentPhotoFilename.isEmpty() && 
                !currentPhotoFilename.contains("DefaultIMG")) {
                try {
                    Path filePath = Paths.get(PHOTO_DIR + "/" + currentPhotoFilename);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                    }
                } catch (IOException e) {
                    log.error("Error deleting photo file: {}", e.getMessage());
                }
            }
            
            // Update database to set photo URL to null based on role
            if ("professeur".equals(roleNom)) {
                profilsProfesseursRepository.findByUserId(userId).ifPresent(profile -> {
                    profile.setPhotoUrl(null);
                    profilsProfesseursRepository.save(profile);
                });
            } else if ("secretariat".equals(roleNom)) {
                profilsSecretariatRepository.findByUserId(userId).ifPresent(profile -> {
                    profile.setPhotoUrl(null);
                    profilsSecretariatRepository.save(profile);
                });
            } else if ("directeur".equals(roleNom)) {
                profilsDirecteursRepository.findByUserId(userId).ifPresent(profile -> {
                    profile.setPhotoUrl(null);
                    profilsDirecteursRepository.save(profile);
                });
            } else if ("comptable".equals(roleNom)) {
                profilsComptablesRepository.findByUserId(userId).ifPresent(profile -> {
                    profile.setPhotoUrl(null);
                    profilsComptablesRepository.save(profile);
                });
            }
        }
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public Map<String, Object> validateAndCheckPhone(String telephone) {
        Map<String, Object> result = new HashMap<>();
        
        if (telephone == null || telephone.trim().isEmpty()) {
            result.put("valid", true);
            result.put("exists", false);
            result.put("normalized", "");
            result.put("message", "");
            return result;
        }

        String normalized = normalizeMalagasyPhone(telephone);
        if (normalized == null) {
            result.put("valid", false);
            result.put("exists", false);
            result.put("normalized", "");
            result.put("message", "Numéro invalide. Utilisez un numéro malgache commençant par +261, 0, 032, 033, 034, 037 ou 038.");
            return result;
        }

        String duplicateOwner = findDuplicateEmployeePhone(normalized);
        if (duplicateOwner != null) {
            result.put("valid", true);
            result.put("exists", true);
            result.put("normalized", normalized);
            result.put("message", "Ce numéro " + normalized + " est déjà utilisé par " + duplicateOwner + ".");
            return result;
        }

        result.put("valid", true);
        result.put("exists", false);
        result.put("normalized", normalized);
        result.put("message", "Numéro valide et disponible.");
        return result;
    }

    public Map<String, Object> validatePassword(String password) {
        Map<String, Object> result = new HashMap<>();
        
        if (password == null || password.trim().isEmpty()) {
            result.put("valid", false);
            result.put("message", "Le mot de passe est requis.");
            return result;
        }

        if (password.length() < 8) {
            result.put("valid", false);
            result.put("message", "Le mot de passe doit contenir au moins 8 caractères.");
            return result;
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            result.put("valid", false);
            result.put("message", "Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&*()_+-=[]{};':\"\\|,.<>/?)");
            return result;
        }

        result.put("valid", true);
        result.put("message", "Mot de passe valide.");
        return result;
    }

    private String normalizeMalagasyPhone(String telephone) {
        String digits = telephone.replaceAll("\\D+", "");
        if (digits == null || digits.isEmpty()) {
            return null;
        }
        
        // Handle +261 prefix (12 digits: 261 + 10 digit number)
        if (digits.length() == 12 && digits.startsWith("261")) {
            digits = digits.substring(2); // Remove 261
        }
        
        // Handle 0 prefix (10 digits: 0 + 9 digit number)
        if (digits.length() == 10 && digits.startsWith("0")) {
            digits = digits.substring(1); // Remove leading 0
        }
        
        // Now handle standard formats
        if (digits.length() == 10 && digits.startsWith("03")) {
            String prefix = digits.substring(0, 2);
            if (prefix.equals("032") || prefix.equals("033") || prefix.equals("034") || 
                prefix.equals("037") || prefix.equals("038")) {
                return digits;
            }
        }
        
        if (digits.length() == 9 && digits.startsWith("3")) {
            String prefix = digits.substring(0, 1);
            if (prefix.equals("2") || prefix.equals("3") || prefix.equals("4") || 
                prefix.equals("7") || prefix.equals("8")) {
                return "03" + digits;
            }
        }
        
        return null;
    }

    private String findDuplicateEmployeePhone(String normalizedPhone) {
        if (profilsProfesseursRepository.findByTelephone(normalizedPhone).isPresent()) {
            return "un professeur";
        }
        if (profilsSecretariatRepository.findByTelephone(normalizedPhone).isPresent()) {
            return "un secrétaire";
        }
        if (profilsDirecteursRepository.findByTelephone(normalizedPhone).isPresent()) {
            return "un directeur";
        }
        if (profilsComptablesRepository.findByTelephone(normalizedPhone).isPresent()) {
            return "un comptable";
        }
        return null;
    }

    private String generateMatricule(String nom, String prenom) {
        String initials = "";
        if (nom != null && !nom.isEmpty()) {
            initials += nom.substring(0, 1).toUpperCase();
        }
        if (prenom != null && !prenom.isEmpty()) {
            initials += prenom.substring(0, 1).toUpperCase();
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "PROF-" + initials + "-" + timestamp.substring(timestamp.length() - 6);
    }

    @Transactional
    public void createEmployee(String fonction, String email, String prenom, String nom, String sexe,
            String telephone, String password, String adresse, Long typeContratId, BigDecimal salaireMensuel,
            LocalDate dateDebut, LocalDate dateFin, Long matiereId, BigDecimal heuresHebdo,
            String specialite, MultipartFile photo) throws IOException {
        
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        String normalizedPhone = null;
        if (telephone != null && !telephone.trim().isEmpty()) {
            normalizedPhone = normalizeMalagasyPhone(telephone);
            if (normalizedPhone == null) {
                throw new IllegalArgumentException("Numéro de téléphone invalide");
            }
            if (findDuplicateEmployeePhone(normalizedPhone) != null) {
                throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé");
            }
        }

        String defaultPassword = (password != null && !password.trim().isEmpty()) ? password : "ChangeMe123!";
        
        User user = new User();
        user.setEmail(email);
        user.setPassword(defaultPassword);
        user.setIsActive(true);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        user = userRepository.save(user);

        String photoFilename = null;
        if (photo != null && !photo.isEmpty()) {
            photoFilename = uploadEmployeePhoto(user.getId(), photo);
        } else {
            // Set default photo based on gender
            if ("F".equals(sexe)) {
                photoFilename = "DefaultIMG_Femme.png";
            } else {
                photoFilename = "DefaultIMG_Homme.png";
            }
        }

        // Create contract entry
        ContratsEmployes contrat = new ContratsEmployes();
        contrat.setUser(user);
        contrat.setFonction(fonction);
        contrat.setTypeContratId(typeContratId);
        contrat.setSexe(sexe);

        // Generate contract reference: CTR-YYYYMMDD-FONCTION_3-USER_ID
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String funcCode = fonction.substring(0, Math.min(fonction.length(), 3)).toUpperCase();
        String refContrat = "CTR-" + dateStr + "-" + funcCode + "-" + user.getId();
        contrat.setReferenceContrat(refContrat);

        contrat.setDateDebut(dateDebut);
        contrat.setDateFin(dateFin);
        contrat.setSalaireMensuel(salaireMensuel);
        contrat.setHeuresHebdo(heuresHebdo);
        contrat.setStatut("actif");
        contrat.setCreatedAt(java.time.LocalDateTime.now());
        contrat.setUpdatedAt(java.time.LocalDateTime.now());
        contrat = contratsEmployesRepository.save(contrat);

        // Assign the role in user_roles table
        Role role = roleRepository.findByNom(fonction.toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("Fonction non reconnue: " + fonction));
        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(user.getId(), role.getId()));
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        switch (fonction.toLowerCase()) {
            case "professeur":
                ProfilsProfesseurs professeur = new ProfilsProfesseurs();
                professeur.setUser(user);
                professeur.setNom(nom);
                professeur.setPrenom(prenom);
                professeur.setSexe(sexe);
                professeur.setTelephone(normalizedPhone);
                professeur.setPhotoUrl(photoFilename);
                professeur.setIdMatiere(matiereId);
                professeur.setMatricule(generateMatricule(nom, prenom));
                professeur.setAdresse(adresse);
                professeur.setSpecialite(specialite);
                professeur.setDateDebutContrat(dateDebut);
                professeur.setDateFinContrat(dateFin);
                professeur.setIdContrat(contrat.getId());
                professeur.setCreatedAt(java.time.LocalDateTime.now());
                profilsProfesseursRepository.save(professeur);
                break;
            case "secretariat":
                ProfilsSecretariat secretariat = new ProfilsSecretariat();
                secretariat.setUser(user);
                secretariat.setNom(nom);
                secretariat.setPrenom(prenom);
                secretariat.setSexe(sexe);
                secretariat.setTelephone(normalizedPhone);
                secretariat.setPhotoUrl(photoFilename);
                secretariat.setIdContrat(contrat.getId());
                secretariat.setCreatedAt(java.time.LocalDateTime.now());
                profilsSecretariatRepository.save(secretariat);
                break;
            case "directeur":
                ProfilsDirecteurs directeur = new ProfilsDirecteurs();
                directeur.setUser(user);
                directeur.setNom(nom);
                directeur.setPrenom(prenom);
                directeur.setSexe(sexe);
                directeur.setTelephone(normalizedPhone);
                directeur.setPhotoUrl(photoFilename);
                directeur.setIdContrat(contrat.getId());
                directeur.setCreatedAt(java.time.LocalDateTime.now());
                profilsDirecteursRepository.save(directeur);
                break;
            case "comptable":
                ProfilsComptables comptable = new ProfilsComptables();
                comptable.setUser(user);
                comptable.setNom(nom);
                comptable.setPrenom(prenom);
                comptable.setSexe(sexe);
                comptable.setTelephone(normalizedPhone);
                comptable.setPhotoUrl(photoFilename);
                comptable.setIdContrat(contrat.getId());
                comptable.setCreatedAt(java.time.LocalDateTime.now());
                profilsComptablesRepository.save(comptable);
                break;
            default:
                throw new IllegalArgumentException("Fonction non reconnue: " + fonction);
        }
    }
}
