package com.ecole.dto.Secretaire;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class BilanGlobalDTO {

    private BigDecimal totalAttenduGlobal;
    private BigDecimal totalEncaisseGlobal;
    private List<BilanClasseDTO> lignes;

    // Pourcentage global
    public double getPourcentageGlobal() {
        if (totalAttenduGlobal == null || totalAttenduGlobal.compareTo(BigDecimal.ZERO) == 0) return 0;
        return totalEncaisseGlobal.divide(totalAttenduGlobal, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    // Total restant à encaisser
    public BigDecimal getResteAEncaisser() {
        if (totalAttenduGlobal == null || totalEncaisseGlobal == null) return BigDecimal.ZERO;
        return totalAttenduGlobal.subtract(totalEncaisseGlobal);
    }
}