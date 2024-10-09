package com.peru.reniecservice.search.infrastructure.adapter.output.repository;

import com.peru.reniecservice.search.domain.model.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ITokenRepository extends JpaRepository<TokenEntity, Long> {
}
