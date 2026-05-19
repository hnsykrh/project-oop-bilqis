# BAXU 3113 Rubric checklist

Use this list when preparing your submission video and report.

## Architecture

- [x] **Model** — `com.hnsykrh.blooddonation.model` (+ domain logic in `service`, persistence bootstrap in `db`)
- [x] **View** — `com.hnsykrh.blooddonation.view` (Swing only; no SQL)
- [x] **Controller** — `com.hnsykrh.blooddonation.controller` (orchestration; no SQL)
- [x] **DAO** — `com.hnsykrh.blooddonation.dao` (JDBC only)

## Database (5 tables)

1. `staff` — authentication, soft delete via `is_active`
2. `donors` — donor registry, `last_donation_date`, `is_active`
3. `donations` — events linked to donor/staff, `is_voided`
4. `blood_inventory` — stock per blood type
5. `recipient_requests` — hospital demand, `fulfilled_ml`, `is_cancelled`

Foreign keys: donations → donors, donations → staff.

## GUI operations (demo in video)

| Tab | Search | View | Add | Update | Soft delete / void |
|-----|--------|------|-----|--------|-------------------|
| Donors | Yes | Yes | Yes | Yes | Deactivate |
| Donations | Yes | Yes | Record | — | Void |
| Inventory | — | Yes | — | — | — |
| Recipient Requests | Yes | Yes | Yes | Yes | Cancel |
| Analytics | — | Chart | — | — | — |

## Calculations

- Minimum **90 days** between whole-blood donations
- Minimum **12.5 g/dL** hemoglobin at donation
- **Days until next eligible** shown on donor view
- Fulfillment: `min(stock, remaining_need, requested_amount)`

## Third-party integrations

- **JFreeChart** — donation volume bar chart by blood type
- **OpenPDF** — inventory PDF export from Inventory tab

## Run command (repeat in video)

```bash
mvn -q compile exec:java
```

Login: `admin` / `admin123`
