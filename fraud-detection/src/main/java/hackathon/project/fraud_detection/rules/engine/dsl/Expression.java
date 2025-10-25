package hackathon.project.fraud_detection.rules.engine.dsl;

import lombok.RequiredArgsConstructor;

public abstract class Expression {
    public abstract boolean evaluate(EvaluationContext context);
}

@RequiredArgsConstructor
class AndExpression extends Expression {

    private final Expression left;
    private final Expression right;

    @Override
    public boolean evaluate(EvaluationContext context) {
        return left.evaluate(context) && right.evaluate(context);
    }
}

@RequiredArgsConstructor
class OrExpression extends Expression {

    private final Expression left;
    private final Expression right;

    @Override
    public boolean evaluate(EvaluationContext context) {
        return left.evaluate(context) || right.evaluate(context);
    }
}

@RequiredArgsConstructor
class NotExpression extends Expression {

    private final Expression exp;

    @Override
    public boolean evaluate(EvaluationContext context) {
        return !exp.evaluate(context);
    }
}

@RequiredArgsConstructor
class FunctionCall extends Expression {

    private final String functionName;
    private final Object[] arguments;

    @Override
    public boolean evaluate(EvaluationContext context) {
        return context.evaluateFunction(functionName, arguments);
    }
}