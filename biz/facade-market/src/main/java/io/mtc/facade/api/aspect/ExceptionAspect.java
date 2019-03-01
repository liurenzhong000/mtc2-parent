package io.mtc.facade.api.aspect;

import io.mtc.common.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Created by hooger on 2018/8/3.
 */
@Slf4j
@Component
@Aspect
public class ExceptionAspect {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void requestAspect(){};


    @Around("requestAspect()")
    public Object  around(JoinPoint joinPoint) {

        try{
            return ((ProceedingJoinPoint) joinPoint).proceed();

        }catch (Throwable e){
            log.error("",e );
            return ResultUtil.error(e.getMessage());
        }
    }
}
