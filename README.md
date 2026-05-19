# BAXU 3113 — Blood Donation Management System

Desktop Java application (MVC) for managing donors, donations, blood inventory, and hospital recipient requests. Uses **SQLite**, **Swing** GUI, **JFreeChart** analytics, and **OpenPDF** reporting.

## Requirements

- Java 17+
- Apache Maven 3.8+

## How to run

From the project root (requires Java 17+ and Maven):

```bash
mvn compile exec:java
```

Quiet output:

```bash
mvn -q compile exec:java
```

On Windows PowerShell, if Maven is not on PATH, use the full path or install Maven from [maven.apache.org](https://maven.apache.org/download.cgi).

The SQLite database file `blooddonation.db` is created in the project directory on first launch. Demo data (2 donors, 1 donation, 1 hospital request) is seeded automatically.

## Default login (demo only)

| Field    | Value     |
|----------|-----------|
| Username | `admin`   |
| Password | `admin123` |

Do not use plain-text passwords in production; this is for coursework demonstration only.

## Project layout (MVC)

| Layer        | Package / path | Responsibility |
|--------------|----------------|----------------|
| **Model**    | `model`, `service`, `db` | Entities, eligibility & fulfillment calculations, schema bootstrap |
| **DAO**      | `dao` | JDBC access only (prepared statements, search, soft delete) |
| **Controller** | `controller` | Business transactions (no SQL) |
| **View**     | `view` | Swing UI — calls controllers only |

## Features

- **Donors** — search, view, add, update, soft-delete (`is_active`)
- **Donations** — record (updates donor `last_donation_date` + inventory), void (reverses stock, refreshes last donation from max non-voided row)
- **Inventory** — view stock by blood type; export PDF snapshot
- **Recipient requests** — CRUD, fulfill (caps by stock and remaining need), cancel (`is_cancelled`)
- **Analytics** — bar chart of donation volume by blood type (JFreeChart)
- **Calculations** — 90-day donation interval, minimum hemoglobin 12.5 g/dL, age 18–65 (`EligibilityCalculator`); fulfillment cap (`FulfillmentCalculator`)

## Rubric mapping (BAXU 3113)

| Criterion | Implementation |
|-----------|----------------|
| MVC architecture | Strict packages: `view` → `controller` → `dao` → SQLite |
| 5+ related tables | `staff`, `donors`, `donations`, `blood_inventory`, `recipient_requests` + FKs & indexes |
| GUI | Swing `MainFrame` with tabbed modules |
| Search | Text search on donors, donations, requests |
| Soft delete | Donors `is_active`, donations `is_voided`, requests `is_cancelled` |
| Update / View | Dialogs and detail views per tab |
| Calculations | `EligibilityCalculator`, `FulfillmentCalculator` |
| Third-party libraries | sqlite-jdbc, JFreeChart, OpenPDF |
| Video demo | Record a short walkthrough for submission (login → donate → inventory → fulfill request → chart/PDF) |

See also [docs/RUBRIC.md](docs/RUBRIC.md).

## Build JAR (optional)

```bash
mvn package
```

## Author

Course project — `project-oop-bilqis` (GitHub: [hnsykrh/project-oop-bilqis](https://github.com/hnsykrh/project-oop-bilqis)).
