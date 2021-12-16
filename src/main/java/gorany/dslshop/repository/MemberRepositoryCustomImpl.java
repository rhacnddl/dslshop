package gorany.dslshop.repository;

import static gorany.dslshop.entity.QMember.member;
import static gorany.dslshop.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import gorany.dslshop.dto.MemberSearchCondition;
import gorany.dslshop.dto.MemberTeamDTO;
import gorany.dslshop.dto.QMemberTeamDTO;
import gorany.dslshop.entity.Member;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory factory;

    public MemberRepositoryCustomImpl(EntityManager em) {
        factory = new JPAQueryFactory(em);
    }

    public List<MemberTeamDTO> searchByBuilder(MemberSearchCondition condition) {

        BooleanBuilder builder = new BooleanBuilder();

        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return factory
            .select(new QMemberTeamDTO(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(builder)
            .fetch();
    }

    public List<MemberTeamDTO> search(MemberSearchCondition condition) {
        return factory
            .select(new QMemberTeamDTO(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                getUsernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                getAgeGoe(condition.getAgeGoe()),
                getAgeLoe(condition.getAgeLoe())

            )
            .fetch();
    }

    @Override
    public Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {

        QueryResults<MemberTeamDTO> results = factory
            .select(new QMemberTeamDTO(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                getUsernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                getAgeGoe(condition.getAgeGoe()),
                getAgeLoe(condition.getAgeLoe())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults(); //content query 1 + count query 1

        List<MemberTeamDTO> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDTO> searchByBuilderComplex(MemberSearchCondition condition, Pageable pageable) {

        List<MemberTeamDTO> content = factory
            .select(new QMemberTeamDTO(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                getUsernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                getAgeGoe(condition.getAgeGoe()),
                getAgeLoe(condition.getAgeLoe())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Member> countQuery = factory
            .select(member)
            .from(member)
            .leftJoin(member.team, team)
            .where(
                getUsernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                getAgeGoe(condition.getAgeGoe()),
                getAgeLoe(condition.getAgeLoe())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);

        //return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression getUsernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression getAgeGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression getAgeLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }


}
