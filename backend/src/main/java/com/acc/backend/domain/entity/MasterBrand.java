package com.acc.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "mst_brand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MasterBrand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kode_brand", nullable = false, length = 20)
    private String kodeBrand;

    @Column(name = "nama_brand", nullable = false, length = 50)
    private String namaBrand;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;
}