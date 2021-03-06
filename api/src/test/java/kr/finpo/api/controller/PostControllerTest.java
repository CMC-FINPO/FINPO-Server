package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.repository.*;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Controller - Post")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.finpo.kr", uriPort = 443)
@WithMockUser
@Transactional
@SpringBootTest
public
class PostControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private LikePostRepository likePostRepository;

  @Autowired
  private BookmarkPostRepository bookmarkPostRepository;

  @Autowired
  private CommunityReportRepository communityReportRepository;
  @Autowired
  private BlockedUserRepository blockedUserRepository;


  String accessToken, refreshToken, otherAccessToken, anotherAccessToken;

  UserControllerTest uc = new UserControllerTest();

  @BeforeEach
  void setUp() throws Exception {
    if(accessToken != null) return;
    OAuthControllerTest oc = new OAuthControllerTest();
    HashMap<String, String> map = oc.registerAndGetToken(mockMvc);
    accessToken = map.get("accessToken");
    refreshToken = map.get("refreshToken");

    map = oc.registerAndGetToken(mockMvc, "leeekim");
    otherAccessToken = map.get("accessToken");

    map = oc.registerAndGetToken(mockMvc, "wwwwwlllll");
    anotherAccessToken = map.get("accessToken");

    uc.set(mockMvc, accessToken);

    NotificationControllerTest nc = new NotificationControllerTest();
    nc.set(mockMvc, accessToken);
    nc.updateMy(accessToken);
    nc.updateMy(otherAccessToken);
    nc.updateMy(anotherAccessToken);
  }

  public void set(MockMvc mockMvc, String accessToken) {
    this.mockMvc = mockMvc;
    this.accessToken = accessToken;
  }

  @Test
  void gett() throws Exception {
    int id = insertPost(accessToken);
    mockMvc.perform(RestDocumentationRequestBuilders.get("/post/{id}", id)
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
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.status").description("??? ?????? (?????? ??? false)")
                    , fieldWithPath("data.id").description("??? id")
                    , fieldWithPath("data.content").description("??? ??????")
                    , fieldWithPath("data.anonymity").description("??? ????????? ?????? ??????")
                    , fieldWithPath("data.likes").description("????????? ???")
                    , fieldWithPath("data.hits").description("?????????")
                    , fieldWithPath("data.countOfComment").description("?????????")
                    , fieldWithPath("data.user").description("??? ?????????")
                    , fieldWithPath("data.isUserWithdraw").description("????????? ????????? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.isMine").description("?????? ????????? ?????????")
                    , fieldWithPath("data.isLiked").description("?????? ????????? ??? ?????????")
                    , fieldWithPath("data.isBookmarked").description("?????? ????????? ??? ?????????")
                    , fieldWithPath("data.isModified").description("????????? ?????????")
                    , fieldWithPath("data.isMine").description("?????? ????????? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.createdAt").description("?????????")
                    , fieldWithPath("data.modifiedAt").description("?????????")
                    , fieldWithPath("data.imgs").description("??? ????????????").optional().type(JsonFieldType.ARRAY)
                    , fieldWithPath("data.imgs.[].order").description("????????? ??????").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.imgs.[].img").description("????????? ??????").optional().type(JsonFieldType.STRING)
                )
            )
        );
  }


  @Test
  void getMy() throws Exception {
    insertPostTest();
    mockMvc.perform(get("/post/me?page=0&size=5&sort=id,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andExpect(jsonPath("$.data.content.length()").value(5))
        .andDo(
            document("????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("page").description("????????? ?????? (0?????? ??????)").optional(),
                    parameterWithName("size").description("??? ???????????? ????????? ??????").optional(),
                    parameterWithName("sort").description("?????? ??????\n id desc:?????????(createdAt??????) ????????????\n [createdAt, likes]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.content.[].id").description("??? id")
                    , fieldWithPath("data.content.[].content").description("??? ??????")
                    , fieldWithPath("data.content.[].anonymity").description("??? ????????? ?????? ??????")
                    , fieldWithPath("data.content.[].likes").description("????????? ???")
                    , fieldWithPath("data.content.[].hits").description("?????????")
                    , fieldWithPath("data.content.[].countOfComment").description("?????????")
                    , fieldWithPath("data.content.[].user").description("??? ?????????")
                    , fieldWithPath("data.content.[].createdAt").description("?????????")
                    , fieldWithPath("data.content.[].modifiedAt").description("?????????")

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
  void getMyLikesTest() throws Exception {
    long beforeCnt = likePostRepository.count();
    like(insertPost(otherAccessToken), accessToken);
    like(insertPost(anotherAccessToken), accessToken);
    like(insertAnonymity(otherAccessToken), accessToken);
    getMy("like", accessToken);
    then(beforeCnt + 3).isEqualTo(likePostRepository.count());
  }

  @Test
  void getMyBookmarksTest() throws Exception {
    long beforeCnt = bookmarkPostRepository.count();
    bookmark(insertPost(otherAccessToken), accessToken);
    bookmark(insertPost(anotherAccessToken), accessToken);
    bookmark(insertAnonymity(otherAccessToken), accessToken);
    getMy("bookmark", accessToken);
    then(beforeCnt + 3).isEqualTo(bookmarkPostRepository.count());
  }

  @Test
  void getMyCommentPostsTest() throws Exception {
    CommentControllerTest cc = new CommentControllerTest();
    cc.set(mockMvc, accessToken);
    int id = insertPost(otherAccessToken);
    cc.insertAnonymity(id, "???????????????", accessToken, false);
    cc.insertAnonymity(id, "???????????????222", accessToken, false);
    int id2 = insertAnonymity(otherAccessToken);
    cc.insert(id2, "???????????????", accessToken, false);
    getMy("comment", accessToken);
  }

  void getMy(String type, String accessToken) throws Exception {
    mockMvc.perform(get("/post/" + type + "/me?page=0&size=5&sort=id,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document(type.equals("like")?"????????????????????????":type.equals("bookmark")?"?????????????????????":"?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("page").description("????????? ?????? (0?????? ??????)").optional(),
                    parameterWithName("size").description("??? ???????????? ????????? ??????").optional(),
                    parameterWithName("sort").description("?????? ??????\n id desc:????????? ????????????\n [id, likes]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.content.[].status").description("??? ?????? (?????? ??? false)")
                    , fieldWithPath("data.content.[].id").description("??? id")
                    , fieldWithPath("data.content.[].content").description("??? ??????")
                    , fieldWithPath("data.content.[].anonymity").description("??? ????????? ?????? ??????")
                    , fieldWithPath("data.content.[].likes").description("????????? ???")
                    , fieldWithPath("data.content.[].hits").description("?????????")
                    , fieldWithPath("data.content.[].countOfComment").description("?????????")
                    , fieldWithPath("data.content.[].user").description("??? ?????????").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.content.[].createdAt").description("?????????")
                    , fieldWithPath("data.content.[].modifiedAt").description("?????????")

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
  void blockUserTest() throws Exception {
    int id = insertPost(accessToken);
    long beforeCnt = blockedUserRepository.count();
    blockUser(id, accessToken);
    then(beforeCnt + 1).isEqualTo(blockedUserRepository.count());
  }

  void blockUser(int postId, String accessToken) throws Exception {

      mockMvc.perform(RestDocumentationRequestBuilders.post("/post/{id}/block", postId)
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
                pathParameters(
                    parameterWithName("id").description("????????? ????????? ??? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data").description("?????? ??????")
                )
            )
        );
  }


  @Test
  void search() throws Exception {
    insertPost(accessToken);
    insertPost(accessToken);
    insertPost(accessToken);
    insertPost(accessToken);
    insertPost(accessToken);
    insertPost(anotherAccessToken);
    int id = insertPost(accessToken);
    insertAnonymity(anotherAccessToken);
    insertPost(otherAccessToken);
    blockUser(id, otherAccessToken);

    uc.deleteMe(accessToken);
    mockMvc.perform(get("/post/search?content=??????&page=0&size=5&sort=id,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + otherAccessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("?????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("content").description("????????? ??????").optional(),
                    parameterWithName("page").description("????????? ?????? (0?????? ??????)").optional(),
                    parameterWithName("size").description("??? ???????????? ????????? ??????").optional(),
                    parameterWithName("sort").description("?????? ??????\n id desc:?????????(createdAt??????) ????????????\n [createdAt, likes]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.content.[].status").description("??? ?????? (?????? ??? false)")
                    , fieldWithPath("data.content.[].id").description("??? id")
                    , fieldWithPath("data.content.[].content").description("??? ??????")
                    , fieldWithPath("data.content.[].anonymity").description("??? ????????? ?????? ??????")
                    , fieldWithPath("data.content.[].likes").description("????????? ???")
                    , fieldWithPath("data.content.[].hits").description("?????????")
                    , fieldWithPath("data.content.[].countOfComment").description("?????????")
                    , fieldWithPath("data.content.[].user").description("??? ?????????").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.content.[].isUserWithdraw").description("????????? ????????? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].isMine").description("?????? ????????? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].isLiked").description("????????? ??? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].isBookmarked").description("????????? ??? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].modified").description("????????? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].createdAt").description("?????????")
                    , fieldWithPath("data.content.[].modifiedAt").description("?????????")


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
  void searchByLastIdTest() throws Exception {
    insertPost(accessToken);
    insertPost(accessToken);
    insertPost(anotherAccessToken);
    int id = insertPost(accessToken);
    insertPost(anotherAccessToken);
    insertPost(otherAccessToken);
    insertPost(accessToken);
    insertPost(anotherAccessToken);
    searchByLastId(id);
    uc.deleteMe(accessToken);
  }

  void searchByLastId(int lastId) throws Exception {

    mockMvc.perform(get("/post/search?content=??????&lastId="+lastId+"&page=0&size=5&sort=id,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + otherAccessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("??????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("content").description("????????? ??????").optional(),
                    parameterWithName("lastId").description("????????? ????????? ????????? ??? id").optional(),
                    parameterWithName("page").description("????????? ?????? (0?????? ??????)").optional(),
                    parameterWithName("size").description("??? ???????????? ????????? ??????").optional(),
                    parameterWithName("sort").description("?????? ??????\n id desc:?????????(createdAt??????) ????????????\n [createdAt, likes]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.content.[].status").description("??? ?????? (?????? ??? false)")
                    , fieldWithPath("data.content.[].id").description("??? id")
                    , fieldWithPath("data.content.[].content").description("??? ??????")
                    , fieldWithPath("data.content.[].anonymity").description("??? ????????? ?????? ??????")
                    , fieldWithPath("data.content.[].likes").description("????????? ???")
                    , fieldWithPath("data.content.[].hits").description("?????????")
                    , fieldWithPath("data.content.[].countOfComment").description("?????????")
                    , fieldWithPath("data.content.[].user").description("??? ?????????").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.content.[].isUserWithdraw").description("????????? ????????? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].isMine").description("?????? ????????? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].isLiked").description("????????? ??? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].isBookmarked").description("????????? ??? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].modified").description("????????? ?????????").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("data.content.[].createdAt").description("?????????")
                    , fieldWithPath("data.content.[].modifiedAt").description("?????????")


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
  void insertPostTest() throws Exception {
    long beforeCnt = postRepository.count();
    insertPost(accessToken);
    insertPost(accessToken);
    insertPost(accessToken);
    insertPost(accessToken);
    insertPost(accessToken);
    insertPost(anotherAccessToken);
    insertPost(accessToken);
    insertAnonymity(anotherAccessToken);
    insertPost(otherAccessToken);
    then(beforeCnt + 9).isEqualTo(postRepository.count());
  }


  int insertPost(String accessToken) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("content", "?????? ??????\nsdfjagf ear34329u4jrq3 fmfmmmm ");
    body.put("anonymity", false);
    body.put("imgs", new ArrayList<>(){{
      add(new HashMap<>(){{
        put("order", 0);
        put("img", "https://dev.finpo.kr/upload/post/5bd2f027-07ce-49bd-b980-47b01de5c523.jpeg");
      }});
      add(new HashMap<>(){{
        put("order", 1);
        put("img", "https://...");
      }});
      add(new HashMap<>(){{
        put("order", 2);
        put("img", "https://...");
      }});
    }});


    MvcResult res = mockMvc.perform(post("/post")
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
            document("?????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("content").description("??? ??????")
                    , fieldWithPath("anonymity").description("?????? ??????")
                    , fieldWithPath("imgs.[].order").description("????????? ??????").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("imgs.[].img").description("????????? ??????").optional().type(JsonFieldType.STRING)
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.id").description("??? id")
                    , fieldWithPath("data.content").description("??? ??????")
                    , fieldWithPath("data.anonymity").description("??? ?????? ??????")
                    , fieldWithPath("data.user").description("??? ?????? ??????(?????? ??? ???)").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.createdAt").description("?????????")
                    , fieldWithPath("data.modifiedAt").description("?????????")
                    , fieldWithPath("data.imgs").description("??? ????????????").optional().type(JsonFieldType.ARRAY)
                    , fieldWithPath("data.imgs.[].order").description("????????? ??????").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.imgs.[].img").description("????????? ??????").optional().type(JsonFieldType.STRING)
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
    long beforeCnt = postRepository.count();
    insertAnonymity(accessToken);
    insertAnonymity(anotherAccessToken);
    insertAnonymity(accessToken);
    insertAnonymity(anotherAccessToken);
    insertAnonymity(otherAccessToken);
    then(beforeCnt + 5).isEqualTo(postRepository.count());
  }

  int insertAnonymity(String accessToken) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("content", "?????? ?????? ??????\nsdfjagf ear34329u4jrq3 fmfmmmm ");
    body.put("anonymity", true);

    MvcResult res = mockMvc.perform(post("/post")
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
            document("???????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("content").description("??? ??????")
                    , fieldWithPath("anonymity").description("?????? ??????")
                    , fieldWithPath("imgs.[].order").description("????????? ??????").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("imgs.[].img").description("????????? ??????").optional().type(JsonFieldType.STRING)
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.id").description("??? id")
                    , fieldWithPath("data.content").description("??? ??????")
                    , fieldWithPath("data.anonymity").description("??? ?????? ??????")
                    , fieldWithPath("data.createdAt").description("?????????")
                    , fieldWithPath("data.modifiedAt").description("?????????")
                    , fieldWithPath("data.user").description("??? ?????? ??????(?????? ??? ???)").optional().type(JsonFieldType.OBJECT)
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
  void update() throws Exception {
    int id = insertPost(accessToken);
    long beforeCnt = postRepository.count();
    then(update(id)).isEqualTo(id);
    then(beforeCnt).isEqualTo(postRepository.count());
  }

  int update(int id) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("content", "????????????????????????????????? ??????????????? ??????????????????");
    body.put("imgs", new ArrayList<>(){{
      add(new HashMap<>(){{
        put("order", 0);
        put("img", "https://dev.finpo.kr/upload/post/5bd2f027-07ce-49bd-b980-47b01de5c523.jpeg");
      }});
      add(new HashMap<>(){{
        put("order", 1);
        put("img", "https://...");
      }});
    }});

    MvcResult res = mockMvc.perform(RestDocumentationRequestBuilders.put("/post/{id}", id)
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
            document("?????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ??? id")
                ),
                requestFields(
                    fieldWithPath("content").description("????????? ??? ??????").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("imgs.[].order").description("????????? ??????").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("imgs.[].img").description("????????? ??????").optional().type(JsonFieldType.STRING)
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.id").description("??? id")
                    , fieldWithPath("data.content").description("??? ??????")
                    , fieldWithPath("data.anonymity").description("??? ?????? ??????")
                    , fieldWithPath("data.user").description("??? ?????? ??????(?????? ??? ???)").optional().type(JsonFieldType.OBJECT)
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
  void delete() throws Exception {
    int id = insertAnonymity(accessToken);
    long beforeCnt = postRepository.count();
    delete(id);
    then(beforeCnt).isEqualTo(postRepository.count());
    then(postRepository.findById((long)id).get().getStatus()).isEqualTo(false);
  }

  void delete(int id) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.delete("/post/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("?????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ??? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data").description("?????? ??????")
                )
            )
        )
    ;
  }

  @Test
  void like() throws Exception {
    long beforeCnt = likePostRepository.count();
    int id = insertAnonymity(accessToken);
    like(id, otherAccessToken);
    like(insertAnonymity(accessToken), otherAccessToken);
    like(insertAnonymity(accessToken), otherAccessToken);
    like(insertAnonymity(accessToken), otherAccessToken);
    then(beforeCnt + 4).isEqualTo(likePostRepository.count());
  }

  void like(int id, String accessToken) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.post("/post/{id}/like", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ??? ??? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.id").description("??? id")
                    , fieldWithPath("data.content").description("??? ??????")
                    , fieldWithPath("data.anonymity").description("??? ????????? ?????? ??????")
                    , fieldWithPath("data.likes").description("????????? ???")
                    , fieldWithPath("data.hits").description("?????????")
                    , fieldWithPath("data.user").description("??? ?????????").optional().type(JsonFieldType.OBJECT)
                )
            )
        )
    ;
  }



  @Test
  void deleteLike() throws Exception {
    int id = insertAnonymity(accessToken);
    like(id, otherAccessToken);
    long beforeCnt = likePostRepository.count();
    deleteLike(id, otherAccessToken);
    then(beforeCnt - 1).isEqualTo(likePostRepository.count());
  }

  void deleteLike(int id, String accessToken) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.delete("/post/{id}/like", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("??????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ????????? ??? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.id").description("??? id")
                    , fieldWithPath("data.content").description("??? ??????")
                    , fieldWithPath("data.anonymity").description("??? ????????? ?????? ??????")
                    , fieldWithPath("data.likes").description("????????? ???")
                    , fieldWithPath("data.hits").description("?????????")
                    , fieldWithPath("data.user").description("??? ?????????").optional().type(JsonFieldType.OBJECT)
                )
            )
        )
    ;
  }


  @Test
  void bookmartTest() throws Exception {
    long beforeCnt = bookmarkPostRepository.count();
    int id = insertAnonymity(accessToken);
    bookmark(id, otherAccessToken);
    bookmark(insertAnonymity(accessToken), otherAccessToken);
    bookmark(insertAnonymity(accessToken), otherAccessToken);
    bookmark(insertAnonymity(accessToken), otherAccessToken);
    then(beforeCnt + 4).isEqualTo(bookmarkPostRepository.count());
  }

  void bookmark(int id, String accessToken) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.post("/post/{id}/bookmark", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ??? ??? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.id").description("??? id")
                    , fieldWithPath("data.content").description("??? ??????")
                    , fieldWithPath("data.anonymity").description("??? ????????? ?????? ??????")
                    , fieldWithPath("data.likes").description("????????? ???")
                    , fieldWithPath("data.hits").description("?????????")
                    , fieldWithPath("data.user").description("??? ?????????").optional().type(JsonFieldType.OBJECT)
                )
            )
        )
    ;
  }


  @Test
  void deleteBookmarkTest() throws Exception {
    int id = insertAnonymity(accessToken);
    bookmark(id, otherAccessToken);
    long beforeCnt = bookmarkPostRepository.count();
    deleteBookmark(id, otherAccessToken);
    then(beforeCnt - 1).isEqualTo(bookmarkPostRepository.count());
  }

  void deleteBookmark(int id, String accessToken) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.delete("/post/{id}/bookmark", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("??????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("????????? ????????? ??? id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.id").description("??? id")
                    , fieldWithPath("data.content").description("??? ??????")
                    , fieldWithPath("data.anonymity").description("??? ????????? ?????? ??????")
                    , fieldWithPath("data.likes").description("????????? ???")
                    , fieldWithPath("data.hits").description("?????????")
                    , fieldWithPath("data.user").description("??? ?????????").optional().type(JsonFieldType.OBJECT)
                )
            )
        )
    ;
  }

  @Test
  void reportTest() throws Exception {
    int postId = insertAnonymity(accessToken);
    long beforeCnt = communityReportRepository.count();
    report(postId, accessToken, "?????????");
    then(beforeCnt + 1).isEqualTo(communityReportRepository.count());
  }

  void report(int id, String accessToken, String documentName) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("report", new HashMap<>(){{
      put("id", "5");
    }});

    mockMvc.perform(RestDocumentationRequestBuilders.post("/post/{id}/report", id)
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
                    parameterWithName("id").description("????????? ??? id")
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
  void getReportTest() throws Exception {
    getReport(accessToken, "??????????????????");
  }

  void getReport(String accessToken, String documentName) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.get("/report/reason")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
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
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    , fieldWithPath("errorCode").description("?????? ??????")
                    , fieldWithPath("message").description("?????? ?????????")
                    , fieldWithPath("data.[].id").description("?????? ?????? id")
                    , fieldWithPath("data.[].reason").description("?????? ??????")
                )
            )
        )
        .andReturn();
  }


  @Disabled
  @Test
  void uploadPostImg() throws Exception {
    MockMultipartFile image = new MockMultipartFile("imgFiles", "imagefile.jpeg", "image/jpeg", new FileInputStream(System.getProperty("user.dir") + "/" + "test.png"));

    mockMvc.perform(RestDocumentationRequestBuilders.fileUpload("/upload/post")
            .file(image)
            .file(image)
            .file(image)
            .file(image)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.imgUrls.length()").value(4))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("?????????????????????",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParts(
                    partWithName("imgFiles").description("???????????? ????????? ?????????")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("?????? ??????")
                    ,fieldWithPath("errorCode").description("?????? ??????")
                    ,fieldWithPath("message").description("?????? ?????????")
                    ,fieldWithPath("data.imgUrls").description("????????? ??? ????????? url??? (request ????????????)")
                )
            )
        )
    ;
  }
}