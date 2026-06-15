package com.resumeoptimizer.repository;

import com.resumeoptimizer.entity.CoverLetterVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoverLetterVersionRepository extends JpaRepository<CoverLetterVersion, Long> {
    List<CoverLetterVersion> findByCoverLetterIdOrderByVersionNumberDesc(Long coverLetterId);
}
