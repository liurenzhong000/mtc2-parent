package io.mtc.facade.backend.aop;

import io.mtc.common.util.ResultUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常捕获
 *
 * @author Chinhin
 * 2018/6/10
 */
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object badRequestException(IllegalArgumentException exception) {
        return ResultUtil.error(exception.getMessage());
    }

}
