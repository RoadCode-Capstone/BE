package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Submission extends CreatedOnlyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Lob
    @Column
    private String sourceCode;

    private String language;

    private boolean isSuccess;

    @OneToMany(mappedBy = "submission")
    private List<Review> reviews = new ArrayList<>();
}
