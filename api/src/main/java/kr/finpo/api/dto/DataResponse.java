package kr.finpo.api.dto;

import kr.finpo.api.constant.ErrorCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class DataResponse<T> extends Response {

  private final T data;

  private DataResponse(T data) {
    super(true, ErrorCode.OK.getCode(), ErrorCode.OK.getMessage());
    this.data = data;
  }

  private DataResponse(T data, String message) {
    super(true, ErrorCode.OK.getCode(), message);
    this.data = data;
  }

  private DataResponse(T data, String message, int code) {
    super(true, code, message);
    this.data = data;
  }

  public static <T> DataResponse<T> of(T data) {
    return new DataResponse<>(data);
  }

  public static <T> DataResponse<T> of(T data, String message) {
    return new DataResponse<>(data, message);
  }

  public static <T> DataResponse<T> of(T data, String message, int code) {
    return new DataResponse<>(data, message, code);
  }

  public static <T> DataResponse<T> empty() {
    return new DataResponse<>(null);
  }
}

