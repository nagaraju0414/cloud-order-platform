Interviewer:

"What happens if your Order Service becomes unhealthy in Kubernetes?"

Answer:

"It depends on the type of failure. If the liveness probe fails repeatedly, Kubernetes restarts the container. If the readiness probe fails, Kubernetes removes the pod from the service endpoints so it stops receiving traffic, but Kubernetes doesn't necessarily restart the pod.

What is Micrometer?

Say:

"Micrometer is an instrumentation facade for JVM applications. It provides a common API for collecting application metrics and integrates with monitoring systems such as Prometheus, Datadog, and others."
Application
│
▼
Micrometer
│
├── Prometheus
├── Datadog
├── CloudWatch
└── Other monitoring systems
Why Prometheus uses "pull"

This is a very common interview question.

Our application exposes:

GET /actuator/prometheus

Prometheus periodically calls it:

                GET /actuator/prometheus
Prometheus ──────────────────────────────► Order Service
◄──────────────────────────────
metrics

This is called the pull model.

Interview answer:

"With Prometheus, the application exposes metrics through an endpoint and Prometheus periodically scrapes that endpoint. This is a pull-based monitoring model."

Senior interview takeaway

Remember this chain:

Actuator exposes → Micrometer measures → Prometheus collects → Grafana visualizes.



4.16 Very important senior interview question
Q: Why don't you configure Prometheus with localhost:8081?

Answer:

"Because Prometheus is running inside a Docker container. Inside that container, localhost refers to the Prometheus container itself. Docker Compose provides service-name DNS, so Prometheus should access the Order Service using order-service:8081."

Excellent practical answer.

4.17 Another interview question
Q: How does Prometheus know where to get metrics?

Answer:

"We configure a scrape job in prometheus.yml. The job specifies the target service and metrics path. Prometheus periodically sends HTTP requests to that endpoint and stores the returned time-series metrics."

Interviewer:

"How do you trace a request across multiple microservices?"

Answer:

"We propagate a correlation or trace ID with every request. At the HTTP boundary, we extract the ID or generate one if it doesn't exist, and store it in MDC for structured logging. For asynchronous communication such as Kafka, we explicitly propagate the ID as part of the event or message headers. This allows us to correlate logs and events across Order, Inventory and other downstream services."

7.20 Interview question
Q: Why use structured logging?

Answer:

"Structured logging represents logs as machine-readable fields such as timestamp, level, service name, correlation ID, order ID and message. This makes logs easier to search, aggregate and analyze in centralized systems such as ELK or OpenSearch. It also allows us to correlate failures across multiple microservices."

7.21 Interview question
Q: How do you troubleshoot a failed order?

Strong answer:

"I first take the order ID or correlation ID from the request. I search centralized logs using that ID, trace the request through Order Service and downstream services, then correlate the application logs with Kafka events and Prometheus metrics. If necessary, distributed tracing can provide the complete request path and latency between services."

That's exactly the kind of answer expected from a senior microservices engineer.

Q1. What are the three pillars of observability?

Answer:

Metrics, logs and traces.

Q2. Why Prometheus?

Prometheus is used to collect and query time-series metrics. Spring Boot exposes metrics through Micrometer and Actuator, which Prometheus scrapes.

Q3. Why Grafana?

Grafana provides dashboards and visualization over Prometheus metrics, allowing teams to monitor application health, business KPIs, latency, error rate and resource utilization.

Q4. Why distributed tracing?

In a microservice architecture a single request can cross multiple services. Distributed tracing allows us to follow that request and identify which service or dependency caused latency or failure.

Q5. What is a span?

A span represents one operation within a trace, such as an HTTP request, database call or Kafka operation.

Q6. What is a trace?

A trace represents the complete journey of a request across multiple operations and services. It consists of multiple spans connected through trace context.

Q7. Correlation ID vs Trace ID?

Correlation ID is an application-level identifier used mainly for log correlation. Trace ID is part of distributed tracing and connects spans across services.

Q8. What happens if Inventory Service is down?

Strong answer:

