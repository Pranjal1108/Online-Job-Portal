# Jobify+ — Online Job Portal

A job portal for candidates and recruiters, built with React 19 and a Java 21 Spring Boot backend. The application serves its frontend and API together and saves data in a local H2 database.

## What works

- Candidate and recruiter registration, login, logout, and editable profiles.
- Search by title, company, skill, and city; category, employment, and workplace filters; salary sorting and pagination. Searches can be bookmarked or shared and survive reloads.
- Candidate bookmarks, applications with cover letters and optional resume links, status tracking, and withdrawal.
- Recruiter job creation, editing, closing and reopening; applicant review and status updates.
- Session cookies, CSRF protection, BCrypt password hashing, validated inputs, and recruiter/candidate ownership checks.
- Responsive layouts, keyboard-accessible dialogs, reduced-motion support, and locally bundled fonts.

Starter jobs are clearly marked as samples. Applying to them saves an application in this portal; it does not contact an external employer.

## Run on Windows

Install Java 21 or later, Maven 3.6.3 or later, and Node.js 22.12 or later. Install pnpm 11 (or npm) and ensure the tools are on PATH.

Double-click `Start-Jobify.cmd`, or run:

```powershell
.\Start-Jobify.ps1 -Rebuild
```

Open http://localhost:8080. Stop the application with Ctrl+C. Subsequent starts can omit `-Rebuild`; use it after changing source files. A different port can be selected with `-Port 8082`.

The launcher also recognizes the Codex bundled Node/pnpm runtime when available. The project itself does not require Codex.

## Build and test manually

```powershell
cd frontend
pnpm install --frozen-lockfile
pnpm run build
cd ..
mvn -B -f backend/pom.xml package
java -jar backend/target/jobify-2.0.0.jar
```

Build the frontend before packaging the backend so the latest UI is included in the executable JAR. Backend integration tests run during packaging. GitHub Actions runs the same build and checks.

For frontend development, start the backend on port 8080 and run `pnpm dev` from `frontend`. Vite proxies API requests to that backend.

## Data and configuration

The default database is `data/jobify` relative to the directory where the server starts. Accounts, jobs, bookmarks, and applications persist between restarts. Back up that directory while the server is stopped. There are no preset accounts; create one through Sign Up and choose candidate or recruiter.

`.env.example` documents configuration. Spring Boot reads actual environment variables, not a `.env` file automatically. For example:

```powershell
$env:PORT = '8082'
$env:JOBIFY_BIND = '127.0.0.1'
java -jar backend/target/jobify-2.0.0.jar
```

Available settings: `PORT`, `JOBIFY_BIND`, `JOBIFY_DB_URL`, `JOBIFY_DB_USER`, `JOBIFY_DB_PASSWORD`, and `JOBIFY_SECURE_COOKIES`. The default server binds only to this computer. HTTPS hosting requires secure cookies and an appropriate bind address.

## Source layout

- `frontend/`: React UI, Vite, Motion, Radix dialogs, Tabler icons, and bundled fonts.
- `backend/`: Spring Boot API, JDBC persistence, schema, sample jobs, and integration tests.
- `Start-Jobify.*`: Windows build/start launcher.
- `src/Main/`, `lib/`, and older build files: original servlet prototype retained for reference. The new Maven workspace builds `backend/`; it does not compile the legacy servlet tree.

The original prototype's database password has been replaced with an environment lookup locally. Historical repository commits may still contain the old credential.

## Current scope

This is a local portfolio application. It does not yet provide email verification, password reset, resume uploads, employer verification, email delivery, or a hosted production database. Recruiters review applications inside their workspace. Historical sample listings are not connected to real companies.

## Credits

React Bits inspired the headline reveal; the retained component attribution and license are in `frontend/src/components/REACT-BITS-LICENSE.md`. 21st.dev was used as a design reference. UI icons are from Tabler and fonts are DM Sans and Manrope. Third-party code and assets retain their respective licenses.
