# Sprint Meet Summarizer

Upload a meeting recording and get back a clean summary, the key discussion
points, and a list of action items with owners and deadlines — all searchable
and shareable by email.

The app transcribes audio with **OpenAI Whisper**, then uses **GPT** to extract
a structured summary and action items, stores everything in **PostgreSQL**, and
presents it in an **Angular** dashboard backed by a **Spring Boot** API.

\---

## Table of contents

* [Features](#features)
* [Tech stack](#tech-stack)
* [Architecture](#architecture)
* [How processing works](#how-processing-works)
* [Quick start with Docker](#quick-start-with-docker)
* [Running locally without Docker](#running-locally-without-docker)
* [Configuration reference](#configuration-reference)
* [API reference](#api-reference)
* [Project structure](#project-structure)
* [Notes \& next steps](#notes--next-steps)

\---

## Features

* **Upload** `.mp3`, `.wav`, `.m4a`, or `.mp4` recordings (drag-and-drop or browse)
* **Automatic transcription** via Whisper
* **AI summary** with key discussion points and follow-up questions
* **Action item extraction** — owner, task, and a resolved calendar deadline
* **Live status** — the meeting page polls while transcription/summarization runs
* **Searchable history** — search across titles, summaries, and transcripts
* **Action item tracking** — toggle each item between Pending / In progress / Completed
* **Email the summary** to teammates as a formatted message
* **JWT authentication** — each user only sees their own meetings

\---

## Tech stack

|Layer|Technology|
|-|-|
|Frontend|Angular 18 (standalone components), Angular Material|
|Backend|Java 21, Spring Boot 3.3, Spring Security|
|Database|PostgreSQL 16|
|AI|OpenAI Whisper (transcription) + GPT (summarization)|
|Auth|Spring Security + JWT (jjwt)|
|Storage|Local filesystem (S3-swappable interface)|
|Email|Spring Mail (JavaMailSender)|
|Deploy|Docker + Docker Compose|

\---

## Architecture

```
┌─────────────┐      REST + JWT      ┌──────────────────────────┐
│   Angular    │ ───────────────────▶ │      Spring Boot API      │
│  (Material)  │ ◀─────────────────── │                          │
└─────────────┘     JSON responses   │  Controllers             │
                                      │  Services                │
                                      │   ├─ StorageService      │
                                      │   ├─ TranscriptionService│──▶ Whisper API
                                      │   ├─ AiSummaryService    │──▶ GPT API
                                      │   └─ EmailService        │──▶ SMTP
                                      │  Async pipeline          │
                                      └────────────┬─────────────┘
                                                   │ JPA / Hibernate
                                                   ▼
                                          ┌──────────────────┐
                                          │   PostgreSQL      │
                                          │ users, meetings,  │
                                          │ transcripts,      │
                                          │ summaries,        │
                                          │ action\_items      │
                                          └──────────────────┘
```

The backend follows a standard layered design: **controllers** handle HTTP,
**services** hold the business logic, and **repositories** (Spring Data JPA)
handle persistence. DTOs (Java records) keep entities out of the API surface.

\---

## How processing works

Uploading returns immediately (HTTP `202 Accepted`) and the heavy work runs on a
background thread pool, so the UI never blocks:

1. `UPLOADED` — file is validated and saved; a `Meeting` row is created.
2. `TRANSCRIBING` — the audio is sent to Whisper; the transcript is saved.
3. `SUMMARIZING` — the transcript is sent to GPT with a strict JSON schema.
4. `COMPLETED` — summary, key points, action items, and follow-up questions are saved.
5. `FAILED` — if anything errors, the message is stored and shown in the UI.

Each step commits in its own transaction, so the dashboard sees progress as it
happens (the meeting detail page polls every few seconds while in progress).

GPT is asked to return JSON matching a fixed schema and is given today's date so
it can resolve relative deadlines ("by Friday") into real dates. The response is
deserialized straight into typed records — no brittle text parsing.

\---

## Quick start with Docker

This is the fastest way to run the whole stack (database + API + web app).

**Prerequisites:** Docker and Docker Compose, plus an API key for an AI provider.

> \*\*No payment method? Run it free.\*\* You don't need a paid OpenAI account.
> Groq offers a free, no-credit-card API that hosts both Whisper (transcription)
> and Llama (summaries) through an OpenAI-compatible endpoint. Sign up at
> \[console.groq.com](https://console.groq.com), create an API key, and use
> \*\*Option A\*\* in `.env.example` (already filled in except for the key). The app
> works unchanged — only the provider settings differ.

```bash
# 1. From the project root, create your env file
cp .env.example .env

# 2. Edit .env and set OPENAI\_API\_KEY (and SMTP settings if you want email)

# 3. Build and start everything
docker compose up --build
```

Then open:

* **Web app:** http://localhost:4200
* **API:** http://localhost:8080

Create an account, upload a recording, and watch the notes generate.

To stop: `docker compose down` (add `-v` to also wipe the database and uploads).

\---

## Running locally without Docker

Useful for active development with hot reload.

### 1\. PostgreSQL

Have PostgreSQL running and create a database:

```sql
CREATE DATABASE meeting\_notes;
```

(The default connection is `localhost:5432`, user `postgres`, password
`postgres` — override via env vars if yours differs.)

### 2\. Backend (Java 21 + Maven)

```bash
cd backend

# Required: your OpenAI key
export OPENAI\_API\_KEY=sk-your-key-here

# Optional overrides (defaults shown)
export DB\_URL=jdbc:postgresql://localhost:5432/meeting\_notes
export DB\_USERNAME=postgres
export DB\_PASSWORD=postgres

./mvnw spring-boot:run     # or: mvn spring-boot:run
```

The API starts on http://localhost:8080. Hibernate creates the tables on first
run (`ddl-auto: update`).

> \*\*Note:\*\* A Maven wrapper (`./mvnw`) is referenced for convenience. If it isn't
> present, use a locally installed `mvn`.

### 3\. Frontend (Node 18+ / Angular)

```bash
cd frontend
npm install
npm start          # ng serve
```

Open http://localhost:4200. The dev server proxies nothing — the app calls the
API at `http://localhost:8080/api` (configurable in
`src/environments/environment.ts`).

\---

## Configuration reference

All backend config is environment-variable driven (see
`backend/src/main/resources/application.yml`).

|Variable|Default|Description|
|-|-|-|
|`OPENAI\_API\_KEY`|*(none — required)*|OpenAI key for Whisper + GPT|
|`GPT\_MODEL`|`gpt-4o-mini`|Chat model for summarization|
|`WHISPER\_MODEL`|`whisper-1`|Transcription model|
|`DB\_URL`|`jdbc:postgresql://localhost:5432/meeting\_notes`|JDBC URL|
|`DB\_USERNAME`|`postgres`|DB user|
|`DB\_PASSWORD`|`postgres`|DB password|
|`JWT\_SECRET`|*(dev placeholder)*|Base64 secret, must decode to ≥ 256 bits|
|`JWT\_EXPIRATION\_MS`|`86400000`|Token lifetime (24h)|
|`MAIL\_HOST`|`smtp.gmail.com`|SMTP host|
|`MAIL\_PORT`|`587`|SMTP port|
|`MAIL\_USERNAME`|*(empty)*|SMTP user|
|`MAIL\_PASSWORD`|*(empty)*|SMTP password / app password|
|`MAIL\_FROM`|`no-reply@meetingnotes.local`|From address|
|`STORAGE\_DIR`|`./uploads`|Where uploaded files are stored|
|`MAX\_FILE\_SIZE`|`100MB`|Max upload size|
|`CORS\_ALLOWED\_ORIGINS`|`http://localhost:4200`|Allowed frontend origin(s)|

> \*\*Generate a real JWT secret\*\* before deploying:
> `openssl rand -base64 32`

\---

## API reference

All `/api/meetings/\*\*` and `/api/action-items/\*\*` endpoints require an
`Authorization: Bearer <token>` header.

### Auth

|Method|Path|Body|Description|
|-|-|-|-|
|POST|`/api/auth/register`|`{ fullName, email, password }`|Create account → JWT|
|POST|`/api/auth/login`|`{ email, password }`|Sign in → JWT|

### Meetings

|Method|Path|Description|
|-|-|-|
|POST|`/api/meetings/upload`|Multipart `file` (+ optional `title`)|
|GET|`/api/meetings`|List the current user's meetings|
|GET|`/api/meetings/{id}`|Full meeting detail|
|GET|`/api/meetings/search?keyword=`|Search title/summary/transcript|
|POST|`/api/meetings/{id}/send-email`|Body `{ recipients: \[email, ...] }`|

### Action items

|Method|Path|Description|
|-|-|-|
|PUT|`/api/action-items/{id}/status`|Body `{ status: PENDING\|IN\_PROGRESS\|COMPLETED }`|

**Example — register then upload:**

```bash
# Register
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/register \\
  -H 'Content-Type: application/json' \\
  -d '{"fullName":"Ada Lovelace","email":"ada@example.com","password":"password123"}' \\
  | python3 -c "import sys,json;print(json.load(sys.stdin)\['token'])")

# Upload a recording
curl -X POST http://localhost:8080/api/meetings/upload \\
  -H "Authorization: Bearer $TOKEN" \\
  -F "file=@sprint-planning.mp3" \\
  -F "title=Sprint Planning"
```

\---

## Project structure

```
meeting-notes-ai/
├── backend/                     # Spring Boot API
│   ├── src/main/java/com/meetingnotes/
│   │   ├── config/              # Security, OpenAI client, async executor
│   │   ├── security/            # JWT service, filter, user details
│   │   ├── entity/              # JPA entities + enums
│   │   ├── repository/          # Spring Data repositories
│   │   ├── dto/                 # Request/response records
│   │   ├── service/             # Business logic + AI pipeline
│   │   ├── controller/          # REST endpoints
│   │   └── exception/           # Global error handling
│   ├── src/main/resources/application.yml
│   └── Dockerfile
├── frontend/                    # Angular app
│   └── src/app/
│       ├── core/                # Services, guards, interceptor, models
│       ├── features/            # auth, dashboard, upload, meeting-detail
│       └── shared/              # Status helpers
├── db/schema.sql                # Reference schema (Hibernate auto-generates)
├── docker-compose.yml
├── .env.example
└── README.md
```

\---

## Notes \& next steps

This is a portfolio-grade reference implementation. A few things worth knowing,
and natural directions to take it further:

* **Costs:** Whisper and GPT calls are billed to your OpenAI account. Test with
short clips first.
* **Storage:** Files are stored on local disk by default. `StorageService` is an
interface — drop in an `S3StorageService` (add the AWS SDK, mark it `@Primary`)
to switch to S3 without touching anything else.
* **Schema management:** `ddl-auto: update` is convenient for development. For
production, set it to `validate` and introduce Flyway or Liquibase migrations
(the `db/schema.sql` here is a good starting point).
* **Possible enhancements:** speaker diarization, calendar integration for
deadlines, re-running analysis with a different model, role-based sharing of
meetings across a team, and pagination on the dashboard.

```