"Order Service does not synchronously depend on Inventory Service. It persists the order and outbox event atomically in DynamoDB. The outbox publisher publishes the event to Kafka. If Inventory is unavailable, Kafka retains the event and Inventory can process it after recovery. This provides fault isolation and eventual consistency."

Q9. How do you troubleshoot a slow order?

Answer:

1. Grafana
   ↓
   Check latency/error rate

2. Jaeger
   ↓
   Find slow span

3. Logs
   ↓
   Use correlation/trace ID

4. Identify dependency
   ↓
   DynamoDB / Kafka / Inventory

5. Fix bottleneck

That's a very strong production troubleshooting answer.

Step 16 — Interview checkpoint

If interviewer asks:

"How does Spring Cloud Config Server work?"

Say:

"Config Server provides centralized external configuration for microservices. A client identifies itself using spring.application.name, and the Config Server resolves the corresponding configuration, such as order-service.yml, and returns it to the client. We can use native files for local development and Git or another configuration backend in production."

🧠 Interview Explanation

If interviewer asks:

"Why are you using a namespace?"

Answer:

"A Kubernetes namespace provides logical isolation for resources within a cluster. In our application I use a cloud-order namespace to group all microservices such as API Gateway, Order Service, Inventory Service and Config Server. It also allows us to manage resources, configurations and access policies independently from other applications."

1. Why do we need a Kubernetes Service?

A Pod IP is ephemeral and can change when the Pod is recreated. A Service provides a stable network endpoint and routes traffic to matching Pods.

2. Why ClusterIP?

Config Server is an internal service. It doesn't need to be exposed to external clients, so ClusterIP is appropriate.

3. How will Order Service access Config Server?

Inside Kubernetes:

http://config-server:8888

Kubernetes DNS resolves:

config-server

to the Service.

4. What happens if Config Server Pod dies?

The Deployment's ReplicaSet ensures the desired replica count is maintained and Kubernetes creates a replacement Pod.

5. Deployment vs Pod?

A Pod is the smallest deployable Kubernetes unit. A Deployment manages the desired number and lifecycle of Pods and supports rolling updates.

🎯 Interview Questions From This Step
1. Why do we need a Kubernetes Service?

A Pod IP is ephemeral and can change when the Pod is recreated. A Service provides a stable network endpoint and routes traffic to matching Pods.

2. Why ClusterIP?

Config Server is an internal service. It doesn't need to be exposed to external clients, so ClusterIP is appropriate.

3. How will Order Service access Config Server?

Inside Kubernetes:

http://config-server:8888

Kubernetes DNS resolves:

config-server

to the Service.

4. What happens if Config Server Pod dies?

The Deployment's ReplicaSet ensures the desired replica count is maintained and Kubernetes creates a replacement Pod.

5. Deployment vs Pod?

A Pod is the smallest deployable Kubernetes unit. A Deployment manages the desired number and lifecycle of Pods and supports rolling updates.


If asked "How did you Kubernetes-enable your microservices?", you can say:

"I containerized the services and deployed them using Kubernetes Deployments and Services in a dedicated namespace. Internal services such as Config Server, Order, Inventory, Kafka and DynamoDB use ClusterIP and Kubernetes DNS for service discovery, while the API Gateway is the external entry point. I added readiness and liveness probes, resource requests and limits, horizontal scaling and rolling deployments. For resilience, Kubernetes automatically recreates failed Pods, and deployments support rollback. In the AWS phase, the same workloads can be deployed to EKS, with ECR for images and managed services such as DynamoDB and MSK."

Interview Explanation ⭐

If interviewer asks:

"How does Kubernetes HPA work?"

Answer:

"HPA automatically adjusts the number of Pod replicas based on resource utilization or custom metrics. In our application, Metrics Server collects CPU and memory metrics from the Pods. HPA compares the current CPU utilization with the configured target, for example 50%. If utilization increases beyond the target, HPA increases the Deployment's desired replica count. If utilization decreases, it scales down subject to the configured stabilization behavior."

"Why do we need Metrics Server?"

"HPA needs resource metrics such as CPU and memory. Metrics Server collects those metrics from kubelets and exposes them through the Kubernetes Metrics API, which HPA uses."
