package com.peru.reniecservice.search.infrastructure.adapter.output.persistence;

import com.peru.reniecservice.search.application.port.output.persistence.IUserPersistPort;
import com.peru.reniecservice.search.domain.model.entity.UserEntity;
import com.peru.reniecservice.search.infrastructure.adapter.output.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserPersistAdapter implements IUserPersistPort {

    private final IUserRepository userRepository;

    @Override
    public void saveUserEntity(UserEntity userEntity) {
        userRepository.save(userEntity);
    }
}
