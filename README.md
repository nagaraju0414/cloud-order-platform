Absolutely. Below are Day 5 senior-level interview notes designed so you can explain each concept clearly in an interview—not just memorize definitions. I’ll use a consistent pattern: What → Why → How → Example → Interview answer → Common pitfalls.

Day 5 — Senior Microservices Interview Concepts
1. DynamoDB
5
1.1 Partition Key

The partition key is the attribute DynamoDB uses to determine where an item is physically distributed.

For a table with only a partition key:

User
-----
userId   ← Partition Key
name
email

Example:

userId = USER1001
name   = Ravi
email  = ravi@example.com

DynamoDB hashes userId and uses that hash to determine the storage partition.

Why is it important?

Because DynamoDB is designed for horizontal scalability.

Instead of putting all records on one database server:

                    DynamoDB
                       |
          +------------+------------+
          |            |            |
       Partition 1  Partition 2  Partition 3
          |            |            |
       Users A-F    Users G-M    Users N-Z

The partition key determines how evenly traffic and data are distributed.

Good partition key

A good partition key has high cardinality.

Good:

customerId
orderId
userId
deviceId

Potentially bad:

country
gender
status

Why?

If millions of requests use:

status = ACTIVE

the same partition-key value can become extremely hot.

Interview answer

"The DynamoDB partition key determines the logical distribution of items across storage partitions. I choose a high-cardinality key that distributes both data and traffic evenly. The key should be selected based on the application's access patterns rather than simply based on normalization."

1.2 Sort Key

A sort key is used together with the partition key.

Example:

PK              SK
-----------------------------
CUSTOMER#1001   ORDER#001
CUSTOMER#1001   ORDER#002
CUSTOMER#1001   ORDER#003
CUSTOMER#1002   ORDER#004

All items having:

PK = CUSTOMER#1001

belong to the same logical partition-key collection.

The sort key allows DynamoDB to organize and query those items.

For example:

PK = CUSTOMER#1001
SK begins_with ORDER#

can retrieve all orders for that customer.

Very important

DynamoDB doesn't work like a traditional relational database where you freely query any column.

You normally design the table around:

How will the application query the data?

For example:

Get all orders for customer
        ↓
PK = CUSTOMER#1001

or:

Get orders after a specific date
        ↓
PK = CUSTOMER#1001
SK > 2026-01-01
Interview answer

"The sort key allows multiple related items to share the same partition key while maintaining an ordered key space. It is particularly useful for time-series data, hierarchical data, and range queries."

1.3 GSI — Global Secondary Index

A GSI allows you to query a DynamoDB table using an alternative key structure.

Suppose your main table is:

PK = customerId
SK = orderId

But you need:

Find orders by status

You could create:

GSI1PK = status
GSI1SK = createdAt

Now you can query:

status = SHIPPED

instead of scanning the entire table.

Example

Main table:

customerId | orderId | status | createdAt
------------------------------------------
C101       | O001    | SHIPPED| 2026-08-01
C102       | O002    | NEW    | 2026-08-02
C103       | O003    | SHIPPED| 2026-08-03

GSI:

GSI_PK = status
GSI_SK = createdAt

Query:

GSI_PK = SHIPPED
Important interview point

A GSI is not simply a traditional SQL index.

It is essentially another queryable key structure maintained by DynamoDB.

You should create GSIs based on real access patterns.

Don't create indexes for every column.

Interview answer

"A GSI provides an alternate access path to DynamoDB data using a different partition and optional sort key. I design GSIs based on known query patterns, because DynamoDB is access-pattern driven."

1.4 Access-Pattern-Driven Design

This is one of the most important DynamoDB interview concepts.

In relational database design, you might start with:

Customer
Order
Product
Payment

and normalize them.

With DynamoDB, you first ask:

"What queries does the application need to execute?"

For example:

Q1: Get order by orderId
Q2: Get all orders for customer
Q3: Get order by paymentId
Q4: Get orders by status

Then design the table around those queries.

Example:

PK                  SK
--------------------------------
ORDER#1001          METADATA
ORDER#1001          ITEM#1
ORDER#1001          ITEM#2


CUSTOMER#101        ORDER#1001
CUSTOMER#101        ORDER#1002

You may intentionally duplicate data.

This is called denormalization.

Why?

Because DynamoDB prioritizes:

Predictable performance
        +
Scalability
        +
Known access patterns

rather than relational normalization.

Senior interview statement

"With DynamoDB I don't start by modeling entities. I start by identifying access patterns, expected read/write traffic, cardinality, and consistency requirements, then design the partition key, sort key and GSIs around those patterns."

That's a strong senior-level answer.

1.5 TransactWriteItems

DynamoDB normally provides atomic operations at the item level.

But sometimes you need multiple writes to succeed or fail together.

Example:

Create Order
     +
Update Inventory
     +
Create Payment Record

If the first succeeds but the second fails, your system can become inconsistent.

TransactWriteItems allows multiple DynamoDB writes to execute atomically.

Conceptually:

Transaction
   |
   +-- Put Order
   |
   +-- Update Inventory
   |
   +-- Put Payment

Either:

ALL SUCCESS

or:

ALL ROLLBACK
Important

DynamoDB transactions are useful when the items are within the supported transaction constraints.

They are not a replacement for distributed transactions across:

DynamoDB
Kafka
Payment Service
External API

You still need patterns such as:

Transactional Outbox
Saga
Idempotency

for distributed workflows.

Interview answer

"TransactWriteItems provides atomicity across multiple DynamoDB write operations. I use it when multiple changes within DynamoDB must commit together, but I don't treat it as a distributed transaction across microservices."

1.6 Conditional Writes

A conditional write allows DynamoDB to perform an operation only when a condition is satisfied.

Example:

Update inventory
IF quantity >= requestedQuantity

Suppose:

quantity = 5
requested = 3

The operation succeeds:

5 → 2

But if:

quantity = 2
requested = 3

the update fails.

Another example

Prevent duplicate order creation:

Put order
IF attribute_not_exists(orderId)

This is extremely useful for idempotency.

Interview answer

"Conditional writes allow DynamoDB to enforce business conditions atomically at the database level. I commonly use them for optimistic concurrency, preventing duplicate records, inventory checks, and idempotency."

2. Kafka
5
2.1 Topic

A Kafka topic is a logical stream/category of events.

Example:

order-created
payment-completed
inventory-updated

Producer:

Order Service
     |
     | OrderCreated
     ↓
Kafka
     |
     ↓
order-created topic

Consumers can subscribe to the topic.

2.2 Partition

A topic is divided into partitions.

order-created
     |
     +---- Partition 0
     +---- Partition 1
     +---- Partition 2

Partitions provide:

scalability
parallelism
ordering within a partition
Critical interview point

Kafka guarantees ordering within a partition, not across the entire topic.

For example:

Partition 0:


O1 → O2 → O3 → O4

Ordering is maintained there.

But:

Partition 0: O1 O3
Partition 1: O2 O4

does not provide global ordering.

Choosing a partition key

For orders:

key = customerId

