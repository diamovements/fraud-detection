package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.engine.dsl.DslParser;
import hackathon.project.fraud_detection.rules.engine.dsl.EvaluationContext;
import hackathon.project.fraud_detection.rules.engine.dsl.Expression;
import hackathon.project.fraud_detection.rules.engine.dsl.ExpressionParser;
import hackathon.project.fraud_detection.rules.model.Operator;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.rules.model.RuleType;
import hackathon.project.fraud_detection.rules.service.RuleEngine;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class CompositeRule extends Rule {

    private final UUID id;
    private final int priority;
    private final boolean enabled;
    private final String expression;
    private final RuleType ruleType;
    private final String name;
    private final Expression ast;
    private final SubRuleEvaluationService subRuleEvaluationService;
    private final DslParser dslParser;

    public CompositeRule(UUID id, int priority, boolean enabled, String name, String params, SubRuleEvaluationService subRuleEvaluationService, DslParser dslParser) {
        this.id = id;
        this.priority = priority;
        this.enabled = enabled;
        this.expression = (String) parseJsonParams(params).get("expr");
        this.name = name;
        this.subRuleEvaluationService = subRuleEvaluationService;
        this.dslParser = dslParser;
        this.ast = compileExpression();
        this.ruleType = RuleType.THRESHOLD;
    }

    @Override
    public RuleResult evaluate(TransactionRequest transactionRequest) {
        EvaluationContext evaluationContext = new EvaluationContext(
                transactionRequest,
                dslParser,
                subRuleEvaluationService
        );
        return new RuleResult(ast.evaluate(evaluationContext), "composite rule triggered");
    }

    private Expression compileExpression() {
        ExpressionParser parser = new ExpressionParser(expression);
        return parser.parse();
    }
}
