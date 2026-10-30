# onodemBackend — Honour Your Mentor (Java + Spring Boot + Supabase)

Modular monolith backend for **Honour Your Mentor**, built with **Java 21**, **Spring Boot 3.3.4**, **PostgreSQL (Supabase / Neon)**, **Spring Data JPA**, **Spring Security (JWT)**, **Flyway**, and **OpenAPI / Swagger UI**.

---

## 📁 Project Locations

- **IntelliJ Project**: `C:\Users\Sharon\IdeaProjects\onodemBackend`
- **Frontend Workspace Copy**: `c:\Users\Sharon\honor\NewApp\backend`

---

## 🚀 Quick Start in IntelliJ IDEA

1. Open **IntelliJ IDEA**.
2. Select **File → Open** and choose:
   ```
   C:\Users\Sharon\IdeaProjects\onodemBackend
   ```
3. IntelliJ will automatically detect `pom.xml`. If prompted, click **"Load Maven Project"**.
4. Open `src/main/java/com/honouryourmentor/HonourYourMentorApplication.java` and click the green **Run** ▶ icon.
5. Visit the interactive Swagger documentation:
   **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

---

## 🗄️ Supabase PostgreSQL Setup

You can configure Supabase in **one of two ways**:

### Option 1: One-Click SQL Paste (Easiest)
1. Go to your [Supabase Dashboard](https://supabase.com/dashboard) → **SQL Editor**.
2. Open the file `supabase_schema.sql` located in this project root.
3. Paste all contents into the Supabase SQL Editor and click **RUN**.
4. All tables, indexes, constraints, and initial demo data (Mrs. Anita Sharma, Dr. David Mitchell, Vikram Rao, Elena Rostova, Priya Nair) are created instantly!

### Option 2: Automatic Spring Boot Flyway Connection
Configure your Supabase connection strings in `src/main/resources/application-supabase.yml` or set environment variables:
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://db.<YOUR-PROJECT-REF>.supabase.co:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<YOUR-DB-PASSWORD>
```
*Tip: If using Supabase's Connection Pooler (recommended for low-latency), use port 6543:*
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-<region>.pooler.supabase.com:6543/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.<YOUR-PROJECT-REF>
```

---

## 🏗️ Architecture & Modules

The backend is structured as a clean modular monolith:

```
com.honouryourmentor/
│
├── config/             # CORS, OpenAPI / Swagger UI, Spring Beans
├── security/           # JWT Service, Authentication Filter, SecurityFilterChain
├── common/             # ApiResponse<T>, PagedResponse<T>, BaseEntity, GlobalExceptionHandler
├── auth/               # Register, Login, Refresh JWT, 5-digit OTP Password Reset
├── user/               # User profiles, bio, avatar, user settings
├── mentor/             # Mentor CRUD, category (school, study, work, life), traits
├── tribute/            # Tributes, Memories, Letters, Lessons, Milestones, Pinning
├── feed/               # Public Feed with category, relationship and search filters
├── engagement/         # Likes and Comments for public tributes
├── reminder/           # Birthday and anniversary reminders for mentors
├── notification/       # In-app notifications
└── admin/              # Tribute content reports and moderation
```

---

## 🌐 REST API Endpoints

### 🔐 Authentication (`/api/v1/auth`)
| Method | Endpoint | Description | Public? |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Register new user account | Yes |
| `POST` | `/api/v1/auth/login` | Login with email or username | Yes |
| `POST` | `/api/v1/auth/refresh` | Refresh expired JWT token | Yes |
| `POST` | `/api/v1/auth/forgot-password` | Send 5-digit OTP to user email | Yes |
| `POST` | `/api/v1/auth/verify-otp` | Verify 5-digit OTP | Yes |
| `POST` | `/api/v1/auth/reset-password` | Set new password with OTP | Yes |
| `GET`  | `/api/v1/auth/me` | Get current logged-in user info | No |

### 👤 User Profiles (`/api/v1/users`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/users/me` | Get logged-in user profile |
| `PUT` | `/api/v1/users/me` | Update bio, avatar, location, etc. |
| `GET` | `/api/v1/users/{id}` | Get public user summary |

### 🎓 Mentors (`/api/v1/mentors`)
| Method | Endpoint | Description |
|---|---|---|
| `GET`    | `/api/v1/mentors` | List all mentors for logged-in user |
| `POST`   | `/api/v1/mentors` | Add a new mentor |
| `GET`    | `/api/v1/mentors/{id}` | Get mentor profile details |
| `PUT`    | `/api/v1/mentors/{id}` | Update mentor details |
| `DELETE` | `/api/v1/mentors/{id}` | Delete mentor |

### 📜 Tributes & Memories (`/api/v1/tributes`)
| Method | Endpoint | Description |
|---|---|---|
| `POST`   | `/api/v1/tributes` | Create tribute / memory / letter / lesson |
| `GET`    | `/api/v1/tributes/my-tributes` | Get current user's tributes & memories |
| `GET`    | `/api/v1/tributes/public-feed` | Paginated public feed |
| `GET`    | `/api/v1/tributes/{id}` | Get single tribute by ID |
| `PUT`    | `/api/v1/tributes/{id}` | Update tribute |
| `DELETE` | `/api/v1/tributes/{id}` | Delete tribute |
| `POST`   | `/api/v1/tributes/{id}/pin` | Toggle pinned on Memory Wall |
| `GET`    | `/api/v1/tributes/mentor/{mentorId}` | Get tributes for a mentor |

### 🔍 Feed (`/api/v1/feed`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/feed` | Filtered public feed (`category`, `relationship`, `kind`, `search`, `page`, `size`) |

### ❤️ Engagement (`/api/v1`)
| Method | Endpoint | Description |
|---|---|---|
| `POST`   | `/api/v1/tributes/{id}/like` | Toggle like/unlike |
| `GET`    | `/api/v1/tributes/{id}/like-status` | Get like status |
| `POST`   | `/api/v1/tributes/{id}/comments` | Add comment |
| `GET`    | `/api/v1/tributes/{id}/comments` | Get comments |
| `DELETE` | `/api/v1/comments/{id}` | Delete comment |

### ⏰ Reminders (`/api/v1/reminders`)
| Method | Endpoint | Description |
|---|---|---|
| `GET`    | `/api/v1/reminders` | List mentor birthday & anniversary reminders |
| `POST`   | `/api/v1/reminders` | Create reminder |
| `PUT`    | `/api/v1/reminders/{id}` | Update reminder |
| `DELETE` | `/api/v1/reminders/{id}` | Delete reminder |

---

## 🧪 Default Test User & Seed Data

- **Email**: `sarita@example.com`
- **Password**: `password123`
- Pre-seeded mentors: `Mrs. Anita Sharma`, `Dr. David Mitchell`, `Vikram Rao`, `Elena Rostova`, `Priya Nair`