might ensure events for the same customer go to the same partition.

Example:

customer A → Partition 2
customer A → Partition 2
customer A → Partition 2

Therefore:

A1 → A2 → A3

maintains order.

2.3 Producer

A Kafka producer publishes records.

Example:

Order Service
     |
     | OrderCreated
     ↓
Kafka Producer
     |
     ↓
order-created topic

A producer can configure:

acks
retries
idempotence
compression
batching
partitioning

For production systems, these settings matter significantly.

2.4 Consumer

A consumer reads messages from Kafka.

Kafka
  |
  ↓
Consumer
  |
  ↓
Order processing

The consumer maintains an offset indicating its progress.

2.5 Consumer Group

A consumer group allows multiple consumers to process partitions in parallel.

Suppose:

Topic
Partitions = 4

and:

Consumer Group A
C1
C2
C3
C4

Kafka can assign:

C1 → P0
C2 → P1
C3 → P2
C4 → P3

This provides parallel processing.

Critical interview question

Can two consumers in the same consumer group consume the same partition simultaneously?

Normally:

No.

One partition is assigned to one consumer within a consumer group at a time.

But two different consumer groups can independently consume the same topic.

Example:

order-created
       |
       +-------- Payment Group
       |
       +-------- Inventory Group
       |
       +-------- Notification Group

Each group gets its own consumption progress.

2.6 Offset

An offset identifies a record's position within a Kafka partition.

Example:

Partition 0


Offset:
0 → Order A
1 → Order B
2 → Order C
3 → Order D

Consumer processes:

0
1
2

and commits offset:

3

Meaning:

The consumer has successfully processed records before offset 3 and will resume from 3.

Important

Kafka does not normally delete a message immediately after consumption.

Retention is controlled by Kafka configuration.

Therefore another consumer group can read the same events later.

2.7 acks=all

Producer acknowledgement determines how much confirmation the producer requires.

Simplified:

acks=0

Producer doesn't wait for acknowledgement.

Potentially fastest, but weakest durability.

acks=1

Leader acknowledges the record.

acks=all

Leader waits for all required in-sync replicas to acknowledge.

For important production events:

acks=all

is commonly preferred.

But acks=all alone doesn't magically guarantee zero data loss. Durability also depends on replication, ISR configuration, broker settings, and producer behavior.

2.8 Idempotent Producer

Imagine producer sends:

OrderCreated

Network problem occurs.

Producer doesn't receive the acknowledgement.

It retries.

Kafka might otherwise receive:

OrderCreated
OrderCreated

Potential duplicate.

An idempotent producer uses producer/broker mechanisms to prevent duplicate writes caused by producer retries.

Conceptually:

Producer
   |
   | OrderCreated
   ↓
Kafka
   X ACK lost
   |
   ↓
Retry
   |
   ↓
Kafka

With idempotence enabled, Kafka can identify the retry appropriately.

Important distinction

Idempotent producer ≠ idempotent consumer.

Producer idempotence protects the Kafka write path.

Consumer idempotence protects your application's processing.

3. Distributed Systems

This section is especially important for senior interviews.

3.1 Dual-Write Problem

Suppose your Order Service needs to:

save order to DynamoDB
publish Kafka event

Naive implementation:

saveOrder()
   ↓
DynamoDB


publishEvent()
   ↓
Kafka

What happens here?

DynamoDB SUCCESS
Kafka FAILURE

Now database says:

Order = CREATED

but Kafka doesn't contain:

OrderCreated

Your downstream services never receive the event.

The reverse can also happen:

Kafka SUCCESS
DynamoDB FAILURE

Now consumers believe the order exists when it doesn't.

This is the dual-write problem.

3.2 Transactional Outbox

Transactional Outbox is one of the most important patterns for solving dual writes.

Instead of:

DynamoDB
   +
Kafka

directly, you persist the business data and an event/outbox record as part of the same atomic database operation.

Conceptually:

                 Order Service
                      |
               Atomic DB operation
                /              \
               /                \
          Order Record       Outbox Record
               |                 |
               ↓                 ↓
           DynamoDB          Outbox
                                 |
                                 ↓
                              Publisher
                                 |
                                 ↓
                               Kafka

Example:

Orders Table


ORDER#1001
status = CREATED

and:

Outbox


EVENT#10001
type = OrderCreated
status = PENDING

Then an outbox publisher reads pending events:

Outbox
   ↓
Publisher
   ↓
Kafka
Why does this solve the problem?

The important operation becomes:

Order + Outbox Event

written atomically.

Therefore:

Order saved
Outbox saved

or:

Neither saved

The Kafka publication happens asynchronously afterward.

Important DynamoDB consideration

With DynamoDB, the outbox record can be written atomically with the business record using mechanisms such as TransactWriteItems, when the records fit the transaction model.

Interview answer

"The Transactional Outbox pattern avoids the dual-write problem by atomically persisting the business state and the event record in the same datastore transaction. A separate publisher then delivers the outbox event to Kafka. This gives reliable event publication without requiring a distributed transaction between DynamoDB and Kafka."

3.3 At-Least-Once Delivery

At-least-once means:

A message will be delivered one or more times.

So:

Message
   ↓
Consumer
   ↓
Processing SUCCESS
   ↓
ACK/offset commit fails
   ↓
Kafka redelivers

Consumer sees:

OrderCreated
OrderCreated

Therefore your consumer must tolerate duplicates.

Why use at-least-once?

Because guaranteeing no loss is often more important than guaranteeing no duplicates.

Instead of:

Could lose message

we accept:

Could process duplicate

and solve duplicates through idempotency.

3.4 Idempotent Consumer

An idempotent consumer produces the same final result even if it receives the same message multiple times.

Suppose:

PaymentCompleted
eventId = E1001

Consumer receives it twice.

Bad implementation:

chargeCreditCard()

twice.

Customer gets charged twice.

Better approach:

eventId = E1001

Store processed event IDs.

ProcessedEvents


E1001
E1002
E1003

Before processing:

Does E1001 exist?
       |
   +---+---+
   |       |
 YES      NO
   |       |
skip     process
           |
           ↓
       mark processed
Important senior-level issue

Don't do:

if (!processed(eventId)) {
    process();
    markProcessed(eventId);
}

without atomic protection.

Two consumers/retries could race.

You want an atomic mechanism such as:

Put eventId
IF attribute_not_exists(eventId)

or a transaction that combines the idempotency record with the business update where appropriate.

3.5 Eventual Consistency

In distributed systems, different services may temporarily have different views of the data.

Example:

Order Service
Order = CREATED

publishes:

OrderCreated

Inventory Service receives it slightly later.

For a short period:

Order Service → CREATED
Inventory      → NOT RESERVED

Eventually:

Inventory → RESERVED

This is eventual consistency.

Why is it acceptable?

Because distributed systems prioritize:

Availability
Scalability
Loose coupling

rather than requiring every service to be immediately synchronized.

Senior interview answer

"Eventual consistency means that replicas or services may temporarily have different states, but assuming successful propagation and no conflicting updates, they converge to a consistent state."

