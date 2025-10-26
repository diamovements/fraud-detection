package hackathon.project.fraud_detection.notifications;

import hackathon.project.fraud_detection.rules.model.FraudPredictionResult;
import hackathon.project.fraud_detection.security.service.UserService;
import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class SendNotificationsClient {

    private final RestTemplate restTemplate;
    private final String url;
    private final UserService userService;

    public SendNotificationsClient(
            RestTemplate restTemplate,
            @Value("${notifications.url}") String url, UserService userService
    ) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.userService = userService;
    }

    public void sendNotifications(TransactionEntity transactionEntity, FraudPredictionResult prediction) {
        TelegramAlertRequest requestBot = new TelegramAlertRequest(
                transactionEntity.getOriginalTransactionId(),
                transactionEntity.getSenderAccount(),
                transactionEntity.getAmount(),
                prediction.fraudProbability(),
                List.of(transactionEntity.getTriggeredRules()),
                userService.getAllUserTelegramIds()
        );

        restTemplate.postForEntity(url + "/send-telegram-alert",
                requestBot, String.class);

        EmailAlertRequest requestEmail = new EmailAlertRequest(
                userService.getAll().getFirst().getLogin(),
                transactionEntity.getOriginalTransactionId(),
                transactionEntity.getSenderAccount(),
                transactionEntity.getAmount(),
                prediction.fraudProbability(),
                List.of(transactionEntity.getTriggeredRules()),
                userService.getAllUserEmails()
        );

        restTemplate.postForEntity(url + "/send-transaction-alert",
                requestEmail, String.class);
    }
}
