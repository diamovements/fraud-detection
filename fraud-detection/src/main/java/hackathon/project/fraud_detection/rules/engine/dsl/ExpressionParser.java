package hackathon.project.fraud_detection.rules.engine.dsl;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class ExpressionParser {

    private static final Pattern RULE_NAME_PATTERN = Pattern.compile(
            "^[a-zA-Z][a-zA-Z0-9_]*(_[a-zA-Z0-9]+)*$"
    );

    private static final Pattern FUNCTION_CALL_PATTERN = Pattern.compile(
            "([a-zA-Z_][a-zA-Z0-9_]*)\\(([^)]*)\\)"
    );

    private static final Set<String> DSL_FUNCTIONS = Set.of(
            "isNighttime", "isAmountMore", "isAmountLess", "isSuspiciousMerchant"
    );

    private static final Set<String> KEYWORDS = Set.of("AND", "OR", "NOT");

    private final List<String> tokens;
    private int position;

    public ExpressionParser(String expression) {
        this.tokens = tokenize(expression);
        this.position = 0;
    }

    //реализация ast (проход по поддеревьям выражения)
    public Expression parse() {
        return parseExpression();
    }

    private Expression parseExpression() {
        return parseOr();
    }

    private Expression parseOr() {
        Expression left = parseAnd();

        while (match("OR")) {
            Expression right = parseAnd();
            left = new OrExpression(left, right);
        }
        return left;
    }

    private Expression parseAnd() {
        Expression left = parsePrimary();

        while (match("AND")) {
            Expression right = parsePrimary();
            left = new AndExpression(left, right);
        }
        return left;
    }

    private Expression parsePrimary() {
        if (match("NOT")) {
            return new NotExpression(parsePrimary());
        }

        if (match("(")) {
            Expression expr = parseExpression();
            expect(")");
            return expr;
        }

        String token = peek();

        // если функция из списка dsl
        if (DSL_FUNCTIONS.contains(token)) {
            next();
            //текущий токен - скобка, значит функция с аргументом
            if (position < tokens.size() && "(".equals(tokens.get(position))) {
                StringBuilder functionCall = new StringBuilder(token);
                functionCall.append("(");
                position++;

                int parenCount = 1;
                // формируем вызов функции чтобы передать в парсер функций
                while (position < tokens.size() && parenCount > 0) {
                    String part = tokens.get(position);
                    functionCall.append(part);
                    if ("(".equals(part)) {
                        parenCount++;
                    } else if (")".equals(part)) {
                        parenCount--;
                    }
                    position++;
                }
                return parseFunctionCall(functionCall.toString());
            } else {
                // если функция без аргументов - просто ее вызываем
                return new FunctionCall(token, new Object[0]);
            }
        }

        if (FUNCTION_CALL_PATTERN.matcher(token).matches()) {
            next();
            return parseFunctionCall(token);
        }

        // мэтчинг по имени правила из бд
        if (RULE_NAME_PATTERN.matcher(token).matches() &&
                !KEYWORDS.contains(token.toUpperCase()) &&
                !DSL_FUNCTIONS.contains(token)) {
            next();
            return new RuleReference(token);
        }

        throw new RuntimeException("Unexpected token: " + token);
    }

    private Expression parseFunctionCall(String functionCall) {
        var matcher = FUNCTION_CALL_PATTERN.matcher(functionCall);
        if (matcher.find()) {
            String functionName = matcher.group(1);
            String argsStr = matcher.group(2);

            if (!DSL_FUNCTIONS.contains(functionName)) {
                throw new RuntimeException("Unknown function: " + functionName);
            }

            Object[] arguments = parseArguments(argsStr);
            return new FunctionCall(functionName, arguments);
        }
        throw new RuntimeException("Invalid function call: " + functionCall);
    }
//тут пусть будет прсинг аргументов как массива тк в теории можем потом добавить функции с неск аргументами
    private Object[] parseArguments(String argsStr) {
        if (argsStr.trim().isEmpty()) {
            return new Object[0];
        }

        List<Object> args = new ArrayList<>();
        String[] parts = argsStr.split(",");

        for (String part : parts) {
            String trimmed = part.trim();
            try {
                if (trimmed.contains(".")) {
                    args.add(Double.parseDouble(trimmed));
                } else {
                    args.add(Integer.parseInt(trimmed));
                }
            } catch (NumberFormatException e) {
                args.add(trimmed);
            }
        }
        return args.toArray();
    }

    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : expression.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else if ((c == '(' || c == ')' || c == ',') && !inQuotes) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                tokens.add(String.valueOf(c));
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        log.info("Tokenized expression: {}", tokens);
        return tokens;
    }

    private boolean match(String expected) {
        if (position < tokens.size() && tokens.get(position).equalsIgnoreCase(expected)) {
            position++;
            return true;
        }
        return false;
    }

    private void expect(String expected) {
        if (!match(expected)) {
            throw new RuntimeException("Expected: " + expected + ", but found: " + peek());
        }
    }

    private String peek() {
        return position < tokens.size() ? tokens.get(position) : "";
    }

    private void next() {
        if (position < tokens.size()) {
            position++;
        }
    }
}