3.6 Retry

Distributed systems fail temporarily.

Examples:

Kafka unavailable
DynamoDB throttling
Network timeout
External API timeout
Service temporarily overloaded

Instead of immediately failing:

Request
  ↓
Failure
  ↓
Retry

we can use:

Retry 1 → 100 ms
Retry 2 → 500 ms
Retry 3 → 1 sec
Retry 4 → 2 sec

This is exponential backoff.

Usually add jitter:

delay = exponentialBackoff + randomJitter
Why jitter?

Imagine 10,000 clients fail simultaneously.

Without jitter:

10,000 requests
       ↓
retry at exactly 1 sec
       ↓
service overloaded again

This creates a thundering herd.

Jitter spreads retries over time.

3.7 Dead-Letter Handling

Suppose a Kafka consumer repeatedly fails processing:

Message
   ↓
Consumer
   ↓
FAIL
   ↓
Retry
   ↓
FAIL
   ↓
Retry
   ↓
FAIL

If we continue forever, the message may block useful processing or consume excessive resources.

A Dead Letter Topic/Queue can hold messages that cannot be processed after configured attempts.

Example:

order-created
      |
      ↓
 Consumer
      |
      +---- SUCCESS
      |
      +---- FAILURE
              |
          Retry 1
              |
          Retry 2
              |
          Retry 3
              |
              ↓
       order-created.DLT

Then engineers can inspect:

eventId
payload
exception
timestamp
retry count
correlation ID

and determine the problem.

Important

A DLT is not a garbage dump.

You need:

monitoring
alerting
ownership
replay strategy
retention policy
root-cause analysis
4. Putting Everything Together

This is the architecture you should be able to explain in a senior interview.

5

Consider:

                 Client
                   |
                   ↓
             Order Service
                   |
          +--------+--------+
          |                 |
          ↓                 ↓
      Orders Table       Outbox
      DynamoDB           DynamoDB
          |                 |
          |          Transactional Write
          +--------+--------+
                   |
                   ↓
             Outbox Publisher
                   |
                   ↓
                 Kafka
                   |
       +-----------+-----------+
       |           |           |
       ↓           ↓           ↓
 Inventory      Payment    Notification
 Service        Service       Service

The flow is:

1. Client creates order
          ↓
2. Order Service validates request
          ↓
3. DynamoDB transaction
      ├── Save Order
      └── Save Outbox Event
          ↓
4. Outbox Publisher reads event
          ↓
5. Publish OrderCreated to Kafka
          ↓
6. Kafka stores event in partition
          ↓
7. Consumer groups consume event
          ↓
8. Each consumer processes independently
          ↓
9. Consumers commit offsets

If the consumer crashes:

Message processed
      ↓
Offset NOT committed
      ↓
Consumer restarts
      ↓
Message delivered again

Therefore:

At-least-once
      +
Idempotent Consumer

protects the business operation.

5. The Senior-Level Connection

You should understand that these aren't isolated concepts.

They form a chain:

DynamoDB
   |
   | Atomic business + event write
   ↓
Transactional Outbox
   |
   | Reliable publication
   ↓
Kafka
   |
   | At-least-once delivery
   ↓
Consumer
   |
   | Duplicate protection
   ↓
Idempotent Consumer
   |
   | Temporary inconsistency
   ↓
Eventual Consistency
   |
   | Temporary failures
   ↓
Retry + Exponential Backoff + Jitter
   |
   | Poison messages
   ↓
Dead Letter Topic

This is exactly the type of relationship a senior interviewer may test.

6. Common Senior Interview Questions
Q1. Why can't you simply save to DynamoDB and then publish to Kafka?

Because those are two independent systems and there is no single atomic transaction covering both.

Failure between the two operations creates inconsistent state.

Solution: Transactional Outbox.

Q2. Does Kafka guarantee exactly-once processing?

Be careful with this answer.

Kafka provides mechanisms for exactly-once semantics in specific Kafka processing scenarios, but end-to-end exactly-once business processing is much harder, especially when external systems such as DynamoDB are involved.

A safe senior answer is:

"Kafka supports exactly-once semantics for certain Kafka-to-Kafka processing workflows, but I don't assume end-to-end exactly-once behavior when external databases or APIs are involved. I generally design consumers to be idempotent."

Q3. Why is idempotent producer not enough?

Because producer idempotence protects duplicate writes from producer retries.

It does not guarantee that the consumer's business operation won't execute twice.

You still need:

Idempotent Consumer
Q4. Where should the Kafka key come from?

It depends on the ordering requirement.

For example:

key = orderId

ensures events for the same order are routed consistently to the same partition.

If ordering is required per customer:

key = customerId

The key should be selected based on the business ordering requirement and partition distribution.

Q5. What happens if there are more consumers than partitions?

Suppose:

Partitions = 3
Consumers = 5

Only three consumers can actively own partitions.

Conceptually:

C1 → P0
C2 → P1
C3 → P2
C4 → idle
C5 → idle

Therefore:

Maximum parallelism within one consumer group is bounded by the number of partitions.

Q6. What happens if a consumer crashes?

Kafka rebalances the consumer group.

The partition is assigned to another consumer.

The new consumer resumes from the last committed offset.

If processing occurred but the offset wasn't committed:

message can be processed again

Hence:

At-least-once + Idempotency

is a common design.

Q7. Why not use DynamoDB Streams instead of an Outbox?

This is a good senior-level discussion.

DynamoDB Streams can capture item-level changes and can be used to drive downstream processing.

However, an Outbox gives you an explicit event record and event contract, allowing you to control:

event type
event payload
event version
publishing status
retry
replay
event metadata

The correct choice depends on the architecture and requirements.

7. One-Line Revision Sheet

Before your interview, remember these:

Concept	Remember
Partition Key	Determines primary data distribution
Sort Key	Organizes/ranges items under a partition key
GSI	Alternative access pattern
Access-pattern design	Design tables from queries, not entities
TransactWriteItems	Atomic multiple DynamoDB writes
Conditional Write	Execute only if condition is true
Topic	Logical Kafka event stream
Partition	Scalability + ordering boundary
Producer	Writes events
Consumer	Reads/processes events
Consumer Group	Parallel processing of partitions
Offset	Consumer's position
acks=all	Strong producer acknowledgement
Idempotent Producer	Prevents duplicate Kafka writes from retries
Dual Write	DB and Kafka can become inconsistent
Outbox	Atomically save state + event, publish later
At-least-once	Message may be delivered more than once
Idempotent Consumer	Duplicate event doesn't duplicate business effect
Eventual Consistency	Services converge over time
Retry	Recover from transient failures
Backoff + Jitter	Prevent retry storms
DLT/DLQ	Isolate repeatedly failing messages
8. The 60-Second Senior Interview Answer

If the interviewer asks:

"How would you design reliable event processing using DynamoDB and Kafka?"

A strong answer is:

