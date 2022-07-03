package kr.finpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.repository.InterestRegionRepository;
import kr.finpo.api.repository.LikePostRepository;
import kr.finpo.api.repository.PostRepository;
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
@EnableJpaAuditing
@SpringBootTest
public
class PostControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private LikePostRepository likePostRepository;

  String accessToken, refreshToken, otherAccessToken;

  @BeforeEach
  void setUp() throws Exception {
    OAuthControllerTest oc = new OAuthControllerTest();
    HashMap<String, String> map = oc.registerAndGetToken(mockMvc);
    accessToken = map.get("accessToken");
    refreshToken = map.get("refreshToken");

    map = oc.registerAndGetToken(mockMvc, "leeekim");
    otherAccessToken = map.get("accessToken");
  }

  public void set(MockMvc mockMvc, String accessToken) {
    this.mockMvc = mockMvc;
    this.accessToken = accessToken;
  }

  @Test
  void gett() throws Exception {
    int id = insertPost();
    mockMvc.perform(RestDocumentationRequestBuilders.get("/post/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("글상세조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("글 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.id").description("글 id")
                    , fieldWithPath("data.content").description("글 내용")
                    , fieldWithPath("data.anonymity").description("글 작성자 익명 여부")
                    , fieldWithPath("data.likes").description("좋아요 수")
                    , fieldWithPath("data.hits").description("조회수")
                    , fieldWithPath("data.user").description("글 작성자")
                    , fieldWithPath("data.createdAt").description("작성일")
                    , fieldWithPath("data.modifiedAt").description("수정일")
                    , fieldWithPath("data.imgs").description("글 이미지들").optional().type(JsonFieldType.ARRAY)
                    , fieldWithPath("data.imgs.[].order").description("이미지 순서").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.imgs.[].img").description("이미지 링크").optional().type(JsonFieldType.STRING)
                )
            )
        );
  }


  @Test
  void getMy() throws Exception {
    insertPostTest();
    mockMvc.perform(get("/post/me?page=0&size=5&sort=createdAt,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andExpect(jsonPath("$.data.content.length()").value(5))
        .andDo(
            document("내글조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("page").description("페이지 위치 (0부터 시작)").optional(),
                    parameterWithName("size").description("한 페이지의 데이터 개수").optional(),
                    parameterWithName("sort").description("정렬 기준 (복수 정렬 가능)\n createdAt desc:작성일 내림차순\n [createdAt, likes]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.content.[].id").description("글 id")
                    , fieldWithPath("data.content.[].content").description("글 내용")
                    , fieldWithPath("data.content.[].anonymity").description("글 작성자 익명 여부")
                    , fieldWithPath("data.content.[].likes").description("좋아요 수")
                    , fieldWithPath("data.content.[].hits").description("조회수")
                    , fieldWithPath("data.content.[].user").description("글 작성자")
                    , fieldWithPath("data.content.[].createdAt").description("작성일")
                    , fieldWithPath("data.content.[].modifiedAt").description("수정일")

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
  void search() throws Exception {
    insertPostTest();
    mockMvc.perform(get("/post/search?page=0&size=5&sort=createdAt,desc")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andExpect(jsonPath("$.data.content.length()").value(5))
        .andDo(
            document("글조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParameters(
                    parameterWithName("page").description("페이지 위치 (0부터 시작)").optional(),
                    parameterWithName("size").description("한 페이지의 데이터 개수").optional(),
                    parameterWithName("sort").description("정렬 기준 (복수 정렬 가능)\n createdAt desc:작성일 내림차순\n [createdAt, likes]").optional()
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.content.[].id").description("글 id")
                    , fieldWithPath("data.content.[].content").description("글 내용")
                    , fieldWithPath("data.content.[].anonymity").description("글 작성자 익명 여부")
                    , fieldWithPath("data.content.[].likes").description("좋아요 수")
                    , fieldWithPath("data.content.[].hits").description("조회수")
                    , fieldWithPath("data.content.[].user").description("글 작성자").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.content.[].createdAt").description("작성일")
                    , fieldWithPath("data.content.[].modifiedAt").description("수정일")

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
  void insertPostTest() throws Exception {
    long beforeCnt = postRepository.count();
    insertPost();
    insertPost();
    insertPost();
    insertPost();
    insertPost();
    then(beforeCnt + 5).isEqualTo(postRepository.count());
  }


  int insertPost() throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("content", "내용 내용\nsdfjagf ear34329u4jrq3 fmfmmmm ");
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
            document("글쓰기",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("content").description("글 내용")
                    , fieldWithPath("anonymity").description("익명 여부")
                    , fieldWithPath("imgs.[].order").description("이미지 순서").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("imgs.[].img").description("이미지 링크").optional().type(JsonFieldType.STRING)
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.id").description("글 id")
                    , fieldWithPath("data.content").description("글 내용")
                    , fieldWithPath("data.anonymity").description("글 익명 여부")
                    , fieldWithPath("data.user").description("글 작성 유저(익명 시 빔)").optional().type(JsonFieldType.OBJECT)
                    , fieldWithPath("data.createdAt").description("작성일")
                    , fieldWithPath("data.modifiedAt").description("수정일")
                    , fieldWithPath("data.imgs").description("글 이미지들").optional().type(JsonFieldType.ARRAY)
                    , fieldWithPath("data.imgs.[].order").description("이미지 순서").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("data.imgs.[].img").description("이미지 링크").optional().type(JsonFieldType.STRING)
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
    insertAnonymity();
    insertAnonymity();
    insertAnonymity();
    insertAnonymity();
    insertAnonymity();
    then(beforeCnt + 5).isEqualTo(postRepository.count());
  }

  int insertAnonymity() throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("content", "내용 내용\nsdfjagf ear34329u4jrq3 fmfmmmm ");
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
            document("글쓰기익명",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("content").description("글 내용")
                    , fieldWithPath("anonymity").description("익명 여부")
                    , fieldWithPath("imgs.[].order").description("이미지 순서").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("imgs.[].img").description("이미지 링크").optional().type(JsonFieldType.STRING)
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.id").description("글 id")
                    , fieldWithPath("data.content").description("글 내용")
                    , fieldWithPath("data.anonymity").description("글 익명 여부")
                    , fieldWithPath("data.createdAt").description("작성일")
                    , fieldWithPath("data.modifiedAt").description("수정일")
                    , fieldWithPath("data.user").description("글 작성 유저(익명 시 빔)").optional().type(JsonFieldType.OBJECT)
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
    int id = insertPost();
    long beforeCnt = postRepository.count();
    then(update(id)).isEqualTo(id);
    then(beforeCnt).isEqualTo(postRepository.count());
  }

  int update(int id) throws Exception {
    HashMap<String, Object> body = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    body.put("content", "수정한내용임얼헌엎ㅎ댝 ㅓㅐㄷ먀거 ㅁ더ㅜㄴㄹㅇ");
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
            document("글수정",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("수정할 글 id")
                ),
                requestFields(
                    fieldWithPath("content").description("수정할 글 내용").optional().type(JsonFieldType.STRING)
                    , fieldWithPath("anonymity").description("수정할 익명 여부").optional().type(JsonFieldType.BOOLEAN)
                    , fieldWithPath("imgs.[].order").description("이미지 순서").optional().type(JsonFieldType.NUMBER)
                    , fieldWithPath("imgs.[].img").description("이미지 링크").optional().type(JsonFieldType.STRING)
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.id").description("글 id")
                    , fieldWithPath("data.content").description("글 내용")
                    , fieldWithPath("data.anonymity").description("글 익명 여부")
                    , fieldWithPath("data.user").description("글 작성 유저(익명 시 빔)").optional().type(JsonFieldType.OBJECT)
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
  void delete() throws Exception {
    int id = insertAnonymity();
    long beforeCnt = postRepository.count();
    delete(id);
    then(beforeCnt - 1).isEqualTo(postRepository.count());
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
            document("글삭제",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("삭제할 글 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data").description("삭제 여부")
                )
            )
        )
    ;
  }

  @Test
  void like() throws Exception {
    long beforeCnt = likePostRepository.count();
    int id = insertAnonymity();
    like(id);
    like(insertAnonymity());
    like(insertAnonymity());
    like(insertAnonymity());
    then(beforeCnt + 4).isEqualTo(likePostRepository.count());
  }

  void like(int id) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.post("/post/{id}/like", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + otherAccessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("글좋아요",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("좋아요 할 글 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.id").description("글 id")
                    , fieldWithPath("data.content").description("글 내용")
                    , fieldWithPath("data.anonymity").description("글 작성자 익명 여부")
                    , fieldWithPath("data.likes").description("좋아요 수")
                    , fieldWithPath("data.hits").description("조회수")
                    , fieldWithPath("data.user").description("글 작성자").optional().type(JsonFieldType.OBJECT)
                )
            )
        )
    ;
  }



  @Test
  void deleteLike() throws Exception {
    int id = insertAnonymity();
    like(id);
    long beforeCnt = likePostRepository.count();
    deleteLike(id);
    then(beforeCnt - 1).isEqualTo(likePostRepository.count());
  }

  void deleteLike(int id) throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.delete("/post/{id}/like", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + otherAccessToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("글좋아요취소",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                pathParameters(
                    parameterWithName("id").description("좋아요 취소할 글 id")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    , fieldWithPath("errorCode").description("응답 코드")
                    , fieldWithPath("message").description("응답 메시지")
                    , fieldWithPath("data.id").description("글 id")
                    , fieldWithPath("data.content").description("글 내용")
                    , fieldWithPath("data.anonymity").description("글 작성자 익명 여부")
                    , fieldWithPath("data.likes").description("좋아요 수")
                    , fieldWithPath("data.hits").description("조회수")
                    , fieldWithPath("data.user").description("글 작성자").optional().type(JsonFieldType.OBJECT)
                )
            )
        )
    ;
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
            document("글이미지업로드",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("Access Token")
                ),
                requestParts(
                    partWithName("imgFiles").description("업로드할 이미지 파일들")
                ),
                relaxedResponseFields(
                    fieldWithPath("success").description("성공 여부")
                    ,fieldWithPath("errorCode").description("응답 코드")
                    ,fieldWithPath("message").description("응답 메시지")
                    ,fieldWithPath("data.imgUrls").description("업로드 된 이미지 url들 (request 순서대로)")
                )
            )
        )
    ;
  }
}