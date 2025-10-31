package hackathon.project.fraud_detection.rules.service;

import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class ExporterService {

    public void exportToCsv(List<TransactionEntity> transactions, HttpServletResponse response, UserDetails userDetails) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = "transactions_" + timestamp + ".csv";

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");
        response.setHeader("X-Exported-By", userDetails.getUsername());
        response.setHeader("X-Export-Timestamp", LocalDateTime.now().toString());
        response.setHeader("X-Total-Records", String.valueOf(transactions.size()));
        try (PrintWriter writer = response.getWriter()) {
            writer.write("UUID;CorrelationId;Id транзакции;Дата;Сумма;Тип транзакции;Категория;Отправитель;Получатель;Статус;Подозрительная;IP-адрес;Локация;Устройство;Сработавшие правила;Дата обработки\n");

            for (TransactionEntity transaction : transactions) {
                writer.write(convertToCsvRow(transaction) + "\n");
            }
            log.info("Exported {} transactions to CSV", transactions.size());
        } catch (Exception e) {
            log.error("Error exporting transactions to CSV", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error exporting transactions");
        }
    }

    private String convertToCsvRow(TransactionEntity transaction) {
        return String.join(";",
                escapeCsvField(transaction.getId().toString()),
                escapeCsvField(transaction.getCorrelationId()),
                escapeCsvField(transaction.getOriginalTransactionId()),
                escapeCsvField(transaction.getTimestamp() != null ?
                        transaction.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : ""),
                escapeCsvField(transaction.getAmount() != null ?
                        transaction.getAmount().toString() : ""),
                escapeCsvField(transaction.getTransactionType()),
                escapeCsvField(transaction.getMerchantCategory()),
                escapeCsvField(transaction.getSenderAccount()),
                escapeCsvField(transaction.getReceiverAccount()),
                escapeCsvField(transaction.getStatus().toString()),
                escapeCsvField(String.valueOf(transaction.getSuspicious())),
                escapeCsvField(transaction.getIpAddress()),
                escapeCsvField(transaction.getLocation()),
                escapeCsvField(transaction.getDeviceUsed()),
                escapeCsvField(transaction.getTriggeredRules()),
                escapeCsvField(transaction.getProcessedAt() != null ?
                        transaction.getProcessedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
        );
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(";") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