"I would avoid directly performing a DynamoDB write followed by a Kafka publish because that creates a dual-write consistency problem. Instead, I would atomically persist the business record and an outbox event in DynamoDB, potentially using TransactWriteItems. An outbox publisher would then publish the event to Kafka. Kafka would use appropriate partitioning based on the business ordering requirement, replication, acks=all, and idempotent producer settings. Consumers would use consumer groups for horizontal scalability and commit offsets after successful processing. Since failures can occur between processing and offset commits, I would assume at-least-once delivery and make the consumer idempotent using an event ID or business idempotency key. Transient failures would use bounded retries with exponential backoff and jitter, while poison messages would eventually go to a dead-letter topic with monitoring and replay capability. The overall system would embrace eventual consistency between services rather than trying to implement a distributed transaction across DynamoDB and Kafka."

That is the level of explanation you should target for a senior/principal-level interview.

Yesterday 10:35 PM
Day 6 
Day 6 — Microservices: Resilience, Failure Handling & Distributed Transactions

For Day 6, let's move from the Day 5 fundamentals into the concepts that are frequently used to differentiate a senior/principal engineer from someone who only knows Spring Boot APIs.

The focus today:

Circuit Breaker
Retry
Timeout
Bulkhead
Rate Limiting
Saga Pattern
Choreography vs Orchestration
Distributed Transaction
Compensation
Idempotency in distributed workflows
Failure scenarios
Senior-level interview questions
1. Why Resilience Is Required

Imagine:

Client
  |
  ↓
Order Service
  |
  ↓
Payment Service
  |
  ↓
Bank API

Everything works normally.

But the Bank API becomes slow.

Bank API
   ↓
10 seconds response

Now Payment Service waits.

Then Order Service waits.

Then hundreds of requests accumulate.

Eventually:

Bank API
   ↓
Payment Service
   ↓
Order Service
   ↓
API Gateway
   ↓
Entire system becomes unhealthy

This is called cascading failure.

The purpose of resilience patterns is to prevent one failing dependency from bringing down the entire system.

2. Timeout

The first resilience mechanism should usually be a timeout.

Never allow a synchronous service call to wait indefinitely.

Bad:

Order Service
     |
     ↓
Payment Service
     |
     ↓
wait forever

Better:

Order Service
     |
     ↓
Payment Service
     |
     ↓
5 second timeout
     |
     X

After 5 seconds:

Payment unavailable

The Order Service can:

retry
return an appropriate response
initiate asynchronous processing
mark order as PAYMENT_PENDING
Senior interview answer

"Timeouts establish an upper bound on how long a service waits for a dependency. Without timeouts, threads and connection pools can become exhausted and cause cascading failures."

3. Retry

A retry is appropriate for transient failures.

Examples:

Connection timeout
Temporary network failure
HTTP 503
DynamoDB throttling
Temporary Kafka issue

Example:

Request
   ↓
FAIL
   ↓
Retry 1
   ↓
FAIL
   ↓
Retry 2
   ↓
FAIL
   ↓
Retry 3

But retrying everything is dangerous.

Don't blindly retry:

400 Bad Request
401 Unauthorized
403 Forbidden
Business validation failure

These generally won't become successful by retrying.

4. Exponential Backoff

Instead of:

Retry every 1 second
Retry every 1 second
Retry every 1 second

use:

Attempt 1 → 100 ms
Attempt 2 → 200 ms
Attempt 3 → 400 ms
Attempt 4 → 800 ms

or another bounded exponential strategy.

The purpose is to give the downstream system time to recover.

5. Jitter

Suppose 10,000 requests fail at the same time.

Without jitter:

10,000 requests
       ↓
retry after 1 sec
       ↓
10,000 requests
       ↓
service overloaded

This is a retry storm.

With jitter:

Request 1 → 1.1 sec
Request 2 → 1.4 sec
Request 3 → 1.8 sec
Request 4 → 1.2 sec
...

Requests are distributed over time.

Interview answer

"I use exponential backoff with jitter to avoid synchronized retries and reduce the risk of a retry storm."

6. Circuit Breaker

This is one of the most important Day 6 concepts.

Suppose:

Order Service
      |
      ↓
Payment Service

Payment Service is continuously failing.

Without circuit breaker:

Request 1 → Payment → FAIL
Request 2 → Payment → FAIL
Request 3 → Payment → FAIL
...
Request 10000 → Payment → FAIL

You're wasting resources.

A Circuit Breaker detects repeated failures and stops sending requests.

Circuit Breaker States

There are generally three states:

          failures
CLOSED  -------------> OPEN
  ↑                       |
  |                       |
  | successful            | timeout
  |                       ↓
  +---------------- HALF-OPEN
CLOSED

Normal operation.

Request
  ↓
Payment Service

Failures are monitored.

OPEN

Too many failures.

Circuit opens:

Request
  ↓
Circuit Breaker
  ↓
FAIL FAST

The request doesn't even reach Payment Service.

This protects the downstream service and the caller.

HALF-OPEN

After a waiting period, the circuit allows a limited number of test requests.

Circuit
   ↓
HALF-OPEN
   ↓
Test request

If successful:

HALF-OPEN → CLOSED

If failure:

HALF-OPEN → OPEN
7. Circuit Breaker vs Retry

Interviewers frequently ask this.

Retry

Means:

"The failure might be temporary, so try again."

Circuit Breaker

Means:

"The dependency is currently unhealthy, so stop calling it."

They often work together:

Request
   ↓
Circuit Breaker
   ↓
Retry
   ↓
Payment Service

But configuration must be carefully bounded.

Otherwise:

Circuit Breaker
    +
Retry
    +
Large timeout

can actually make the system worse.

8. Bulkhead Pattern

Imagine your service has:

100 threads

and Payment Service becomes slow.

If all 100 threads wait for Payment:

Payment
   ↓
100 threads occupied
   ↓
No threads available
   ↓
Other APIs fail

Bulkhead isolates resources.

For example:

Application
 |
 +---- Payment Pool: 20 threads
 |
 +---- Inventory Pool: 20 threads
 |
 +---- Other APIs: 60 threads

If Payment fails:

Payment
   ↓
20 threads affected

The other APIs can continue.

This is called the Bulkhead pattern, named after compartments in a ship that prevent one flooded section from sinking the entire vessel.

9. Rate Limiting

Rate limiting controls how many requests a client can make.

Example:

Customer
   |
   ↓
API Gateway
   |
   ↓
100 requests/minute

If the client sends:

101 requests

the extra request can be rejected or delayed.

Typical responses:

HTTP 429 Too Many Requests
Why?

To protect:

CPU
memory
database
Kafka
downstream services

It also helps protect APIs from abusive traffic.

10. Saga Pattern

Now we move into distributed transactions.

Suppose an Order workflow involves:

Order Service
Payment Service
Inventory Service
Shipping Service

A traditional database transaction cannot span all these independent services easily.

We therefore use a Saga.

A Saga breaks one distributed business transaction into multiple local transactions.

Example:

Create Order
     ↓
Reserve Inventory
     ↓
Take Payment
     ↓
Create Shipment

Each service commits its own local transaction.

If something fails, previously completed actions are compensated.

