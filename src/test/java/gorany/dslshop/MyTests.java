package gorany.dslshop;

import static gorany.dslshop.entity.QBoard.board;
import static gorany.dslshop.entity.QReply.reply;
import static gorany.dslshop.entity.QUser.user;
import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import gorany.dslshop.entity.Board;
import gorany.dslshop.entity.QBoard;
import gorany.dslshop.entity.QReply;
import gorany.dslshop.entity.QUser;
import gorany.dslshop.entity.Reply;
import gorany.dslshop.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;

@DataJpaTest
@Transactional
public class MyTests {

    @Autowired
    EntityManager em;

    JPAQueryFactory factory;

    @BeforeEach
    void before() {
        factory = new JPAQueryFactory(em);
        List<User> users = new ArrayList<>();
        User u1 = User.builder().name("user1").build();
        User u2 = User.builder().name("user2").build();
        User u3 = User.builder().name("user3").build();
        User u4 = User.builder().name("user4").build();
        User u5 = User.builder().name("user5").build();
        em.persist(u1);
        em.persist(u2);
        em.persist(u3);
        em.persist(u4);
        em.persist(u5);
        users.add(u1);
        users.add(u2);
        users.add(u3);
        users.add(u4);
        users.add(u5);

        List<Board> boards = new ArrayList<>();
        Board b1 = Board.builder().title("board1").content("board_content1").user(u1).build();
        Board b2 = Board.builder().title("board2").content("board_content2").user(u2).build();
        Board b3 = Board.builder().title("board3").content("board_content3").user(u3).build();
        Board b4 = Board.builder().title("board4").content("board_content4").user(u4).build();
        Board b5 = Board.builder().title("board5").content("board_content5").user(u5).build();
        Board b6 = Board.builder().title("board6").content("board_content6").user(u1).build();
        Board b7 = Board.builder().title("board7").content("board_content7").user(u2).build();
        Board b8 = Board.builder().title("board8").content("board_content8").user(u3).build();
        em.persist(b1);
        em.persist(b2);
        em.persist(b3);
        em.persist(b4);
        em.persist(b5);
        em.persist(b6);
        em.persist(b7);
        em.persist(b8);
        boards.add(b1);
        boards.add(b2);
        boards.add(b3);
        boards.add(b4);
        boards.add(b5);
        boards.add(b6);
        boards.add(b7);
        boards.add(b8);

        List<Reply> replies = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            replies.add(Reply.builder().board(boards.get(i % 8)).user(users.get(i % 5)).content("reply" + i).build());
        }
        replies.forEach(em::persist);

