package kr.finpo.api.controller;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.service.OAuthService;
import kr.finpo.api.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



@DisplayName("Controller - Region")
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WithMockUser()
@SpringBootTest
class RegionControllerTest {

  @Autowired
  private MockMvc mockMvc;


  @Test
  void region1() throws Exception {
    mockMvc.perform(get("/region").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value("3"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("region1",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[]").description("지역1 이름")
                )
            )
        )
    ;
  }

  @Test
  void region2() throws Exception {
    mockMvc.perform(get("/region?region1=부산").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.OK.getCode()))
        .andDo(
            document("region2-busan",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("region1").description("지역1\n[서울/경기/부산]")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("errorCode").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.[]").description("지역2 이름")
                )
            )
        );
  }

}


