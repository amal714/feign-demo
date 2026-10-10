# Enterprise Spring Cloud Microservices Architecture

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![OpenTelemetry](https://img.shields.io/badge/OpenTelemetry-Tracing-orange.svg)](https://opentelemetry.io/)

A production-ready, fault-tolerant microservices cluster demonstrating modern cloud-native patterns. Built to showcase strict separation of concerns, centralized configuration, edge routing, and deep observability.

## 🏗️ Architectural Blueprint

```mermaid
graph TD
    Client([🌐 Web/Mobile Client]) -->|HTTP GET :8080| Gateway[🛡️ API Gateway]
    
    subgraph Business Services
        Gateway -->|Load Balanced| Main[⚙️ Main Service :8100]
        Main -->|OpenFeign| Data[🗄️ Data Service :8101]
    end

    subgraph Infrastructure Layer
        Eureka((🗺️ Eureka Registry :8761))
        Config((📜 Config Server :8888))
    end

    %% Discovery Connections
    Gateway -.->|Registers/Discovers| Eureka
    Main -.->|Registers/Discovers| Eureka
    Data -.->|Registers/Discovers| Eureka

    %% Config Connections
    Gateway -.->|Fetches YAML| Config
    Main -.->|Fetches YAML| Config
    Data -.->|Fetches YAML| Config
    
    classDef service fill:#0d1117,stroke:#58a6ff,stroke-width:2px,color:#c9d1d9;
    classDef infra fill:#161b22,stroke:#3fb950,stroke-width:2px,color:#c9d1d9;
    class Gateway,Main,Data service;
    class Eureka,Config infra;
```

This project implements a 5-node distributed system using the **API Gateway Pattern** and **Client-Side Load Balancing**.

*   **`api-gateway` (Port 8080):** The single entry point. Routes external traffic to internal services using Spring Cloud Gateway and Eureka path predicates.
*   **`main-service` (Port 8100):** The primary consumer. Orchestrates business logic and communicates with downstream services via OpenFeign.
*   **`data-service` (Port 8101):** The backend provider exposing core data endpoints.
*   **`config-server` (Port 8888):** Centralized configuration manager serving environment-specific YAML rules to all nodes.
*   **`eureka-server` (Port 8761):** Service registry enabling dynamic discovery and eliminating hardcoded IPs.

## 🚀 Key Enterprise Patterns Implemented

### 1. Fault Tolerance & Resilience (Resilience4j)
```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant Gateway as API Gateway
    participant Main as Main Service
    participant Data as Data Service (DOWN)

    Client->>Gateway: GET /api/main/greet
    Gateway->>Main: Route Request
    
    activate Main
    Main->>Data: Feign Call /message
    Data--xMain: Connection Refused
    
    Note over Main: Resilience4j Retry (3 attempts, 2s wait)
    Main->>Data: Retry 1, 2, 3
    Data--xMain: Fails
    
    Note over Main: Circuit Breaker OPENS
    Main-->>Gateway: FallbackFactory: "Service Unavailable"
    deactivate Main
    
    Gateway-->>Client: Graceful Degradation Response
```
*   **Circuit Breaker:** Protects `main-service` from cascading failures. If `data-service` fails, the circuit opens (50% threshold) to instantly reject requests and save Tomcat threads.
*   **Retry Mechanism:** Automatically retries transient network failures (3 attempts, 2s backoff) before opening the circuit.
*   **Fallback Factory:** Implements the OpenFeign `FallbackFactory` pattern to gracefully degrade service while capturing and logging the exact `Throwable` cause for monitoring.

### 2. Deep Observability (OpenTelemetry + Jaeger)
```mermaid
sequenceDiagram
    participant C as 🌐 Client
    participant G as 🛡️ API Gateway
    participant M as ⚙️ Main Service
    participant D as 🗄️ Data Service
    participant J as 👁️ Jaeger (OTLP)

    C->>G: HTTP Request
    Note over G: Generates TraceId: abc-123
    
    par Async Export
        G-)J: Export Span (Gateway)
    and Synchronous Flow
        G->>M: Route Request (Header: traceparent=abc-123)
    end
    
    par Async Export
        M-)J: Export Span (Main)
    and Synchronous Flow
        M->>D: Feign Call (Header: traceparent=abc-123)
    end
    
    par Async Export
        D-)J: Export Span (Data)
    and Synchronous Flow
        D-->>M: 200 OK
    end
    
    M-->>G: 200 OK
    G-->>C: 200 OK
```
*   **Distributed Tracing:** Utilizes Micrometer and OpenTelemetry (OTLP) to inject a unique `traceId` at the Gateway, propagating it across all JVM boundaries via HTTP headers (`feign-micrometer`).
*   **Visual Waterfall:** Traces are exported to a Jaeger backend, allowing millisecond-level latency analysis across the network hops.

### 3. Structured JSON Logging
```mermaid
graph TD
    subgraph Microservice JVM
        Log["☕ log.info('Processing request')"]
        MDC[("🧠 MDC Context")]
        Encoder{"⚙️ LogstashEncoder"}
        
        Log -->|1. Raw Message| Encoder
        MDC -.->|2. Auto-injects traceId| Encoder
    end

    JSON[/"{ timestamp, level, traceId, message }"/]
    
    Aggregator(("📊 ELK / Datadog"))

    Encoder -->|3. Outputs| JSON
    JSON -->|4. Ingested for Search| Aggregator

    %% Colorful Styling
    classDef log fill:#0d47a1,stroke:#64b5f6,stroke-width:2px,color:#fff;
    classDef mdc fill:#e65100,stroke:#ffb74d,stroke-width:2px,color:#fff;
    classDef encoder fill:#4a148c,stroke:#ba68c8,stroke-width:2px,color:#fff;
    classDef json fill:#1b5e20,stroke:#81c784,stroke-width:2px,color:#fff;
    classDef agg fill:#b71c1c,stroke:#ef5350,stroke-width:2px,color:#fff;

    class Log log;
    class MDC mdc;
    class Encoder encoder;
    class JSON json;
    class Aggregator agg;
    
    style Microservice JVM fill:#0d1117,stroke:#8b949e,stroke-dasharray: 5 5,color:#c9d1d9
```
*   **Machine-Readable Logs:** Replaced standard console text with `logstash-logback-encoder`.
*   **MDC Context Injection:** The `traceId` and `spanId` are automatically injected into the JSON log payload, allowing log aggregators (ELK/Datadog) to instantly correlate logs to specific user requests.

### 4. Strict Maven Multi-Module Design
```mermaid
graph TD
    Root["📦 feign-demo (Root Aggregator POM)<br/>Enforces Spring Boot 3.3.4 & Spring Cloud 2023.0.3"]
    
    subgraph Child Modules [Inherit Versions - No Hardcoded Tags]
        Config["⚙️ config-server"]
        Eureka["🗺️ eureka-server"]
        Gateway["🛡️ api-gateway"]
        Main["🚀 main-service"]
        Data["🗄️ data-service"]
    end

    Root ==>|Manages Dependencies| Config
    Root ==>|Manages Dependencies| Eureka
    Root ==>|Manages Dependencies| Gateway
    Root ==>|Manages Dependencies| Main
    Root ==>|Manages Dependencies| Data

    %% Styling
    classDef rootPom fill:#4a148c,stroke:#ba68c8,stroke-width:2px,color:#fff;
    classDef childPom fill:#004d40,stroke:#4db6ac,stroke-width:2px,color:#fff;
    
    class Root rootPom;
    class Config,Eureka,Gateway,Main,Data childPom;
    
    style Child Modules fill:#0d1117,stroke:#8b949e,stroke-dasharray: 5 5,color:#c9d1d9
```
*   **Dependency Management:** Utilizes a Root Aggregator POM to enforce strict version control (Spring Boot 3.3.4, Spring Cloud 2023.0.3) across all child modules, eliminating dependency hell and classpath collisions.

## 🛠️ Local Setup & Chaos Testing

### Prerequisites
*   Java 17+
*   Maven 3.8+
*   Docker (for Jaeger)

### 1. Start the Observability Backend
```bash
docker run -d -p 16686:16686 -p 4317:4317 -p 4318:4318 jaegertracing/all-in-one:latest
```
```mermaid
graph LR
    subgraph Docker Environment
        Jaeger[("👁️ Jaeger (all-in-one container)")]
    end

    Cluster["🧩 Microservices Cluster"] -.->|"OTLP HTTP (Port 4318)"| Jaeger
    Cluster -.->|"OTLP gRPC (Port 4317)"| Jaeger
    SRE["👨‍💻 Developer / SRE"] ===>|"Web UI (Port 16686)"| Jaeger

    %% Styling
    classDef docker fill:#0db7ed,stroke:#0288d1,stroke-width:2px,color:#fff;
    classDef cluster fill:#161b22,stroke:#3fb950,stroke-width:2px,color:#fff;
    classDef user fill:#232F3E,stroke:#FF9900,stroke-width:2px,color:#fff;

    class Jaeger docker;
    class Cluster cluster;
    class SRE user;
    
    style Docker Environment fill:#0d1117,stroke:#8b949e,stroke-dasharray: 5 5,color:#c9d1d9
```


### 2. Boot the Cluster (Strict Order)
1. `ConfigServerApplication`
2. `EurekaServerApplication`
3. `DataApplication` & `MainApplication`
4. `ApiGatewayApplication`
```mermaid
graph TD
    C["1️⃣ Config Server (:8888)<br/>Serves Centralized YAMLs"]
    E["2️⃣ Eureka Server (:8761)<br/>Service Registry"]
    M["3️⃣ Main & Data Services (:8100 / :8101)<br/>Business Logic Providers"]
    G["4️⃣ API Gateway (:8080)<br/>Edge Router"]

    C ==>|Must be up for others to fetch configs| E
    E ==>|Must be up for services to register| M
    M ==>|Must be up to receive external traffic| G

    %% Styling
    classDef config fill:#4a148c,stroke:#ba68c8,stroke-width:2px,color:#fff;
    classDef eureka fill:#004d40,stroke:#4db6ac,stroke-width:2px,color:#fff;
    classDef micro fill:#e65100,stroke:#ffb74d,stroke-width:2px,color:#fff;
    classDef gate fill:#0d47a1,stroke:#64b5f6,stroke-width:2px,color:#fff;

    class C config;
    class E eureka;
    class M micro;
    class G gate;
```




### 3. Execute the Chaos Test
1. **The Happy Path:** Navigate to `http://localhost:8080/api/main/greetUsingFeignClient`. You will receive a successful response from the Data Service.
2. **View the Trace:** Open Jaeger at `http://localhost:16686` to view the end-to-end trace propagation.
3. **Simulate Outage:** Stop the `DataApplication` JVM.
4. **Verify Circuit Breaker:** Hit the Gateway URL again. Observe the Resilience4j Retry mechanism wait, followed by the graceful Fallback Factory response. Check the JSON logs to see the captured `ConnectionRefused` exception tied to the exact `traceId`.

```mermaid
graph TD
    subgraph Phase 1: Normal Operations
        S1["🟢 1. Hit Gateway URL"] --> S2["✅ 2. Receive 'Hello from Data Service'"]
        S2 --> S3["👁️ 3. View End-to-End Trace in Jaeger"]
    end

    subgraph Phase 2: Chaos Injection
        S4["🛑 4. STOP DataApplication JVM"]
    end

    subgraph Phase 3: Resilience Verification
        S5["🌐 5. Hit Gateway URL Again"] --> S6["⏳ 6. Resilience4j Retries (3 attempts)"]
        S6 --> S7["⚡ 7. Circuit Breaker OPENS"]
        S7 --> S8["🛡️ 8. FallbackFactory Returns Graceful Response"]
        S8 --> S9["📜 9. JSON Log Captures 'ConnectionRefused' with TraceId"]
    end

    S3 ===>|Initiate Chaos| S4
    S4 ===>|Test Resilience| S5

    %% Styling
    classDef happy fill:#1b5e20,stroke:#81c784,stroke-width:2px,color:#fff;
    classDef chaos fill:#b71c1c,stroke:#ef5350,stroke-width:2px,color:#fff;
    classDef resil fill:#e65100,stroke:#ffb74d,stroke-width:2px,color:#fff;
    classDef log fill:#4a148c,stroke:#ba68c8,stroke-width:2px,color:#fff;

    class S1,S2,S3 happy;
    class S4 chaos;
    class S5,S6,S7,S8 resil;
    class S9 log;
    
    style Phase 1: Normal Operations fill:#0d1117,stroke:#3fb950,stroke-dasharray: 5 5,color:#c9d1d9
    style Phase 2: Chaos Injection fill:#0d1117,stroke:#f85149,stroke-dasharray: 5 5,color:#c9d1d9
    style Phase 3: Resilience Verification fill:#0d1117,stroke:#d29922,stroke-dasharray: 5 5,color:#c9d1d9
```