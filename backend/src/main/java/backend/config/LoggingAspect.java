package backend.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // Log method entry
    @Before("execution(* backend..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Entering method: {} with arguments: {}",
                 joinPoint.getSignature(),
                 joinPoint.getArgs());
    }

    // Log method exit
    @AfterReturning(pointcut = "execution(* backend..*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Exiting method: {} with result: {}",
                 joinPoint.getSignature(),
                 result);
    }

    // Log exceptions
    @AfterThrowing(pointcut = "execution(* backend..*(..))", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        log.error("Exception in method: {} with cause: {}",
                  joinPoint.getSignature(),
                  error.getMessage(), error);
    }
}