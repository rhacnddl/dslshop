package gorany.dslshop.controller;

import gorany.dslshop.dto.MemberDTO;
import gorany.dslshop.dto.MemberSearchCondition;
import gorany.dslshop.dto.MemberTeamDTO;
import gorany.dslshop.entity.Member;
import gorany.dslshop.repository.MemberJpaRepository;
import gorany.dslshop.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public ResponseEntity<List<MemberTeamDTO>> searchMemberV1(MemberSearchCondition condition) {
        return new ResponseEntity<>(memberJpaRepository.search(condition), HttpStatus.OK);
    }

    @GetMapping("/v2/members")
    public ResponseEntity<Page<MemberTeamDTO>> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return new ResponseEntity<>(memberRepository.searchPageSimple(condition, pageable), HttpStatus.OK);
    }

    @GetMapping("/v3/members")
    public ResponseEntity<Page<MemberTeamDTO>> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return new ResponseEntity<>(memberRepository.searchByBuilderComplex(condition, pageable), HttpStatus.OK);
    }
}
