package gorany.dslshop;

import static gorany.dslshop.entity.QCity.*;
import static gorany.dslshop.entity.QMember.member;
import static gorany.dslshop.entity.QTeam.team;
import static gorany.dslshop.entity.QUser.user;
import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import gorany.dslshop.dto.CityDTO;
import gorany.dslshop.dto.MemberDTO;
import gorany.dslshop.entity.Member;
import gorany.dslshop.entity.QCity;
import gorany.dslshop.entity.Team;
import java.util.List;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory factory;

    @BeforeEach
    void before() {
        factory = new JPAQueryFactory(em);

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
    @DisplayName("join Test!!")
    void joinTest_1() throws Exception {
        //given
        
        //when
        
        //then
    }
    
    @Test
    @DisplayName("베이직 테스트")
    void startJPQL() throws Exception {
        //given
        String username = "member1";

        //when
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
            .setParameter("username", username)
            .getSingleResult();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("start Querydsl")
    void startQuerydslTest() throws Exception {
        //given
        String username = "member1";

        //when
        Member findMember = factory
            .select(member)
            .from(member)
            .where(member.username.eq(username))
            .fetchOne();
        //then
        assertThat(findMember.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("검색조건 쿼리 테스트")
    void searchTest() throws Exception {
        //given
        Member findMember = factory
            .selectFrom(member)
            .where(member.username.eq("member1")
                .and(member.age.eq(10)))
            .fetchOne();

        //when

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("search and param")
    void searchAndParamTest() throws Exception {
        //given
        List<Member> list = factory
            .selectFrom(member)
            .where(
                member.username.eq("member1"),
                member.age.in(10, 20, 30)
            )
            .fetch();
        //when

        //then
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0).getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("결과조회 테스트")
    void resultFetchTest() throws Exception {
        //given
//        List<Member> fetch = factory
//            .selectFrom(member)
//            .fetch();
//        Member fetchOne = factory
//            .selectFrom(QMember.member)
//            .fetchOne();
//        Member fetchFirst = factory
//            .selectFrom(QMember.member)
//            .fetchFirst(); //limit(1).fetch();
        QueryResults<Member> fetchResults = factory
            .selectFrom(member)
            .fetchResults(); //전체 카운트 조회 쿼리 + 결과 조회 쿼리 => 페이징용 쿼리
        long total = fetchResults.getTotal();
        List<Member> content = fetchResults.getResults();

        long count = factory
            .selectFrom(member)
            .fetchCount();
        System.out.println("count = " + count);
        //when

        //then
    }

    /*
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순
     * 2. 회원 이름 오름차순
     * 단, 2에서 회원 이름이 없으면 마짐가에 출력 (null last)
     * */
    @Test
    @DisplayName("정렬 테슽")
    void sortTest() throws Exception {
        //given
        em.persist(Member.builder().username(null).age(100).build());
        em.persist(Member.builder().username("member5").age(100).build());
        em.persist(Member.builder().username("member6").age(100).build());

        List<Member> result = factory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc()
                , member.username.asc().nullsLast())
            .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        //when

        //then
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    @DisplayName("pagingTest")
    void pagingTest() throws Exception {
        //given
        List<Member> result = factory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetch();
        //when

        //then
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("aggregation")
    void aggregationTest() throws Exception {
        //given
        List<Tuple> result = factory
            .select(member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min()
            )
            .from(member)
            .fetch();
        //when

        //then
        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
    }

    /*
    * 팀의 이름과 각 팀의 평균 연령을 구하라
라   * */
    @Test
    @DisplayName("groupbyTest")
    void groupbyTest() throws Exception {
        //given
        List<Tuple> result = factory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team) //entity, alias
            .groupBy(team.name)
            .fetch();
        //when

        //then
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    @DisplayName("joinTest")
    void joinTest() throws Exception {
        //given
        List<Member> result = factory
            .selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq("teamA"))
            .fetch();
        //when

        //then
        assertThat(result.get(0).getTeam().getName()).isEqualTo("teamA");
        assertThat(result).extracting("username").containsExactly("member1", "member2");
    }

    @Test
    @DisplayName("세타조인 (연관관계가 없는 엔티티 조인)")
    void thetaJoinTest() throws Exception {
        //given
        em.persist(Member.builder().username("teamA").age(15).build());
        em.persist(Member.builder().username("teamB").age(20).build());

        List<Member> result = factory
            .select(member)
            .from(member, team)
            .where(member.username.eq(team.name))
            .fetch();
        //when

        //then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting("username").containsExactly("teamA", "teamB");
        for (Member member1 : result) {
            System.out.println(member1);
        }
    }

    @Test
    @DisplayName("on절 필터링")
    void ON_filtering_test() throws Exception {
        //given

        //when
        List<Tuple> result = factory
            .select(member, team)
            .from(member)
            .leftJoin(member.team, team).on(team.name.eq("teamA"))
            .fetch();
        //then
        result.forEach(m -> {
            System.out.println(m);
        });
    }

    @Test
    @DisplayName("조인 온 노 릴레이션")
    void join_on_no_relation_test() throws Exception {
        //given
        em.persist(Member.builder().username("teamA").build());
        em.persist(Member.builder().username("teamB").build());
        em.persist(Member.builder().username("teamC").build());
        em.flush();
        em.clear();
        //when
        List<Tuple> result = factory
            .select(member, team)
            .from(member)
            .leftJoin(team).on(team.name.eq(member.username))
            .fetch();
        //then
        assertThat(result.get(0).get(member.username)).isEqualTo(result.get(0).get(team.name));
    }

    @Test
    @DisplayName("tuple projection")
    void tuple_projection() throws Exception {
        //given
        List<Tuple> result = factory
            .select(member, team)
            .from(member)
            .join(member.team, team)
            .where(team.name.eq("teamA"))
            .fetch();
        //when

        //then
        result.forEach(t -> {
            System.out.println(t.get(member) + ", " + t.get(member.team));
            assertThat(t.get(team).getName()).isEqualTo("teamA");
        });
    }

    @Test
    @DisplayName("dto")
    void dto_jpql() throws Exception {

    }

    @Test
    @DisplayName("dto querydsl")
    void dto_querydsl() throws Exception {
        //given
        List<MemberDTO> result = factory
            .select(Projections.bean(MemberDTO.class,
                member.age,
                member.username))
            .from(member)
            .fetch();
        //when

        //then
        result.forEach(System.out::println);
    }

    @Test
    @DisplayName("dto tst")
    void dto_depth_test() throws Exception {
        //given
        List<MemberDTO> result = factory
            .select(Projections.bean(MemberDTO.class,
                member.age,
                member.username,
                member.team.name))
            .from(member)
            .join(member.team, team)
            .fetch();
        //when

        //then
        for (MemberDTO m : result) {
            System.out.println(m);
        }
    }

    @Test
    @DisplayName("cityDTO Test")
    void cityDTOTest() throws Exception {
        //given
        List<CityDTO> result = factory
            .select(Projections.bean(CityDTO.class,
                city.name.as("cityName"),
                city.member.username.as("memberName"),
                city.member.team.name.as("teamName")))
            .from(city)
            .join(city.member, member)
            .join(member.team, team)
            .fetch();
        //when

        //then
        result.forEach(System.out::println);
    }
    @Test
    @DisplayName("booleanBuilder")
    void booleanBuilderTest() throws Exception {
        //given
        String username = "member1";
        Integer ageParam = 10;
        //when
        List<Member> result = factory
            .select(member)
            .from(member)
            .where(
                search1(username, ageParam)
            )
            .fetch();
        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo(username);
        assertThat(result.get(0).getAge()).isEqualTo(ageParam);
    }
    @Test
    @DisplayName("where다중 파라미터")
    void dynamic_where_multi_param_test() throws Exception {
        //given
        String username = "member1";
        Integer ageParam = 10;
        //when
        List<Member> result = factory
            .select(member)
            .from(member)
            .where(
                usernameEq(username),
                ageEq(ageParam)
            )
            .fetch();
        //then
    }

    private Predicate usernameEq(String username) {
        if(username != null)
            return member.username.eq(username);

        return null;
    }

    private Predicate ageEq(Integer ageParam) {
        if (ageParam != null) {
            return member.age.eq(ageParam);
        }

        return null;
    }

    private BooleanBuilder search1(String username, Integer ageParam) {
        BooleanBuilder builder = new BooleanBuilder(member.username.like("%m%"));
        if(username != null && !username.equals("")){
            builder.and(member.username.like(username));
        }
        if(ageParam != null && ageParam != 0){
            builder.and(member.age.eq(ageParam));
        }
        return builder;
    }

    @Test
    @DisplayName("bulk update or remove")
    void bulkUpdate() throws Exception {
        //given

        //when
        long count = factory
            .update(member)
            .set(member.username, "non-member")
            .where(member.age.lt(28))
            .execute();
        List<Member> result = factory
            .selectFrom(member)
            .fetch();
        //then
        assertThat(count).isEqualTo(2);
        for (Member member1 : result) {
            System.out.println(member1);
        }
    }
    @Test
    @DisplayName("bulk Add")
    void bulk_add_test() throws Exception {
        //given

        //when
        long count = factory
            .update(member)
            .set(member.age, member.age.add(1))
            .execute();
        //then
        assertThat(count).isEqualTo(4);
        List<Member> result = factory.selectFrom(member)
            .fetch();

        result.forEach(System.out::println);
    }
    @Test
    @DisplayName("bulk delete")
    void bulk_delete_test() throws Exception {
        //given
        long count = factory
            .delete(member)
            .where(member.age.gt(20))
            .execute();
        //when

        //then
        assertThat(count).isEqualTo(2);
    }
}
