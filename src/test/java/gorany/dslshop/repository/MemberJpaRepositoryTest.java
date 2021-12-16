package gorany.dslshop.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import gorany.dslshop.dto.MemberSearchCondition;
import gorany.dslshop.dto.MemberTeamDTO;
import gorany.dslshop.entity.Member;
import gorany.dslshop.entity.Team;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

//@SpringBootTest
@DataJpaTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberJpaRepository;

    @BeforeEach
    void before() {
        Team teamA = Team.builder().name("teamA").build();
        em.persist(teamA);
        Team teamB = Team.builder().name("teamB").build();
        em.persist(teamB);
        Member member1 = Member.builder().username("member1").age(10).team(teamA).build();
        em.persist(member1);
        Member member2 = Member.builder().username("member2").age(20).team(teamA).build();
        em.persist(member2);
        Member member3 = Member.builder().username("member3").age(30).team(teamB).build();
        em.persist(member3);
        Member member4 = Member.builder().username("member4").age(40).team(teamB).build();
        em.persist(member4);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("basic Test")
    void basicTest() throws Exception {
        //given
        Member member = new Member("member1", 10);
        //when
        memberJpaRepository.save(member);
        Optional<Member> result = memberJpaRepository.findById(member.getId());
        //then
        assertThat(member).isEqualTo(result.orElseThrow(() -> new NoResultException("No result")));
    }

    @Test
    @DisplayName("searchByBuilder Test")
    void searchByBuilderTest() throws Exception {
        //given
        MemberSearchCondition con = new MemberSearchCondition();
        con.setAgeGoe(35);
        con.setAgeLoe(40);
        con.setTeamName("teamB");
        //when
        List<MemberTeamDTO> result = memberJpaRepository.searchByBuilder(con);
        //then
        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    @DisplayName("search Test")
    void searchTest() throws Exception {
        //given
        MemberSearchCondition con = new MemberSearchCondition();
        con.setAgeGoe(35);
        con.setAgeLoe(40);
        con.setTeamName("teamB");
        //when
        List<MemberTeamDTO> result = memberJpaRepository.search(con);
        //then
        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    @DisplayName("Querydsl searchByBuilder 테스트")
    void searchByBuilder() throws Exception {
        //given
        String teamName = "teamB";
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setTeamName(teamName);

        //when
        List<MemberTeamDTO> result = memberJpaRepository.searchByBuilder(condition);

        //then
        assertThat(result)
            .extracting("teamName")
            .containsOnly(teamName);
    }

    @Test
    @DisplayName("페이징 + 컨텐트 심플")
    void simpleSearchQuery() throws Exception {
        //given
        MemberSearchCondition con = new MemberSearchCondition();

        PageRequest pageable = PageRequest.of(0, 3);

        //when
        Page<MemberTeamDTO> results = memberJpaRepository.searchPageSimple(con, pageable);

        //then
        assertThat(results.getSize()).isEqualTo(3);
        assertThat(results.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }
}