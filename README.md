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
