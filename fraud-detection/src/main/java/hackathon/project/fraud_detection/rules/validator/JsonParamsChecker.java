package hackathon.project.fraud_detection.rules.validator;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.RuleType;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public abstract class JsonParamsChecker {

    public abstract boolean checkJsonParams(String params, RuleType ruleType);

     Map<String, Object> parseJsonParams(String params) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(params, new tools.jackson.core.type.TypeReference<>() {});
    }

    abstract boolean hasRequiredFields(String params);
}
