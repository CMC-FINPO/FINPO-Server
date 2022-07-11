package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.Policy;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.dto.NotificationDto;
import kr.finpo.api.dto.PolicyDto;
import kr.finpo.api.repository.*;
import kr.finpo.api.service.CategoryService;
import kr.finpo.api.service.PolicyService;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Controller - Notification")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.finpo.kr", uriPort = 443)
@WithMockUser
@Transactional
@SpringBootTest
@EnableJpaAuditing
public class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CategoryService categoryService;

  @Autowired
  private InterestRegionRepository interestRegionRepository;

  @Autowired
  private PolicyService policyService;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private RegionRepository regionRepository;
  @Autowired
  private NotificationRepository notificationRepository;

  public void set(MockMvc mockMvc, String accessToken) {
    this.mockMvc = mockMvc;
    this.accessToken = accessToken;
  }

  String accessToken, refreshToken, otherAccessToken, anotherAccessToken;
  List<InterestCategoryDto> interestCategories;
  List<InterestRegionDto> interestRegions;


  @BeforeEach
  void setUp() throws Exception {
    OAuthControllerTest oc = new OAuthControllerTest();
    HashMap<String, String> map = oc.registerAndGetToken(mockMvc);

    accessToken = map.get("accessToken");
    refreshToken = map.get("refreshToken");

    map = oc.registerAndGetToken(mockMvc, "leeekim");
    otherAccessToken = map.get("accessToken");

    map = oc.registerAndGetToken(mockMvc, "wwwwwlllll");
    anotherAccessToken = map.get("accessToken");
  }

  @Test
  void getMy() throws Exception {
    updateMy(accessToken);

    mockMvc.perform(get("/notification/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내알림조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.subscribe").description("전체 알림 구독 설정 여부")
                    , fieldWithPath("data.interestCategories.[].subscribe").description("관심 카테고리별 알림 구독 설정 여부")
                )
            )
        );
  }


  @Test
  void updateMyTest() throws Exception {
    updateMy(accessToken);
  }

  void updateMy(String accessToken) throws Exception {
    PolicyCategoryControllerTest cc = new PolicyCategoryControllerTest();
    cc.set(mockMvc, accessToken);
    interestCategories = cc.insertMyInterestCategories();

    RegionControllerTest rc = new RegionControllerTest();
    rc.set(mockMvc, interestRegionRepository, accessToken);
    interestRegions = rc.insertMyInterests(101L);

    HashMap<String, Object> body = new HashMap<>() {{
      put("subscribe", true);
      put("registrationToken", "sssssssssssss");
    }};


    mockMvc.perform(put("/notification/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(new ObjectMapper().writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.subscribe").value(true))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("알림받기",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("subscribe").optional().description("전체 알림 구독 설정 여부")
                    , fieldWithPath("registrationToken").optional().description("FCM client registration token (최초 로그인 시에만(디바이스 변경 시) 보내주시면 됩니다)")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.subscribe").description("전체 알림 구독 설정 여부")
                    , fieldWithPath("data.interestCategories.[].subscribe").description("관심 카테고리별 알림 구독 설정 여부").optional().type(JsonFieldType.BOOLEAN)

                )
            )
        )
    ;

    body = new HashMap<>() {{
      put("subscribe", true);
      put("interestCategories", new ArrayList<>() {{
        boolean flag = true;
        for (InterestCategoryDto interestCategory : interestCategories) {
          flag = !flag;
          if (flag) continue;
          add(new HashMap<>() {{
            put("id", interestCategory.id());
            put("subscribe", false);
          }});
        }
      }});
      put("interestRegions", new ArrayList<>() {{
        boolean flag = true;
        for (InterestRegionDto interestRegion : interestRegions) {
          flag = !flag;
          if (flag) continue;
          add(new HashMap<>() {{
            put("id", interestRegion.id());
            put("subscribe", false);
          }});
        }
      }});
    }};

    mockMvc.perform(put("/notification/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(new ObjectMapper().writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("알림수정",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("subscribe").optional().description("전체 알림 구독 설정 여부").type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("interestCategories.[].id").description("관심 카테고리 id")
                    , fieldWithPath("interestCategories.[].subscribe").description("관심 카테고리 알림 구독 설정 여부 (변경된 것만 보내주셔도 됩니다)")
                    , fieldWithPath("interestRegions.[].id").description("관심/기본 지역 id")
                    , fieldWithPath("interestRegions.[].subscribe").description("관심/기본 지역 알림 구독 설정 여부 (변경된 것만 보내주셔도 됩니다)")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.subscribe").description("전체 알림 구독 설정 여부")
                    , fieldWithPath("data.interestCategories.[].subscribe").description("관심 카테고리별 알림 구독 설정 여부")
                    , fieldWithPath("data.interestRegions.[].subscribe").description("관심/기본 지역별 알림 구독 설정 여부")
                )
            )
        )
    ;
  }


  @Test
  void deleteMyTest() throws Exception {
    updateMy(accessToken);
    deleteMy(accessToken);
  }

  void deleteMy(String accessToken) throws Exception {
    HashMap<String, Object> body = new HashMap<>() {{
      put("subscribe", false);
    }};

    mockMvc.perform(put("/notification/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(new ObjectMapper().writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.subscribe").value(false))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("알림끊기",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("subscribe").optional().description("전체 알림 구독 설정 여부")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.subscribe").description("전체 알림 구독 설정 여부")
                    , fieldWithPath("data.interestCategories.[].subscribe").description("관심 카테고리별 알림 구독 설정 여부")
                    , fieldWithPath("data.interestRegions.[].subscribe").description("관심/기본 지역별 알림 구독 설정 여부")
                )
            )
        )
    ;
  }


  @Test
  void getMyNotificationHistoryTest() throws Exception {
    updateMy(accessToken);

    CommentControllerTest ct = new CommentControllerTest();
    ct.set(mockMvc, accessToken);
    ct.pc.set(mockMvc, accessToken);
    ct.uc.set(mockMvc, accessToken);

    int postId = ct.pc.insertPost(accessToken);
    int parentId = ct.insertAnonymity(postId, "익명으로 다는 댓글임", otherAccessToken, false);
    int parentId2 = ct.insert(postId, "작성자가쓰는글", accessToken, false);
    ct.insertAnonymity(postId, "익명으로 단 사람이 또 단 댓글임", otherAccessToken, false);
    ct.insert(postId, "11딴사람이단댓글", anotherAccessToken, false);
    ct.insert(postId, "222딴사람이단댓글", anotherAccessToken, false);
    ct.insert(postId, "333딴사람이단댓글", anotherAccessToken, false);
     ct.insertAnonymity(postId, "익명댓글에 첫번째로 다는 대댓글", parentId, otherAccessToken, false);
    int deleteId = ct.insertAnonymity(postId, "fsdfsd", parentId, anotherAccessToken, false);
    ct.insertAnonymity(postId, "두번째익명 대댓글", parentId2, anotherAccessToken, false);
    int updateId = ct.insert(postId, "글작성자가쓰는 세번째 대댓글", parentId, accessToken, false);
    ct.delete(parentId, otherAccessToken, false);
    ct.delete(deleteId, anotherAccessToken, false);
    ct.update(updateId, "글작성자가 쓰는 세번째 대댓글 수정된거임", accessToken, false);
    ct.uc.deleteMe(anotherAccessToken);


    ArrayList<Object> arr = new ArrayList<>() {{
      add(new HashMap<>() {{
        put("title", "테스트 정책 제목");
        put("region", new HashMap<>() {{
          put("id", "14");
        }});
        put("category", new HashMap<>() {{
          put("id", "6");
        }});
      }});
    }};

    mockMvc.perform(post("/policy")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + accessToken)
        .content(new ObjectMapper().writeValueAsString(arr))
    );

    getMyNotificationHistory(accessToken);
  }

  @Test
  void getMyLastNotificationHistoryTest() throws Exception {
    getMyNotificationHistoryTest();
    Long id = getMyNotificationHistory(accessToken);

    getMyNotificationHistory(accessToken, id);
  }

  Long getMyNotificationHistory(String accessToken, long lastId) throws Exception {

    MvcResult res = mockMvc.perform(get("/notification/history/me?lastId=" + lastId + "&page=0&size=5&sort=id,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내알림기록최신순조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("lastId").description("최근에 불러온 마지막 알림 기록 id").optional(),
                    parameterWithName("page").description("페이지 위치 (0부터 시작)").optional(),
                    parameterWithName("size").description("한 페이지의 데이터 개수").optional(),
                    parameterWithName("sort").description("정렬 기준\n id asc:작성일(createdAt 대신 id 써주세요) 오름차순").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.content.[].id").description("알림 히스토리 id")
                    , fieldWithPath("data.content.[].type").description("알림 타입 (COMMENT, CHILDCOMMENT, POLICY")
                    , fieldWithPath("data.content.[].comment").description("알림 온 댓글").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.content.[].policy").description("알림 온 정책").optional().type(JsonFieldType.OBJECT)
                )
            )
        ).andReturn();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(res.getResponse().getContentAsString());
    json = (JSONObject) parser.parse(json.get("data").toString());

    JSONArray jsonArray = (JSONArray) parser.parse(json.get("content").toString());
    for (Object o : jsonArray) {
      JSONObject jsonObj = (JSONObject) o;
      return Long.valueOf((Integer) jsonObj.get("id"));
    }
    return -1L;
  }

  Long getMyNotificationHistory(String accessToken) throws Exception {

    MvcResult res = mockMvc.perform(get("/notification/history/me?page=0&size=5&sort=id,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내알림기록조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("page").description("페이지 위치 (0부터 시작)").optional(),
                    parameterWithName("size").description("한 페이지의 데이터 개수").optional(),
                    parameterWithName("sort").description("정렬 기준\n id asc:작성일(createdAt 대신 id 써주세요) 오름차순").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.content.[].id").description("알림 히스토리 id")
                    , fieldWithPath("data.content.[].type").description("알림 타입 (COMMENT, CHILDCOMMENT, POLICY")
                    , fieldWithPath("data.content.[].comment").description("알림 온 댓글").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.content.[].policy").description("알림 온 정책").optional().type(JsonFieldType.OBJECT)
                )
            )
        ).andReturn();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(res.getResponse().getContentAsString());
    json = (JSONObject) parser.parse(json.get("data").toString());

    JSONArray jsonArray = (JSONArray) parser.parse(json.get("content").toString());
    for (Object o : jsonArray) {
      JSONObject jsonObj = (JSONObject) o;
      return Long.valueOf((Integer) jsonObj.get("id"));
    }
    return -1L;
  }

  @Test
  void deleteMyNotificationHistoryTest() throws Exception {
    getMyNotificationHistoryTest();
    Long id = getMyNotificationHistory(accessToken );
    long beforeCnt = notificationRepository.count();
    deleteMyNotificationHistory(accessToken, id);
    then(beforeCnt - 1).isEqualTo(notificationRepository.count());
  }

  void deleteMyNotificationHistory(String accessToken, Long id) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.delete("/notification/history/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내알림기록삭제",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("삭제할 알림 히스토리 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data").description("삭제 여부")
                )
            )
        )
    ;
  }
}