11. Saga Example

Suppose:

Step 1
Create Order       SUCCESS


Step 2
Reserve Inventory  SUCCESS


Step 3
Payment            SUCCESS


Step 4
Shipping           FAILURE

We need to compensate:

Shipping FAILED
       ↓
Refund Payment
       ↓
Release Inventory
       ↓
Cancel Order

Conceptually:

Create Order
     ↓
Reserve Inventory
     ↓
Payment
     ↓
Shipping X
     ↓
Refund Payment
     ↓
Release Inventory
     ↓
Cancel Order

These aren't database rollbacks.

They are business compensation operations.

That's an important distinction.

12. Saga Choreography

In choreography, services communicate through events.

Example:

Order Service
     |
     | OrderCreated
     ↓
   Kafka
     |
     ↓
Inventory Service
     |
     | InventoryReserved
     ↓
   Kafka
     |
     ↓
Payment Service
     |
     | PaymentCompleted
     ↓
   Kafka
     |
     ↓
Shipping Service

No central controller coordinates the workflow.

Each service reacts to events.

Advantages
loose coupling
highly distributed
no central orchestrator
natural fit for event-driven architecture
Disadvantages

As workflows become complicated:

Service A
 → Event
 → B
 → Event
 → C
 → Event
 → D
 → compensation
 → event
 → B

it becomes difficult to understand the complete business workflow.

This can become an event spaghetti problem.

13. Saga Orchestration

In orchestration, one component controls the workflow.

              Order Saga
              Orchestrator
             /     |      \
            ↓      ↓       ↓
         Order  Inventory Payment
                     |
                     ↓
                  Shipping

The orchestrator says:

1. Create Order
2. Reserve Inventory
3. Process Payment
4. Create Shipment

If payment fails:

Orchestrator
      ↓
Release Inventory
      ↓
Cancel Order
Advantages
centralized workflow
easier to understand
easier monitoring
easier compensation logic
Disadvantages
orchestrator becomes important infrastructure
potentially more coupling to workflow
must be designed for high availability
14. Choreography vs Orchestration
Feature	Choreography	Orchestration
Central controller	No	Yes
Communication	Events	Commands/calls/events
Coupling	Lower initially	More centralized
Simple workflows	Excellent	Good
Complex workflows	Can become difficult	Usually easier
Monitoring	More difficult	Easier
Compensation	Distributed	Central workflow
Event-driven	Strong fit	Can also be event-driven
Senior answer

"I prefer choreography for simple event-driven workflows where services can independently react to events. For complex business workflows with many steps, branching, timeouts and compensation requirements, orchestration generally provides better visibility and control."

15. Distributed Transaction

Consider:

Order DB
Payment DB
Inventory DB

You cannot simply do:

BEGIN TRANSACTION


Order DB
Payment DB
Inventory DB


COMMIT

because they are separate services and databases.

Trying to coordinate everything through a distributed two-phase commit often introduces significant complexity and operational coupling.

Modern microservice architectures generally prefer:

Saga
+
Eventual Consistency
+
Idempotency
+
Compensation
16. Compensation Transaction

A compensation transaction doesn't undo database changes physically.

It performs a business reversal.

Example:

Payment charged

You cannot simply:

ROLLBACK

the bank transaction after another service has committed.

Instead:

Refund Payment

Similarly:

Inventory Reserved

compensation:

Release Inventory

And:

Order Created

compensation:

Cancel Order
17. Idempotency in Distributed Workflows

Consider:

Payment request
   ↓
Bank
   ↓
SUCCESS

But the response gets lost.

Your service doesn't know whether payment succeeded.

It retries:

Payment request
   ↓
Bank

Without idempotency:

₹10,000
+
₹10,000

Customer gets charged twice.

With an idempotency key:

idempotencyKey = PAYMENT-ORDER-1001

The payment provider recognizes the duplicate request.

First request:
PAYMENT-ORDER-1001 → process


Second request:
PAYMENT-ORDER-1001 → return existing result
Senior interview point

Idempotency is particularly important for:

payment
order creation
inventory reservation
message processing
external API calls
18. Failure Scenario — Complete Interview Example

Imagine interviewer asks:

"What happens if Payment Service is down while creating an order?"

A weak answer:

"Retry payment."

A senior answer:

Client
  ↓
Order Service
  ↓
Create Order
  ↓
Outbox Event
  ↓
Kafka
  ↓
Payment Service

Payment Service is unavailable.

We don't continuously retry synchronously.

Instead:

Order status = PAYMENT_PENDING

The event remains recoverable.

Consumer uses:

Retry
+
Exponential Backoff
+
Jitter

If the dependency continues failing:

DLT

If the order exceeds its business timeout:

Cancel Order

and potentially:

Release Inventory

if inventory had already been reserved.

This gives us:

Resilience
+
No lost event
+
No duplicate payment
+
Eventual consistency
+
Business compensation
19. Recommended Spring Boot Architecture

For your Day 6 project, think about this structure:

Order Service
│
├── REST Controller
│
├── Order Service
│
├── DynamoDB Repository
│
├── Outbox Service
│
├── Kafka Producer
│
├── Kafka Consumer
│
├── Idempotency Handler
│
└── Resilience Layer
       ├── Timeout
       ├── Retry
       ├── Circuit Breaker
       └── Bulkhead

A production request might look like:

POST /orders
      ↓
Controller
      ↓
Order Service
      ↓
Validate
      ↓
DynamoDB Transaction
   ├── Order
   └── Outbox
      ↓
HTTP 202 / 201

Then asynchronously:

Outbox Publisher
      ↓
Kafka
      ↓
Payment Consumer
      ↓
Idempotency Check
      ↓
Payment Service
      ↓
Update State
20. Day 6 — Must-Know Interview Questions

Make sure you can answer these without looking at your notes.

Resilience
What is a circuit breaker?
Explain CLOSED, OPEN and HALF-OPEN.
Circuit breaker vs retry?
Why do we need timeouts?
What is exponential backoff?
Why is jitter required?
What is a retry storm?
What is bulkhead isolation?
What is rate limiting?
What is cascading failure?
Distributed Transactions
What is a distributed transaction?
Why is 2PC generally avoided in microservices?
What is the Saga pattern?
Choreography vs orchestration?
What is a compensation transaction?
Give a real-world Saga example.
Kafka + Microservices
What happens if a Kafka consumer crashes after processing but before committing the offset?
How do you prevent duplicate processing?
How would you design a payment workflow?
How would you guarantee that an order event is not lost?
Production Design
What happens if DynamoDB is throttled?
What happens if Kafka is unavailable?
What happens if Payment Service takes 30 seconds?
How do you prevent cascading failures?
How do you handle poison messages?
How do you monitor failed events?
How do you replay DLT messages safely?
How do you make an API idempotent?
21. Day 6 — One Diagram to Remember
                         CLIENT
                            |
                            ↓
                     API Gateway
                            |
                            ↓
                     ORDER SERVICE
                            |
                  +---------+---------+
                  |                   |
                  ↓                   ↓
              DynamoDB             Outbox
                  |                   |
                  +---------+---------+
                            |
                            ↓
                       Kafka Topic
                            |
            +---------------+---------------+
            |               |               |
            ↓               ↓               ↓
       INVENTORY         PAYMENT        NOTIFICATION
          |                 |
          ↓                 ↓
       DynamoDB         External API
                            |
                     +------+------+
                     |             |
                  Retry         Timeout
                     |
                Circuit Breaker
                     |
                 Failure?
                     |
                     ↓
                    DLT

