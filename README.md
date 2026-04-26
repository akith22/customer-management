<div align="center">

# 🗂️ Customer Management System

**A full-stack platform for creating, managing, and bulk-importing customers**

![Java](https://img.shields.io/badge/Java-8-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7.18-brightgreen?style=flat-square)
![React](https://img.shields.io/badge/React-19-blue?style=flat-square)
![MariaDB](https://img.shields.io/badge/MariaDB-10.6+-teal?style=flat-square)
![Maven](https://img.shields.io/badge/Maven-3.6+-red?style=flat-square)
![License](https://img.shields.io/badge/Tests-JUnit_5-purple?style=flat-square)

</div>

---

## 🚀 Getting Started

### Step 1 — Run DDL *(creates database & tables)*

```bash
mariadb -u root -p < backend/sql/ddl.sql
```

### Step 2 — Run DML *(loads seed data)*

```bash
mariadb -u root -p < backend/sql/dml.sql
```

> Inserts 5 countries, 79 cities, 15 sample customers with mobiles, addresses, and family links.

### Step 3 — Set Application Properties

Open `backend/src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/customer_management
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

### Step 4 — Run the Backend

```bash
cd backend
mvn spring-boot:run
```

> ✅ API running at **http://localhost:8080**

### Step 5 — Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

> ✅ App running at **http://localhost:5173**

> ⚠️ Both services must be running at the same time.

### Step 6 — Run Tests

```bash
cd backend
mvn test
```

> Uses H2 in-memory database — no MariaDB connection required.

---

## ✅ What's Implemented

### Customer CRUD

| Feature | Description |
|---|---|
| **Create** | Add a customer with name, date of birth (date picker), and NIC |
| **Edit** | Update any field — contacts, addresses, family members |
| **View** | Full detail page — all info in one place |
| **List** | Paginated table with search by name or NIC |

---

### Customer Attributes

- ✅ **Name** — mandatory
- ✅ **Date of Birth** — mandatory, date selector in UI
- ✅ **NIC Number** — mandatory, unique across all customers
- ✅ **Mobile Numbers** — optional, multiple per customer
- ✅ **Family Members** — link customers to other customers as family
- ✅ **Addresses** — optional, multiple per customer *(line 1, line 2, city, country)*
- ✅ **Cities & Countries** — master data tables, loaded in background, not visible on frontend
- ✅ **Minimal DB Calls** — full record in a single fetch; city list cached; bulk upload uses batch inserts

---

### 📦 Bulk Upload

Upload an `.xlsx` or `.xls` file to create up to **1,000,000 customers** at once.

| Stat | Value |
|---|---|
| Max rows | 1,000,000 |
| Rows per transaction | 500 (chunked) |
| NIC checks | 1 query per chunk, not per row |

**How it works:**
- Rows are processed in chunks of 500 — each chunk is its own DB transaction
- One NIC-existence query per chunk drastically reduces DB load
- A failed row does not stop the rest — errors are reported individually
- Returns a full summary: total rows, succeeded, failed, and reason per failure

**Excel column format:**

| Column A | Column B | Column C |
|---|---|---|
| Name | DOB `(yyyy-MM-dd)` | NIC |

> 📎 Sample file: `bulk upload excel - for testing/customer_bulk_upload_test.xlsx`

---

## 🔌 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/customers` | Create a customer |
| `PUT` | `/api/customers/{id}` | Update a customer |
| `GET` | `/api/customers/{id}` | Get full customer detail |
| `GET` | `/api/customers` | Paginated customer list |
| `POST` | `/api/customers/bulk/upload` | Bulk Excel import |
| `GET` | `/api/cities` | List all cities |

---

## 🧪 Tests

| Test | What it covers |
|---|---|
| `CustomerControllerTest` | Create, update, get, list endpoints |
| `BulkUploadControllerTest` | File upload — valid, empty, wrong type |
| `CityControllerTest` | City listing endpoint |
| `CustomerServiceTest` | Business logic, duplicate NIC handling |
| `BulkUploadServiceTest` | Chunk processing, row-level errors |
| `CustomerRepositoryTest` | JPA queries against H2 |

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 8 |
| Backend | Spring Boot | 2.7.18 |
| Database | MariaDB | 10.6+ |
| Build | Maven | 3.6+ |
| Frontend | React + Vite | 19 |
| HTTP Client | Axios | 1.15.x |
| Excel | Apache POI | 5.2.5 |
| Testing | JUnit 5 + Spring Boot Test | — |
| Test DB | H2 (in-memory) | — |

> All libraries are **free, open-source, and stable** release versions.
