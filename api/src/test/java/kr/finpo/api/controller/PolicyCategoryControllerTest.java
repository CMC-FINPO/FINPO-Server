package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.repository.CategoryRepository;
import kr.finpo.api.repository.InterestCategoryRepository;
import kr.finpo.api.service.UserService;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.json.JSONArray;
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
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@DisplayName("Controller - PolicyCategory")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.finpo.kr", uriPort = 443)
@WithMockUser
@Transactional
@SpringBootTest
public
class PolicyCategoryControllerTest {

  @Autowired
  private MockMvc mockMvc;

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

  @Test
  void get1Categories() throws Exception{
    mockMvc.perform(get("/policy/category/name")
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("1차카테고리조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("카테고리 id"),
                    fieldWithPath("data.[].name").description("카테고리 이름"),
                    fieldWithPath("data.[].img").description("카테고리 이미지"),
                    fieldWithPath("data.[].depth").description("카테고리 깊이")
                )
            )
        );
  }

  @Test
  void get2Categories() throws Exception{
    mockMvc.perform(get("/policy/category/name?depth=2")
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("2차카테고리조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("depth").description("카테고리 깊이 (2차 카테고리면 2)")
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
  void getAllByChildFormat() throws Exception{
    mockMvc.perform(get("/policy/category/name/child-format")
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("child형식카테고리조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("카테고리 id"),
                    fieldWithPath("data.[].name").description("카테고리 이름"),
                    fieldWithPath("data.[].depth").description("카테고리 깊이"),
                    fieldWithPath("data.[].childs").description("자식 카테고리들")
                )
            )
        );
  }

  @Test
  void getCategoriesByDepth() throws Exception{
    mockMvc.perform(get("/policy/category/name?parentId=4")
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("자식카테고리조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("parentId").description("부모 카테고리 id")
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
  void getMyInterestCategories() throws Exception{
    insertMyInterestCategories();
    mockMvc.perform(get("/policy/category/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.size()").value(3))
        .andExpect(jsonPath("$.data[0].category.id").value(5))
        .andExpect(jsonPath("$.data[0].category.name").value("진로"))
        .andExpect(jsonPath("$.data[1].category.id").value(6))
        .andExpect(jsonPath("$.data[1].category.name").value("취업"))
        .andExpect(jsonPath("$.data[2].category.id").value(11))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내관심카테고리",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("관심 카테고리 id (카테고리 id와 다름"),
                    fieldWithPath("data.[].category").description("카테고리 정보"),
                    fieldWithPath("data.[].category.id").description("카테고리 id"),
                    fieldWithPath("data.[].category.name").description("카테고리 이름"),
                    fieldWithPath("data.[].category.depth").description("카테고리 깊이"),
                    fieldWithPath("data.[].category.parent").description("카테고리 부모"),
                    fieldWithPath("data.[].subscribe").description("카테고리 알림 구독 여부")
                )
            )
        );
  }

    // @Test
  void deleteMyInterestRegion() throws Exception {
    insertMyInterestCategories();

    mockMvc.perform(RestDocumentationRequestBuilders.delete("/policy/category?id=758,759", 4)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내관심카테고리삭제",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("id").description("삭제할 관심 카테고리 id들(여러개 가능)")
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
  void insertMyInterestCategoriesTest() throws Exception{
    insertMyInterestCategories();
  }

  List<InterestCategoryDto> insertMyInterestCategories() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    HashMap<String, Object> body = new HashMap<>(){{
      put("categoryId", 5);
    }};

    HashMap<String, Object> body2 = new HashMap<>(){{
      put("categoryId",6);
    }};

    HashMap<String, Object> body3 = new HashMap<>(){{
      put("categoryId", 11);
    }};

    ArrayList<Object> arr = new ArrayList<>(){{
      add(body);
      add(body2);
      add(body2);
      add(body3);
    }};

    MvcResult res = mockMvc.perform(post("/policy/category/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(arr))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.size()").value(3))
        .andExpect(jsonPath("$.data[0].category.id").value(5))
        .andExpect(jsonPath("$.data[0].category.name").value("진로"))
        .andExpect(jsonPath("$.data[1].category.id").value(6))
        .andExpect(jsonPath("$.data[1].category.name").value("취업"))
        .andExpect(jsonPath("$.data[2].category.id").value(11))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내관심카테고리추가",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("[].categoryId").description("추가할 관심 카테고리 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("관심 카테고리 id (카테고리 id와 다름"),
                    fieldWithPath("data.[].category").description("카테고리 정보"),
                    fieldWithPath("data.[].category.id").description("카테고리 id"),
                    fieldWithPath("data.[].category.name").description("카테고리 이름"),
                    fieldWithPath("data.[].category.depth").description("카테고리 깊이"),
                    fieldWithPath("data.[].category.parent").description("카테고리 부모"),
                    fieldWithPath("data.[].subscribe").description("카테고리 알림 구독 여부")
                )
            )
        ).andReturn();


    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(res.getResponse().getContentAsString());
    return Arrays.asList(new ObjectMapper().readValue(json.get("data").toString(), InterestCategoryDto[].class));

  }

  @Test
  void updateMyInterestCategoriesTest() throws Exception{
    updateMyInterestCategories(10L);
    updateMyInterestCategories(5L);
  }

  void updateMyInterestCategories(Long id) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    HashMap<String, Object> body = new HashMap<>(){{
      put("categoryId", id);
    }};

    HashMap<String, Object> body2 = new HashMap<>(){{
      put("categoryId",6);
    }};

    HashMap<String, Object> body3 = new HashMap<>(){{
      put("categoryId", 11);
    }};

    ArrayList<Object> arr = new ArrayList<>(){{
      add(body);
      add(body2);
      add(body2);
      add(body3);
    }};

    mockMvc.perform(put("/policy/category/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(arr))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.size()").value(3))
        .andExpect(jsonPath("$.data[0].category.id").value(id))
        .andExpect(jsonPath("$.data[1].category.id").value(6))
        .andExpect(jsonPath("$.data[1].category.name").value("취업"))
        .andExpect(jsonPath("$.data[2].category.id").value(11))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내관심카테고리수정",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("[].categoryId").description("추가할 관심 카테고리 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("관심 카테고리 id (카테고리 id와 다름"),
                    fieldWithPath("data.[].category").description("카테고리 정보"),
                    fieldWithPath("data.[].category.id").description("카테고리 id"),
                    fieldWithPath("data.[].category.name").description("카테고리 이름"),
                    fieldWithPath("data.[].category.depth").description("카테고리 깊이"),
                    fieldWithPath("data.[].subscribe").description("카테고리 알림 구독 여부")
//                    fieldWithPath("data.[].category.parent").description("카테고리 부모")
                )
            )
        );
  }

}