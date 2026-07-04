# Sprint Meet Summarizer

Upload a meeting recording and get back a summary, the key discussion points, and
a list of action items with owners and deadlines. I built it to stop losing track
of what actually gets decided in sprint meetings and standups.

**Live demo:** https://sprint-meet-summarizer.onrender.com

> It's on free hosting, so the first load can take 30–60 seconds to wake up. Sign
> up, then upload a video/audio file (`.mp3` / `.m4a`) to try it.



## What it does

* Transcribes the video/audio into text (Whisper)
* Writes a summary and pulls out the key discussion points
* Extracts action items — who owns each task and when it's due
* Flags open follow-up questions
* Lets you search past meetings by keyword
* Emails a summary to your team
* Has user accounts, so your meetings stay private



## Tech stack

* **Backend:** Java 21, Spring Boot, Spring Security (JWT)
* **Frontend:** Angular, Angular Material
* **Database:** PostgreSQL
* **AI:** Whisper (transcription) + Llama (summaries), running on Groq
* **Deploy:** Docker, hosted on Render
* **Email:** Resend(HTTP API)

The AI calls go through an OpenAI-compatible API, so you can point it at Groq
(free) or OpenAI by changing a couple of environment variables — no code changes.



## Running it

You'll need Docker and a free Groq API key from [console.groq.com](https://console.groq.com).

```bash
cp .env.example .env      # then paste your Groq key into OPENAI\\\_API\\\_KEY
docker compose up --build
```

The app runs at http://localhost:4200 and the API at http://localhost:8080.

The only variables you need to set in `.env`:

* `OPENAI\\\_API\\\_KEY` — your Groq key (`gsk\\\_...`)
* `RESEND\\\_API\\\_KEY` — optional, only if you want the email feature ([resend.com](https://resend.com))
* `JWT\\\_SECRET` — any base64 string (`openssl rand -base64 32`)

The database and model settings already have working defaults.



## How it works

Uploading a file kicks off a background job so the page never freezes. The video/audio
goes to Whisper for a transcript, the transcript goes to Llama (with a fixed JSON
format so the results are structured, not free text), and the summary and action
items get saved to Postgres. The meeting page checks for updates while it runs, so
you watch it move from Transcribing → Summarizing → done.



## Main API endpoints

* `POST /api/auth/register`, `POST /api/auth/login`
* `POST /api/meetings/upload` — upload a recording
* `GET /api/meetings` — list your meetings; `GET /api/meetings/{id}` — full detail
* `GET /api/meetings/search?keyword=` — search titles, summaries, transcripts
* `PUT /api/action-items/{id}/status` — mark a task Pending / In progress / Completed
* `POST /api/meetings/{id}/send-email` — email the summary



## Good to know

* Keep test clips short and use video/audio — Whisper caps uploads around 25 MB.
* On the free Render tier the services sleep when idle and the demo database is
reset from time to time, so the live link is best for a quick look, not permanent storage.



