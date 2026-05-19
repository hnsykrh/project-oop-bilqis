# Test report — Blood Donation Management System

Aligned with **BAXU 3113 OOP Group Project** rubric (PDF).

## How to run all tests

```bash
mvn test
```

Expected: **25 test methods**, including **1001 scenario assertions** in `ScenarioMatrixTest`.

## Results summary (automated)

| Suite | What it verifies |
|-------|------------------|
| `ScenarioMatrixTest` | **1001** fulfillment + eligibility combinations |
| `EligibilityCalculatorTest` | Hb threshold, 90-day rule, age, inactive donor |
| `FulfillmentCalculatorTest` | Stock vs clinical need cap |
| `IntegrationWorkflowTest` | Login, CRUD, soft delete, search, donation, void safety, fulfill, PDF, analytics |
| `RubricComplianceTest` | 5 DB tables, MVC class layers |

## Bugs found and fixed during testing

1. **Donor search crash** — `DonorDao.search` bound 8 parameters for 7 SQL placeholders (`ArrayIndexOutOfBounds`). Fixed.
2. **Void donation negative stock** — voiding after blood was issued to a hospital could drive inventory below zero. Void now checks stock and blocks with a clear message.
3. **`DatabaseManager` test isolation** — added constructor `DatabaseManager(Path)` and `initialize(boolean seedDemo)` for automated tests.

## Rubric self-check (40 marks)

| Criterion | Status |
|-----------|--------|
| MVC separation | View → Controller → DAO (no SQL in view/controller) |
| 5+ tables + indexes | staff, donors, donations, blood_inventory, recipient_requests |
| GUI | Swing tabs (not CLI) |
| Search / Soft delete / Update / View | Donors, Donations, Requests |
| Calculations | Eligibility + fulfillment |
| Third-party | JFreeChart + OpenPDF |
| Video | **You must record** (not automated) |

## Manual GUI checklist (for your video)

1. Login `admin` / `admin123`
2. Donors: Search → View eligibility → Add → Update → Deactivate
3. Donations: Record (eligibility check) → Void (with/without issued blood)
4. Inventory: View → Export PDF
5. Requests: Add → Fulfill → Cancel
6. Analytics: Refresh chart
