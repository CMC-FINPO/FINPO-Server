package kr.finpo.api.controller;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.repository.CategoryRepository;
import kr.finpo.api.repository.InterestCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@DisplayName("Controller - Policy")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.finpo.kr", uriPort = 443)
@WithMockUser
@Transactional
@SpringBootTest
class PolicyControllerTest {

  @Autowired
  private MockMvc mockMvc;

  String accessToken, refreshToken;

  @BeforeEach
  void setUp() throws Exception {
    OAuthControllerTest oc = new OAuthControllerTest();
    HashMap<String, String> map = oc.registerAndGetToken(mockMvc);
    accessToken = map.get("accessToken");
    refreshToken = map.get("refreshToken");

    RegionControllerTest rc = new RegionControllerTest(mockMvc, accessToken);
    rc.insertRegionTest();

    PolicyCategoryControllerTest cc = new PolicyCategoryControllerTest(mockMvc, accessToken);
    cc.insertMyInterestCategories();
  }

  @Test
  void getMy() throws Exception {
    mockMvc.perform(get("/policy/me?page=1&size=5&sort=title,asc&sort=modifiedAt,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andExpect(jsonPath("$.data.content.length()").value(5))
        .andDo(
            document("내맞춤정책조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("page").description("페이지 위치 (0부터 시작)").optional(),
                    parameterWithName("size").description("한 페이지의 데이터 개수").optional(),
                    parameterWithName("sort").description("정렬 기준 (복수 정렬 가능)\n title,asc:제목 오름차순\nmodifiedAt:수정일 내림차순\n[title, institution, startDate, endDate, modifiedAt]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.content.[].id").description("정책 id"),
                    fieldWithPath("data.content.[].title").description("정책 제목").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].content").description("정책 내용").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].institution").description("주관 기관").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].supportScale").description("지원 규모").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].support").description("지원 내용").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].period").description("기간").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].startDate").description("시작일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].endDate").description("종료일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].process").description("신청 절차").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].announcement").description("결과 발표").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].detailUrl").description("상세내용 url").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].openApiType").description("Open API 출처").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].modifiedAt").description("수정일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].category").description("카테고리").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].region").description("지역").optional().type(JsonFieldType.OBJECT),

                    fieldWithPath("data.last").description("현재가 마지막 페이지인가"),
                    fieldWithPath("data.first").description("현재가 첫 페이지인가"),
                    fieldWithPath("data.totalElements").description("전체 데이터 수"),
                    fieldWithPath("data.totalPages").description("전체 페이지 수"),
                    fieldWithPath("data.number").description("현재 페이지"),
                    fieldWithPath("data.size").description("한 페이지 데이터 개수"),
                    fieldWithPath("data.numberOfElements").description("현재 페이지 데이터 개수"),
                    fieldWithPath("data.empty").description("현재 페이지가 비어있는가")
                )
            )
        );
  }

  @Test
  void search() throws Exception {
    mockMvc.perform(get("/policy/search?title=청년&&region=107,4,10,11,102,104&category=9,10,11&page=0&size=5&sort=title,asc&sort=modifiedAt,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("정책제목검색",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("title").description("제목 검색").optional(),
                    parameterWithName("startDate").description("시작 날짜").optional(),
                    parameterWithName("endDate").description("종료 날짜").optional(),
                    parameterWithName("region").description("지역 id(복수개 가능)").optional(),
                    parameterWithName("category").description("카테고리 id(복수개 가능)").optional(),

                    parameterWithName("page").description("페이지 위치 (0부터 시작)").optional(),
                    parameterWithName("size").description("한 페이지의 데이터 개수").optional(),
                    parameterWithName("sort").description("정렬 기준 (복수 정렬 가능)\n title,asc:제목 오름차순\nmodifiedAt:수정일 내림차순\n[title, institution, startDate, endDate, modifiedAt]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.content.[].id").description("정책 id"),
                    fieldWithPath("data.content.[].title").description("정책 제목").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].content").description("정책 내용").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].institution").description("주관 기관").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].supportScale").description("지원 규모").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].support").description("지원 내용").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].period").description("기간").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].startDate").description("시작일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].endDate").description("종료일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].process").description("신청 절차").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].announcement").description("결과 발표").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].detailUrl").description("상세내용 url").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].openApiType").description("Open API 출처").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].modifiedAt").description("수정일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].category").description("카테고리").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].region").description("지역").optional().type(JsonFieldType.OBJECT),

                    fieldWithPath("data.last").description("현재가 마지막 페이지인가"),
                    fieldWithPath("data.first").description("현재가 첫 페이지인가"),
                    fieldWithPath("data.totalElements").description("전체 데이터 수"),
                    fieldWithPath("data.totalPages").description("전체 페이지 수"),
                    fieldWithPath("data.number").description("현재 페이지"),
                    fieldWithPath("data.size").description("한 페이지 데이터 개수"),
                    fieldWithPath("data.numberOfElements").description("현재 페이지 데이터 개수"),
                    fieldWithPath("data.empty").description("현재 페이지가 비어있는가")
                )
            )
        )
        ;

    mockMvc.perform(get("/policy/search?startDate=2022-05-20&category=6&page=0&size=5&sort=startDate,asc&sort=modifiedAt,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("정책날짜검색",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("title").description("제목 검색").optional(),
                    parameterWithName("startDate").description("시작 날짜").optional(),
                    parameterWithName("endDate").description("종료 날짜").optional(),
                    parameterWithName("region").description("지역 id(복수개 가능)").optional(),
                    parameterWithName("category").description("카테고리 id(복수개 가능)").optional(),

                    parameterWithName("page").description("페이지 위치 (0부터 시작)").optional(),
                    parameterWithName("size").description("한 페이지의 데이터 개수").optional(),
                    parameterWithName("sort").description("정렬 기준 (복수 정렬 가능)\n title,asc:제목 오름차순\nmodifiedAt:수정일 내림차순\n[title, institution, startDate, endDate, modifiedAt]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.content.[].id").description("정책 id"),
                    fieldWithPath("data.content.[].title").description("정책 제목").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].content").description("정책 내용").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].institution").description("주관 기관").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].supportScale").description("지원 규모").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].support").description("지원 내용").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].period").description("기간").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].startDate").description("시작일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].endDate").description("종료일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].process").description("신청 절차").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].announcement").description("결과 발표").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].detailUrl").description("상세내용 url").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].openApiType").description("Open API 출처").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].modifiedAt").description("수정일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].category").description("카테고리").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].region").description("지역").optional().type(JsonFieldType.OBJECT),

                    fieldWithPath("data.last").description("현재가 마지막 페이지인가"),
                    fieldWithPath("data.first").description("현재가 첫 페이지인가"),
                    fieldWithPath("data.totalElements").description("전체 데이터 수"),
                    fieldWithPath("data.totalPages").description("전체 페이지 수"),
                    fieldWithPath("data.number").description("현재 페이지"),
                    fieldWithPath("data.size").description("한 페이지 데이터 개수"),
                    fieldWithPath("data.numberOfElements").description("현재 페이지 데이터 개수"),
                    fieldWithPath("data.empty").description("현재 페이지가 비어있는가")
                )
            )
        )
    ;
  }
}