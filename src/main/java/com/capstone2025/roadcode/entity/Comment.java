package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Comment extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Lob
    @Column
    private String content;

}
