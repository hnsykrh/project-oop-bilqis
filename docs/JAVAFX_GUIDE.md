# JavaFX guide — Blood Donation Management System

This project now uses **JavaFX** as the main GUI (modern desktop UI). Your **MVC**, **SQLite database**, **controllers**, and **business rules** are unchanged — only the **View** layer was rebuilt.

---

## 1. Does JavaFX still match the rubric?

| BAXU 3113 requirement | JavaFX answer |
|----------------------|---------------|
| GUI (not CLI) | Yes — windows, tables, dialogs |
| MVC | Yes — see folder structure below |
| 4+ database tables | Yes — same `blooddonation.db` |
| Search / Soft delete / Update / View | Yes — all tabs |
| Calculations | Yes — `service` package |
| Third-party library | **OpenPDF** (Inventory → Export PDF) + **JavaFX Charts** (Analytics tab) |
| Video demo | You record the JavaFX app |

---

## 2. How to run (easiest)

1. Open folder: `C:\Users\bilqi\project-oop-bilqis`
2. Double-click **`run-app.bat`**
3. First run may download Maven into `.tools` (wait until login appears)
4. Login: **`admin`** / **`admin123`**

### Old Swing UI (backup)

Double-click **`run-app-swing.bat`** if you need the previous interface.

---

## 3. MVC structure with JavaFX

```
src/main/java/com/hnsykrh/blooddonation/
├── model/              ← MODEL (data classes)
├── service/            ← MODEL (calculations)
├── db/                 ← MODEL (database setup)
├── dao/                ← DATA ACCESS (SQL only)
├── controller/         ← CONTROLLER (business logic, no SQL)
├── view/               ← Swing views (legacy, optional)
└── fx/                 ← JavaFX VIEW
    ├── BloodDonationFxApplication.java   ← starts the app
    ├── LoginController.java              ← FXML login controller
    ├── MainController.java               ← FXML main window
    ├── FxAppContext.java                 ← wires controllers for tabs
    └── tabs/                             ← Donors, Donations, etc.

src/main/resources/fxml/
├── login.fxml          ← login screen layout
└── main.fxml           ← main window layout
```

**Rule for lecturers:**  
- **View** = `fx` + `resources/fxml` (buttons, tables — no SQL)  
- **Controller** = `controller` package  
- **Model** = `model`, `service`, `db` + `dao` for persistence  

---

## 4. Database (same as before)

| Item | Location |
|------|----------|
| File | `blooddonation.db` in project folder |
| Created | Automatically on first run |
| Reset | Close app → delete `blooddonation.db` → run again |

You do **not** install SQLite separately for normal use.

---

## 5. What each tab does

| Tab | Features |
|-----|----------|
| **Donors** | Search, View, Add, Update, Deactivate (soft delete) |
| **Donations** | Search, View, Record, Void |
| **Inventory** | View stock, Export PDF |
| **Recipient Requests** | Search, View, Add, Update, Fulfill, Cancel |
| **Analytics** | Bar chart by blood type |

---

## 6. Run from terminal (optional)

```powershell
cd C:\Users\bilqi\project-oop-bilqis
.\.tools\apache-maven-3.9.6\bin\mvn.cmd javafx:run
```

Or after `setup-maven.ps1`:

```powershell
mvn javafx:run
```

### Tests

```powershell
mvn test
```

---

## 7. Video demo script (2–3 minutes)

1. Run `run-app.bat` → login  
2. **Donors** — Search, View eligibility message, show Add  
3. **Donations** — Record donation (Donor ID from table)  
4. **Inventory** — Show stock → Export PDF  
5. **Requests** — Fulfill a request  
6. **Analytics** — Refresh chart  
7. Say aloud: “We use MVC — model, controller, and JavaFX view with FXML; five SQLite tables; soft delete on donors.”

---

## 8. Troubleshooting

| Problem | Fix |
|---------|-----|
| `run-app.bat` closes instantly | Open CMD in project folder, run `run-app.bat`, read error text |
| Login works but blank main window | Pull latest code; report error from terminal |
| Table columns empty | Click **Refresh** or **Search** |
| Cannot void donation | Blood may have been issued — message explains this |

---

## 9. For your report / presentation

- **Title:** Blood Donation Management System  
- **Tech:** Java 17+, JavaFX, SQLite, Maven, OpenPDF  
- **Architecture:** Model–View–Controller  
- **GUI:** JavaFX FXML + controllers (not web, not mobile)
