package hackathon.project.fraud_detection.rules.engine.dsl;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.engine.SubRuleEvaluationService;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public record EvaluationContext(TransactionRequest transactionRequest, DslParser dslParser, SubRuleEvaluationService subRuleEvaluationService) {

    // движок подправил
    public RuleResult evaluateSingleRule(String name) {
        log.info("Evaluating sub-rule with name: {}", name);
        return subRuleEvaluationService.evaluateSingleRule(name, transactionRequest);
    }

    // движок dsl
    public boolean evaluateFunction(String functionName, Object arguments) {
        try {
            return switch (functionName) {
                case "isNighttime" -> dslParser.isNighttime(transactionRequest);
                case "isAmountMore" -> {
                    BigDecimal threshold = new BigDecimal(arguments.toString());
                    yield dslParser.isAmountMore(threshold, transactionRequest);
                }
                case "isAmountLess" -> {
                    BigDecimal threshold = new BigDecimal(arguments.toString());
                    yield dslParser.isAmountLess(threshold, transactionRequest);
                }
                case "isSuspiciousMerchant" -> dslParser
                        .isSuspiciousMerchant(transactionRequest);
                default -> throw new IllegalArgumentException("Unknown function: " + functionName);
            };
        } catch (Exception e) {
            log.error("Error while evaluating function {}: {}",
                    functionName, e.getMessage()
            );
            return false;
        }
    }
}
