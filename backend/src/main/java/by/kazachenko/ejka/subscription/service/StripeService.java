package by.kazachenko.ejka.subscription.service;

import by.kazachenko.ejka.subscription.dto.response.PlanResponse;
import com.stripe.Stripe;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.checkout.Session;
import com.stripe.param.PriceRetrieveParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StripeService {

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    @Value("${STRIPE_PRICE_ID}")
    private String priceId;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public String createCheckoutSession(UUID userId, String userEmail, String stripeCustomerId) throws Exception {
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setClientReferenceId(userId.toString())
                .setSuccessUrl(frontendUrl + "/subscription?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/subscription?error=payment_cancelled")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPrice(priceId)
                                .build()
                );

        if (stripeCustomerId != null && !stripeCustomerId.trim().isEmpty()) {
            paramsBuilder.setCustomer(stripeCustomerId);
        } else {
            paramsBuilder.setCustomerEmail(userEmail);
        }

        Session session = Session.create(paramsBuilder.build());
        return session.getUrl();
    }

    public PlanResponse getPlanDetails() throws Exception {
        PriceRetrieveParams params = PriceRetrieveParams.builder()
                .addExpand("product")
                .build();

        Price stripePrice = Price.retrieve(priceId, params, null);
        Product stripeProduct = stripePrice.getProductObject();

        return PlanResponse.builder()
                .id(stripePrice.getId())
                .name(stripeProduct.getName())
                .price(stripePrice.getUnitAmount() / 100.0)
                .currency(stripePrice.getCurrency().toUpperCase())
                .interval(stripePrice.getRecurring() != null ? stripePrice.getRecurring().getInterval() : "month")
                .build();
    }

    public void cancelSubscription(String stripeCustomerId) throws Exception {
        SubscriptionCollection subscriptions = Subscription.list(
                SubscriptionListParams.builder().setCustomer(stripeCustomerId).build()
        );

        for (Subscription sub : subscriptions.getData()) {
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
            sub.update(params);
        }
    }
}
