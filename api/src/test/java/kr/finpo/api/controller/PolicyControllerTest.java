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
            document("??????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ?????? id")
                ),

                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.id").description("?????? id"),
                    fieldWithPath("data.title").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.institution").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.supportScale").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.support").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.period").description("??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.startDate").description("?????????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.endDate").description("?????????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.process").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.announcement").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.detailUrl").description("???????????? url").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.openApiType").description("Open API ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.modifiedAt").description("?????????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.category").description("????????????").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.region").description("??????").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.countOfInterest").description("?????????????????? ????????? ?????? ???").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("data.hits").description("?????????").type(JsonFieldType.NUMBER),
                    fieldWithPath("data.isInterest").description("???????????? ????????????")
                )
            )
        );
  }

  @Test
  void getMy() throws Exception {
    mockMvc.perform(get("/policy/me?page=1&size=5&sort=title,asc&sort=id,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andExpect(jsonPath("$.data.content.length()").value(5))
        .andDo(
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("page").description("????????? ?????? (0?????? ??????)").optional(),
                    parameterWithName("size").description("??? ???????????? ????????? ??????").optional(),
                    parameterWithName("sort").description("?????? ?????? (?????? ?????? ??????)\n title,asc:?????? ????????????\nid:????????? ????????????\n[title, institution, startDate, endDate, id, countOfInterest, countOfInterest]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.content.[].id").description("?????? id"),
                    fieldWithPath("data.content.[].title").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].institution").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].category").description("????????????").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].region").description("??????").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].countOfInterest").description("?????????????????? ????????? ?????? ???").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("data.content.[].isInterest").description("???????????? ????????????"),

                    fieldWithPath("data.last").description("????????? ????????? ???????????????"),
                    fieldWithPath("data.first").description("????????? ??? ???????????????"),
                    fieldWithPath("data.totalElements").description("?????? ????????? ???"),
                    fieldWithPath("data.totalPages").description("?????? ????????? ???"),
                    fieldWithPath("data.number").description("?????? ?????????"),
                    fieldWithPath("data.size").description("??? ????????? ????????? ??????"),
                    fieldWithPath("data.numberOfElements").description("?????? ????????? ????????? ??????"),
                    fieldWithPath("data.empty").description("?????? ???????????? ???????????????")
                )
            )
        );
  }

  @Test
  void search() throws Exception {
    mockMvc.perform(get("/policy/search?title=??????&&region=107,4,10,11,102,104&category=9,10,11&page=0&size=5&sort=title,asc&sort=id,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("??????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("title").description("?????? ??????").optional(),
                    parameterWithName("startDate").description("?????? ??????").optional(),
                    parameterWithName("endDate").description("?????? ??????").optional(),
                    parameterWithName("region").description("?????? id(????????? ??????)").optional(),
                    parameterWithName("category").description("???????????? id(????????? ??????)").optional(),

                    parameterWithName("page").description("????????? ?????? (0?????? ??????)").optional(),
                    parameterWithName("size").description("??? ???????????? ????????? ??????").optional(),
                    parameterWithName("sort").description("?????? ?????? (?????? ?????? ??????)\n title,asc:?????? ????????????\nid:????????? ????????????\n[title, institution, startDate, endDate, id, countOfInterest]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.content.[].id").description("?????? id"),
                    fieldWithPath("data.content.[].title").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].institution").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].category").description("????????????").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].region").description("??????").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].countOfInterest").description("?????????????????? ????????? ?????? ???").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("data.content.[].isInterest").description("???????????? ????????????"),

                    fieldWithPath("data.last").description("????????? ????????? ???????????????"),
                    fieldWithPath("data.first").description("????????? ??? ???????????????"),
                    fieldWithPath("data.totalElements").description("?????? ????????? ???"),
                    fieldWithPath("data.totalPages").description("?????? ????????? ???"),
                    fieldWithPath("data.number").description("?????? ?????????"),
                    fieldWithPath("data.size").description("??? ????????? ????????? ??????"),
                    fieldWithPath("data.numberOfElements").description("?????? ????????? ????????? ??????"),
                    fieldWithPath("data.empty").description("?????? ???????????? ???????????????")
                )
            )
        )
    ;

    mockMvc.perform(get("/policy/search?startDate=2022-05-20&category=6&page=0&size=5&sort=startDate,asc&sort=id,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("??????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("title").description("?????? ??????").optional(),
                    parameterWithName("startDate").description("?????? ??????").optional(),
                    parameterWithName("endDate").description("?????? ??????").optional(),
                    parameterWithName("region").description("?????? id(????????? ??????)").optional(),
                    parameterWithName("category").description("???????????? id(????????? ??????)").optional(),

                    parameterWithName("page").description("????????? ?????? (0?????? ??????)").optional(),
                    parameterWithName("size").description("??? ???????????? ????????? ??????").optional(),
                    parameterWithName("sort").description("?????? ?????? (?????? ?????? ??????)\n title,asc:?????? ????????????\nid:????????? ????????????\n[title, institution, startDate, endDate, id, countOfInterest]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.content.[].id").description("?????? id"),
                    fieldWithPath("data.content.[].title").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].institution").description("?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("data.content.[].category").description("????????????").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].region").description("??????").optional().type(JsonFieldType.OBJECT),
                    fieldWithPath("data.content.[].countOfInterest").description("?????????????????? ????????? ?????? ???").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("data.content.[].isInterest").description("???????????? ????????????"),

                    fieldWithPath("data.last").description("????????? ????????? ???????????????"),
                    fieldWithPath("data.first").description("????????? ??? ???????????????"),
                    fieldWithPath("data.totalElements").description("?????? ????????? ???"),
                    fieldWithPath("data.totalPages").description("?????? ????????? ???"),
                    fieldWithPath("data.number").description("?????? ?????????"),
                    fieldWithPath("data.size").description("??? ????????? ????????? ??????"),
                    fieldWithPath("data.numberOfElements").description("?????? ????????? ????????? ??????"),
                    fieldWithPath("data.empty").description("?????? ???????????? ???????????????")
                )
            )
        )
    ;

    mockMvc.perform(get("/policy/search?title=??????&page=0&size=5&sort=countOfInterest,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("title").description("?????? ??????").optional(),
                    parameterWithName("startDate").description("?????? ??????").optional(),
                    parameterWithName("endDate").description("?????? ??????").optional(),
                    parameterWithName("region").description("?????? id(????????? ??????)").optional(),
                    parameterWithName("category").description("???????????? id(????????? ??????)").optional(),

                    parameterWithName("page").description("????????? ?????? (0?????? ??????)").optional(),
                    parameterWithName("size").description("??? ???????????? ????????? ??????").optional(),
                    parameterWithName("sort").description("?????? ?????? (?????? ?????? ??????)\ncountOfInterest:?????????, desc:????????????\n").optional()
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
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("policyId").description("??????????????? ????????? ??????id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.id").description("????????????id"),
                    fieldWithPath("data.policy.id").description("??????id")
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
    insertMyJoined(37L, "?????? ?????????????????? ?????????");
    insertMyJoined(47L, null);
    insertMyJoined(4L, null);
    insertMyJoined(91L, null);
    insertMyJoined(78L, "?????? ??? ????????????");
//    for (Long i = 1L; i < 20L; i++)
//      insertMyJoined(i, "?????? ??? ????????????");

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
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("policyId").description("??????????????? ????????? ??????id"),
                    fieldWithPath("memo").description("??????").optional().type(JsonFieldType.STRING)
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.id").description("????????????id").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("data.policy.id").description("??????id").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("data.memo").description("??????").optional().type(JsonFieldType.STRING)
                )
            )
        )
        .andReturn();

    try {
      JSONParser parser = new JSONParser();
      JSONObject json = (JSONObject) parser.parse(res.getResponse().getContentAsString());
      json = (JSONObject) parser.parse(json.get("data").toString());
      return (int) json.get("id");
    } catch (NullPointerException e) {
      return -1;
    }
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
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("????????????id"),
                    fieldWithPath("data.[].policy.id").description("??????id"),
                    fieldWithPath("data.[].policy.isInterest").description("??????????????????")
                )
            )
        )
    ;
  }

  @Test
  void getMyJoins() throws Exception {
    insertMyInterest(37L);
    insertMyJoined();

    mockMvc.perform(get("/policy/joined/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("????????????id"),
                    fieldWithPath("data.[].policy.id").description("??????id"),
                    fieldWithPath("data.[].memo").description("??????").optional().type(JsonFieldType.STRING)
                )
            )
        )
    ;
  }

  @Test
  void updateMyJoined() throws Exception {
    int id = insertMyJoined(312L, "?????? ??? ??????????????????????????????????????????");

    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("memo", "?????? ??? ?????????");

    long beforeCnt = joinedPolicyRepository.count();

    mockMvc.perform(RestDocumentationRequestBuilders.put("/policy/joined/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ???????????? id")
                ),
                requestFields(
                    fieldWithPath("memo").description("????????? ??????")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.id").description("????????????id"),
                    fieldWithPath("data.policy.id").description("??????id"),
                    fieldWithPath("data.memo").description("??????").optional().type(JsonFieldType.STRING)
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
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ???????????? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data").description("?????? ?????? ??????")
                )
            )
        )
    ;
    then(beforeCnt - 1).isEqualTo(interestPolicyRepository.count());
  }

  @Test
  void deleteMyJoined() throws Exception {
    insertMyJoined(40L, "????????????");
    insertMyJoined(312L, null);
    int id = insertMyJoined(24L, "?????? ??? ???????????????");
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
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ???????????? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data").description("?????? ?????? ??????")
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

    mockMvc.perform(RestDocumentationRequestBuilders.delete("/policy/interest/me?policyId=" + policyId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("???????????????????????????id",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("policyId").description("????????? ?????? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data").description("?????? ?????? ??????")
                )
            )
        )
    ;
    then(beforeCnt - 1).isEqualTo(interestPolicyRepository.count());
  }

  @Test
  void deleteMyJoinedByPolicyId() throws Exception {
    Long policyId = 24L;
    insertMyJoined(40L, "????????????");
    insertMyJoined(312L, null);
    int id = insertMyJoined(policyId, "?????? ??? ???????????????");
    long beforeCnt = joinedPolicyRepository.count();

    mockMvc.perform(RestDocumentationRequestBuilders.delete("/policy/joined/me?policyId=" + policyId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("???????????????????????????id",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("policyId").description("????????? ?????? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data").description("?????? ?????? ??????")
                )
            )
        )
    ;
    then(beforeCnt - 1).isEqualTo(joinedPolicyRepository.count());
  }

}