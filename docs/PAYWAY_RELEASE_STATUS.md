# ABA PayWay Integration — Release and Verification Notes

## Implemented

- Server creates a PENDING order before generating a PayWay QR.
- Order/payment records persist in PostgreSQL (`payment_orders`, `payment_order_items`).
- Browser-supplied price/title are ignored; asset IDs are resolved against the backend catalog.
- Merchant ID/API key remain backend environment variables.
- PayWay QR request is signed server-side with HMAC-SHA512.
- Callback is treated as notification only and cannot directly set PAID.
- Every payment confirmation uses PayWay Check Transaction server-to-server.
- PAID requires payment_status_code=0, payment_status=APPROVED, and matching stored amount/currency.
- Duplicate callbacks do not create duplicate orders/unlocks.
- Purchased asset access requires a persisted PAID order with a verification timestamp.
- Frontend handles pending/verification, failed/cancelled, retry, and verified success.
- CORS is restricted to configured marketplace frontend origins.
- No card/PIN/OTP/payment credential storage is implemented.

## Required environment variables

Backend: DATABASE_URL, PAYWAY_ENVIRONMENT, PAYWAY_MERCHANT_ID, PAYWAY_API_KEY, PAYWAY_QR_URL (optional), PAYWAY_CHECK_URL (optional), PAYWAY_CALLBACK_URL, MARKETPLACE_CORS_ORIGINS.
Frontend: VITE_API_URL.

## Sandbox setup

1. Use PayWay sandbox merchant credentials.
2. Whitelist the API host/IP/domain with PayWay.
3. Set callback URL to https://ai-asset-marketplace-api.onrender.com/api/payway/callback
4. Keep production credentials/endpoints separate from sandbox values.

## QA scenarios

- Successful payment stays PENDING until Check Transaction returns APPROVED with matching amount/currency, then access unlocks.
- Pending, failed, or cancelled payment does not unlock.
- Amount or currency mismatch does not unlock.
- Repeated callback does not create duplicate order/unlock.
- Refresh/back/repeated payment attempts do not let browser state forge PAID.
- Wrong buyer email on order/access endpoint is denied.
- Regression-test cart, order history, and responsive PayWay UI.

## Automated verification

`PaymentVerifierTest` covers approved matching transaction, forged amount rejection, currency mismatch rejection, and pending rejection.

## Remaining production gate

The Render API must have DATABASE_URL configured before end-to-end PayWay QA can pass. The Neon database/schema is already created. The connected tools do not allow transferring this secret directly between Neon and Render, so this one secret must be copied in the Render dashboard.