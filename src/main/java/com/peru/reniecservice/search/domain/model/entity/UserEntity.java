package com.peru.reniecservice.search.domain.model.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, updatable = false)
    private String name;

    @Column(length = 50, nullable = false, updatable = false)
    private String paternalSurname;

    @Column(length = 50, nullable = false, updatable = false)
    private String maternalSurname;

    @Column(length = 8, nullable = false, updatable = false)
    private String dni;

    @Column(length = 2, nullable = false, updatable = false)
    private String checkDigit;
}