        em.flush();
        em.clear();
    }

    /*
    * 댓글을 작성한 사람과,
    * 댓글이 달린 게시글의 제목,
    * 댓글의 내용
    * 필터링 하여 검색
    * */
    @Test
    @DisplayName("댓글 조회 테스트_JPQL")
    void read_replies_test() throws Exception {
        //given
        String username = "name";
        String title = "title";
        String content = "content";
        //when
        String query = "select r "
            + "from Reply r "
            + "left join fetch r.board b "
            + "left join fetch r.user u "
            + "left join fetch r.parent p "
            + "where ( "
            + "b.title = :title "
            + "and u.name = :username "
            + "and r.content = :content "
            + " ) "
            + "order by r.id desc";

        List<Reply> replies = em.createQuery(query, Reply.class)
            .setParameter("username", username)
            .setParameter("title", title)
            .setParameter("content", content)
            .getResultList();
        //then
    }
    @Test
    @DisplayName("댓글 조회 테스트_Querydsl")
    void read_replies_test_querydsl() throws Exception {
        //given
        String username = "name";
        String title = "title";
        String content = "content";
        //when
        QReply parent = new QReply("p");
        List<Reply> replies = factory
            .selectFrom(reply)
            .leftJoin(reply.board, board).fetchJoin()
            .leftJoin(reply.user, user).fetchJoin()
            .leftJoin(reply.parent, parent).fetchJoin()
            .where(
                user.name.eq(username),
                board.title.eq(title),
                reply.content.eq(content)
            )
            .orderBy(reply.id.desc())
            .fetch();
        //then
    }
    @Test
    @DisplayName("동적 쿼리")
    void dynamicQuery_test() throws Exception {
        //given
        String username = "name";
        String title = "title";
        String content = "content";
        //when
        QReply parent = new QReply("p");
        List<Reply> replies = factory
            .selectFrom(reply)
            .leftJoin(reply.board, board).fetchJoin()
            .leftJoin(reply.user, user).fetchJoin()
            .leftJoin(reply.parent, parent).fetchJoin()
            .where(
                dynamicSearch(username, title, content)
            )
            .orderBy(reply.id.desc())
            .fetch();
        //then
    }

    private BooleanBuilder dynamicSearch(String username, String title, String content) {
        BooleanBuilder builder = new BooleanBuilder();

        if(username != null){
            builder.and(user.name.eq(username));
        }
        if(title != null){
            builder.and(board.title.eq(title));
        }
        if(content != null){
            builder.and(reply.content.eq(content));
        }

        return builder;
    }
    @Test
    @DisplayName("fetch")
    void fetch_test() throws Exception {
        //given
        
        //when
        long count_jpql = em.createQuery("select count(u) from User u", Long.class)
            .getSingleResult();

        long count_querydsl = factory
            .selectFrom(user)
            .fetchCount();

        //then
        assertThat(count_jpql).isEqualTo(count_querydsl);
    }

    @Test
    @DisplayName("paging")
    void paging_test() throws Exception {
        //given
        String content = "reply";
        //when
        List<Reply> result = factory
            .selectFrom(reply)
            .join(reply.user, user).fetchJoin()
            .join(reply.board, board).fetchJoin()
            .where(reply.content.startsWith(content))
            .offset(0)
            .limit(10)
            .fetch();
        //then
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.get(0).getContent()).contains("reply");
    }
    @Test
    @DisplayName("paging fetchResults")
    void paging_fetchResults_test() throws Exception {
        //given
        String content = "reply";
        //when
        QueryResults<Reply> result = factory
            .selectFrom(reply)
            .join(reply.user, user).fetchJoin()
            .join(reply.board, board).fetchJoin()
            .where(reply.content.startsWith(content))
            .offset(0)
            .limit(10)
            .fetchResults();
        //then
        assertThat(result.getTotal()).isEqualTo(50);
        assertThat(result.getResults().get(0).getContent()).contains("reply");
    }
    @Test
    @DisplayName("paging JPQL")
    void paging_jpql_test() throws Exception {
        //given
        String content = "reply%";
        //when
        List<Reply> result = em.createQuery("select r from Reply r "
                + "join fetch r.user u "
                + "join fetch r.board b "
                + "where r.content like :content ", Reply.class)
            .setParameter("content", content)
            .setFirstResult(0)
            .setMaxResults(10)
            .getResultList();
        //then
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.get(0).getContent()).contains("reply");
    }
    @Test
    @DisplayName("theta join")
    void theta_join_where_test() throws Exception {
        //given

        //when
        List<Reply> result = factory
            .select(reply)
            .from(reply, board)
            .where(reply.content.eq(board.content))
            .fetch();
        //then
        /*
        * 실행된 SQL
        select
            reply0_.reply_id as reply_id1_3_,
            reply0_.board_id as board_id3_3_,
            reply0_.content as content2_3_,
            reply0_.parent_id as parent_i4_3_,
            reply0_.user_id as user_id5_3_
        from
            reply reply0_ cross
        join
            board board1_
        where
            reply0_.content=board1_.content
        * */
    }
    @Test
    @DisplayName("using on join")
    void using_join_on_test() throws Exception {
        //given

        //when
        List<Tuple> result = factory
            .select(reply, board)
            .from(reply)
            .leftJoin(board).on(reply.content.eq(board.content))
            .fetch();
        //then
        result.forEach(System.out::println);
        /*
        * 실행된 SQL
        select
            reply0_.reply_id as reply_id1_3_0_,
            board1_.board_id as board_id1_0_1_,
            reply0_.board_id as board_id3_3_0_,
            reply0_.content as content2_3_0_,
            reply0_.parent_id as parent_i4_3_0_,
            reply0_.user_id as user_id5_3_0_,
            board1_.content as content2_0_1_,
            board1_.title as title3_0_1_,
            board1_.user_id as user_id4_0_1_
        from
            reply reply0_
        left outer join
            board board1_
                on (
                    reply0_.content=board1_.content
                )
        * */
    }
    @Test
    @DisplayName("조인 대상 필터링 using on")
    void filtering_using_on() throws Exception {
        //given
        String boardTitle = "board1";
        //when
        List<Tuple> result = factory
            .select(reply, board)
            .from(reply)
            .leftJoin(reply.board, board).on(board.title.eq(boardTitle))
            .fetch();
        //then
        result.forEach(System.out::println);
        /*
        * 실행된 SQL
        select
            reply0_.reply_id as reply_id1_3_0_,
            board1_.board_id as board_id1_0_1_,
            reply0_.board_id as board_id3_3_0_,
            reply0_.content as content2_3_0_,
            reply0_.parent_id as parent_i4_3_0_,
            reply0_.user_id as user_id5_3_0_,
            board1_.content as content2_0_1_,
            board1_.title as title3_0_1_,
            board1_.user_id as user_id4_0_1_
        from
            reply reply0_
        left outer join
            board board1_
                on reply0_.board_id=board1_.board_id
                and (
                    board1_.title=?
                )
        * */

        /*
        * 출력 결과
        [Reply(id=1, content=reply0), Board(id=1, title=board1, content=board_content1)]
        [Reply(id=2, content=reply1), null]
        [Reply(id=3, content=reply2), null]
        [Reply(id=4, content=reply3), null]
        [Reply(id=5, content=reply4), null]
        [Reply(id=6, content=reply5), null]
        [Reply(id=7, content=reply6), null]
        [Reply(id=8, content=reply7), null]
        ...
        * */
    }
    @Test
    @DisplayName("filtering using where")
    void filtering_using_where() throws Exception {
        //given

        //when
        List<Tuple> result = factory
            .select(reply, board)
            .from(reply)
            .innerJoin(reply.board, board)
            .where(board.title.eq("board1"))
            .fetch();
        //then
        result.forEach(System.out::println);

        /*
        * 실행된 SQL
        select
            reply0_.reply_id as reply_id1_3_0_,
            board1_.board_id as board_id1_0_1_,
            reply0_.board_id as board_id3_3_0_,
            reply0_.content as content2_3_0_,
            reply0_.parent_id as parent_i4_3_0_,
            reply0_.user_id as user_id5_3_0_,
            board1_.content as content2_0_1_,
            board1_.title as title3_0_1_,
            board1_.user_id as user_id4_0_1_
        from
            reply reply0_
        inner join
            board board1_
                on reply0_.board_id=board1_.board_id
        where
            board1_.title=?
        * */

        /*
        * 출력 결과
        [Reply(id=1, content=reply0), Board(id=1, title=board1, content=board_content1)]
        [Reply(id=9, content=reply8), Board(id=1, title=board1, content=board_content1)]
        [Reply(id=17, content=reply16), Board(id=1, title=board1, content=board_content1)]
        [Reply(id=25, content=reply24), Board(id=1, title=board1, content=board_content1)]
        [Reply(id=33, content=reply32), Board(id=1, title=board1, content=board_content1)]
        [Reply(id=41, content=reply40), Board(id=1, title=board1, content=board_content1)]
        [Reply(id=49, content=reply48), Board(id=1, title=board1, content=board_content1)]
        * */
    }
}
