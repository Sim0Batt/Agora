# Agora

Agora is a local-first note platform composed of three main parts:

- a Kotlin/Ktor backend server (`AgoraServer`)
- a web frontend built with React + Vite (`web-app`)
- a Kotlin Multiplatform frontend (`AgoraFrontend`) for Desktop/JVM

The project is designed to run on your own machine or your own infrastructure. Notes and LaTeX content are managed by your own server and can be edited from either the web frontend or the KMP app.

## Repository Structure

- `AgoraServer/`: Ktor REST API + database access layer
- `web-app/`: browser frontend for notes and LaTeX notes
- `AgoraFrontend/`: Compose Multiplatform client (Android, iOS, Desktop/JVM)
- `dump-tabelle-agora-data.sql`: SQL dump for database bootstrap/migration support

## Core Features

- Local/self-hosted architecture (no required external SaaS)
- User login and user-scoped note management
- Plain text note creation, update, and delete
- LaTeX note creation, update, listing, and delete
- Dedicated LaTeX editing workflow
- Multi-client access:
	- Web app for browser usage
	- KMP app for mobile/desktop usage

## Architecture at a Glance

1. `AgoraServer` exposes REST endpoints (users, notes, LaTeX notes).
2. Data is persisted in a local database (configured in server code).
3. `web-app` calls the backend via `/api` (proxied by Vite in development).
4. `AgoraFrontend` (KMP) calls the backend directly via HTTP.

## Local-First / Self-Hosted Notes

This repository is intended to be managed locally:

- backend and database can run on your machine or your private network
- clients connect to your backend endpoint
- you keep control over the data and deployment

If you want full local development, ensure every client points to your local server address and port.

## Prerequisites

- JDK 17+ (project currently uses modern Kotlin/Gradle setup)
- Gradle (wrapper scripts are included)
- Node.js 18+ and npm
- MySQL (or a compatible database setup according to server config)

## Backend Setup (`AgoraServer`)

1. Configure database connection values in `AgoraServer/src/main/kotlin/database/DatabaseConfig.kt`.
2. Start the server from the `AgoraServer` folder:

```bash
./gradlew run
```

or build the runnable jar:

```bash
./gradlew shadowJar
java -jar build/libs/AgoraServer.jar
```

Default HTTP port is `18035` unless overridden by the `PORT` environment variable.

## Web Frontend Setup (`web-app`)

1. Install dependencies:

```bash
cd web-app
npm install
```

2. Review API proxy target in `web-app/vite.config.ts` and point it to your backend host/port.
3. Start development server:

```bash
npm run dev
```

The web app provides note and LaTeX-related pages, including a dedicated LaTeX editing flow.

## KMP Frontend Setup (`AgoraFrontend`)

From the `AgoraFrontend` folder, use Gradle tasks depending on target:

- Desktop/JVM:

```bash
./gradlew :composeApp:run
```
KMP networking is implemented in shared code and targets the backend HTTP API.

## API Scope (High Level)

The backend includes endpoints for:

- user registration/login and lookup
- text notes CRUD
- LaTeX notes CRUD and listing
- note content retrieval/update

## Deployment Notes

- Everything can be hosted privately (local machine, NAS, VPS, LAN server).
- A Dockerfile is available for backend containerization.
- Keep secrets and DB credentials out of source control for production usage.

## Suggested Development Flow

1. Start database.
2. Start `AgoraServer`.
3. Start either `web-app` or `AgoraFrontend` (or both).
4. Create/edit notes and LaTeX notes through your preferred client.
