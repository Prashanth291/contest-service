# Contest Service

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.6-green?style=for-the-badge&logo=springboot)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-black?style=for-the-badge&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue?style=for-the-badge&logo=postgresql)
![JPA](https://img.shields.io/badge/Spring%20Data%20JPA-Hibernate-green?style=for-the-badge&logo=hibernate)
![Maven](https://img.shields.io/badge/Maven-red?style=for-the-badge&logo=apachemaven)
![Lombok](https://img.shields.io/badge/Lombok-gray?style=for-the-badge)

> Part of the contest-manager platform — responsible for contest lifecycle and problem orchestration.

---

## Overview

The Contest Service is the **source of truth for contests and problems**.

It handles:
- Contest creation and scheduling
- Problem assignment and ordering
- Contest state transitions (UPCOMING → LIVE → ENDED)
- Publishing contest metadata to Kafka

Unlike Leaderboard Service, this is a **write-heavy service**.

---

## Responsibilities

- Create and manage contests
- Attach problems with scoring configuration
- Validate contest time windows
- Transition contest states automatically
- Publish `contest-events` when contest starts
- Serve contest data via REST APIs

---

## Platform Role

```
        ┌──────────────┐
        │ Contest      │
        │ Service      │
        └──────┬───────┘
               │ publishes
               ▼
        Kafka Topic
        "contest-events"
               │
┌──────────────┴──────────────┐
▼                             ▼

Execution Service Leaderboard Service
```

---

## Tech Stack

### Core
- Java 21
- Spring Boot 3.3.6

### Data
- PostgreSQL
- Spring Data JPA / Hibernate

### Messaging
- Apache Kafka

### Build
- Maven

---

## Database Schema

### `contests`

| Column | Type | Description |
| --- | --- | --- |
| id | VARCHAR PK | Contest ID |
| title | VARCHAR | Contest name |
| start_time | TIMESTAMP | Start time |
| end_time | TIMESTAMP | End time |
| status | VARCHAR | UPCOMING / LIVE / ENDED |
| created_at | TIMESTAMP |  |

---

### `problems`

| Column | Type | Description |
| --- | --- | --- |
| id | VARCHAR PK | Problem ID |
| title | VARCHAR | Problem title |
| difficulty | VARCHAR | EASY / MEDIUM / HARD |

---

### `contest_problems`

| Column | Type | Description |
| --- | --- | --- |
| id | UUID PK |  |
| contest_id | VARCHAR | FK |
| problem_id | VARCHAR | FK |
| problem_order | INT | A, B, C... |
| score | INT | Base score |

**Unique:** `(contest_id, problem_id)`

---

## Contest Lifecycle

UPCOMING → LIVE → ENDED

### Rules

- Contest becomes **LIVE** when `start_time <= now`
- Contest becomes **ENDED** when `end_time < now`
- No submissions allowed outside LIVE window

---

## Kafka Integration

### Topic: `contest-events`

Published when contest becomes LIVE.

```json
{
  "contestId": "contest_01",
  "title": "Weekly Round 1",
  "startTime": "2026-03-24T05:00:00Z",
  "endTime": "2026-03-24T07:00:00Z",
  "problems": [
    {
      "problemId": "prob_01",
      "order": 0,
      "label": "A",
      "title": "Two Sum",
      "score": 500
    }
  ]
}
```

**Why publish only at start?**

Because downstream services (Leaderboard, Execution) only need finalized contest state, not drafts.

---

## REST API

### POST `/contests`

Create a new contest.

```json
{
  "title": "Weekly Round 1",
  "startTime": "2026-03-24T05:00:00Z",
  "endTime": "2026-03-24T07:00:00Z"
}
```

### POST `/contests/{contestId}/problems`

Attach problems to contest.

```json
[
  {
    "problemId": "prob_01",
    "order": 0,
    "score": 500
  }
]
```

### GET `/contests/{id}`

Fetch contest details.

### GET `/contests`

List all contests.

---

## Scheduler

A background scheduler checks contest state transitions.

Responsibilities:
- Move UPCOMING → LIVE
- Publish Kafka event
- Move LIVE → ENDED

**Why scheduler instead of manual trigger?**

Manual triggers are unreliable. Time-based systems must be deterministic.

---

## Configuration

```
server:
  port: 8082

spring:
  application:
    name: contest-service

  datasource:
    url: jdbc:postgresql://localhost:5432/contest_db
    username: postgres
    password: your_password

  jpa:
    hibernate:
      ddl-auto: update

  kafka:
    bootstrap-servers: localhost:9092
```

---

## Running Locally

1. Create DB

```
CREATE DATABASE contest_db;
```

2. Start Kafka

```
zookeeper-server-start.sh config/zookeeper.properties
kafka-server-start.sh config/server.properties
```

3. Run service

```
mvn spring-boot:run
```

---

## Key Design Decisions

**Why separate contest_problems table?**

Because:

- A problem can exist in multiple contests
- Score varies per contest

**Why publish only one event?**

Avoids:

- Event duplication
- Inconsistent state across services

**Why not store submissions here?**

Separation of concerns:

- Contest Service → metadata
- Submission Service → data ingestion
- Execution Service → verdicts
- Leaderboard → ranking

---

## Project Structure

```
contest-service/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── kafka/
└── scheduler/
```

---

## Related Services

- Auth Service
- Submission Service
- Execution Service
- Leaderboard Service

---

## License

Personal learning project.
