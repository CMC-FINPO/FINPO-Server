package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.PostImg;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImgDto(
    String img,
    Integer order
) {
  public static ImgDto response(PostImg postImg) {
    return new ImgDto(
        postImg.getImg(),
        postImg.getOorder()
    );
  }
}