The key senior-level story is:

DynamoDB + Outbox gives reliable event creation → Kafka gives scalable asynchronous communication → Consumer groups provide parallelism → At-least-once delivery requires idempotent consumers → Retry handles transient failures → Timeout prevents resource exhaustion → Circuit Breaker prevents cascading failures → Saga handles multi-service business transactions → Compensation handles failures → DLT handles poison messages.

Day 6 target

By the end of Day 6, you should be able to design and explain this complete flow on a whiteboard in 5–10 minutes. This is the bridge from knowing individual technologies to demonstrating senior-level distributed-system design.

I lost previous implementation of Day 6 which is related to inventory service

Yes. I checked the available previous context, but the actual Day 6 Inventory Service implementation/code is not recoverable from the retained conversation context. I don't want to invent code and tell you it is the previous implementation.

What I can do is reconstruct Day 6 consistently with your Day 1–5 project, rather than starting a different project.

From our earlier project, the target architecture was:

Microservices Application
│
├── Order Service
├── Inventory Service        ← Day 6
├── Payment Service
├── Notification Service
│
├── Kafka
├── DynamoDB
├── Docker
├── Kubernetes
└── AWS

And Day 5 established the important concepts:

Order Service
     │
     ├── DynamoDB
     │
     └── Transactional Outbox
              │
              ↓
            Kafka
              │
              ↓
       Inventory Service
For Day 6 Inventory Service

The implementation should cover:

Inventory Service
│
├── REST API
│   ├── Create inventory
│   ├── Get inventory
│   ├── Update inventory
│   └── Reserve inventory
│
├── DynamoDB
│   └── Inventory table
│
├── Kafka Consumer
│   └── OrderCreated
│
├── Kafka Producer
│   ├── InventoryReserved
│   └── InventoryReservationFailed
│
├── Idempotency
│
├── Conditional DynamoDB Update
│
└── Error handling

The most important Day 6 business flow should be:

Order Service
      │
      │ OrderCreated
      ↓
    Kafka
      │
      ↓
Inventory Service
      │
      ├── Check idempotency
      │
      ├── Check stock
      │
      ├── Conditional update
      │
      └── Reserve quantity
             │
        +----+----+
        │         │
        ↓         ↓
   SUCCESS      FAILURE
        │         │
        ↓         ↓
InventoryReserved
        │
        └──────────────→ Kafka

For example:

Product:
  productId = P100
  availableQuantity = 10

Order requests:

quantity = 3

Inventory becomes:

10 → 7

But if two orders simultaneously request:

8

and:

5

we must not allow the quantity to become negative.

That's where the DynamoDB conditional update from Day 5 becomes important:

availableQuantity >= requestedQuantity

This is one of the key pieces I would preserve from the Day 6 implementation.

I suggest we restore it as

Day 6 — Inventory Service Implementation

Project structure
pom.xml
application.yml
Inventory model
DynamoDB configuration
Repository
Service
REST controller
Kafka consumer
Kafka producer
Conditional stock reservation
Idempotent event processing
Exception handling
Unit tests
Docker configuration
Test the complete Order → Kafka → Inventory flow

If you still have any of the old Day 6 files/code, upload or paste them here. I can then restore the implementation exactly from that point, instead of making assumptions.



DAY 6 — Resilience & Distributed Transactions
1. What is resilience in microservices?

Resilience is the ability of a distributed system to continue providing acceptable functionality despite failures in dependencies, networks, infrastructure, or services.

Typical resilience patterns:

Timeout
Retry
Circuit Breaker
Bulkhead
Rate Limiting
Fallback
Dead Letter Queue
2. What is a timeout?

A timeout limits how long a service waits for a dependency.

Order Service
     |
     ↓
Payment Service
     |
   5 sec
     |
   TIMEOUT

Without timeout:

Threads
  ↓
waiting
  ↓
connection pool exhausted
  ↓
service failure
Interview answer

A timeout prevents indefinite waiting on downstream services and protects application threads, connection pools, and overall system availability.

3. Why should timeout be configured before retry?

Because retry without a timeout can cause a request to wait indefinitely.

Example:

Request
 ↓
Timeout
 ↓
Retry
 ↓
Timeout
 ↓
Retry

Each attempt needs a bounded timeout.

4. What is Retry?

Retry means repeating an operation after a temporary failure.

Good candidates:

Network timeout
503
Temporary throttling
Transient infrastructure failure

Bad candidates:

400
401
403
Invalid business request
5. What is exponential backoff?

Instead of retrying immediately:

1 sec
1 sec
1 sec

increase the delay:

100ms
200ms
400ms
800ms

It gives the downstream system time to recover.

6. What is jitter?

Jitter introduces randomness into retry delays.

Without jitter:

10,000 requests
      ↓
retry at exactly 1 second
      ↓
10,000 requests again

This creates a retry storm.

With jitter:

1.1 sec
1.3 sec
1.7 sec
1.2 sec
...

Requests are distributed over time.

Senior answer

I use exponential backoff with jitter to avoid synchronized retries and thundering-herd effects.

7. What is a retry storm?

A large number of clients retry simultaneously after a failure.

Service fails
     ↓
10000 clients retry
     ↓
Service overloaded
     ↓
More failures
     ↓
More retries

Solutions:

Exponential Backoff
+
Jitter
+
Circuit Breaker
+
Rate Limiting
8. What is Circuit Breaker?

Circuit Breaker prevents continuous calls to an unhealthy dependency.

States:

CLOSED
   ↓ failures
OPEN
   ↓ wait
HALF-OPEN
   ↓
SUCCESS → CLOSED
FAILURE → OPEN
CLOSED

Requests flow normally.

OPEN

Requests fail fast without calling the dependency.

HALF-OPEN

A few test requests determine whether the dependency recovered.

9. Why do we need Circuit Breaker if we already have Retry?

Retry says:

"Maybe this failure is temporary. Try again."

Circuit Breaker says:

"This dependency is unhealthy. Stop calling it for now."

They solve different problems.

Typical combination:

Circuit Breaker
       ↓
Retry
       ↓
Timeout
       ↓
Downstream

Configuration must be bounded to avoid multiplying latency.

10. What is cascading failure?

Failure of one service causes dependent services to fail.

Payment
   ↓
slow
   ↓
Order threads blocked
   ↓
connection pool exhausted
   ↓
Order fails
   ↓
API Gateway affected

Prevention:

Timeout
Circuit Breaker
Bulkhead
Rate Limiting
Retry limits
Async processing
11. What is Bulkhead?

