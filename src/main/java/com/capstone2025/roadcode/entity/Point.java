package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Point extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private int point;

    @Enumerated(value = EnumType.STRING)
    private PointReason reason;

    @Column(nullable = false)
    private boolean isDeleted = false;

    public void markDeleted() {
        this.isDeleted = true;
    }
}
