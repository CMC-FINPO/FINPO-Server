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
            document("1?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("???????????? id"),
                    fieldWithPath("data.[].name").description("???????????? ??????"),
                    fieldWithPath("data.[].img").description("???????????? ?????????"),
                    fieldWithPath("data.[].depth").description("???????????? ??????")
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
            document("2?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("depth").description("???????????? ?????? (2??? ??????????????? 2)")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("???????????? id"),
                    fieldWithPath("data.[].name").description("???????????? ??????"),
                    fieldWithPath("data.[].depth").description("???????????? ??????"),
                    fieldWithPath("data.[].parent").description("???????????? ??????")
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
            document("child????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("???????????? id"),
                    fieldWithPath("data.[].name").description("???????????? ??????"),
                    fieldWithPath("data.[].depth").description("???????????? ??????"),
                    fieldWithPath("data.[].childs").description("?????? ???????????????")
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
            document("????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("parentId").description("?????? ???????????? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("???????????? id"),
                    fieldWithPath("data.[].name").description("???????????? ??????"),
                    fieldWithPath("data.[].depth").description("???????????? ??????"),
                    fieldWithPath("data.[].parent").description("???????????? ??????")
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
        .andExpect(jsonPath("$.data[0].category.name").value("??????"))
        .andExpect(jsonPath("$.data[1].category.id").value(6))
        .andExpect(jsonPath("$.data[1].category.name").value("??????"))
        .andExpect(jsonPath("$.data[2].category.id").value(11))
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
                    fieldWithPath("data.[].id").description("?????? ???????????? id (???????????? id??? ??????"),
                    fieldWithPath("data.[].category").description("???????????? ??????"),
                    fieldWithPath("data.[].category.id").description("???????????? id"),
                    fieldWithPath("data.[].category.name").description("???????????? ??????"),
                    fieldWithPath("data.[].category.depth").description("???????????? ??????"),
                    fieldWithPath("data.[].category.parent").description("???????????? ??????"),
                    fieldWithPath("data.[].subscribe").description("???????????? ?????? ?????? ??????")
                )
            )
        );
  }

  @Test
  void getMyParentInterestCategories() throws Exception{
    insertMyInterestCategories();
    mockMvc.perform(get("/policy/category/me/parent")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.size()").value(2))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.data[0].name").value("?????????"))
        .andExpect(jsonPath("$.data[1].id").value(3))
        .andExpect(jsonPath("$.data[1].name").value("????????????"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("???????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("???????????? id"),
                    fieldWithPath("data.[].name").description("???????????? ??????")
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
            document("???????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("id").description("????????? ?????? ???????????? id???(????????? ??????)")
                ),
                responseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data").description("?????? ?????? ??????")
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
        .andExpect(jsonPath("$.data[0].category.name").value("??????"))
        .andExpect(jsonPath("$.data[1].category.id").value(6))
        .andExpect(jsonPath("$.data[1].category.name").value("??????"))
        .andExpect(jsonPath("$.data[2].category.id").value(11))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("???????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("[].categoryId").description("????????? ?????? ???????????? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("?????? ???????????? id (???????????? id??? ??????"),
                    fieldWithPath("data.[].category").description("???????????? ??????"),
                    fieldWithPath("data.[].category.id").description("???????????? id"),
                    fieldWithPath("data.[].category.name").description("???????????? ??????"),
                    fieldWithPath("data.[].category.depth").description("???????????? ??????"),
                    fieldWithPath("data.[].category.parent").description("???????????? ??????"),
                    fieldWithPath("data.[].subscribe").description("???????????? ?????? ?????? ??????")
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
        .andExpect(jsonPath("$.data[1].category.name").value("??????"))
        .andExpect(jsonPath("$.data[2].category.id").value(11))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("???????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("[].categoryId").description("????????? ?????? ???????????? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("?????? ???????????? id (???????????? id??? ??????"),
                    fieldWithPath("data.[].category").description("???????????? ??????"),
                    fieldWithPath("data.[].category.id").description("???????????? id"),
                    fieldWithPath("data.[].category.name").description("???????????? ??????"),
                    fieldWithPath("data.[].category.depth").description("???????????? ??????"),
                    fieldWithPath("data.[].subscribe").description("???????????? ?????? ?????? ??????")
//                    fieldWithPath("data.[].category.parent").description("???????????? ??????")
                )
            )
        );
  }

}