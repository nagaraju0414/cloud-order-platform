34. Senior Interview Questions — Day 9

These are the questions you should be able to answer without code.

Resilience
Why do we need timeout?
What happens if we don't configure timeout?
Retry vs Circuit Breaker?
What is exponential backoff?
Why jitter?
What is retry storm?
What is Circuit Breaker?
Explain CLOSED, OPEN and HALF_OPEN.
What is Bulkhead?
What is cascading failure?
How would you protect an external payment API?
When should you NOT retry?
How do retries affect latency?
How can retries overload a system?
How do you configure a Circuit Breaker?
What is fallback?
Should fallback return success when payment fails?
How do you prevent resource exhaustion?
Kafka
How do you handle Kafka consumer failure?
What is a Dead Letter Topic?
What is a poison message?
How do you retry Kafka messages?
Why is idempotency required with Kafka?
What happens if consumer crashes before ACK?
What happens if consumer crashes after DB update but before ACK?
How do you prevent duplicate payment?
How do you replay a DLT message safely?
Observability
What is a correlation ID?
Correlation ID vs Event ID?
How do you trace a request across microservices?
What is distributed tracing?
What are the four golden signals?
What metrics would you monitor for Kafka?
What metrics would you monitor for a payment service?
Liveness vs readiness?
Why is readiness important in Kubernetes?
What information should structured logs contain?
35. Most Important Interview Scenario
    Interviewer:

"Your Payment Service calls an external payment gateway. The gateway becomes slow and starts returning 503 errors. How would you design the service?"

Strong senior answer:

"First I would configure a strict timeout so requests don't wait indefinitely. For transient failures such as 503, I would use a bounded retry policy with exponential backoff and jitter. I would put a circuit breaker around the downstream dependency so that after the configured failure threshold, calls fail fast rather than continuing to overload the gateway. I would use bulkhead isolation if payment calls could otherwise exhaust shared resources. Since payment processing is part of an asynchronous Saga, I would persist the payment state and use an outbox for reliable event publication. Kafka consumers would be idempotent because delivery can be at least once. I would send poison messages to a DLT and monitor circuit-breaker state, latency, error rate, retry count, and Kafka consumer lag. Correlation and trace IDs would allow the entire order workflow to be traced."

That is a senior/principal-level answer.

36. Day 9 Quick Revision Card

Memorize this:

TIMEOUT
↓
Don't wait forever


RETRY
↓
Transient failure


BACKOFF + JITTER
↓
Prevent retry storm


CIRCUIT BREAKER
↓
Fail fast when dependency is unhealthy


BULKHEAD
↓
Protect resources


KAFKA
↓
Async communication


IDEMPOTENCY
↓
Duplicate messages are safe


DLT
↓
Poison message isolation


CORRELATION ID
↓
Track one business workflow


TRACE ID
↓
Trace across services


METRICS
↓
Know what is happening


READINESS
↓
Can receive traffic?


LIVENESS
↓
Should Kubernetes restart it?
37. Day 9 Final Architecture

You now have the foundation of a production-grade interview project:

                         ┌──────────────┐
                         Trace/Correlation
                                │
                                ▼
                     ┌────────────────────┐
                     │   Order Service    │
                     │                    │
                     │ DynamoDB + Outbox  │
                     └─────────┬──────────┘
                               │
                         OrderCreated
                               │
                               ▼
                         ┌───────────┐
                         │   Kafka   │
                         └─────┬─────┘
                               │
                               ▼
                  ┌────────────────────────┐
                  │   Inventory Service    │
                  │                        │
                  │ Conditional Writes     │
                  │ Idempotency            │
                  │ Compensation            │
                  └───────────┬────────────┘
                              │
                       InventoryReserved
                              │
                              ▼
                         ┌───────────┐
                         │   Kafka   │
                         └─────┬─────┘
                               │
                               ▼
                  ┌────────────────────────┐
                  │    Payment Service     │
                  │                        │
                  │ Retry                  │
                  │ Timeout                │
                  │ Circuit Breaker        │
                  │ Bulkhead               │
                  │ Idempotency             │
                  │ DynamoDB + Outbox      │
                  └───────────┬────────────┘
                              │
                       PaymentCompleted
                              │
                              ▼
                         ┌───────────┐
                         │   Kafka   │
                         └─────┬─────┘
                               │
                               ▼
                     ┌────────────────────┐
                     │   Order Service    │
                     │                    │
                     │   COMPLETED        │
                     └────────────────────┘


                  Failure Path
                        │
                 PaymentFailed
                        ↓
                Release Inventory
                        ↓
                InventoryReleased
                        ↓
                 Order CANCELLED
Day 9 checkpoint

Before Day 10, you should be able to explain this entire sentence:

"My microservices communicate asynchronously through Kafka using at-least-once delivery and idempotent consumers. Local state and events are made reliable using the transactional outbox. The Order workflow is implemented as a Saga with compensating transactions. Synchronous external dependencies are protected with timeouts, bounded retries with exponential backoff and jitter, circuit breakers and bulkheads. Production observability is provided through correlation/trace IDs, structured logging, metrics, health checks and distributed tracing."

If you can explain that confidently and draw the architecture on a whiteboard, you're moving from "Spring Boot developer who knows Kafka" toward a senior microservices/system-design interview profile.