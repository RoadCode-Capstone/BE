package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Review extends CreatedOnlyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Lob
    @Column
    private String content;
}
