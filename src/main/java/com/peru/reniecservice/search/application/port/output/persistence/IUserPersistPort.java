package com.peru.reniecservice.search.application.port.output.persistence;

import com.peru.reniecservice.search.domain.model.entity.UserEntity;


public interface IUserPersistPort {
    void saveUserEntity(UserEntity userEntity);
}
