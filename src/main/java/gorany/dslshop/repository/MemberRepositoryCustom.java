package gorany.dslshop.repository;

import gorany.dslshop.dto.MemberSearchCondition;
import gorany.dslshop.dto.MemberTeamDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    List<MemberTeamDTO> search(MemberSearchCondition condition);
    Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    List<MemberTeamDTO> searchByBuilder(MemberSearchCondition condition);
    Page<MemberTeamDTO> searchByBuilderComplex(MemberSearchCondition condition, Pageable pageable);

}
