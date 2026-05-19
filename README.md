# BAXU 3113 — Blood Donation Management System

Desktop Java application (MVC) for managing donors, donations, blood inventory, and hospital recipient requests. Uses **SQLite**, **JavaFX** GUI (FXML), **JavaFX Charts**, and **OpenPDF** reporting.

## Requirements

- Java 17+
- Apache Maven 3.8+

## How to run

**Easiest (Windows):** double-click **`run-app.bat`** in the project folder.

Or from terminal (Java 17+):

```bash
mvn javafx:run
```

Legacy Swing UI: **`run-app-swing.bat`** or `mvn -Dexec.mainClass=com.hnsykrh.blooddonation.BloodDonationApplication compile exec:java`

Full JavaFX guide: [docs/JAVAFX_GUIDE.md](docs/JAVAFX_GUIDE.md)

The SQLite database file `blooddonation.db` is created in the project directory on first launch. Demo data is seeded automatically.

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
| **View**     | `fx` + `resources/fxml` (JavaFX); `view` = legacy Swing | UI — calls controllers only |

## Features

- **Donors** — search, view, add, update, soft-delete (`is_active`)
- **Donations** — record (updates donor `last_donation_date` + inventory), void (reverses stock, refreshes last donation from max non-voided row)
- **Inventory** — view stock by blood type; export PDF snapshot
- **Recipient requests** — CRUD, fulfill (caps by stock and remaining need), cancel (`is_cancelled`)
- **Analytics** — bar chart of donation volume by blood type (JavaFX Charts)
- **Calculations** — 90-day donation interval, minimum hemoglobin 12.5 g/dL, age 18–65 (`EligibilityCalculator`); fulfillment cap (`FulfillmentCalculator`)

## Rubric mapping (BAXU 3113)

| Criterion | Implementation |
|-----------|----------------|
| MVC architecture | Strict packages: `view` → `controller` → `dao` → SQLite |
| 5+ related tables | `staff`, `donors`, `donations`, `blood_inventory`, `recipient_requests` + FKs & indexes |
| GUI | JavaFX `MainController` + FXML, tabbed modules |
| Search | Text search on donors, donations, requests |
| Soft delete | Donors `is_active`, donations `is_voided`, requests `is_cancelled` |
| Update / View | Dialogs and detail views per tab |
| Calculations | `EligibilityCalculator`, `FulfillmentCalculator` |
| Third-party libraries | sqlite-jdbc, OpenPDF; charts via JavaFX Charts |
| Video demo | Record a short walkthrough for submission (login → donate → inventory → fulfill request → chart/PDF) |

See also [docs/RUBRIC.md](docs/RUBRIC.md) and [docs/TEST_REPORT.md](docs/TEST_REPORT.md).

## Automated tests (1001+ scenarios)

```bash
mvn test
```

Runs eligibility/fulfillment matrix tests, integration workflows, and rubric compliance checks.

## Build JAR (optional)

```bash
mvn package
```

## Author

Course project — `project-oop-bilqis` (GitHub: [hnsykrh/project-oop-bilqis](https://github.com/hnsykrh/project-oop-bilqis)).
