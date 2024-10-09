package com.peru.reniecservice.search.infrastructure.adapter.output.repository;

import com.peru.reniecservice.search.domain.model.entity.RecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IRecordRepository extends JpaRepository<RecordEntity, Long> {
}
