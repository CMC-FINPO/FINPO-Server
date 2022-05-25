package kr.finpo.api.controller.error;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.dto.ErrorResponse;
import kr.finpo.api.exception.GeneralException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.data.rest.webmvc.RepositoryRestController;


@RestControllerAdvice(annotations = {RestController.class, RepositoryRestController.class})
public class ExceptionHandler extends ResponseEntityExceptionHandler {

  @org.springframework.web.bind.annotation.ExceptionHandler
  public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
    return handleExceptionInternal(e, ErrorCode.VALIDATION_ERROR, request);
  }

  @org.springframework.web.bind.annotation.ExceptionHandler
  public ResponseEntity<Object> general(GeneralException e, WebRequest request) {
    return handleExceptionInternal(e, e.getErrorCode(), request);
  }

  @org.springframework.web.bind.annotation.ExceptionHandler
  public ResponseEntity<Object> exception(Exception e, WebRequest request) {
    return handleExceptionInternal(e, ErrorCode.INTERNAL_ERROR, request);
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
    return handleExceptionInternal(ex, ErrorCode.valueOf(status), headers, status, request);
  }


  private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorCode errorCode, WebRequest request) {
    return handleExceptionInternal(e, errorCode, HttpHeaders.EMPTY, errorCode.getHttpStatus(), request);
  }

  private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorCode errorCode, HttpHeaders headers, HttpStatus status, WebRequest request) {
    return super.handleExceptionInternal(
        e,
        ErrorResponse.of(false, errorCode.getCode(), errorCode.getMessage(e)),
        headers,
        status,
        request
    );
  }

}
