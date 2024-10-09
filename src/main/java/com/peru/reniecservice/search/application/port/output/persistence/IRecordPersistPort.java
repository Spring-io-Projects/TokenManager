package com.peru.reniecservice.search.application.port.output.persistence;

import com.peru.reniecservice.search.domain.model.entity.RecordEntity;

public interface IRecordPersistPort {
    void saveRecordEntity(RecordEntity recordEntity);
}
