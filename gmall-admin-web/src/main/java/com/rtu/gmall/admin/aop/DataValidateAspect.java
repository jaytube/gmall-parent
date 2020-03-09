package com.rtu.gmall.admin.aop;


import com.rtu.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Slf4j
@Aspect
@Component
public class DataValidateAspect {

    @Around("execution(* com.rtu.gmall.admin..*Controller.*(..))")
    public Object validateAround(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        for(Object arg : args) {
            if(arg instanceof BindingResult) {
                BindingResult r = (BindingResult) arg;
                if(r.getErrorCount() >0 )
                    return new CommonResult().validateFailed(r);
            }
        }
        return point.proceed(args);

    }
}
