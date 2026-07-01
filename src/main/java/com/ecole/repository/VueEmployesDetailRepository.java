package com.ecole.repository;

import com.ecole.entity.VueEmployesDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VueEmployesDetailRepository extends JpaRepository<VueEmployesDetail, Long> {
    List<VueEmployesDetail> findByRoleNomIn(List<String> roleNoms);
    List<VueEmployesDetail> findByIsActiveTrue();
    Optional<VueEmployesDetail> findByUserId(Long userId);
}
