package com.peru.reniecservice.search.infrastructure.adapter.output.repository;

import com.peru.reniecservice.search.domain.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IUserRepository extends JpaRepository<UserEntity, Long> {
}
