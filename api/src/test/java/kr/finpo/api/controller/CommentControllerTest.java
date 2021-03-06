package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.dto.BlockedUserDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.repository.*;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Controller - Comment")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.finpo.kr", uriPort = 443)
@WithMockUser
@Transactional
@SpringBootTest
public
class CommentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private CommunityReportRepository communityReportRepository;

  @Autowired
  private BlockedUserRepository blockedUserRepository;


  String accessToken, refreshToken, otherAccessToken, anotherAccessToken, aac;

  PostControllerTest pc = new PostControllerTest();
  UserControllerTest uc = new UserControllerTest();

  public void set(MockMvc mockMvc, String accessToken) {
    this.mockMvc = mockMvc;
    this.accessToken = accessToken;
  }

  @BeforeEach
  void setUp() throws Exception {
    OAuthControllerTest oc = new OAuthControllerTest();
    HashMap<String, String> map = oc.registerAndGetToken(mockMvc);
    accessToken = map.get("accessToken");
    refreshToken = map.get("refreshToken");

    pc.set(mockMvc, accessToken);
    uc.set(mockMvc, accessToken);

    map = oc.registerAndGetToken(mockMvc, "leeekim");
    otherAccessToken = map.get("accessToken");

    map = oc.registerAndGetToken(mockMvc, "wwwwwlllll");
    anotherAccessToken = map.get("accessToken");

    map = oc.registerAndGetToken(mockMvc, "???????????????");
    aac = map.get("accessToken");

    NotificationControllerTest nc = new NotificationControllerTest();
    nc.set(mockMvc, accessToken);
    nc.updateMy(accessToken);
    nc.updateMy(otherAccessToken);
    nc.updateMy(anotherAccessToken);
  }


  @Test
  void getByPostId() throws Exception {
    int postId = pc.insertPost(accessToken);
    insert(postId, "1??????", anotherAccessToken, false);
    insert(postId, "2??????", anotherAccessToken, false);
    insertAnonymity(postId, "3??????", anotherAccessToken, false);
    int blockedId3 = insert(postId, "4??????", aac, false);
    insert(postId, "5??????", anotherAccessToken, false);
    int blockId2 = insert(postId, "6??????", otherAccessToken, false);
    int parentId = insertAnonymity(postId, "???????????? ?????? ?????????", otherAccessToken, false);
    insert(postId, "?????????????????????", accessToken, false);
    int blockId = insertAnonymity(postId, "???????????? ??? ????????? ??? ??? ?????????", otherAccessToken, false);
    insert(postId, "?????????????????????", anotherAccessToken, false);
    insertAnonymity(postId, "??????????????? ???????????? ?????? ?????????", parentId, otherAccessToken, false);
    int deleteId = insertAnonymity(postId, "fsdfsd", parentId, anotherAccessToken, false);
    insertAnonymity(postId, "??????????????? ?????????", parentId, anotherAccessToken, false);
    int updateId = insert(postId, "????????????????????? ????????? ?????????", parentId, accessToken, false);
    delete(parentId, otherAccessToken, false);
    delete(deleteId, anotherAccessToken, false);
    update(updateId, "??????????????? ?????? ????????? ????????? ???????????????", accessToken, false);
    uc.deleteMe(anotherAccessToken);
    blockUser(blockedId3, accessToken);
    getByPostId(postId, accessToken);
  }

  void getByPostId(int postId, String accessToken) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.get("/post/{id}/comment?page=1&size=5&sort=id,asc", postId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("???????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("??? id")
                ),
                requestParameters(
                    parameterWithName("page").description("????????? ?????? (0?????? ??????)").optional(),
                    parameterWithName("size").description("??? ???????????? ????????? ??????").optional(),
                    parameterWithName("sort").description("?????? ??????\n id asc:?????????(createdAt ?????? id ????????????) ????????????").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.content.[].status").description("false??? ????????? ??????")
                    , fieldWithPath("data.content.[].id").description("?????? id")
                    , fieldWithPath("data.content.[].parent.id").description("?????? ?????? id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.content.[].content").description("?????? ??????").optional()
                    , fieldWithPath("data.content.[].anonymity").description("?????? ??????").optional()
                    , fieldWithPath("data.content.[].anonymityId").description("?????? id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.content.[].user").description("?????? ?????? ??????(?????? ??? ???)").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.content.[].isUserWithdraw").description("?????? ?????? ?????? ?????? ??????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].isWriter").description("?????? ???????????? ??? ???????????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].isMine").description("?????? ???????????? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].createdAt").description("?????????").optional()
                    , fieldWithPath("data.content.[].modifiedAt").description("?????????").optional()

                    , fieldWithPath("data.last").description("????????? ????????? ???????????????")
                    , fieldWithPath("data.first").description("????????? ??? ???????????????")
                    , fieldWithPath("data.totalElements").description("?????? ????????? ???")
                    , fieldWithPath("data.totalPages").description("?????? ????????? ???")
                    , fieldWithPath("data.number").description("?????? ?????????")
                    , fieldWithPath("data.size").description("??? ????????? ????????? ??????")
                    , fieldWithPath("data.numberOfElements").description("?????? ????????? ????????? ??????")
                    , fieldWithPath("data.empty").description("?????? ???????????? ???????????????")
                )
            )
        );
  }

  @Test
  void insertTest() throws Exception {
    int postId = pc.insertPost(accessToken);
    long beforeCnt = commentRepository.count();
    insert(postId, "kkekkek", accessToken, false);
    insert(postId, "kkekkek", accessToken, false);
    insert(postId, "???????????????????????? ???????????? ???????????????????????????????????????", otherAccessToken, true);
    then(beforeCnt + 3).isEqualTo(commentRepository.count());
  }

  @Test
  void insertParentTest() throws Exception {
    int postId = pc.insertPost(accessToken);
    long beforeCnt = commentRepository.count();
    insert(postId, "kkekkek", accessToken, false);
    insert(postId, "kkekkek", accessToken, false);
    int parentId = insertAnonymity(postId, "???????????????????????? ???????????? ???????????????????????????????????????", otherAccessToken, false);
    insertAnonymity(postId, "???????????? ????????????????????????", parentId, anotherAccessToken, true);
    then(beforeCnt + 4).isEqualTo(commentRepository.count());
  }

  int insert(int postId, String comment, String accessToken, boolean doDocument) throws Exception {
    return insert(postId, comment, null, accessToken, doDocument);
  }

  int insert(int postId, String comment, Integer parentId, String accessToken, boolean doDocument) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("content", comment);
    body.put("anonymity", false);
    if (parentId != null)
      body.put("parent", new HashMap<>() {{
        put("id", parentId);
      }});

    MvcResult res = mockMvc.perform(RestDocumentationRequestBuilders.post("/post/{id}/comment", postId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.anonymity").value(false))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document(!doDocument ? "x" : parentId != null ? "???????????????" : "????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("??? id")
                ),
                requestFields(
                    fieldWithPath("parent.id").description("???????????? ??? ?????? ?????? id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("content").description("?????? ??????")
                    , fieldWithPath("anonymity").description("?????? ??????")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.id").description("?????? id")
                    , fieldWithPath("data.parent.id").description("?????? ?????? id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.content").description("?????? ??????")
                    , fieldWithPath("data.anonymity").description("?????? ??????")
                    , fieldWithPath("data.user").description("?????? ?????? ??????(?????? ??? ???)").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.isWriter").description("?????? ???????????? ??? ???????????????")
                    , fieldWithPath("data.isMine").description("?????? ???????????? ?????????")
                    , fieldWithPath("data.createdAt").description("?????????")
                    , fieldWithPath("data.modifiedAt").description("?????????")
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
  void insertAnonymityTest() throws Exception {
    int postId = pc.insertAnonymity(accessToken);
    long beforeCnt = commentRepository.count();
    insertAnonymity(postId, "kkekkek", otherAccessToken, false);
    insertAnonymity(postId, "kkekkek", anotherAccessToken, false);
    insertAnonymity(postId, "????????????????????????????????? ???????????? ???????????????????????????????????????", accessToken, true);
    then(beforeCnt + 3).isEqualTo(commentRepository.count());
  }

  int insertAnonymity(int postId, String comment, String accessToken, boolean doDocument) throws Exception {
    return insertAnonymity(postId, comment, null, accessToken, doDocument);
  }

  int insertAnonymity(int postId, String comment, Integer parentId, String accessToken, boolean doDocument) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("content", comment);
    body.put("anonymity", true);
    body.put("parent", new HashMap<>() {{
      put("id", parentId);
    }});


    MvcResult res = mockMvc.perform(RestDocumentationRequestBuilders.post("/post/{id}/comment", postId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.anonymity").value(true))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document(!doDocument ? "x" : parentId != null ? "???????????????" : "??????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("??? id")
                ),
                requestFields(
                    fieldWithPath("parent.id").description("???????????? ??? ?????? ?????? id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("content").description("?????? ??????")
                    , fieldWithPath("anonymity").description("?????? ??????")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.id").description("?????? id")
                    , fieldWithPath("data.parent.id").description("?????? ?????? id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.content").description("?????? ??????")
                    , fieldWithPath("data.anonymity").description("?????? ??????")
                    , fieldWithPath("data.user").description("?????? ?????? ??????(?????? ??? ???)").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.isWriter").description("?????? ???????????? ??? ???????????????")
                    , fieldWithPath("data.isMine").description("?????? ???????????? ?????????")
                    , fieldWithPath("data.createdAt").description("?????????")
                    , fieldWithPath("data.modifiedAt").description("?????????")
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
  void deleteTest() throws Exception {
    int postId = pc.insertAnonymity(accessToken);
    int commentId = insertAnonymity(postId, "eotemfdsgsdfgd", accessToken, false);
    long beforeCnt = commentRepository.count();
    delete(commentId, accessToken, true);
    then(beforeCnt).isEqualTo(commentRepository.count());
    then(commentRepository.findById((long) commentId).get().getStatus()).isEqualTo(false);
  }

  void delete(int id, String accessToken, boolean doDocument) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.delete("/comment/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document(!doDocument ? "x" : "????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("?????? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data").description("?????? ??????")
                )
            )
        )
        .andReturn();
  }

  @Test
  void updateTest() throws Exception {
    int postId = pc.insertAnonymity(accessToken);
    int commentId = insertAnonymity(postId, "eotemfdsgsdfgd", accessToken, false);
    long beforeCnt = commentRepository.count();
    String content = "????????? ???????????????";
    update(commentId, content, accessToken, true);
    then(beforeCnt).isEqualTo(commentRepository.count());
    then(commentRepository.findById((long) commentId).get().getContent()).isEqualTo(content);
  }

  void update(int id, String content, String accessToken, boolean doDocument) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("content", content);

    mockMvc.perform(RestDocumentationRequestBuilders.put("/comment/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            !doDocument ? result -> {
            } : document("????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ?????? id")
                ),
                requestFields(
                    fieldWithPath("content").description("????????? ?????? ??????")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.id").description("?????? id").optional()
                    , fieldWithPath("data.parent.id").description("?????? ?????? id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.content").description("?????? ??????").optional()
                    , fieldWithPath("data.anonymity").description("?????? ??????").optional()
                    , fieldWithPath("data.user").description("?????? ?????? ??????(?????? ??? ???)").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.isWriter").description("?????? ???????????? ??? ???????????????").optional()
                    , fieldWithPath("data.isMine").description("?????? ???????????? ?????????").optional()
                    , fieldWithPath("data.createdAt").description("?????????").optional()
                    , fieldWithPath("data.modifiedAt").description("?????????").optional()
                )

            )
        )
        .andReturn();
  }

  @Test
  void reportTest() throws Exception {
    int postId = pc.insertAnonymity(accessToken);
    int commentId = insertAnonymity(postId, "eotemfdsgsdfgd", accessToken, false);
    long beforeCnt = communityReportRepository.count();
    report(commentId, accessToken, "????????????");
    then(beforeCnt + 1).isEqualTo(communityReportRepository.count());
  }

  void report(int id, String accessToken, String documentName) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("report", new HashMap<>() {{
      put("id", "3");
    }});

    mockMvc.perform(RestDocumentationRequestBuilders.post("/comment/{id}/report", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document(documentName,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ?????? id")
                ),
                requestFields(
                    fieldWithPath("report.id").description("?????? ?????? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data").description("?????? ?????? ??????")
                )
            )
        )
        .andReturn();
  }

  @Test
  void blockUserTest() throws Exception {
    int postId = pc.insertPost(accessToken);
    int commentId = insert(postId, "1??????", anotherAccessToken, false);
    long beforeCnt = blockedUserRepository.count();
    blockUser(commentId, accessToken);
    then(beforeCnt + 1).isEqualTo(blockedUserRepository.count());
  }

  void blockUser(int commentId, String accessToken) throws Exception {

    mockMvc.perform(RestDocumentationRequestBuilders.post("/comment/{id}/block", commentId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("????????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ????????? ?????? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data").description("?????? ??????")
                )
            )
        ).andReturn();
  }

  @Test
  void getByBlockTest() throws  Exception{
    getByPostId();
    getMyBlock(accessToken);
  }

  List<BlockedUserDto> getMyBlock(String accessToken) throws Exception {
    MvcResult res = mockMvc.perform(get("/user/block/me")
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
                    , fieldWithPath("data.[].id").description("?????? id")
                    , fieldWithPath("data.[].createdAt").description("?????????")
                    , fieldWithPath("data.[].blockedUser").description("????????? ??????")
                )
            )
        ).andReturn();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(res.getResponse().getContentAsString());
    return Arrays.asList(new ObjectMapper().registerModule(new JavaTimeModule()).readValue(json.get("data").toString(), BlockedUserDto[].class));
  }

  @Test
  void blockUserDeleteTest() throws Exception {
    int postId = pc.insertPost(accessToken);
    int commentId = insert(postId, "1??????", anotherAccessToken, false);
    blockUser(commentId, accessToken);
    Long blockId = getMyBlock(accessToken).get(0).id();
    long beforeCnt = blockedUserRepository.count();

    mockMvc.perform(RestDocumentationRequestBuilders.delete("/user/block/{id}", blockId)
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
                    , fieldWithPath("data").description("?????? ?????? ??????")
                )
            )
        );
    then(beforeCnt - 1).isEqualTo(blockedUserRepository.count());
  }

}