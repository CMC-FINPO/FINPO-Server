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
        .andExpect(jsonPath("$.data.name").value("?????????"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("???????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.id").description("?????? id"),
                    fieldWithPath("data.name").description("??????"),
                    fieldWithPath("data.nickname").description("?????????"),
                    fieldWithPath("data.birth").description("????????????"),
                    fieldWithPath("data.gender").description("??????\n[MALE/FEMALE/PRIVATE]"),
//                    fieldWithPath("data.email").description("????????? ??????"),
                    fieldWithPath("data.statusId").description("????????? ???????????? id"),
                    fieldWithPath("data.profileImg").description("?????????????????? url"),
                    fieldWithPath("data.oAuthType").description("??????????????? ??????"),
                    fieldWithPath("data.defaultRegion").description("?????? ??????")
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
            document("???????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("name").description("??????").optional().optional().type(JsonFieldType.STRING),
                    fieldWithPath("nickname").description("?????????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("birth").description("????????????(YYYY-MM-DD)").optional().type(JsonFieldType.STRING),
                    fieldWithPath("gender").description("??????\n[MALE/FEMALE/PRIVATE]").optional().type(JsonFieldType.STRING),
//                    fieldWithPath("email").description("????????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("regionId").description("?????? ?????? id").optional().type(JsonFieldType.NUMBER),
                    fieldWithPath("region2").description("?????? ?????? ??????").optional().type(JsonFieldType.STRING),
                    fieldWithPath("statusId").description("????????? ???????????? id").optional().type(JsonFieldType.STRING)
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.id").description("?????? id"),
                    fieldWithPath("data.name").description("????????? ??????"),
                    fieldWithPath("data.nickname").description("????????? ?????????"),
                    fieldWithPath("data.birth").description("????????? ????????????"),
                    fieldWithPath("data.gender").description("????????? ??????\n[MALE/FEMALE/PRIVATE]"),
//                    fieldWithPath("data.email").description("????????? ????????? ??????"),
                    fieldWithPath("data.statusId").description("????????? ???????????? id"),
                    fieldWithPath("data.profileImg").description("?????????????????? url"),
                    fieldWithPath("data.oAuthType").description("??????????????? ??????"),
                    fieldWithPath("data.defaultRegion").description("????????? ?????? ??????")
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
            document("??????????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParts(
                    partWithName("profileImgFile").description("????????? ????????? ????????? ??????")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.id").description("?????? id"),
                    fieldWithPath("data.name").description("??????"),
                    fieldWithPath("data.nickname").description("?????????"),
                    fieldWithPath("data.birth").description("????????????"),
                    fieldWithPath("data.gender").description("??????\n[MALE/FEMALE/PRIVATE]"),
//                    fieldWithPath("data.email").description("????????? ??????"),
                    fieldWithPath("data.statusId").description("?????? ?????? id"),
                    fieldWithPath("data.profileImg").description("????????? ?????????????????? url"),
                    fieldWithPath("data.oAuthType").description("??????????????? ??????"),
                    fieldWithPath("data.defaultRegion").description("????????? ?????? ??????")
                )
            )
        )
    ;
  }

  @Test
  void checkNicknameDuplicate() throws Exception {
    mockMvc.perform(get("/user/check-duplicate?nickname=?????????").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").value(true))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("nickname").description("?????? ?????? ??? ?????????")
                ),
                responseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data").description("???????????? true ??????????????? false")
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
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("email").description("?????? ?????? ??? ?????????")
                ),
                responseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data").description("???????????? true ??????????????? false")
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
            document("????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("access_token").description("?????? access token").optional().type(JsonFieldType.STRING),
                    fieldWithPath("code").description("?????? authorization code").optional().type(JsonFieldType.STRING)
                ),
                responseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data").description("?????? ?????? ??? true")
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
            document("????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                responseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("???????????? id"),
                    fieldWithPath("data.[].name").description("????????????")
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
            document("????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                responseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.[].id").description("???????????? id"),
                    fieldWithPath("data.[].name").description("????????????")
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
            document("??????????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("statusId").description("????????? ???????????? id").optional(),
                    fieldWithPath("purposeIds").description("????????? ???????????? ids").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????"),
                    fieldWithPath("data.id").description("?????? id"),
                    fieldWithPath("data.name").description("????????? ??????"),
                    fieldWithPath("data.nickname").description("????????? ?????????"),
                    fieldWithPath("data.birth").description("????????? ????????????"),
                    fieldWithPath("data.gender").description("????????? ??????\n[MALE/FEMALE/PRIVATE]"),
//                    fieldWithPath("data.email").description("????????? ????????? ??????"),
                    fieldWithPath("data.statusId").description("????????? ???????????? id"),
                    fieldWithPath("data.profileImg").description("?????????????????? url"),
                    fieldWithPath("data.oAuthType").description("??????????????? ??????"),
                    fieldWithPath("data.defaultRegion").description("????????? ?????? ??????")
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
                    fieldWithPath("data.[]").description("?????? ?????? ?????? id")
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
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????"),
                    fieldWithPath("errorCode").description("?????? ??????"),
                    fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.[].id").description("?????? id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.[].releaseDate").description("?????? ?????????").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.[].detail").description("?????? ??????").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.[].report.reason").description("?????? ??????").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("data.[].createdAt").description("?????????").optional().type(JsonFieldType.STRING)
                )
            )
        );
  }
}