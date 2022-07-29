package kr.finpo.api.controller.error;

import javax.servlet.http.HttpServletResponse;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.dto.ErrorResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BaseErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Object> error(HttpServletResponse response) {
        HttpStatus httpStatus = HttpStatus.valueOf(response.getStatus());
        ErrorCode errorCode = httpStatus.is4xxClientError() ? ErrorCode.BAD_REQUEST : ErrorCode.INTERNAL_ERROR;

        if (httpStatus == HttpStatus.OK) {
            httpStatus = HttpStatus.FORBIDDEN;
            errorCode = ErrorCode.BAD_REQUEST;
        }

        return ResponseEntity
            .status(httpStatus)
            .body(ErrorResponse.of(false, errorCode));
    }

}