Bulkhead isolates resources so failure in one dependency doesn't consume all application resources.

Example:

Application
│
├── Payment Pool     20
├── Inventory Pool   20
└── Other APIs       60

Payment failure consumes only its allocated resources.

12. What is Rate Limiting?

Rate limiting controls how many requests a client can make.

Example:

100 requests/minute

Request 101:

HTTP 429

Used to protect:

application
database
Kafka
downstream services
external APIs
13. Retry vs Circuit Breaker vs Timeout
Pattern	Purpose
Timeout	Stop waiting
Retry	Try transient failure again
Circuit Breaker	Stop calling unhealthy dependency
Bulkhead	Isolate resources
Rate Limiter	Control traffic
DAY 6 — Saga & Distributed Transactions
14. What is a distributed transaction?

A business transaction spanning multiple independent services/databases.

Example:

Order DB
Inventory DB
Payment DB
Shipping DB

A single local database transaction cannot atomically cover all of them.

15. Why don't we simply use one database transaction?

Because microservices are intentionally independently owned.

Using one transaction across services creates:

tight coupling
availability problems
operational complexity
long-running transactions

Modern microservices generally use:

Saga
+
Eventual Consistency
+
Compensation
16. What is Saga?

Saga breaks a distributed transaction into multiple local transactions.

Example:

Create Order
     ↓
Reserve Inventory
     ↓
Process Payment
     ↓
Create Shipment

If a later step fails, compensation reverses earlier business actions.

17. What is a compensation transaction?

A business operation that reverses a previous operation.

Examples:

Payment completed
      ↓
Refund payment
Inventory reserved
      ↓
Release inventory
Order created
      ↓
Cancel order

It is not the same as database rollback.

18. Why can't compensation simply rollback the database?

Because the original transaction may already have committed.

For example:

Inventory DB → COMMITTED
Payment DB   → COMMITTED

A later failure cannot execute:

ROLLBACK Inventory DB

Instead:

Release Inventory
19. Saga Choreography vs Orchestration
Choreography

Services communicate through events.

Order
 ↓
Kafka
 ↓
Inventory
 ↓
Kafka
 ↓
Payment

No central controller.

Orchestration

A central orchestrator controls the workflow.

          Saga Orchestrator
          /       |       \
         ↓        ↓        ↓
      Order   Inventory  Payment
20. When would you choose choreography?

Good for:

simple workflows
event-driven architecture
loosely coupled services

Problem:

A → B → C → D → E

can become difficult to understand when the workflow becomes complex.

21. When would you choose orchestration?

Good for:

complex workflows
many steps
branching
compensation
timeouts
business workflow visibility
Senior answer

I prefer choreography for simple event-driven workflows and orchestration when the business process has complex branching, compensation and workflow-state requirements.

DAY 7 — Payment Service
22. Why did we introduce Payment Service separately?

Payment has its own:

business rules
persistence
lifecycle
external integrations
security requirements
failure scenarios

Therefore it should have independent ownership.

23. Why process payment after inventory reservation?

Our chosen business workflow is:

OrderCreated
    ↓
InventoryReserved
    ↓
Payment

This prevents taking payment for an order that cannot be fulfilled due to insufficient inventory.

This is a business decision, not a universal microservices rule.

24. What happens if Payment Service is down?

Kafka retains the event according to topic retention.

Consumer eventually resumes:

Kafka
 ↓
Payment Service unavailable


...


Payment Service recovers
 ↓
Consume event

We should also have:

Retry
Backoff
DLT
Monitoring
Idempotency
25. Why do we use manual Kafka acknowledgement?

We want:

Process successfully
      ↓
ACK
      ↓
Offset committed

rather than:

Receive message
      ↓
Offset committed
      ↓
Processing fails

This supports at-least-once processing.

26. What happens if Payment Service crashes after processing but before ACK?

Kafka may redeliver the event.

Event
 ↓
Payment SUCCESS
 ↓
Crash
 ↓
No ACK
 ↓
Event redelivered

Therefore Payment Service must be idempotent.

27. What is idempotency?

An operation is idempotent if performing it multiple times produces the same final business result.

Example:

PAYMENT-1001

First request:

Process payment

Second request:

Return existing result

Not:

Charge customer again
28. How would you implement payment idempotency?

Use an idempotency key:

idempotencyKey = eventId

Store it with the payment transaction.

Before processing:

Does eventId already exist?
       |
   +---+---+
   |       |
 YES      NO
   |       |
return    process
existing

For strong correctness, the check-and-create should itself be atomic.

29. Why is eventId important?

Because the same business event can be delivered multiple times.

Example:

OrderCreated
eventId = EVT-10001

If Kafka delivers it twice:

EVT-10001
EVT-10001

we recognize the duplicate.

30. Why shouldn't we generate a new idempotency key for every retry?

Because then every retry looks like a new request.

Bad:

Retry 1 → KEY-1
Retry 2 → KEY-2
Retry 3 → KEY-3

Good:

Retry 1 → EVENT-1001
Retry 2 → EVENT-1001
Retry 3 → EVENT-1001

The same logical operation must retain the same idempotency key.

31. What if payment succeeds but PaymentCompleted isn't published?

This is the dual-write problem.

Payment DB
   ↓
SUCCESS


Kafka
   ↓
FAILED

Now:

Payment = COMPLETED
Order = PAYMENT_PENDING

Solution:

Payment DB Transaction
       |
       +── Payment
       |
       +── Outbox Event
              ↓
          Publisher
              ↓
            Kafka

This is why Transactional Outbox is important.

32. Why does Payment Service need its own database?

Because each microservice should generally own its data.

Order Service
    ↓
Order DB


Inventory Service
    ↓
Inventory DB


Payment Service
    ↓
Payment DB

Avoid:

All services
     ↓
Shared database

because it creates tight coupling.

DAY 8 — Saga Implementation
33. What is the complete order workflow?

Our implementation:

OrderCreated
     ↓
InventoryReserved
     ↓
Payment
     ↓
PaymentCompleted
     ↓
Order COMPLETED
34. What happens when inventory is insufficient?
OrderCreated
     ↓
Inventory Service
     ↓
Insufficient Stock
     ↓
InventoryFailed
     ↓
Order CANCELLED

No payment should be taken.

35. What happens when payment fails after inventory reservation?
OrderCreated
     ↓
InventoryReserved
     ↓
PaymentFailed
     ↓
Release Inventory
     ↓
InventoryReleased
     ↓
Order CANCELLED

This is Saga compensation.

36. Why must inventory release be idempotent?

Suppose:

PaymentFailed
PaymentFailed

is delivered twice.

Without idempotency:

Available = 7


Release #1 → 10
Release #2 → 13 ❌

With reservation state:

RESERVED
   ↓
RELEASED

second release sees:

Already RELEASED

and does nothing.

37. What is an inventory reservation?

Instead of immediately permanently reducing stock, maintain a reservation.

Example:

Product P100


Available = 10
Reserved  = 0

Reserve 3:

Available = 7
Reserved  = 3

Payment succeeds:

Reservation → CONFIRMED

