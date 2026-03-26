package com.fillin.service.member;

import com.fillin.domain.Agreement;
import com.fillin.domain.Member;
import com.fillin.domain.MemberAgreement;
import com.fillin.global.apiPayload.code.ErrorCode;
import com.fillin.global.security.exception.AuthException;
import com.fillin.repository.agreement.AgreementRepository;
import com.fillin.repository.member.MemberAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 약관 동의 저장 및 조회 서비스
 * 책임: 
 * - 사용자가 동의한 약관 정보 저장
 * - 약관 목록 조회 (캐싱)
 */
@Service
@RequiredArgsConstructor
public class AgreementService {

    private final AgreementRepository agreementRepository;
    private final MemberAgreementRepository memberAgreementRepository;

    /**
     * 필수 약관 목록 조회 (캐시됨)
     * - 캐시 시간: 1시간
     * - 캐시 키: agreements:required
     * 
     * @return 필수 약관 목록
     */
    @Cacheable(value = "agreements", key = "'required'")
    @Transactional(readOnly = true)
    public List<Agreement> getRequiredAgreements() {
        return agreementRepository.findByRequiredTrue();
    }

    /**
     * 전체 약관 목록 조회 (캐시됨)
     * - 캐시 시간: 1시간
     * - 캐시 키: agreements:all
     * 
     * @return 전체 약관 목록
     */
    @Cacheable(value = "agreements", key = "'all'")
    @Transactional(readOnly = true)
    public List<Agreement> getAllAgreements() {
        return agreementRepository.findAll();
    }

    /**
     * 사용자가 동의한 약관을 저장합니다.
     * 이미 저장된 약관은 중복 저장하지 않습니다.
     *
     * @param member 약관에 동의한 사용자
     * @param agreedAgreementIds 사용자가 동의한 약관 ID 목록
     * @throws AuthException 약관을 찾을 수 없는 경우
     */
    @Transactional
    public void saveAgreements(Member member, List<Long> agreedAgreementIds) {
        List<Agreement> agreements = agreementRepository.findAllById(agreedAgreementIds);

        // 요청된 약관 개수와 실제 조회된 약관 개수가 맞지 않는 경우
        if (agreements.size() != agreedAgreementIds.size()) {
            throw new AuthException(ErrorCode.AGREEMENT_NOT_FOUND);
        }

        // 각 약관에 대해 중복되지 않게 저장
        for (Agreement agreement : agreements) {
            if (!memberAgreementRepository.existsByMemberIdAndAgreementId(member.getId(), agreement.getId())) {
                memberAgreementRepository.save(MemberAgreement.agree(member, agreement));
            }
        }
    }
}


