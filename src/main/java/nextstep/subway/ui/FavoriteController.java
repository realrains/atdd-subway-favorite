package nextstep.subway.ui;

import nextstep.auth.authentication.UserDetails;
import nextstep.auth.authorization.AuthenticationPrincipal;
import nextstep.auth.secured.Secured;
import nextstep.member.application.MemberService;
import nextstep.subway.applicaion.FavoriteService;
import nextstep.subway.applicaion.dto.FavoriteRequest;
import nextstep.subway.applicaion.dto.FavoriteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final MemberService memberService;

    public FavoriteController(FavoriteService favoriteService, MemberService memberService) {
        this.favoriteService = favoriteService;
        this.memberService = memberService;
    }

    @Secured({"ROLE_ADMIN", "ROLE_NORMAL"})
    @PostMapping
    public ResponseEntity<Void> createFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FavoriteRequest request
    ) {
        var userId = getUserId(userDetails);
        var favorite = favoriteService.createFavorite(userId, request.getSource(), request.getTarget());

        return ResponseEntity
                .created(URI.create("/favorites/" + favorite.getId()))
                .build();
    }

    @Secured({"ROLE_ADMIN", "ROLE_NORMAL"})
    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> getFavorites(@AuthenticationPrincipal UserDetails userDetails) {
        var userId = getUserId(userDetails);
        var favorites = favoriteService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    private Long getUserId(UserDetails userDetails) {
        return memberService.findMember(userDetails.getUsername()).getId();
    }
}