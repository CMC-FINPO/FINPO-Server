package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.Gender;
import kr.finpo.api.service.OAuthService;
import kr.finpo.api.service.UserService;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MvcResult;

import java.io.FileInputStream;
import java.util.HashMap;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@DisplayName("Controller - OAuth")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.finpo.kr")
@WithMockUser
@SpringBootTest
class OAuthControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private OAuthService oAuthService;
  @Autowired
  private UserService userService;


  private final String kakaoToken = "";

  @BeforeEach
  void setUp() throws Exception {

  }

  @AfterEach
  void tearDown() {
  }


  @Test
  void loginWithKakaoTokenFail() throws Exception {
    mockMvc.perform(get("/oauth/login/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + kakaoToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("need register"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("kakao-login-fail",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Kakao Access Token")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("로그인 성공 여부\n회원가입 하지 않았다면 [need register]"),
                    fieldWithPath("data.name").description("이름").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.nickname").description("닉네임").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.birth").description("생년월일(YYYY-MM-DD)").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.gender").description("성별\n(MALE, FEMALE, PRIVATE)").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.email").description("메일주소").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.region1").description("지역1").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.region2").description("지역2").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.oAuthType").description("소셜 로그인 타입\nKAKAO/GOOGLE/APPLE").optional().type(JsonFieldType.STRING)
                )
            )
        )
    ;
  }

  @Test
  void loginWithKakaoTokenSuccess() throws Exception {
    registerByKakao();

    mockMvc.perform(get("/oauth/login/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + kakaoToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("Ok"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("kakao-login-success",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Kakao Access Token")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.grantType").description("Authorization Header 타입"),
                    fieldWithPath("data.accessToken").description("Access Token"),
                    fieldWithPath("data.refreshToken").description("Refresh Token"),
                    fieldWithPath("data.accessTokenExpiresIn").description("Access Token 만료시각")
                )
            )
        )
    ;
  }

  @Test
  MvcResult registerByKakao() throws Exception {
    MockMultipartFile image = new MockMultipartFile("profileImgFile", "imagefile.jpeg", "image/jpeg", new FileInputStream(System.getProperty("user.dir") + "/" + "test.png"));

    return mockMvc.perform(RestDocumentationRequestBuilders.fileUpload("/oauth/register/kakao")
            .file(image)
            .param("name", "김명승")
            .param("nickname", "메이슨")
            .param("birth", "1999-01-01")
            .param("gender", Gender.MALE.toString())
            .param("email", "mskim9967@gmail.com")
            .param("region1", "서울")
            .param("region2", "강동")

            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("Authorization", "Bearer " + kakaoToken)
        )

        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.grantType").value("bearer"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("kakao-register",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Kakao Access Token")
                ),
                requestParameters(
                    parameterWithName("name").description("이름")
                    , parameterWithName("nickname").description("닉네임")
                    , parameterWithName("birth").description("생년월일(YYYY-MM-DD)")
                    , parameterWithName("gender").description("성별\n(MALE, FEMALE, PRIVATE)")
                    , parameterWithName("email").description("메일주소")
                    , parameterWithName("region1").description("지역1")
                    , parameterWithName("region2").description("지역2")
                )
                , requestParts(
                    partWithName("profileImgFile").description("프로필 이미지 파일")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.grantType").description("Authorization Header 타입"),
                    fieldWithPath("data.accessToken").description("Access Token"),
                    fieldWithPath("data.refreshToken").description("Refresh Token"),
                    fieldWithPath("data.accessTokenExpiresIn").description("Access Token 만료시각")
                )
            )
        ).andReturn()
        ;
  }



  @Test
  void reissueTokens() throws Exception {

    MvcResult res = registerByKakao();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(res.getResponse().getContentAsString());
    json = (JSONObject) parser.parse(json.get("data").toString());
    String accessToken = json.get("accessToken").toString(), refreshToken = json.get("refreshToken").toString();


    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("accessToken", accessToken);
    body.put("refreshToken", refreshToken);

    mockMvc.perform(post("/oauth/reissue")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("Ok"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("reissue",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                    fieldWithPath("accessToken").description("만료된 Access Token"),
                    fieldWithPath("refreshToken").description("보관하고 있던 Refresh Token")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.grantType").description("Authorization Header 타입"),
                    fieldWithPath("data.accessToken").description("재발급된 Access Token"),
                    fieldWithPath("data.refreshToken").description("재발급된 Refresh Token"),
                    fieldWithPath("data.accessTokenExpiresIn").description("Access Token 만료시각")
                )
            )
        )
    ;


  }
}