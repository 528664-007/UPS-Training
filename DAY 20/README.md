# Form & Field

> **Good things, chosen well.**
>
> A thoughtfully styled storefront backed by a Spring Boot API. Browse the collection, search by name, save items in a persistent cart, and place authenticated demo orders.

![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![React 18](https://img.shields.io/badge/React-18-149ECA?logo=react&logoColor=white)
![MySQL 8](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)

Form & Field is a full-stack e-commerce starter with a responsive React storefront and a REST API built with Spring Boot. It includes a MySQL-backed catalog, JWT authentication, role-protected catalog management, and order placement with server-side stock checks.

> **Demo scope:** this project does not process payments, arrange delivery, or provide production inventory operations. Orders are recorded with status `PLACED`; no money is charged.

## What you can do

- **Explore the catalog** — browse products, filter by category, search, and view product details.
- **Keep a shopping bag** — add products and adjust quantities; cart contents persist in the browser.
- **Create an account** — register or sign in to receive a signed JWT bearer token.
- **Place and review orders** — check out while signed in and view your order history.
- **Manage the catalog** — administrators can create, update, and delete products and categories.
- **Start with sample data** — the backend seeds a small catalog when both categories and products are empty.

## Built with

| Layer | Technologies |
|---|---|
| Storefront | React 18, React Router, Vite |
| API | Java 17, Spring Boot 3, Spring MVC, Spring Security, Bean Validation |
| Persistence | Spring Data JPA, MySQL 8 |
| Authentication | JWT (JJWT), BCrypt password hashing |
| Tests | JUnit 5, Mockito, Spring Boot Test, H2 |

## How it fits together

```text
┌──────────────────────┐        HTTP / JSON         ┌──────────────────────┐
│  React + Vite store  │ ─────────────────────────▶ │  Spring Boot REST API│
│  localhost:5173      │ ◀───────────────────────── │  localhost:8080/api  │
└──────────────────────┘       JWT for protected    └──────────┬───────────┘
                                  requests                      │ Spring Data JPA
                                                                ▼
                                                      ┌──────────────────────┐
                                                      │      MySQL 8         │
                                                      └──────────────────────┘
```

The frontend and backend are separate applications. The browser sends API requests to the backend; authenticated requests include a JWT bearer token. Order totals and stock availability are checked by the backend when an order is placed.

## Get started

### Prerequisites

- Java 17 or newer
- Maven 3.8 or newer
- Node.js 18 or newer
- MySQL 8 or newer

### 1. Start the API

Start MySQL. The default JDBC URL creates the `ecommerce` database if it does not exist, provided the configured MySQL account has permission.

From the `ecommerce/` directory, set database credentials if they differ from the defaults, then start Spring Boot:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-mysql-password"
mvn spring-boot:run
```

The API starts at `http://localhost:8080/api`.

### 2. Start the storefront

In another terminal, from the `ecommerce-ui/` directory:

```powershell
npm install
npm run dev
```

Open the Vite URL printed in the terminal (typically `http://localhost:5173`).

### Frontend API URL

The storefront defaults to `http://localhost:8080/api`. To point it elsewhere, set `VITE_API_URL` before starting or building the frontend:

```powershell
$env:VITE_API_URL = "http://localhost:8080/api"
npm run dev
```

For production builds, configure `VITE_API_URL` to the deployed API base URL before running `npm run build`.

## End-to-end execution flow

Follow these steps to run and try the application locally:

1. **Start MySQL.** Ensure the server is running and that the configured database user can connect (and create the `ecommerce` database if it does not already exist).
2. **Launch the backend.** In a terminal, open the `ecommerce/` directory, set `DB_USERNAME` and `DB_PASSWORD` if needed, and run `mvn spring-boot:run`. Keep this terminal open. Spring Boot connects to MySQL and starts the API on port `8080`.
3. **Launch the frontend.** In a second terminal, open `ecommerce-ui/`, run `npm install` once, then `npm run dev`. Keep this terminal open and open the Vite URL shown in its output.
4. **Browse the storefront.** The frontend requests categories and products from the API. Search by product name or select a category to filter the catalog.
5. **Create an account or sign in.** Registration and login call the `/api/auth` endpoints. The frontend stores the returned token and attaches it to authenticated API requests.
6. **Build your bag.** Add available products to the cart and adjust quantities. The cart is saved in browser storage, so it remains after a page refresh in that browser.
7. **Place an order.** Sign in, proceed to checkout, and confirm the order. The API validates the requested products and available stock, calculates prices from current product data, and records the order as `PLACED`.
8. **Review your orders.** Open the Orders page to retrieve the signed-in user's order history from the API.

Checkout in this demo does not collect payment or arrange delivery.

## Configuration

The backend reads these environment variables. Defaults are intended for local development.

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:mysql://localhost:3306/ecommerce?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | MySQL JDBC URL |
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | empty | MySQL password |
| `JWT_SECRET` | Development-only secret | HMAC signing secret; provide a unique secret of at least 32 bytes outside local development |
| `JWT_EXPIRATION_MS` | `86400000` | JWT lifetime in milliseconds (24 hours) |
| `CORS_ALLOWED_ORIGIN` | `http://localhost:5173` | Allowed storefront origin |

The application currently uses `spring.jpa.hibernate.ddl-auto=update`, which is convenient for development. For production, configure reviewed schema migrations and deployment-appropriate database, CORS, and secret settings.

## API at a glance

All routes are prefixed with `/api`.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/hello` | Public | Health/greeting endpoint |
| `GET` | `/categories` | Public | List categories |
| `GET` | `/categories/{id}` | Public | Get a category |
| `POST` | `/categories` | Admin | Create a category |
| `PUT`, `DELETE` | `/categories/{id}` | Admin | Update or delete a category |
| `GET` | `/products` | Public | List products; optional `categoryId` and `q` filters |
| `GET` | `/products/{id}` | Public | Get product details |
| `POST` | `/products` | Admin | Create a product |
| `PUT`, `DELETE` | `/products/{id}` | Admin | Update or delete a product |
| `POST` | `/auth/register` | Public | Register and receive an auth response |
| `POST` | `/auth/login` | Public | Sign in and receive an auth response |
| `GET` | `/auth/me` | Authenticated | Get the current user |
| `POST` | `/orders` | Authenticated | Place an order |
| `GET` | `/orders` | Authenticated | List the current user's orders |
| `GET` | `/orders/{id}` | Authenticated | Get one of the current user's orders |

Example catalog requests:

```text
GET http://localhost:8080/api/products
GET http://localhost:8080/api/products?categoryId=1
GET http://localhost:8080/api/products?q=linen
```

New users do not receive catalog-management permissions by self-registering. To grant admin access in a local development database, promote a trusted account out of band. Never expose an unauthenticated admin registration flow.

## Project layout

```text
.
├── ecommerce/                 # Spring Boot API
│   ├── src/main/java/         # Controllers, services, repositories, models
│   ├── src/main/resources/    # Application configuration
│   └── src/test/java/         # Backend tests
├── ecommerce-ui/              # React + Vite storefront
│   └── src/
│       ├── components/        # Storefront views and UI components
│       └── services/          # API client and helpers
└── README.md
```

## Run tests and build

Run backend tests from `ecommerce/`:

```powershell
mvn test
```

Create a production frontend bundle from `ecommerce-ui/`:

```powershell
npm run build
```

To preview the generated frontend bundle locally:

```powershell
npm run preview
```

## Security and production notes

- Set a unique `JWT_SECRET` before deployment; never use the development default in a deployed environment.
- Use HTTPS and protect database credentials through your deployment's secret-management system.
- Keep catalog administration restricted to trusted administrator accounts.
- Review CORS origins, database access, logging, and schema-migration strategy before exposing the service publicly.
- This starter is not a payment system and should not be treated as one.
