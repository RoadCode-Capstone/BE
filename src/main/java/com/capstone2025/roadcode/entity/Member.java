package com.capstone2025.roadcode.entity;

import com.capstone2025.roadcode.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table( uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email", "provider"}) // 복합 유니크 체크 ( email+provider 는 무조건 unique 여야함)
        }
)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider; // LOCAL, GOOGLE, ...

    private int totalPoint = 0;

    @Column(nullable = false)
    private String role = "USER";

    @Column(nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "member")
    private List<Submission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Roadmap> roadmaps = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Point> point = new ArrayList<>();

    @Builder
    private Member(String email, String password, String nickname, AuthProvider provider) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = provider;
    }

    public static Member localCreate(String email, String password, String nickname) {
        return Member.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .provider(AuthProvider.LOCAL)
                .build();
    }

    public static Member socialCreate(String email, String nickname, AuthProvider provider) {
        return Member.builder()
                .email(email)
                .password(null) // 또는 uuid
                .nickname(nickname)
                .provider(provider)
                .build();
    }

    public void updatePassword(String rawPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(rawPassword);
    }

    public void updateNickname(String nickname) {
        if(!this.nickname.equals(nickname)){ // 현재 닉네임이랑 다를 경우만 실제로 db 업데이트 반영
            this.nickname = nickname;
        }
    }

    public void markDeleted() {
        this.isDeleted = true;
    }

    public void addPoint(int point){
        this.totalPoint += point;
    }

    public void subPoint(int point){
        this.totalPoint -= point;
    }
}
