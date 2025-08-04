# A first-come, first-served coupon system under high concurrency
A Spring Boot-based first-come-first-served coupon issuance system.


## 🔹 1. Project Overview

In a first-come-first-served coupon system, when multiple users simultaneously request the same coupon, the following issues arise:
- Race Conditions: Duplicate coupon issuance due to concurrent requests
- Inventory Management Errors: More coupons issued than actual stock
- Data Consistency Violations: Mismatch between issued quantity and actual issuance records

To solve these concurrency issues, this project implements and performance-tests 5 different strategies:
- **Basic Logic**
- **Pessimistic Locking**
- **Optimistic Locking**
- **Redis Distributed Locking**
- **Redis + Kafka Hybrid Processing**

To further improve performance under high concurrency, the project also includes **Redis-based Optimizations**:
- **Redis Cache Pre-check** 
- **Redis List Queue** 

These are designed to reduce DB load and increase throughput.

The project measures **throughput** and **latency** for each strategy to demonstrate performance improvements in concurrent environments.

<br><br>
## 🔹 2. Architecture

Each branch in this project implements a different concurrency strategy, built on a shared structure defined in the main branch.

- master – Shared infrastructure (e.g. controller, service interface, DB config, test utilities)
- [strategy/basic](https://github.com/kangsujin2/coupon/tree/strategy/basic) – No concurrency control (baseline)
- [strategy/pessimistic](https://github.com/kangsujin2/coupon/tree/strategy/pessimistic) – Pessimistic locking
- [strategy/optimistic](https://github.com/kangsujin2/coupon/tree/strategy/optimistic) – Optimistic locking
- [strategy/redis](https://github.com/kangsujin2/coupon/tree/strategy/redis) – Redis-based distributed lock
- [strategy/redis-kafka](https://github.com/kangsujin2/coupon/tree/strategy/redis-kafka) – Redis atomic validation + Kafka asynchronous processing


For a detailed comparison of each strategy, see the tables below.

<br><br>
## 🔹 3. Concurrency Control Strategy Overview

| Strategy | Description | Pros | Cons | 
|----------|-------------|------|------|
| Basic Logic | Simple DB update<br>without concurrency control | • Simple implementation | • Race conditions<br>• Data inconsistency | 
| Pessimistic Locking | Database row-level locks<br> (`SELECT ... FOR UPDATE`) | • Strong consistency<br>• Prevents race conditions<br>• Database-level guarantees | • Performance bottleneck<br>• Lock contention<br>• Potential deadlocks | 
| Optimistic Locking | Conditional updates<br> (`WHERE quantity > 0`) | • No DB-level locks<br>• Fast under low contention | • Retry logic required<br>• Wasted work on conflict | 
| Redis Lock | Distributed lock<br>(Redisson) | • Distributed system support<br>• High performance<br>• Scalable across services | • External dependency<br>• Redis availability risk | 
| Redis + Kafka | Atomic Redis validation + async Kafka queue | • Extremely high throughput<br>• Async DB writes<br>• Event buffering<br>• Strict deduplication via Lua|• Operational complexity (infra)<br>• Eventual consistency|

<br><br>
## 🔹 4. Redis-based Optimizations
To further enhance performance and reduce database load under high concurrency, the following Redis-based optimizations are applied:


### Redis Cache Pre-check
Configured with **Caffeine** and **Redis** cache managers to store coupon availability.  
By checking the cache before querying the database, unnecessary DB hits are avoided and response time is improved.


### Redis Queue Processing
Implements a **Redis-backed queue** to handle coupon issuance requests safely using atomic operations such as `LPOP`.  
This ensures strict **FIFO ordering** and prevents concurrent access issues during high-traffic periods.

<br><br>
## 🔹 5. Performance Testing
Detailed test results and analysis will be added soon.

<br><br>
## 🔹 6. How to Run
1️⃣ Clone the repository
```bash
git clone https://github.com/kangsujin2/coupon.git
cd coupon
```

2️⃣ Checkout a strategy branch
```bash
git checkout strategy/optimistic  # or basic / pessimistic / redis
```

3️⃣ Configure the database
Update your DB settings in src/main/resources/application.yml.

4️⃣ Run the application
```bash
./gradlew bootRun
```

<br><br>
## 🔹 7. Tech Stack

- **Language & Framework:** Java 17, Spring Boot 3.3.0  
- **Database:** MariaDB (JDBC Driver)  
- **Caching & Locking:** Redis (Redisson), Caffeine  
- **Data Access:** Spring Data JPA, Spring Data Redis  
- **Build Tool:** Gradle  
- **Testing:** JUnit 5, K6 (load testing)


<br><br>
## 📌 Future Work

- Add Kafka-based event processing for asynchronous post-coupon handling (e.g. logging, messaging, or analytics)


