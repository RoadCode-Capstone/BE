package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private PointType pointType;

    private int amount;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private boolean isDeleted = false;

    public void markDeleted() {
        this.isDeleted = true;
    }

    @Builder
    private Point(PointType type, int amount, Member member) {
        this.pointType = type;
        this.amount = amount;
        this.member = member;
    }

    public static Point create(PointType type, Member member){
        return Point.builder()
                .type(type)
                .amount(type.getPoint())
                .member(member)
                .build();
    }
}
