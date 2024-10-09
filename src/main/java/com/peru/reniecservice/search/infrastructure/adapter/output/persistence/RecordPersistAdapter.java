package com.peru.reniecservice.search.infrastructure.adapter.output.persistence;

import com.peru.reniecservice.search.application.port.output.persistence.IRecordPersistPort;
import com.peru.reniecservice.search.domain.model.entity.RecordEntity;
import com.peru.reniecservice.search.infrastructure.adapter.output.repository.IRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RecordPersistAdapter implements IRecordPersistPort {

    private final IRecordRepository recordRepository;

    @Override
    public void saveRecordEntity(RecordEntity recordEntity) {
        recordRepository.save(recordEntity);
    }
}
