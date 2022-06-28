package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.repository.InterestRegionRepository;
import kr.finpo.api.service.CategoryService;
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

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Controller - Notification")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.finpo.kr", uriPort = 443)
@WithMockUser
@Transactional
@SpringBootTest
public
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CategoryService categoryService;

  @Autowired
  private InterestRegionRepository interestRegionRepository;

  public void set(MockMvc mockMvc, String accessToken) {
    this.mockMvc = mockMvc;
    this.accessToken = accessToken;
  }

  String accessToken, refreshToken;
  List<InterestCategoryDto> interestCategories;
  List<InterestRegionDto> interestRegions;


  @BeforeEach
  void setUp() throws Exception {
    OAuthControllerTest oc = new OAuthControllerTest();
    HashMap<String, String> map = oc.registerAndGetToken(mockMvc);
    accessToken = map.get("accessToken");
    refreshToken = map.get("refreshToken");

    PolicyCategoryControllerTest cc = new PolicyCategoryControllerTest();
    cc.set(mockMvc, accessToken);
    interestCategories = cc.insertMyInterestCategories();

    RegionControllerTest rc = new RegionControllerTest();
    rc.set(mockMvc, interestRegionRepository, accessToken);
    interestRegions = rc.insertMyInterests(101L);
  }

  @Test
  void getMy() throws Exception {
    updateMy();

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
  void updateMy() throws Exception {
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
                    , fieldWithPath("data.interestCategories.[].subscribe").description("관심 카테고리별 알림 구독 설정 여부")
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
          if(flag) continue;
          add(new HashMap<>(){{
            put("id", interestCategory.id());
            put("subscribe", false);
          }});
        }
      }});
      put("interestRegions", new ArrayList<>() {{
        boolean flag = true;
        for (InterestRegionDto interestRegion : interestRegions) {
          flag = !flag;
          if(flag) continue;
          add(new HashMap<>(){{
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



    body = new HashMap<>() {{
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

}