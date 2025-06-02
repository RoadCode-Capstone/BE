package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Testcase extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "testcase_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Lob
    @Column(nullable = false)
    private String input;

    @Lob
    @Column(nullable = false)
    private String output;

    @Column(nullable = false)
    private boolean isSample = true;

}
