package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@DisplayName("Controller - Region")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.finpo.kr", uriPort = 443)
@WithMockUser
@Transactional
@SpringBootTest
public
class RegionControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserService userService;

  public void set(MockMvc mockMvc, String accessToken) {
    this.mockMvc = mockMvc;
    this.accessToken = accessToken;
  }

  String accessToken, refreshToken;

  @BeforeEach
  void setUp() throws Exception {
    OAuthControllerTest oc = new OAuthControllerTest();
    HashMap<String, String> map = oc.registerAndGetToken(mockMvc);
    accessToken = map.get("accessToken");
    refreshToken = map.get("refreshToken");
  }

  @AfterEach
  void tearDown() {
  }


  @Test
  void region1() throws Exception {
    mockMvc.perform(get("/region/name").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value("16"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("region1",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("지역 id"),
                    fieldWithPath("data.[].name").description("지역 이름"),
                    fieldWithPath("data.[].depth").description("깊이 (카테고리 1)")
                )
            )
        )
    ;
  }

  @Test
  void region2() throws Exception {
    mockMvc.perform(get("/region/name?parentId=200").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("region2-busan",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("parentId").description("부모 지역 id(서울, 경기, 부산)")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("지역 id"),
                    fieldWithPath("data.[].name").description("지역 이름"),
                    fieldWithPath("data.[].depth").description("깊이 (카테고리 2)"),
                    fieldWithPath("data.[].parent").description("부모 지역")
                )
            )
        );
  }

  @Test
  void getRegionByDepth() throws Exception {
    mockMvc.perform(get("/region/name?depth=2")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("자식지역조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("depth").description("카테고리 깊이")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("카테고리 id"),
                    fieldWithPath("data.[].name").description("카테고리 이름"),
                    fieldWithPath("data.[].depth").description("카테고리 깊이"),
                    fieldWithPath("data.[].parent").description("카테고리 부모")
                )
            )
        );
  }


  @Test
  void getMyRegion() throws Exception {
    mockMvc.perform(get("/region/my-default")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.region.id").value("14"))
        .andExpect(jsonPath("$.data.isDefault").value(true))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("get-my-default-region",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.region.id").description("거주지역 id"),
                    fieldWithPath("data.region.name").description("거주지역 이름"),
                    fieldWithPath("data.region.depth").description("지역 깊이"),
                    fieldWithPath("data.region.parent").description("부모 지역"),
                    fieldWithPath("data.isDefault").description("거주지역이면 true\n관심지역이면 false")
                )
            )
        );
  }


  @Test
  void updateMyDefaultRegion() throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("regionId", 103);

    mockMvc.perform(put("/region/my-default")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.region.id").value("103"))
        .andExpect(jsonPath("$.data.isDefault").value(true))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("update-my-default-region",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("regionId").description("갱신할 거주지역 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.region.id").description("갱신된 거주지역 id"),
                    fieldWithPath("data.region.name").description("갱신된 거주지역 이름"),
                    fieldWithPath("data.region.depth").description("갱신된 지역 깊이"),
                    fieldWithPath("data.region.parent").description("부모 지역"),
                    fieldWithPath("data.isDefault").description("거주지역이면 true\n관심지역이면 false")
                )
            )
        );
  }

  @Test
  void insertMyInterestsTest() throws Exception {
    insertMyInterests(102L);
    insertMyInterests(3L);
  }

  void insertMyInterests(Long regionId) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    HashMap<String, Object> body = new HashMap<>();
    body.put("regionId", regionId);

    HashMap<String, Object> body2 = new HashMap<>();
    body2.put("regionId", 205);

    HashMap<String, Object> body3 = new HashMap<>();
    body3.put("regionId", 110);

    ArrayList<Object> arr = new ArrayList<>() {{
      add(body);
      add(body);
      add(body2);
      add(body3);
    }};

    mockMvc.perform(post("/region/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(arr))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data[0].region.id").value(regionId))
        .andExpect(jsonPath("$.data[0].isDefault").value(false))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("insert-my-interest-region",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("[].regionId").description("추가할 관심지역 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("관심 지역 id"),
                    fieldWithPath("data.[].region.id").description("지역 id"),
                    fieldWithPath("data.[].region.name").description("지역 이름"),
                    fieldWithPath("data.[].region.depth").description("지역 깊이"),
                    fieldWithPath("data.[].region.parent").description("부모 지역"),
                    fieldWithPath("data.[].isDefault").description("거주지역이면 true\n관심지역이면 false")
                )
            )
        );
  }

  @Test
  void updateMyInterestsTest() throws Exception {
    updateMyInterests(8L);
  }

  void updateMyInterests(Long regionId) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    HashMap<String, Object> body = new HashMap<>();
    body.put("regionId", regionId);

    HashMap<String, Object> body2 = new HashMap<>();
    body2.put("regionId", 201);

    HashMap<String, Object> body3 = new HashMap<>();
    body3.put("regionId", 202);

    ArrayList<Object> arr = new ArrayList<>() {{
      add(body);
      add(body);
      add(body2);
      add(body3);
    }};

    mockMvc.perform(put("/region/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(arr))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].isDefault").value(false))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("update-my-interest-region",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("[].regionId").description("추가할 관심지역 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("관심 지역 id"),
                    fieldWithPath("data.[].region.id").description("지역 id"),
                    fieldWithPath("data.[].region.name").description("지역 이름"),
                    fieldWithPath("data.[].region.depth").description("지역 깊이"),
                    fieldWithPath("data.[].region.parent").description("부모 지역"),
                    fieldWithPath("data.[].isDefault").description("거주지역이면 true\n관심지역이면 false")
                )
            )
        );

    mockMvc.perform(get("/region/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(arr))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.length()").value(4))
        .andExpect(jsonPath("$.data[0].isDefault").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()));
  }


//  @Test
  void deleteRegions() throws Exception {

    mockMvc.perform(RestDocumentationRequestBuilders.delete("/region?id=4&id=6")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("관심지역들삭제",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("id").description("삭제할 지역 id들(여러개 가능)")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data").description("삭제 성공 여부")
                )
            )
        );
  }

  @Test
  void getMyRegions() throws Exception {
    insertMyInterests(6L);
    insertMyInterests(202L);

    mockMvc.perform(get("/region/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("get-my-regions",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("관심 지역 id"),
                    fieldWithPath("data.[].region.id").description("지역 id"),
                    fieldWithPath("data.[].region.name").description("지역 이름"),
                    fieldWithPath("data.[].region.depth").description("지역 깊이"),
                    fieldWithPath("data.[].region.parent").description("부모 지역"),
                    fieldWithPath("data.[].isDefault").description("거주지역이면 true\n관심지역이면 false")
                )
            )
        );
  }


}


