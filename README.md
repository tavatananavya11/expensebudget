# Paise Track — Expense & Budget Tracker

A responsive personal finance app built with Java 21, Spring Boot, Maven, SQLite, HTML, CSS, JavaScript, and Chart.js. Amounts are shown in Indian Rupees (₹ INR).

## 1. Requirements

- Java 21
- Maven 3.9 or newer
- A modern browser
- Internet access the first time Maven downloads dependencies and when loading Chart.js / web fonts from their CDNs

Node.js is not required. Spring Boot serves the frontend and REST APIs from the same application.

Check Java and Maven from the VS Code terminal:

```powershell
java -version
mvn -version
```

Both commands should report Java 21 and Maven 3.9 or later.

## 2. Open and run the app

1. Open the `expensebudget` folder in VS Code.
2. Open **Terminal → New Terminal**.
3. From the project root, run:

   ```powershell
   mvn spring-boot:run
   ```

4. Wait until the terminal reports that Spring Boot has started.
5. Open <http://localhost:8080>.
6. Choose **Create an account**, register with your name, email, and a password of at least 8 characters, then sign in.
7. Add income or expenses, set the monthly budget, and use the month picker to review another month.

Stop the local server with **Ctrl+C** in its terminal.

## 3. User accounts and privacy

Every account has its own transactions and monthly budgets. Passwords are stored as BCrypt hashes, not as plain text. The backend takes the account ID from the authenticated server session and includes that account ID in each transaction and budget lookup; clients cannot select another account ID.

The default database is the local file `data/expensebudget.db`. It is created automatically on first start and is excluded from source control. A local SQLite file does **not** automatically synchronize between phones or computers. Separate accounts remain isolated in this database. To see the same account's data from multiple devices, deploy the backend to an accessible server and configure that deployment to use one shared, persistent database and HTTPS. Do not expose a developer laptop or a local SQLite file as a public database.

This is a single-server starter deployment. For production use, also configure HTTPS, database backups, account recovery, rate limiting, and a managed shared database appropriate to the hosting platform.

## 4. Features

- Sign up, sign in, and sign out
- BCrypt password hashing, server-side sessions, CSRF protection, and account-scoped authorization
- Monthly dashboard: income, expenses, budget, budget remaining, and net savings
- Add, list, edit, and delete income and expense transactions
- Categories: Food, Rent, Travel, Shopping, Education, Healthcare, Bills, Salary, and Other
- Monthly budget per account
- Monthly report and category spending chart
- Server-side input validation and user-facing success/error messages
- Responsive layout for desktop and mobile

Savings are calculated as income minus expenses. Remaining budget is budget minus expenses and can be negative when spending exceeds the limit.

## 5. Run tests

In the project root:

```powershell
mvn test
```

The tests use a separate SQLite test database at `data/expensebudget-test.db`; they do not use the regular application database.

## 6. Configuration

Defaults are in `src/main/resources/application.properties`:

- Server port: `8080`
- SQLite file: `data/expensebudget.db` in the project root. The app resolves this path from the project location, even if the working directory differs.
- Session timeout: 30 minutes

Override the port or SQLite JDBC URL with environment variables before starting:

```powershell
$env:PORT = "8081"
$env:DATABASE_URL = "jdbc:sqlite:./data/another-expensebudget.db"
mvn spring-boot:run
```

The app creates the default `data` directory when it starts. The directory for a custom database path must already exist. Keep a regular backup of the SQLite database while the app is stopped.

## 7. REST API overview

All endpoints are same-origin. Write requests require the CSRF token returned by `GET /api/csrf` in the `X-XSRF-TOKEN` header. The included frontend does this automatically.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/csrf` | Start the CSRF-protected session |
| POST | `/api/auth/register` | Register an account |
| POST | `/api/auth/login` | Sign in |
| POST | `/api/auth/logout` | Sign out |
| GET | `/api/auth/me` | Current signed-in account |
| GET | `/api/summary?month=YYYY-MM` | Monthly dashboard totals |
| GET | `/api/report?month=YYYY-MM` | Totals plus category expense sums |
| GET | `/api/transactions?month=YYYY-MM` | This account's transactions for the month |
| POST | `/api/transactions` | Add a transaction |
| PUT | `/api/transactions/{id}` | Edit an owned transaction |
| DELETE | `/api/transactions/{id}` | Delete an owned transaction |
| POST | `/api/budgets?month=YYYY-MM` | Create or update this account's monthly budget |

Transaction JSON example:

```json
{
  "amount": 450.00,
  "type": "EXPENSE",
  "category": "Food",
  "note": "Groceries",
  "transactionDate": "2026-10-25"
}
```

## 8. Troubleshooting

- **`java` or `mvn` is not recognized:** Install Java 21 and Maven, add their `bin` folders to `PATH`, then restart VS Code and verify with `java -version` and `mvn -version`.
- **Port 8080 is already in use:** In PowerShell set `$env:PORT = "8081"` and restart the app; then open `http://localhost:8081`.
- **Maven cannot download dependencies:** Check your internet connection and proxy settings, then retry `mvn spring-boot:run`.
- **The app returns an error after login:** Refresh the page to create a fresh CSRF token and sign in again. The session expires after 30 minutes of inactivity.
- **Chart is missing:** The chart library is loaded from jsDelivr. Check internet access, allow `cdn.jsdelivr.net`, then refresh. Transaction management still works if the chart CDN is unavailable.
- **Cannot find a transaction from a different account:** This is expected. Records and budgets are intentionally isolated by the signed-in account.
- **Data is missing on another phone:** A local SQLite file is only on the machine running the backend. Deploy the backend with a persistent shared database for cross-device access; do not expect local databases to sync automatically.
- **SQLite database error:** Stop the application before moving or backing up its database. Ensure the `data` folder is writable and that any custom database directory exists.

## 9. Project layout

```text
src/main/java/in/expensebudget/
  config/       Security configuration
  controller/   Authentication and finance REST APIs
  dto/          Validated API request/response records
  model/        Account, transaction, and budget entities
  repository/   Account-scoped database queries
  security/     Authenticated account principal
  service/      Account registration and finance calculations
src/main/resources/
  static/       Responsive HTML, CSS, and JavaScript
  application.properties
src/test/       Account isolation and calculation tests
```
