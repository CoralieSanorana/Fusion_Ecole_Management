package com.ecole.dto.Secretaire;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class BilanClasseDTO {

    private String nomClasse;
    private BigDecimal totalAttendu;
    private BigDecimal totalEncaisse;
    private int nombreEleves;
    private int nombreSoldes;

    // Pourcentage de recouvrement calculé automatiquement
    public double getPourcentage() {
        if (totalAttendu == null || totalAttendu.compareTo(BigDecimal.ZERO) == 0) return 0;
        return totalEncaisse.divide(totalAttendu, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    // Nombre d'élèves qui n'ont pas encore tout payé
    public int getNombreImpayes() {
        return nombreEleves - nombreSoldes;
    }
}