package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.repository.InterestPolicyRepository;
import kr.finpo.api.repository.InterestRegionRepository;
import kr.finpo.api.repository.JoinedPolicyRepository;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@DisplayName("Controller - Policy")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.finpo.kr", uriPort = 443)
@WithMockUser
@Transactional
@SpringBootTest
public
class PolicyControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private InterestPolicyRepository interestPolicyRepository;

  @Autowired
  private JoinedPolicyRepository joinedPolicyRepository;

  @Autowired
  private InterestRegionRepository interestRegionRepository;


  String accessToken, refreshToken;

  @BeforeEach
  void setUp() throws Exception {
    OAuthControllerTest oc = new OAuthControllerTest();
    HashMap<String, String> map = oc.registerAndGetToken(mockMvc);
    accessToken = map.get("accessToken");
    refreshToken = map.get("refreshToken");

    RegionControllerTest rc = new RegionControllerTest();
    rc.set(mockMvc, interestRegionRepository, accessToken);
    rc.insertMyInterestsTest();

    PolicyCategoryControllerTest cc = new PolicyCategoryControllerTest();
    cc.set(mockMvc, accessToken);
    cc.insertMyInterestCategories();
  }

  @Test
  void gett() throws Exception {
    Long id = 413L;

    insertMyInterest(id);

    mockMvc.perform(RestDocumentationRequestBuilders.get("/policy/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andExpect(jsonPath("$.data.id").value(413))
        .andExpect(jsonPath("$.data.isInterest").value(true))

        .andDo(
            document("정책상세조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("조회할 정책 id")
                ),

                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("정책 id"),
                    fieldWithPath("data.title").description("정책 제목").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content").description("정책 내용").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.institution").description("주관 기관").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.supportScale").description("지원 규모").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.support").description("지원 내용").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.period").description("기간").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.startDate").description("시작일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.endDate").description("종료일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.process").description("신청 절차").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.announcement").description("결과 발표").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.detailUrl").description("상세내용 url").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.openApiType").description("Open API 출처").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.modifiedAt").description("수정일").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.category").description("카테고리").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.region").description("지역").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.countOfInterest").description("관심정책으로 추가한 회원 수").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("data.hits").description("조회수").type(JsonFieldType.NUMBER),
                    fieldWithPath("data.isInterest").description("관심정책 등록여부")
                )
            )
        );
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
                    parameterWithName("sort").description("정렬 기준 (복수 정렬 가능)\n title,asc:제목 오름차순\nmodifiedAt:수정일 내림차순\n[title, institution, startDate, endDate, modifiedAt, countOfInterest, countOfInterest]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.content.[].id").description("정책 id"),
                    fieldWithPath("data.content.[].title").description("정책 제목").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].institution").description("주관 기관").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].category").description("카테고리").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].region").description("지역").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].countOfInterest").description("관심정책으로 추가한 회원 수").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("data.content.[].isInterest").description("관심정책 등록여부"),

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
                    parameterWithName("sort").description("정렬 기준 (복수 정렬 가능)\n title,asc:제목 오름차순\nmodifiedAt:수정일 내림차순\n[title, institution, startDate, endDate, modifiedAt, countOfInterest]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.content.[].id").description("정책 id"),
                    fieldWithPath("data.content.[].title").description("정책 제목").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].institution").description("주관 기관").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].category").description("카테고리").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].region").description("지역").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].countOfInterest").description("관심정책으로 추가한 회원 수").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("data.content.[].isInterest").description("관심정책 등록여부"),

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
                    parameterWithName("sort").description("정렬 기준 (복수 정렬 가능)\n title,asc:제목 오름차순\nmodifiedAt:수정일 내림차순\n[title, institution, startDate, endDate, modifiedAt, countOfInterest]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.content.[].id").description("정책 id"),
                    fieldWithPath("data.content.[].title").description("정책 제목").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].institution").description("주관 기관").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].category").description("카테고리").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].region").description("지역").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].countOfInterest").description("관심정책으로 추가한 회원 수").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("data.content.[].isInterest").description("관심정책 등록여부"),

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

    mockMvc.perform(get("/policy/search?title=무료&page=0&size=5&sort=countOfInterest,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("정책인기순검색",
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
                    parameterWithName("sort").description("정렬 기준 (복수 정렬 가능)\ncountOfInterest:인기순, desc:내림차순\n").optional()
                )));
  }


  @Test
  void insertMyInterest() throws Exception {
    long beforeCnt = interestPolicyRepository.count();

    insertMyInterest(21L);
    insertMyInterest(40L);
    insertMyInterest(73L);
    insertMyInterest(3L);
    insertMyInterest(41L);
    then(beforeCnt + 5).isEqualTo(interestPolicyRepository.count());
  }


  int insertMyInterest(Long id) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("policyId", id);

    MvcResult res = mockMvc.perform(post("/policy/interest")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.policy.id").value(id))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내관심정책추가",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("policyId").description("관심정책에 추가할 정책id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("관심정책id"),
                    fieldWithPath("data.policy.id").description("정책id")
                )
            )
        )
        .andReturn();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(res.getResponse().getContentAsString());
    json = (JSONObject) parser.parse(json.get("data").toString());
    return (int) json.get("id");
  }

  @Test
  void insertMyJoined() throws Exception {
    long beforeCnt = joinedPolicyRepository.count();
    insertMyJoined(37L, "이건 언제언제해서 잘했음");
    insertMyJoined(47L, null);
    insertMyJoined(4L, null);
    insertMyJoined(91L, null);
    insertMyJoined(78L, "이건 좀 아쉬웠음");
    then(beforeCnt + 5).isEqualTo(joinedPolicyRepository.count());
  }

  int insertMyJoined(Long id, String memo) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("policyId", id);
    if (memo != null) body.put("memo", memo);

    MvcResult res = mockMvc.perform(post("/policy/joined")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.policy.id").value(id))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내참여정책추가",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("policyId").description("참여정책에 추가할 정책id"),
                    fieldWithPath("memo").description("메모").optional().type(JsonFieldType.STRING)
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("참여정책id"),
                    fieldWithPath("data.policy.id").description("정책id"),
                    fieldWithPath("data.memo").description("메모").optional().type(JsonFieldType.STRING)
                )
            )
        )
        .andReturn();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(res.getResponse().getContentAsString());
    json = (JSONObject) parser.parse(json.get("data").toString());
    return (int) json.get("id");
  }

  @Test
  void getMyInterests() throws Exception {
    insertMyInterest();

    mockMvc.perform(get("/policy/interest/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.length()").value(5))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내관심정책조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("관심정책id"),
                    fieldWithPath("data.[].policy.id").description("정책id")
                )
            )
        )
    ;
  }

  @Test
  void getMyJoins() throws Exception {
    insertMyJoined();

    mockMvc.perform(get("/policy/joined/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.length()").value(5))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내참여정책조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("참여정책id"),
                    fieldWithPath("data.[].policy.id").description("정책id"),
                    fieldWithPath("data.[].memo").description("메모").optional().type(JsonFieldType.STRING)
                )
            )
        )
    ;
  }

  @Test
  void updateMyJoined() throws Exception {
    int id = insertMyJoined(312L, "변경 전 메모안ㅇㄴ맒ㄴ알ㅇㄴ말ㅇㄴㅁ");

    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("memo", "변경 후 메모임");

    long beforeCnt = joinedPolicyRepository.count();

    mockMvc.perform(RestDocumentationRequestBuilders.put("/policy/joined/{id}",id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내참여정책수정",
               preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("변경할 참여정책 id")
                ),
                requestFields(
                    fieldWithPath("memo").description("변경할 메모")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("참여정책id"),
                    fieldWithPath("data.policy.id").description("정책id"),
                    fieldWithPath("data.memo").description("메모").optional().type(JsonFieldType.STRING)
                )
            )
        )
    ;
    then(beforeCnt).isEqualTo(joinedPolicyRepository.count());
  }

  @Test
  void deleteMyInterest() throws Exception {
    insertMyInterest(40L);
    insertMyInterest(312L);
    int id = insertMyInterest(51L);
    long beforeCnt = interestPolicyRepository.count();

    mockMvc.perform(RestDocumentationRequestBuilders.delete("/policy/interest/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내관심정책삭제",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("삭제할 관심정책 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data").description("삭제 성공 여부")
                )
            )
        )
    ;
    then(beforeCnt - 1).isEqualTo(interestPolicyRepository.count());
  }

  @Test
  void deleteMyJoined() throws Exception {
    insertMyJoined(40L, "그냥저냥");
    insertMyJoined(312L, null);
    int id = insertMyJoined(24L, "이건 꽤 괜찮았던듯");
    long beforeCnt = joinedPolicyRepository.count();

    mockMvc.perform(RestDocumentationRequestBuilders.delete("/policy/joined/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내참여정책삭제",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("삭제할 참여정책 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data").description("삭제 성공 여부")
                )
            )
        )
    ;
    then(beforeCnt - 1).isEqualTo(joinedPolicyRepository.count());
  }

  @Test
  void deleteMyInterestByPolicyId() throws Exception {
    Long policyId = 32L;
    insertMyInterest(40L);
    insertMyInterest(312L);
    int id = insertMyInterest(policyId);
    long beforeCnt = interestPolicyRepository.count();

    mockMvc.perform(RestDocumentationRequestBuilders.delete("/policy/interest/me?policyId="+policyId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내관심정책삭제정책id",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("policyId").description("삭제할 정책 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data").description("삭제 성공 여부")
                )
            )
        )
    ;
    then(beforeCnt - 1).isEqualTo(interestPolicyRepository.count());
  }

  @Test
  void deleteMyJoinedByPolicyId() throws Exception {
    Long policyId = 24L;
    insertMyJoined(40L, "그냥저냥");
    insertMyJoined(312L, null);
    int id = insertMyJoined(policyId, "이건 꽤 괜찮았던듯");
    long beforeCnt = joinedPolicyRepository.count();

    mockMvc.perform(RestDocumentationRequestBuilders.delete("/policy/joined/me?policyId="+policyId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내참여정책삭제정책id",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("policyId").description("삭제할 정책 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data").description("삭제 성공 여부")
                )
            )
        )
    ;
    then(beforeCnt - 1).isEqualTo(joinedPolicyRepository.count());
  }

}