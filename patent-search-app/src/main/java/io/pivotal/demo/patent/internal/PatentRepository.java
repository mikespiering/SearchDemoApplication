package io.pivotal.demo.patent.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatentRepository extends JpaRepository<Patent, Long> {

	Page<Patent> findByTitleContainingOrSummaryContaining(String title, String summary, Pageable page);
}
