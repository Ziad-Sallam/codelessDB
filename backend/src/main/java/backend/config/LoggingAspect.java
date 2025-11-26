package backend.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

   // logged files only
   private static final String LOG_FILES = """
      execution(* backend.user.UserService.*(..)) ||
      execution(* backend.user.UserController.*(..)) ||
      execution(* backend.userDiagramManagement.controller.UserDiagramController.*(..)) ||
      execution(* backend.SQLGeneration.controller.SchemaController.*(..))
   """;

   // Log method entry
   @Before(LOG_FILES)
   public void logBefore(JoinPoint joinPoint) {
      log.info("\n -> Entering: {} \n args = {}\n",
            joinPoint.getSignature(),
            joinPoint.getArgs());
   }

   // Log method exit
   @AfterReturning(pointcut = LOG_FILES, returning = "result")
   public void logAfterReturning(JoinPoint joinPoint, Object result) {
      log.info("\n -> Exiting: {} \n return = {}\n",
            joinPoint.getSignature(),
            result);
   }

   // Log exceptions
   @AfterThrowing(pointcut = LOG_FILES, throwing = "error")
   public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
      log.error("\n ---> Exception in: {} \n  message = {}\n",
            joinPoint.getSignature(),
            error.getMessage());
   }
}
