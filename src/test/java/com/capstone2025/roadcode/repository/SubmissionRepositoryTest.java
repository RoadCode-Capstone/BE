package com.capstone2025.roadcode.repository;

import com.capstone2025.roadcode.common.LanguageType;
import com.capstone2025.roadcode.entity.Member;
import com.capstone2025.roadcode.entity.Problem;
import com.capstone2025.roadcode.entity.Submission;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class SubmissionRepositoryTest {

    @Autowired
    SubmissionRepository submissionRepository;
    @Autowired
    TestEntityManager entityManager;

    @Test
    void 문제_풀이_성공기록_있으면_true_반환(){

        //given
        Member member = Member.localCreate("1234", "1234", "1234");
        Problem problem = Problem.create( 2, "3", "a", 1, "설명", "inputDescription", "outputDescription", "timeLimit", "memoryLimit", "url");
        entityManager.persist(member);
        entityManager.persist(problem);

        Submission submission = Submission.create(problem, member, "code", LanguageType.C, true);
        entityManager.persist(submission);
        entityManager.flush();

        //when
        boolean result = submissionRepository.existsByMemberIdAndProblemIdAndIsSuccessTrue(member.getId(), problem.getId());

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void 문제_풀이_실패기록만_있으면_false_반환(){

        //given
        Member member = Member.localCreate("1234", "1234", "1234");
        Problem problem = Problem.create( 2, "3", "a", 1, "설명", "inputDescription", "outputDescription", "timeLimit", "memoryLimit", "url");
        entityManager.persist(member);
        entityManager.persist(problem);

        Submission submission = Submission.create(problem, member, "code", LanguageType.C, false);
        entityManager.persist(submission);
        entityManager.flush();

        //when
        boolean result = submissionRepository.existsByMemberIdAndProblemIdAndIsSuccessTrue(member.getId(), problem.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void 문제_풀이_성공기록_없으면_false_반환(){

        //given
        Member member = Member.localCreate("1234", "1234", "1234");
        Problem problem = Problem.create( 2, "3", "a", 1, "설명", "inputDescription", "outputDescription", "timeLimit", "memoryLimit", "url");
        entityManager.persist(member);
        entityManager.persist(problem);

        entityManager.flush();

        //when
        boolean result = submissionRepository.existsByMemberIdAndProblemIdAndIsSuccessTrue(member.getId(), problem.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

}