package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.repository.BannedUserRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@DisplayName("Controller - User")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.finpo.kr", uriPort = 443)
@Transactional
@SpringBootTest
public
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserService userService;
  @Autowired
  private BannedUserRepository bannedUserRepository;

  String accessToken, refreshToken;

  @BeforeEach
  void setUp() throws Exception {
    OAuthControllerTest oc = new OAuthControllerTest();
    HashMap<String, String> map = oc.registerAndGetToken(mockMvc);
    accessToken = map.get("accessToken");
    refreshToken = map.get("refreshToken");
  }

  public void set(MockMvc mockMvc, String accessToken) {
    this.mockMvc = mockMvc;
    this.accessToken = accessToken;
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void getMyInfo() throws Exception {
    mockMvc.perform(get("/user/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.name").value("김명승"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내정보조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("유저 id"),
                    fieldWithPath("data.name").description("이름"),
                    fieldWithPath("data.nickname").description("닉네임"),
                    fieldWithPath("data.birth").description("생년월일"),
                    fieldWithPath("data.gender").description("성별\n[MALE/FEMALE/PRIVATE]"),
//                    fieldWithPath("data.email").description("이메일 주소"),
                    fieldWithPath("data.statusId").description("갱신된 유저상태 id"),
                    fieldWithPath("data.profileImg").description("프로필이미지 url"),
                    fieldWithPath("data.oAuthType").description("소셜로그인 타입"),
                    fieldWithPath("data.defaultRegion").description("거주 지역")
                )
            )
        );
  }

  @Test
  void updateMe() throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("nickname", "mason");
    body.put("birth", "1999-12-12");
    body.put("regionId", 3);

    mockMvc.perform(put("/user/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.nickname").value("mason"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내정보변경",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("name").description("이름").optional().optional().type(JsonFieldType.STRING),
                    fieldWithPath("nickname").description("닉네임").optional().type(JsonFieldType.STRING),
                    fieldWithPath("birth").description("생년월일(YYYY-MM-DD)").optional().type(JsonFieldType.STRING),
                    fieldWithPath("gender").description("성별\n[MALE/FEMALE/PRIVATE]").optional().type(JsonFieldType.STRING),
//                    fieldWithPath("email").description("이메일 주소").optional().type(JsonFieldType.STRING),
                    fieldWithPath("regionId").description("거주 지역 id").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("region2").description("세부 거주 지역").optional().type(JsonFieldType.STRING),
                    fieldWithPath("statusId").description("갱신할 유저상태 id").optional().type(JsonFieldType.STRING)
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("유저 id"),
                    fieldWithPath("data.name").description("갱신된 이름"),
                    fieldWithPath("data.nickname").description("갱신된 닉네임"),
                    fieldWithPath("data.birth").description("갱신된 생년월일"),
                    fieldWithPath("data.gender").description("갱신된 성별\n[MALE/FEMALE/PRIVATE]"),
//                    fieldWithPath("data.email").description("갱신된 이메일 주소"),
                    fieldWithPath("data.statusId").description("갱신된 유저상태 id"),
                    fieldWithPath("data.profileImg").description("프로필이미지 url"),
                    fieldWithPath("data.oAuthType").description("소셜로그인 타입"),
                    fieldWithPath("data.defaultRegion").description("갱신된 거주 지역")
                )
            )
        );
  }

  @Test
  void updateMyProfileimg() throws Exception {
    MockMultipartFile image = new MockMultipartFile("profileImgFile", "imagefile.jpeg", "image/jpeg", new FileInputStream(System.getProperty("user.dir") + "/" + "test.png"));

    mockMvc.perform(RestDocumentationRequestBuilders.fileUpload("/user/me/profile-img")
            .file(image)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("Authorization", "Bearer " + accessToken)
        )

        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("프로필이미지업데이트",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParts(
                    partWithName("profileImgFile").description("갱신할 프로필 이미지 파일")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("유저 id"),
                    fieldWithPath("data.name").description("이름"),
                    fieldWithPath("data.nickname").description("닉네임"),
                    fieldWithPath("data.birth").description("생년월일"),
                    fieldWithPath("data.gender").description("성별\n[MALE/FEMALE/PRIVATE]"),
//                    fieldWithPath("data.email").description("이메일 주소"),
                    fieldWithPath("data.statusId").description("현재 상태 id"),
                    fieldWithPath("data.profileImg").description("갱신된 프로필이미지 url"),
                    fieldWithPath("data.oAuthType").description("소셜로그인 타입"),
                    fieldWithPath("data.defaultRegion").description("갱신된 거주 지역")
                )
            )
        )
    ;
  }

  @Test
  void checkNicknameDuplicate() throws Exception {
    mockMvc.perform(get("/user/check-duplicate?nickname=메이슨").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").value(true))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("닉네임중복체크",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("nickname").description("중복 체크 할 닉네임")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data").description("중복이면 true 중복아니면 false")
                )
            )
        );
  }

  //  @Test
  void checkEmailDuplicate() throws Exception {
    mockMvc.perform(get("/user/check-duplicate?email=mskim9967@gmail.com").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").value(true))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("이메일중복체크",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("email").description("중복 체크 할 이메일")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data").description("중복이면 true 중복아니면 false")
                )
            )
        );
  }

  @Test
  void deleteMeTest() throws  Exception {
    deleteMe(accessToken);
  }

  void deleteMe(String accessToken) throws Exception {
    getMyInfo();

    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("access_token", "eydwefaw32....");


    mockMvc.perform(delete("/user/me").
            contentType(MediaType.APPLICATION_JSON).
            header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").value(true))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("회원탈퇴",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("access_token").description("구글 access token").optional().type(JsonFieldType.STRING),
                    fieldWithPath("code").description("애플 authorization code").optional().type(JsonFieldType.STRING)
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data").description("탈퇴 성공 시 true")
                )
            )
        );
  }

  @Test
  void getUserStatusName() throws Exception {
    mockMvc.perform(get("/user/status/name")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("유저상태목록조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("유저상태 id"),
                    fieldWithPath("data.[].name").description("유저상태")
                )
            )
        );
  }

  @Test
  void getUserPurposeName() throws Exception {
    mockMvc.perform(get("/user/purpose/name")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("이용목적목록조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[].id").description("유저상태 id"),
                    fieldWithPath("data.[].name").description("유저상태")
                )
            )
        );
  }

  @Test
  void setMyStatusAndPurpose() throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("statusId", "6");
    body.put("purposeIds", Arrays.asList(1, 4, 5));

    mockMvc.perform(put("/user/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.statusId").value("6"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("현재상태이용목적추가",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("statusId").description("갱신할 유저상태 id").optional(),
                    fieldWithPath("purposeIds").description("갱신할 유저목적 ids").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.id").description("유저 id"),
                    fieldWithPath("data.name").description("갱신된 이름"),
                    fieldWithPath("data.nickname").description("갱신된 닉네임"),
                    fieldWithPath("data.birth").description("갱신된 생년월일"),
                    fieldWithPath("data.gender").description("갱신된 성별\n[MALE/FEMALE/PRIVATE]"),
//                    fieldWithPath("data.email").description("갱신된 이메일 주소"),
                    fieldWithPath("data.statusId").description("갱신될 유저상태 id"),
                    fieldWithPath("data.profileImg").description("프로필이미지 url"),
                    fieldWithPath("data.oAuthType").description("소셜로그인 타입"),
                    fieldWithPath("data.defaultRegion").description("갱신된 거주 지역")
                )
            )
        );
  }

  @Test
  void getMyPurpose() throws Exception {
    setMyStatusAndPurpose();

    mockMvc.perform(get("/user/me/purpose")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.size()").value("3"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내이용목적조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[]").description("유저 이용 목적 id")
                )
            )
        );
  }

//  @Test
  void getMyBanned() throws Exception {
    setMyStatusAndPurpose();

    mockMvc.perform(get("/user/banned/me")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("내정지내역조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.[].id").description("정지 id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.[].releaseDate").description("정지 해제일").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.[].detail").description("세부 사유").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.[].report.reason").description("정지 사유").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.[].createdAt").description("정지일").optional().type(JsonFieldType.STRING)
                )
            )
        );
  }
}