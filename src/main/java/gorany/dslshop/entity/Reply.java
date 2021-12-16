package gorany.dslshop.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = {"board", "user", "children", "parent"})
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Reply parent;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    @Builder.Default
    private List<Reply> children = new ArrayList<>();

    public static Reply createReReply(String content, Board board, User user, Reply reply){
        Reply r = new Reply();
        r.board = board;
        r.user = user;
        r.parent = reply;
        r.content = content;
        return r;
    }

    public static Reply createReply(String content, Board board, User user){
        Reply r = new Reply();
        r.board = board;
        r.user = user;
        r.content = content;
        return r;
    }
}
