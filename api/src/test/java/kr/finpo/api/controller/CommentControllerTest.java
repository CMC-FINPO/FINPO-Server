package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.repository.CommentRepository;
import kr.finpo.api.repository.CommunityReportRepository;
import kr.finpo.api.repository.LikePostRepository;
import kr.finpo.api.repository.PostRepository;
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
import java.util.HashMap;

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
@EnableJpaAuditing
@SpringBootTest
public
class CommentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private CommunityReportRepository communityReportRepository;


  String accessToken, refreshToken, otherAccessToken, anotherAccessToken;

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

    NotificationControllerTest nc = new NotificationControllerTest();
    nc.set(mockMvc, accessToken);
    nc.updateMy(accessToken);
    nc.updateMy(otherAccessToken);
    nc.updateMy(anotherAccessToken);
  }


  @Test
  void getByPostId() throws Exception {
    int postId = pc.insertPost(accessToken);
    insert(postId, "1빠당", anotherAccessToken, false);
    insert(postId, "2빠당", anotherAccessToken, false);
    insertAnonymity(postId, "3빠당", anotherAccessToken, false);
    insertAnonymity(postId, "4빠당", anotherAccessToken, false);
    insert(postId, "5빠당", anotherAccessToken, false);
    insert(postId, "6빠당", anotherAccessToken, false);
    int parentId = insertAnonymity(postId, "익명으로 다는 댓글임", otherAccessToken, false);
    insert(postId, "작성자가쓰는글", accessToken, false);
    insertAnonymity(postId, "익명으로 단 사람이 또 단 댓글임", otherAccessToken, false);
    insert(postId, "딴사람이단댓글", anotherAccessToken, false);
    insertAnonymity(postId, "익명댓글에 첫번째로 다는 대댓글", parentId, otherAccessToken, false);
    int deleteId = insertAnonymity(postId, "fsdfsd", parentId, anotherAccessToken, false);
    insertAnonymity(postId, "두번째익명 대댓글", parentId, anotherAccessToken, false);
    int updateId = insert(postId, "글작성자가쓰는 세번째 대댓글", parentId, accessToken, false);
    delete(parentId, otherAccessToken, false);
    delete(deleteId, anotherAccessToken, false);
    update(updateId, "글작성자가 쓰는 세번째 대댓글 수정된거임", accessToken, false);
    uc.deleteMe(anotherAccessToken);
    getByPostId(postId);
  }

  void getByPostId(int postId) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.get("/post/{id}/comment?page=1&size=5&sort=id,asc", postId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("글댓글조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("글 id")
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
                    , fieldWithPath("data.content.[].status").description("false면 삭제된 댓글")
                    , fieldWithPath("data.content.[].id").description("댓글 id")
                    , fieldWithPath("data.content.[].parent.id").description("부모 댓글 id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.content.[].content").description("댓글 내용").optional()
                    , fieldWithPath("data.content.[].anonymity").description("익명 여부").optional()
                    , fieldWithPath("data.content.[].anonymityId").description("익명 id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.content.[].user").description("댓글 작성 유저(익명 시 빔)").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.content.[].isUserWithdraw").description("댓글 작성 유저 탈퇴 여부").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].isWriter").description("댓글 작성자가 글 작성자인가").optional()
                    , fieldWithPath("data.content.[].isMine").description("댓글 작성자가 나인가").optional()
                    , fieldWithPath("data.content.[].createdAt").description("작성일").optional()
                    , fieldWithPath("data.content.[].modifiedAt").description("수정일").optional()

                    , fieldWithPath("data.last").description("현재가 마지막 페이지인가")
                    , fieldWithPath("data.first").description("현재가 첫 페이지인가")
                    , fieldWithPath("data.totalElements").description("전체 데이터 수")
                    , fieldWithPath("data.totalPages").description("전체 페이지 수")
                    , fieldWithPath("data.number").description("현재 페이지")
                    , fieldWithPath("data.size").description("한 페이지 데이터 개수")
                    , fieldWithPath("data.numberOfElements").description("현재 페이지 데이터 개수")
                    , fieldWithPath("data.empty").description("현재 페이지가 비어있는가")
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
    insert(postId, "남의글에다는댓글 댓글댓글 ㅁㅈ라막ㅎㄹㅁㄷ갖ㄹㄷㄱ머", otherAccessToken, true);
    then(beforeCnt + 3).isEqualTo(commentRepository.count());
  }

  @Test
  void insertParentTest() throws Exception {
    int postId = pc.insertPost(accessToken);
    long beforeCnt = commentRepository.count();
    insert(postId, "kkekkek", accessToken, false);
    insert(postId, "kkekkek", accessToken, false);
    int parentId = insertAnonymity(postId, "남의글에다는댓글 댓글댓글 ㅁㅈ라막ㅎㄹㅁㄷ갖ㄹㄷㄱ머", otherAccessToken, false);
    insertAnonymity(postId, "익명으로 대댓글달아버리기", parentId, anotherAccessToken, true);
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
            document(!doDocument ? "x" : parentId != null ? "대댓글쓰기" : "댓글쓰기",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("글 id")
                ),
                requestFields(
                    fieldWithPath("parent.id").description("대댓글일 시 부모 댓글 id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("content").description("댓글 내용")
                    , fieldWithPath("anonymity").description("익명 여부")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.id").description("댓글 id")
                    , fieldWithPath("data.parent.id").description("부모 댓글 id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.content").description("댓글 내용")
                    , fieldWithPath("data.anonymity").description("익명 여부")
                    , fieldWithPath("data.user").description("댓글 작성 유저(익명 시 빔)").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.isWriter").description("댓글 작성자가 글 작성자인가")
                    , fieldWithPath("data.isMine").description("댓글 작성자가 나인가")
                    , fieldWithPath("data.createdAt").description("작성일")
                    , fieldWithPath("data.modifiedAt").description("수정일")
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
    insertAnonymity(postId, "내가쓴익명글에다는댓글 댓글댓글 ㅁㅈ라막ㅎㄹㅁㄷ갖ㄹㄷㄱ머", accessToken, true);
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
            document(!doDocument ? "x" : parentId != null ? "대댓글쓰기" : "댓글쓰기익명",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("글 id")
                ),
                requestFields(
                    fieldWithPath("parent.id").description("대댓글일 시 부모 댓글 id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("content").description("댓글 내용")
                    , fieldWithPath("anonymity").description("익명 여부")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.id").description("댓글 id")
                    , fieldWithPath("data.parent.id").description("부모 댓글 id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.content").description("댓글 내용")
                    , fieldWithPath("data.anonymity").description("익명 여부")
                    , fieldWithPath("data.user").description("댓글 작성 유저(익명 시 빔)").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.isWriter").description("댓글 작성자가 글 작성자인가")
                    , fieldWithPath("data.isMine").description("댓글 작성자가 나인가")
                    , fieldWithPath("data.createdAt").description("작성일")
                    , fieldWithPath("data.modifiedAt").description("수정일")
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
            document(!doDocument ? "x" : "댓글삭제",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("댓글 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data").description("삭제 여부")
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
    String content = "수정할 댓글내용임";
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
            !doDocument ? result -> {} : document("댓글수정",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("수정할 댓글 id")
                ),
                requestFields(
                    fieldWithPath("content").description("수정할 댓글 내용")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.id").description("댓글 id").optional()
                    , fieldWithPath("data.parent.id").description("부모 댓글 id").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.content").description("댓글 내용").optional()
                    , fieldWithPath("data.anonymity").description("익명 여부").optional()
                    , fieldWithPath("data.user").description("댓글 작성 유저(익명 시 빔)").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.isWriter").description("댓글 작성자가 글 작성자인가").optional()
                    , fieldWithPath("data.isMine").description("댓글 작성자가 나인가").optional()
                    , fieldWithPath("data.createdAt").description("작성일").optional()
                    , fieldWithPath("data.modifiedAt").description("수정일").optional()
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
    report(commentId, accessToken, "댓글신고");
    then(beforeCnt + 1).isEqualTo(communityReportRepository.count());
  }

  void report(int id, String accessToken, String documentName) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("report", new HashMap<>(){{
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
                    parameterWithName("id").description("신고할 댓글 id")
                ),
                requestFields(
                    fieldWithPath("report.id").description("신고 사유 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data").description("신고 성공 여부")
                )
            )
        )
        .andReturn();
  }
}