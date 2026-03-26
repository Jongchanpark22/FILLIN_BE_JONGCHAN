package com.fillin.repository.agreement;

import com.fillin.domain.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgreementRepository extends JpaRepository<Agreement, Long> {
    List<Agreement> findAllById(Long agreementId);
    List<Agreement> findByRequiredTrue();
    long countByIdIn(List<Long> ids);
}
