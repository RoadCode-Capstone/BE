package com.capstone2025.roadcode.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class ProblemTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_tag_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
