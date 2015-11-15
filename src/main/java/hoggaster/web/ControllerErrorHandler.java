package hoggaster.web;

import hoggaster.domain.user.web.UserNotFoundException;
import hoggaster.oanda.exceptions.TradingHaltedException;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by svante2 on 2015-10-08.
 */
@ControllerAdvice
public class ControllerErrorHandler {

    @ResponseBody
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors userNotFoundExceptionHandler(UserNotFoundException ex) {
        return new VndErrors("error", ex.getMessage());
    }


    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors illegalArgumentExceptionHandler(IllegalArgumentException ex) {
        return new VndErrors("error", ex.getMessage());
    }


    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(TradingHaltedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors tradingHaltedExceptionHandler(TradingHaltedException ex) {
        return new VndErrors("error", ex.getMessage());
    }
}
