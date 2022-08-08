package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.applicaion.dto.FavoriteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;

import static nextstep.subway.acceptance.LineSteps.*;
import static nextstep.subway.acceptance.StationSteps.지하철역_생성_요청;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class FavoriteAcceptanceTest extends AcceptanceTest {

    private Long 교대역;
    private Long 강남역;
    private Long 양재역;
    private Long 남부터미널역;
    private Long 이호선;
    private Long 신분당선;
    private Long 삼호선;

    @BeforeEach
    public void setUp() {
        super.setUp();
        교대역 = 지하철역_생성_요청("교대역").jsonPath().getLong("id");
        강남역 = 지하철역_생성_요청("강남역").jsonPath().getLong("id");
        양재역 = 지하철역_생성_요청("양재역").jsonPath().getLong("id");
        남부터미널역 = 지하철역_생성_요청("남부터미널역").jsonPath().getLong("id");

        이호선 = 지하철_노선_생성_요청("2호선", "green", 교대역, 강남역, 10);
        신분당선 = 지하철_노선_생성_요청("신분당선", "red", 강남역, 양재역, 10);
        삼호선 = 지하철_노선_생성_요청("3호선", "orange", 교대역, 남부터미널역, 2);

        지하철_노선에_지하철_구간_생성_요청(삼호선, createSectionCreateParams(남부터미널역, 양재역, 3));
    }

    /**
     * when 로그인한 사용자가 출발역과 도착역으로 구성된 즐겨찾기 경로를 등록하면
     * then 등록된 사용자의 즐겨찾기를 조회할수 있다
     */
    @DisplayName("즐겨찾기 등록")
    @Test
    void createFavorite() {
        var createResponse = 즐겨찾기_등록(교대역, 양재역);

        var favoriteResponse = 즐겨찾기_조회();
        var favorites = favoriteResponse.jsonPath().getList(".", FavoriteResponse.class);

        assertAll(
                () -> assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value()),
                () -> assertThat(favorites).hasSize(1),
                () -> assertThat(favorites.get(0).getSource().getId()).isEqualTo(교대역),
                () -> assertThat(favorites.get(0).getTarget().getId()).isEqualTo(양재역)
        );
    }

    @DisplayName("출발역과 도착역이 동일한 즐겨찾기 등록 실패")
    @Test
    void createFavoriteFailsForSameStations() {
        var createResponse = 즐겨찾기_등록(교대역, 교대역);
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("존재하지 않는 역에 대한 즐겨찾기 등록 실패")
    @Test
    void createFavoriteFailsForStationNotExist() {
        var 존재하지_않는_역 = 123123L;
        var createResponse = 즐겨찾기_등록(교대역, 존재하지_않는_역);
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private ExtractableResponse<Response> 즐겨찾기_등록(Long source, Long target) {
        var body = new HashMap<>();
        body.put("source", source);
        body.put("target", target);

        return MemberSteps
                .givenAdminLogin()
                    .body(body)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().log().all()
                    .post("/favorites")
                .then().log().all()
                    .extract();
    }

    private ExtractableResponse<Response> 즐겨찾기_조회() {
        return RestAssured
                .given()
                .when().log().all()
                    .get("/favorites")
                .then().log().all()
                    .extract();
    }
}