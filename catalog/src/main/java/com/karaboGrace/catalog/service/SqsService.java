package com.karaboGrace.catalog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karaboGrace.catalog.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    public void sendOrderNotification(OrderResponse order, String userEmail) {
        try {
            String messageBody = objectMapper.writeValueAsString(
                    new OrderNotificationMessage(
                            order.getId(),
                            userEmail,
                            order.getTotalAmount().toString(),
                            order.getStatus()
                    )
            );

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    // MessageGroupId not needed for standard queues
                    .build();

            sqsClient.sendMessage(request);
            log.info("Order notification sent to SQS for order {} user {}",
                    order.getId(), userEmail);
        } catch (Exception e) {
            // We log but don't fail the order — notification is best-effort
            // This is the key design decision: the order succeeds even if
            // the notification fails. Never let email block a purchase.
            log.error("Failed to send SQS notification for order {}: {}",
                    order.getId(), e.getMessage());
        }
    }

    public record OrderNotificationMessage(
            Long orderId,
            String userEmail,
            String totalAmount,
            String status
    ) {}
}