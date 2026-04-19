# 🏠 SmartSG

**A Cloud-Based Student Housing Decision Platform**

SmartSG is a full-stack web application designed to help students in Singapore find ideal housing and compatible roommates. It provides housing listing search, roommate matching based on lifestyle preferences, group formation for shared leases, and an invitation management system.

> Built as a group project for **NUS CS5224 — Cloud Computing** (Semester 2, AY2025/26).

---

## ✨ Key Features

| Feature | Description |
|---|---|
| 🔐 **Authentication** | JWT-based user registration & login with Spring Security |
| 🏡 **Listing Search** | Browse and filter rental listings with university distance info |
| 👤 **User Profile** | Set lifestyle preferences (sleep schedule, cleanliness, budget, etc.) |
| 🤝 **Roommate Matching** | Algorithm-driven recommendations based on profile compatibility |
| 👥 **Group Management** | Create / join groups for shared leases, with role-based membership |
| 💌 **Invitation System** | Send, accept, or reject group invitations with privacy masking |
| ❤️ **Favorites** | Save and manage favourite listings |

---

## 🏗️ Architecture

```
┌─────────────┐        REST API (JSON)       ┌──────────────────┐        JDBC        ┌────────────┐
│   Frontend  │  ◄──────────────────────►    │     Backend      │  ◄──────────────►  │ PostgreSQL │
│   (Vue 3)   │        + JWT Auth            │  (Spring Boot)   │                    │  (AWS RDS) │
└─────────────┘                              └──────────────────┘                    └────────────┘
```

### Frontend — `frontend/`

- **Vue 3** with Composition API
- **Vite** for dev server & build tooling
- **Vue Router** for client-side routing
- **Pinia** for state management
- **Element Plus** as UI component library
- **Axios** for HTTP requests

### Backend — `smartsg-backend/`

- **Spring Boot 3.5** (Java 17)
- **Spring Security** + JWT authentication
- **Spring Data JPA** + **MyBatis** (dual ORM)
- **PostgreSQL** database on AWS RDS
- **Springdoc OpenAPI** for Swagger API docs
- **Lombok** to reduce boilerplate

### Database — `db/`

- **PostgreSQL** hosted on **AWS RDS** (ap-southeast-1)
- 7 core tables: `User`, `UserProfile`, `Listing`, `Favorites`, `Group`, `GroupMember`, `GroupRequest`
- Custom `ENUM` types for sleep schedule, cleanliness, gender, group status, and role

---

## 📁 Project Structure

```
SmartSG/
├── frontend/                   # Vue 3 + Vite frontend
│   ├── src/
│   │   ├── api/                # API client modules
│   │   ├── views/              # Page components
│   │   │   ├── LoginView.vue
│   │   │   ├── RegisterView.vue
│   │   │   ├── SearchView.vue
│   │   │   ├── ListingDetailView.vue
│   │   │   ├── ProfileView.vue
│   │   │   ├── GroupsView.vue
│   │   │   ├── InvitationsView.vue
│   │   │   └── FavoritesView.vue
│   │   ├── router/             # Vue Router config
│   │   ├── store/              # Pinia stores
│   │   ├── services/           # Business logic helpers
│   │   └── constants/          # App constants
│   └── package.json
│
├── smartsg-backend/            # Spring Boot backend
│   ├── src/main/java/com/nus/cs5224/smartsg/
│   │   ├── config/             # Security, CORS, JWT filter, OpenAPI
│   │   ├── controller/         # REST controllers
│   │   │   ├── AuthController
│   │   │   ├── ListingController
│   │   │   ├── ProfileController
│   │   │   ├── GroupController
│   │   │   ├── InvitationController
│   │   │   ├── MatchingController
│   │   │   ├── FavoriteController
│   │   │   └── UniversityController
│   │   ├── dto/                # Request / Response DTOs
│   │   ├── entity/             # JPA entities
│   │   ├── enums/              # Enum types
│   │   ├── exception/          # Global exception handling
│   │   ├── mapper/             # MyBatis mappers
│   │   ├── repository/         # Spring Data JPA repositories
│   │   ├── service/            # Service interfaces
│   │   │   └── serviceImpl/    # Service implementations
│   │   └── util/               # Utility classes (JWT, etc.)
│   ├── src/test/java/com/nus/cs5224/smartsg/
│   │   ├── service/            # Functional unit tests
│   │   │   ├── AuthServiceImplTest
│   │   │   ├── FavoriteServiceImplTest
│   │   │   ├── GroupServiceImplTest
│   │   │   ├── InvitationServiceImplTest
│   │   │   ├── MatchingServiceImplTest
│   │   │   └── ProfileServiceImplTest
│   │   ├── util/               # Security unit tests
│   │   │   └── JwtUtilTest
│   │   └── load-test.py        # System performance load test (Locust)
│   ├── src/main/resources/
│   │   └── application.yaml    # App & DB configuration
│   └── pom.xml
│
├── db/                         # Database scripts & data
│   ├── schema.sql              # DDL for all tables
│   ├── reset_db.sql            # Drop & recreate script
│   ├── insert_data/            # SQL insert scripts
│   ├── datasets/               # Raw data files
│   ├── preprocessing.ipynb     # Data preprocessing notebook
│   └── postgresql_setup.md     # DB setup guide
│
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| Java | 17+ |
| Node.js | 18+ |
| npm | 9+ |
| PostgreSQL | 14+ (or use the hosted AWS RDS instance) |

### 1. Clone the Repository

```bash
git clone https://github.com/<your-org>/SmartSG.git
cd SmartSG
```

### 2. Database Setup

The project is pre-configured to connect to a hosted **AWS RDS** PostgreSQL instance. No local DB setup is required for development.

<details>
<summary>▶ Optional: Set up a local PostgreSQL database</summary>

Refer to [`db/postgresql_setup.md`](db/postgresql_setup.md) for full instructions. Quick summary:

```bash
# Create the database
psql -U postgres -c "CREATE DATABASE smartsg;"

