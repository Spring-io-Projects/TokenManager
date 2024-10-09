package com.peru.reniecservice.search.domain.model.entity;

import com.peru.reniecservice.search.domain.model.valueObject.ErrorEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class RecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 8, nullable = false, unique = true, updatable = false)
    private Long lastDni;

    @Column(length = 7, nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ErrorEnum errorEnum;
}
