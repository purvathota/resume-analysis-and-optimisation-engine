package com.resumeoptimizer.repository;

import com.resumeoptimizer.entity.CoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {
    List<CoverLetter> findByUserId(Long userId);
    List<CoverLetter> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<CoverLetter> findByIdAndUserId(Long id, Long userId);
    Optional<CoverLetter> findByUserIdAndCompanyNameAndRoleTitle(Long userId, String companyName, String roleTitle);
}
