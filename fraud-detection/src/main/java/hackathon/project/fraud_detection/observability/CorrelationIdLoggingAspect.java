package hackathon.project.fraud_detection.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class CorrelationIdLoggingAspect {

    @Around("within(hackathon.project.fraud_detection.rules..*)")
    public Object logWithCorrelationId(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String correlationId = MDC.get("correlationId");

        if (log.isDebugEnabled()) {
            log.debug("[correlationId:{}] {}.{} started", correlationId, className, methodName);
        }

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("[correlationId:{}] {}.{} failed: {}", correlationId, className, methodName, e.getMessage());
            throw e;
        }
    }
}
