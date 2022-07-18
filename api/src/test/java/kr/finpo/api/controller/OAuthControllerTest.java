package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.Gender;
import kr.finpo.api.service.OAuthService;
import kr.finpo.api.service.UserService;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.*;
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

import javax.transaction.Transactional;
import java.io.FileInputStream;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Disabled
@DisplayName("Controller - OAuth")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.finpo.kr", uriPort = 443)
@WithMockUser
@Transactional
@SpringBootTest
class OAuthControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private OAuthService oAuthService;
  @Autowired
  private UserService userService;

  String kakaoToken = "",
      googleToken = "",
      appleToken = "";


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
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("need register"))
        .andExpect(jsonPath("$.success").value(true))
        .andDo(
            document("kakao-login-fail",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Kakao Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("로그인 성공 여부\n회원가입 하지 않았다면 [need register]"),
                    fieldWithPath("data.name").description("이름").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.nickname").description("닉네임").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.birth").description("생년월일(YYYY-MM-DD)").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.gender").description("성별\n(MALE, FEMALE, PRIVATE)").optional().type(JsonFieldType.STRING)
//                    , fieldWithPath("data.email").description("메일주소").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.status").description("현재 상태").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.oAuthType").description("소셜 로그인 타입\nKAKAO/GOOGLE/APPLE").optional().type(JsonFieldType.STRING)
                )
            )
        )
    ;
  }

  @Test
  void loginWithKakaoTokenSuccess() throws Exception {
    registerByKakaoTest();

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
  void loginWithGoogleTokenFail() throws Exception {
    mockMvc.perform(get("/oauth/login/google")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + googleToken)
        )
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("need register"))
        .andExpect(jsonPath("$.success").value(true))
        .andDo(
            document("구글로그인실패",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Google Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("로그인 성공 여부\n회원가입 하지 않았다면 [need register]"),
                    fieldWithPath("data.name").description("이름").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.nickname").description("닉네임").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.birth").description("생년월일(YYYY-MM-DD)").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.gender").description("성별\n(MALE, FEMALE, PRIVATE)").optional().type(JsonFieldType.STRING)
//                    , fieldWithPath("data.email").description("메일주소").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.profileImg").description("프로필 이미지 url").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.oAuthType").description("소셜 로그인 타입\nKAKAO/GOOGLE/APPLE").optional().type(JsonFieldType.STRING)
                )
            )
        )
    ;
  }

  @Test
  void loginWithGoogleTokenSuccess() throws Exception {
    registerByGoogleTest();

    mockMvc.perform(get("/oauth/login/google")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + googleToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("Ok"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("구글로그인성공",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Google Access Token")
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
  void loginWithAppleTokenFail() throws Exception {
    mockMvc.perform(get("/oauth/login/apple")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + appleToken)
        )
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("need register"))
        .andExpect(jsonPath("$.success").value(true))
        .andDo(
            document("애플로그인실패",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Apple Identity Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("로그인 성공 여부\n회원가입 하지 않았다면 [need register]"),
                    fieldWithPath("data.oAuthType").description("소셜 로그인 타입\nKAKAO/GOOGLE/APPLE").optional().type(JsonFieldType.STRING)
                )
            )
        )
    ;
  }

  @Test
  void loginWithAppleTokenSuccess() throws Exception {
    registerByAppleTest();

    mockMvc.perform(get("/oauth/login/apple")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + appleToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("Ok"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("애플로그인성공",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Apple Identity Token")
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
  void registerByAppleTest() throws Exception {
    MockMultipartFile image = new MockMultipartFile("profileImgFile", "imagefile.jpeg", "image/jpeg", new FileInputStream(System.getProperty("user.dir") + "/" + "test.png"));

    ObjectMapper objectMapper = new ObjectMapper();
    HashMap<String, Object> body = new HashMap<>() {{
      put("categoryId", 1);
    }};

    HashMap<String, Object> body2 = new HashMap<>() {{
      put("categoryId", 3);
    }};

    ArrayList<Object> arr = new ArrayList<>() {{
      add(body);
      add(body2);
    }};

    mockMvc.perform(RestDocumentationRequestBuilders.fileUpload("/oauth/register/apple")
                .file(image)
                .param("name", "김명승")
                .param("nickname", "mason")
                .param("birth", "1999-01-01")
                .param("gender", Gender.MALE.toString())
//            .param("email", "mskim9967@naver.com")
                .param("regionId", "8")
                .param("categories", objectMapper.writeValueAsString(arr))
                .param("profileImg", "https://lh3.googleusercontent.com/a-/AOh14GgQFwmk2DXogeGilkeY_X1TJAk4gtYcHiHMI68Y=s100")

                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Bearer " + appleToken)
        )

        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.grantType").value("bearer"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("애플회원가입",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Apple Identity Token")
                ),
                requestParameters(
                    parameterWithName("name").description("이름")
                    , parameterWithName("nickname").description("닉네임")
                    , parameterWithName("birth").description("생년월일(YYYY-MM-DD)")
                    , parameterWithName("gender").description("성별\n(MALE, FEMALE, PRIVATE)")
//                    , parameterWithName("email").description("메일주소")
                    , parameterWithName("regionId").description("지역id")
                    , parameterWithName("categories").description("카테고리 id들")

                    , parameterWithName("profileImg").description("프로필 이미지 url").optional()
                )
                , requestParts(
                    partWithName("profileImgFile").description("프로필 이미지 파일").optional()
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
  void registerByKakaoTest() throws Exception {
    MockMultipartFile image = new MockMultipartFile("profileImgFile", "imagefile.jpeg", "image/jpeg", new FileInputStream(System.getProperty("user.dir") + "/" + "test.png"));

    ObjectMapper objectMapper = new ObjectMapper();
    HashMap<String, Object> body = new HashMap<>() {{
      put("categoryId", 1);
    }};

    HashMap<String, Object> body2 = new HashMap<>() {{
      put("categoryId", 3);
    }};

    ArrayList<Object> arr = new ArrayList<>() {{
      add(body);
      add(body2);
    }};

    MvcResult res = mockMvc.perform(RestDocumentationRequestBuilders.fileUpload("/oauth/register/kakao")
                .file(image)
                .param("name", "김명승")
                .param("nickname", "메이슨")
                .param("birth", "1999-01-01")
                .param("gender", Gender.MALE.toString())
//            .param("email", "mskim9967@gmail.com")
                .param("regionId", "14")
                .param("categories", objectMapper.writeValueAsString(arr))
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
//                    , parameterWithName("email").description("메일주소")
                    , parameterWithName("regionId").description("지역id")
                    , parameterWithName("categories").description("카테고리 id들")
                    , parameterWithName("profileImg").description("프로필 이미지 url").optional()
                )
                , requestParts(
                    partWithName("profileImgFile").description("프로필 이미지 파일").optional()
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
        .andReturn();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(res.getResponse().getContentAsString());
    json = (JSONObject) parser.parse(json.get("data").toString());
    String accessToken = json.get("accessToken").toString();

    mockMvc.perform(get("/policy/category/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.size()").value(5))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()));
  }

  @Test
  void registerByGoogleTest() throws Exception {
    MockMultipartFile image = new MockMultipartFile("profileImgFile", "imagefile.jpeg", "image/jpeg", new FileInputStream(System.getProperty("user.dir") + "/" + "test.png"));

    ObjectMapper objectMapper = new ObjectMapper();
    HashMap<String, Object> body = new HashMap<>() {{
      put("categoryId", 1);
    }};

    HashMap<String, Object> body2 = new HashMap<>() {{
      put("categoryId", 3);
    }};

    ArrayList<Object> arr = new ArrayList<>() {{
      add(body);
      add(body2);
    }};

    mockMvc.perform(RestDocumentationRequestBuilders.fileUpload("/oauth/register/google")
                .file(image)
                .param("name", "김명승")
                .param("nickname", "mason")
                .param("birth", "1999-01-01")
                .param("gender", Gender.MALE.toString())
//            .param("email", "mskim9967@naver.com")
                .param("regionId", "8")
                .param("categories", objectMapper.writeValueAsString(arr))
                .param("profileImg", "https://lh3.googleusercontent.com/a-/AOh14GgQFwmk2DXogeGilkeY_X1TJAk4gtYcHiHMI68Y=s100")

                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Bearer " + googleToken)
        )

        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.grantType").value("bearer"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("구글회원가입",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Google Access Token")
                ),
                requestParameters(
                    parameterWithName("name").description("이름")
                    , parameterWithName("nickname").description("닉네임")
                    , parameterWithName("birth").description("생년월일(YYYY-MM-DD)")
                    , parameterWithName("gender").description("성별\n(MALE, FEMALE, PRIVATE)")
//                    , parameterWithName("email").description("메일주소")
                    , parameterWithName("regionId").description("지역id")
                    , parameterWithName("categories").description("카테고리 id들")

                    , parameterWithName("profileImg").description("프로필 이미지 url").optional()
                )
                , requestParts(
                    partWithName("profileImgFile").description("프로필 이미지 파일").optional()
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
  void reissueTokens() throws Exception {
    HashMap<String, String> map = registerAndGetToken(mockMvc);
    String accessToken = map.get("accessToken"), refreshToken = map.get("refreshToken");

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

  public HashMap<String, String> registerAndGetToken(MockMvc mockMvc) throws Exception {
    return registerAndGetToken(mockMvc, "메이슨");
  }
  public HashMap<String, String> registerAndGetToken(MockMvc mockMvc, String nickname) throws Exception {
    MvcResult res = mockMvc.perform(RestDocumentationRequestBuilders.fileUpload("/oauth/register/test")
                .param("name", "김명승")
                .param("nickname", nickname)
                .param("birth", "1999-01-01")
                .param("gender", Gender.MALE.toString())
//            .param("email", "ksksksk@gmail.com")
                .param("profileImg", "https://dev.finpo.kr/upload/profile/1855b430-856d-4e2f-b8f0-554b66608cff.png")
                .param("regionId", "14")
                .param("statusId", "5")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Bearer " + kakaoToken)
        )

        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.grantType").value("bearer"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andReturn();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(res.getResponse().getContentAsString());
    json = (JSONObject) parser.parse(json.get("data").toString());
    String accessToken = json.get("accessToken").toString(), refreshToken = json.get("refreshToken").toString();
    return new HashMap<String, String>() {{
      put("accessToken", accessToken);
      put("refreshToken", refreshToken);
    }};
  }
}