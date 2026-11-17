# Agora

Agora is a self-hosted note platform: plain-text notes, LaTeX notes and a todo list, served by your own backend and editable from a web app or a desktop/mobile client.

## Components

| Folder | Description | Stack |
|--------|-------------|-------|
| `AgoraServer/` | REST API and database layer | Kotlin, Ktor (Netty), Exposed, MariaDB/MySQL |
| `web-app/` | Browser frontend | React 19, Vite, Tailwind, Monaco, KaTeX |
| `AgoraFrontend/` | Multiplatform client | Kotlin Multiplatform / Compose (Desktop/JVM, Android, iOS) |
| `agora_boostrap.sql` | Database bootstrap script (creates `AgoraDB` and its tables) | MySQL/MariaDB |

## Features

- User registration and login, with notes scoped per user
- Plain-text notes: create, update, delete
- LaTeX notes: create, update, list, delete, with a dedicated editor
- Todo list, including an Emacs-oriented export endpoint
- Multiple clients (web and KMP) against the same backend
- Everything can run on your own machine or network

## Architecture

1. `AgoraServer` exposes a REST API (users, notes, LaTeX notes, todos) and stores data in MariaDB/MySQL.
2. `web-app` calls the API under `/api`, proxied by Vite in development.
3. `AgoraFrontend` calls the API directly over HTTP.

## Prerequisites

- JDK (the server Gradle toolchain targets Java 25; the Dockerfile builds with JDK 17 and runs on Temurin 24)
- Node.js 18+ and npm
- MariaDB or MySQL

## Installation

### 1. Get the code

```bash
git clone https://github.com/Sim0Batt/Agora.git
cd Agora
```

### 2. Install prerequisites (example for Debian/Ubuntu)

```bash
sudo apt install mariadb-server nodejs npm openjdk-25-jdk   # use the JDK package available on your distro
sudo systemctl enable --now mariadb
```

### 3. Create the database

```bash
sudo mariadb < agora_boostrap.sql
```

> The script **drops and recreates** `AgoraDB`. Do not run it on a database you want to keep.

Create a dedicated DB user instead of using root:

```sql
CREATE USER 'agora'@'localhost' IDENTIFIED BY 'choose_a_strong_password';
GRANT ALL PRIVILEGES ON AgoraDB.* TO 'agora'@'localhost';
FLUSH PRIVILEGES;
```

### 4. Configure the server

Credentials and server settings are **not** stored in the code: the server reads them from an XML file outside the repository. It uses the first of these paths that exists:

1. `/home/gaia/etc/supervisor/settings.xml`
2. `/Users/Simone/workspace/etc/agora/settings.xml`
3. `/home/simone/workspace/etc/agora/settings.xml`

These paths are hard-coded in `AgoraServer/src/main/kotlin/configuration/ReadXMLConfiguration.kt`; edit them for your machine. If none exists, the server fails with `Configuration Read: File Not Found`.

```xml
<Configuration>
  <Database>
    <DBConnectionString>jdbc:mariadb://localhost:3306/AgoraDB</DBConnectionString>
    <DBDriver>org.mariadb.jdbc.Driver</DBDriver>
    <DBUser>agora</DBUser>
    <DBPassword>choose_a_strong_password</DBPassword>
  </Database>
  <Server>
    <Host>0.0.0.0</Host>
    <Port>18035</Port>
  </Server>
</Configuration>
```

Never commit this file.

### 5. Start the server

```bash
cd AgoraServer
./gradlew run
# or build a standalone jar
./gradlew shadowJar && java -jar build/libs/AgoraServer.jar
```

### 6. Start a client

Web app:

```bash
cd web-app
npm install
npm run dev          # development, usually at http://localhost:5173
npm run build        # production build in dist/
```

Set the backend address in the `proxy` section of `web-app/vite.config.ts` (e.g. `http://localhost:18035`, with no trailing whitespace).

Desktop (KMP):

```bash
cd AgoraFrontend
./gradlew :composeApp:run
```

The backend URL is defined in `AgoraFrontend/composeApp/src/commonMain/kotlin/org/example/agora/server/ServerRequests.kt` (`URL`, default `http://0.0.0.0:18035`). Change it if the server runs on another host.

## Usage

1. **Start order**: database, then `AgoraServer`, then a client.
2. **Account**: register a user and log in from the client. All notes and todos are tied to your user.
3. **Home** (`/`): overview and the todo widget, where you can add and delete todos.
4. **Notes** (`/notes`): list, create and delete plain-text notes; open one (`/notes/editor/:id`) to edit its content, which is saved to the server.
5. **LaTeX notes** (`/latex`): list and create LaTeX notes; open one (`/latex/editor/:id`) for the editor with a KaTeX live preview.
6. **Convert**: `/notes/copy-to-latex` copies a plain note into a new LaTeX note.
7. **Multiple devices**: point every client at the same server, so web and desktop see the same data.
8. **Emacs**: `GET /todo/emacs` returns the todos in a format meant for Emacs integration.

Quick API check once the server is running:

```bash
curl http://localhost:18035/user
```

## API overview

| Area | Endpoints |
|------|-----------|
| Users | `GET/POST /user`, `GET /user/{id}`, `POST /login` |
| Notes | `GET/POST /note`, `GET/DELETE /note/{id}`, `POST /updateNote`, `POST /content/{id}` |
| LaTeX | `GET /latex/list/{userId}` and related note endpoints |
| Todo | `GET/POST /todo`, `DELETE /todo/{todoId}`, `GET /todo/emacs` |

See `AgoraServer/src/main/kotlin/server/ServerConfig.kt` for the full routing.

## Docker

`AgoraServer/DockerFile` builds the server in two stages (Gradle build, then a Temurin JRE image) and exposes port 8000. Build it from the repository root:

```bash
docker build -f AgoraServer/DockerFile -t agora-server .
```

The container still needs the XML configuration file at one of the paths above, for example mounted as a volume.

## Security

- Keep database credentials only in the external XML file, never in source code or commits.
- If a credential was ever committed, rotate it and rewrite the history; deleting it in a later commit is not enough.
- Passwords are stored as provided by the server logic; use the server on a trusted network or behind HTTPS.