# Run the schema
psql -U postgres -d smartsg -f db/schema.sql

# Insert mock data
psql -U postgres -d smartsg -f db/insert_data/insert_listings.sql
psql -U postgres -d smartsg -f db/insert_data/insert_users.sql
```

Then update `smartsg-backend/src/main/resources/application.yaml` with your local DB connection details.

</details>

### 3. Start the Backend

```bash
cd smartsg-backend
./mvnw spring-boot:run
```

The backend will start at **http://localhost:8080**.

> **API Docs:** Swagger UI is available at http://localhost:8080/swagger-ui/index.html

### 4. Start the Frontend

Create a `.env` file under the `frontend/` directory:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Then install dependencies and start the dev server:

```bash
cd frontend
npm install
npm run dev
```

The frontend will start at **http://localhost:5173** (default Vite port).

---

## 🔌 API Modules

| Module | Endpoint Prefix | Description |
|---|---|---|
| Auth | `/api/auth` | Register, login, get current user |
| Profile | `/api/profile` | View and update user lifestyle preferences |
| Listing | `/api/listings` | Search, filter, and view housing listings |
| Favorite | `/api/favorites` | Add / remove / list favourite listings |
| Group | `/api/groups` | Create, join, manage, and confirm groups |
| Invitation | `/api/invitations` | Send, accept, reject group invitations |
| Matching | `/api/matching` | Get roommate recommendations by compatibility |
| University | `/api/universities` | Retrieve supported university list |

All endpoints (except Auth) require a valid JWT token in the `Authorization: Bearer <token>` header.

---

## 🔒 Security

- **JWT Authentication** — Stateless token-based auth with 24-hour expiration
- **Spring Security** — Filter chain for endpoint protection
- **Password Hashing** — Secure credential storage
- **Privacy Masking** — PII (name, email) is masked for unconfirmed group members

---

## 🛠️ Tech Stack Summary

| Layer | Technology |
|---|---|
| Frontend | Vue 3, Vite, Pinia, Vue Router, Element Plus, Axios |
| Backend | Spring Boot 3.5, Spring Security, Spring Data JPA, MyBatis |
| Database | PostgreSQL 14+ (AWS RDS) |
| Auth | JWT (jjwt 0.12.6) |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Build | Maven (backend), npm (frontend) |

---

## 🧪 Testing

The project includes automated test suites covering **Functional**, **Security**, and **System Performance** testing.

### Functional Tests — JUnit 5 + Mockito

Unit tests for all core service implementations, validating business logic in isolation with mocked dependencies.

| Test Class | Coverage |
|---|---|
| `AuthServiceImplTest` | Registration, login, duplicate email detection, case-insensitive email handling |
| `FavoriteServiceImplTest` | Add / remove / list favourite listings |
| `GroupServiceImplTest` | Group creation, join, membership management, role-based operations |
| `InvitationServiceImplTest` | Send, accept, reject invitations with validation |
| `MatchingServiceImplTest` | Roommate compatibility scoring and recommendation logic |
| `ProfileServiceImplTest` | Profile CRUD and lifestyle preference validation |

**Run:**

```bash
cd smartsg-backend
./mvnw test
```

### Security Tests — JWT Validation

Dedicated tests for the JWT authentication mechanism, ensuring token integrity and security boundaries.

| Test Class | Coverage |
|---|---|
| `JwtUtilTest` | Token generation & parsing, valid/invalid token validation, token expiry enforcement |

### System Performance Tests — Locust

A Python-based load test using [Locust](https://locust.io/) to stress-test API endpoints under concurrent user load.

**Run:**

```bash
locust -f smartsg-backend/src/test/java/com/nus/cs5224/smartsg/load-test.py \
  --host "http://<your-host>" \
  --users 200 \
  --spawn-rate 10 \
  --headless \
  --run-time 300s
```

---

## 👥 Team

**NUS CS5224 — Group 3**

---

## 🤖 AI Declaration

This section declares the use of AI tools in the development of this project, as required by NUS CS5224 course policy.

### Backend — `smartsg-backend/`

**AI Tool Used:** Claude Opus 4.6 (Anthropic)

AI was used to assist in developing the backend codebase, including code generation, debugging, writing unit tests, and drafting documentation. All AI-generated code was reviewed, tested, and validated by team members. Core business requirements, API design, and architectural decisions were defined by the team.

---

### Frontend — `frontend/`

<!-- TODO: Please fill in the AI declaration for the frontend -->

**AI Tool Used:** _[To be filled in]_

_[To be filled in]_

---

### Database — `db/`

<!-- TODO: Please fill in the AI declaration for the database -->

**AI Tool Used:** _[To be filled in]_

_[To be filled in]_

---

### Deployment & Infrastructure

<!-- TODO: Please fill in the AI declaration for deployment/infrastructure -->

**AI Tool Used:** _[To be filled in]_

_[To be filled in]_

---

## 📄 License

This project is developed for academic purposes as part of the NUS CS5224 course.