Payment fails:

Reservation → RELEASED
Available → 10

This is safer than simply incrementing/decrementing stock without tracking reservation state.

38. What is eventual consistency in our Saga?

Immediately after:

PaymentFailed

you may temporarily have:

Order        = PAYMENT_FAILED
Inventory    = RESERVED

A short time later:

Order        = CANCELLED
Inventory    = RELEASED

The system converges.

That's eventual consistency.

39. What if the compensation itself fails?

This is an important senior-level question.

Example:

PaymentFailed
      ↓
Release Inventory
      ↓
Inventory Service DOWN

We shouldn't lose the compensation event.

Use:

Retry
+
Persistent event
+
DLT
+
Monitoring
+
Replay

The compensation must eventually be retried until the business state is corrected, subject to appropriate operational/business limits.

40. What if Kafka delivers an event twice?

Don't assume exactly-once business processing.

Design:

At-least-once delivery
        +
Idempotent consumer

This is one of the most important microservices design principles.

41. What if events arrive out of order?

Kafka guarantees ordering within a partition, not globally.

Use an appropriate Kafka key.

For example:

key = orderId

This ensures events for the same order are routed to the same partition.

But consumers should still validate state transitions.

Example:

PAYMENT_COMPLETED

should not blindly overwrite an already cancelled order.

42. How do you prevent invalid state transitions?

Use a state machine.

Example:

PENDING
   ↓
INVENTORY_RESERVED
   ↓
PAYMENT_PENDING
   ↓
COMPLETED

Don't allow:

CANCELLED
   ↓
COMPLETED

unless the business explicitly permits that transition.

43. What is a poison message?

A message that repeatedly fails processing.

Example:

Kafka
 ↓
Consumer
 ↓
Exception
 ↓
Retry
 ↓
Exception
 ↓
Retry
 ↓
Exception

Eventually:

DLT
44. What should a DLT message contain?

At minimum:

eventId
originalTopic
partition
offset
payload
exception
timestamp
retryCount
correlationId

This makes troubleshooting and replay much easier.

45. Should we blindly replay DLT messages?

No.

First determine:

Why did processing fail?

Fix the underlying issue.

Then replay carefully.

For example:

DLT
 ↓
Fix consumer
 ↓
Validate event
 ↓
Replay
 ↓
Monitor

Replay itself must be idempotent.

46. How would you design the complete failure handling?

A strong senior answer:

"I would use timeouts for bounded dependency calls, retries with exponential backoff and jitter for transient failures, circuit breakers to prevent calls to unhealthy dependencies, and bulkheads to isolate resources. For cross-service business transactions, I would use a Saga with compensating actions. Kafka would provide asynchronous communication with at-least-once delivery, so consumers would be idempotent. For reliable database-to-event publication, I would use the transactional outbox pattern. Poison messages would be routed to a DLT with monitoring and replay support."

47. Most Important Architecture Question
"Design an order processing system using DynamoDB and Kafka."

Your answer should be:

                         Client
                           |
                           ↓
                     Order Service
                           |
                    DynamoDB Transaction
                       /           \
                      /             \
                  Order           Outbox
                                   |
                                   ↓
                              Kafka
                                   |
                                   ↓
                          Inventory Service
                                   |
                            Reserve Stock
                                   |
                                   ↓
                         InventoryReserved
                                   |
                                   ↓
                                Kafka
                                   |
                                   ↓
                           Payment Service
                                   |
                              Payment DB
                                   |
                            +------+------+
                            |             |
                         Success        Failure
                            |             |
                            ↓             ↓
                    PaymentCompleted  PaymentFailed
                            |             |
                            ↓             ↓
                         Order       Inventory
                       COMPLETED      RELEASE

Then explain:

Outbox
Idempotency
Conditional writes
At-least-once
Retry
Timeout
Circuit breaker
Saga
Compensation
DLT

That demonstrates system-level understanding, rather than just knowledge of individual technologies.

48. Rapid-Fire Interview Questions

Before an interview, quickly test yourself:

Day 6
What is timeout?
What is retry?
Why exponential backoff?
Why jitter?
What is retry storm?
What is circuit breaker?
Explain three circuit-breaker states.
Retry vs circuit breaker?
What is bulkhead?
What is rate limiting?
What is cascading failure?
What is Saga?
Choreography vs orchestration?
What is compensation?
Why not distributed 2PC?
Day 7
Why separate Payment Service?
Why consume InventoryReserved?
What happens when Payment Service crashes?
What is idempotency?
How do you prevent duplicate payment?
What is an idempotency key?
Why use the same key during retries?
What happens if payment succeeds but Kafka publication fails?
How does Outbox solve that?
Why manual Kafka acknowledgement?
What happens if processing succeeds but ACK fails?
Why does Payment Service need its own database?
What is at-least-once processing?
Day 8
Explain complete Order Saga.
What happens if inventory reservation fails?
What happens if payment fails?
How do you release inventory?
Why is compensation not rollback?
Why must compensation be idempotent?
What is eventual consistency?
What if compensation fails?
What if Kafka delivers duplicate events?
What if events arrive out of order?
How do you prevent invalid state transitions?
What is a poison message?
What is DLT?
How do you safely replay DLT events?
Where would you use Outbox?
Where would you use Conditional Writes?
Where would you use TransactWriteItems?
49. 15 Questions You Absolutely Must Know

If you have only 15 minutes before the interview, revise these:

#	Question	Key words
1	What is Circuit Breaker?	Closed/Open/Half-Open
2	Retry vs Circuit Breaker?	Transient vs unhealthy dependency
3	Why timeout?	Resource protection
4	Why backoff + jitter?	Retry storm
5	What is Bulkhead?	Resource isolation
6	What is Saga?	Local transactions + compensation
7	Choreography vs Orchestration?	Events vs coordinator
8	What is compensation?	Business reversal
9	What is idempotency?	Same result on duplicate
10	Why at-least-once?	Reliability + duplicate handling
11	What is Outbox?	Atomic DB + event
12	What if Kafka ACK fails?	Redelivery + idempotency
13	What if payment fails?	Release inventory
14	What is eventual consistency?	Temporary inconsistency
15	How do you handle poison events?	Retry + DLT + replay
50. One-Minute Revision Card

Memorize this flow:

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
Stop calling unhealthy service


BULKHEAD
   ↓
Isolate resources


SAGA
   ↓
Distributed business transaction


COMPENSATION
   ↓
Reverse business operation


KAFKA
   ↓
Async event communication


AT-LEAST-ONCE
   ↓
Duplicates possible


IDEMPOTENCY
   ↓
Duplicates are safe


OUTBOX
   ↓
Reliable DB → Kafka publication


CONDITIONAL WRITE
   ↓
Prevent race conditions


DLT
   ↓
Handle poison messages


EVENTUAL CONSISTENCY
   ↓
Services converge
The single senior-level principle to remember

In distributed systems, assume that messages can be duplicated, services can fail, networks can timeout, events can be delayed, and operations can partially succeed. Design the system so that these failures are expected rather than exceptional.
