package io.mtc.facade.api.aspect;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class LogAspect {

    @Pointcut("@annotation(io.mtc.facade.api.annotations.SystemLog)")
    public void controllerAspect(){};


    @Around("controllerAspect()")
    public Object  around(JoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();
        String requestmethod="";
        String result="";
        try{
            String targetName = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Object[] arguments = joinPoint.getArgs();
            requestmethod = targetName+"."+methodName;
            log.info("[{}]请求开始:参数=[{}]",requestmethod, StringUtils.join(arguments,","));
            Object ret = ((ProceedingJoinPoint) joinPoint).proceed();
            result = ret instanceof String ? ret.toString() : JSON.toJSONString(ret);
            return ret;
        } finally {
            log.info("[{}]请求结束:耗时=[{}]ms,结果=[{}]",requestmethod, System.currentTimeMillis()-startTime,result);
        }
    }
}
