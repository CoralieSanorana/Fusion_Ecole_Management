package com.ecole.dto.Directeur;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class HoraireEdtSaveRequest {
    private List<HoraireEdtForm> horaires = new ArrayList<>();
}
