package by.kazachenko.ejka.subscription.controller;

import by.kazachenko.ejka.common.exception.ExceptionMessages;
import by.kazachenko.ejka.common.exception.cutom.UserNotFoundException;
import by.kazachenko.ejka.common.security.CustomUserDetails;
import by.kazachenko.ejka.subscription.dto.response.PlanResponse;
import by.kazachenko.ejka.subscription.service.StripeService;
import by.kazachenko.ejka.user.model.User;
import by.kazachenko.ejka.user.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final StripeService stripeService;
    private final UserRepository userRepository;

    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String webhookSecret;

    @GetMapping("/plan")
    public ResponseEntity<PlanResponse> getPlanDetails() {
        try {
            PlanResponse plan = stripeService.getPlanDetails();
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            log.error("Ошибка при получении данных о тарифе из Stripe: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            User user = userRepository.findById(UUID.fromString(currentUser.getId()))
                    .orElseThrow(() -> new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND));

            String sessionUrl = stripeService.createCheckoutSession(
                    user.getId(),
                    user.getEmail(),
                    user.getStripeCustomerId()
            );
            return ResponseEntity.ok(Map.of("url", sessionUrl));
        } catch (Exception e) {
            log.error("Ошибка создания сессии оплаты: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            User user = userRepository.findById(UUID.fromString(currentUser.getId())).orElseThrow();
            if (user.getStripeCustomerId() != null) {
                stripeService.cancelSubscription(user.getStripeCustomerId());
                user.setCancelAtPeriodEnd(true);
                userRepository.save(user);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().body("No active subscription found");
        } catch (Exception e) {
            log.error("Ошибка при отмене подписки: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Ошибка подписи вебхука", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        log.info("Stripe Event: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event);
                break;
            default:
                break;
        }

        return ResponseEntity.ok("Success");
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = getStripeObject(event, Session.class);
        if (session == null || session.getClientReferenceId() == null) return;

        try {
            UUID userId = UUID.fromString(session.getClientReferenceId());
            User user = userRepository.findById(userId).orElseThrow();

            user.setIsPremium(true);
            user.setCancelAtPeriodEnd(false);
            user.setStripeCustomerId(session.getCustomer());
            user.setPremiumUntil(LocalDate.now().plusDays(30));
            userRepository.save(user);

            log.info("Premium активирован для: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Ошибка обновления пользователя (checkout completed): ", e);
        }
    }

    private void handleSubscriptionDeleted(Event event) {
        Subscription subscription = getStripeObject(event, Subscription.class);
        if (subscription == null || subscription.getCustomer() == null) return;

        try {
            Optional<User> userOpt = userRepository.findByStripeCustomerId(subscription.getCustomer());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setIsPremium(false);
                user.setCancelAtPeriodEnd(false);
                user.setPremiumUntil(null);
                userRepository.save(user);

                log.info("Подписка полностью удалена для: {}", user.getEmail());
            }
        } catch (Exception e) {
            log.error("Ошибка обновления пользователя (subscription deleted): ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getStripeObject(Event event, Class<T> type) {
        if (event.getDataObjectDeserializer().getObject().isPresent()) {
            return (T) event.getDataObjectDeserializer().getObject().get();
        } else {
            try {
                return (T) event.getDataObjectDeserializer().deserializeUnsafe();
            } catch (Exception e) {
                log.error("Ошибка десериализации объекта Stripe", e);
                return null;
            }
        }
    }
}