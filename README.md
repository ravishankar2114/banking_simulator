# 🏦 Java Banking Simulator

## **Milestone 1: Core Banking & Security**

A comprehensive, secure, console-based banking application built in Java. This project simulates a real-world banking system, complete with a **3-Tier architecture**, persistent **MySQL database**, and **real-time SMS verification** for advanced security.

---

## ✨ Key Features Implemented

### **1. 🛡️ Multi-Level Security**

| Feature | Implementation | Notes |
| :--- | :--- | :--- |
| **Password Hashing** | Uses the **jBCrypt** library. Passwords are **never** stored in plain-text. |
| **Real-Time 2FA (OTP)** | Integrates the **Twilio API** to send a real SMS code for user/admin login. |
| **Security Levels** | Users can **Upgrade** or **Downgrade** their account security between `STANDARD` and `SECURE_OTP`. |
| **Atomic Transactions** | Fund transfers use **JDBC Transaction Management** (`commit()/rollback()`) to prevent data loss. |
| **Normalization** | Phone numbers are automatically converted to the **E.164 format** (`+91...`) for API compatibility. |

### **2. 👤 User Dashboard Features**

* **Secure Account Creation:** In-depth validation for all identity details (PAN, Aadhar, IFSC).
* **Core Operations:** Deposit, Withdraw, and secure Fund Transfers.
* **Profile Management:** Ability to update contact information and change security status.
* **History:** View both a "Mini Statement" (last 5 transactions) and a full transaction history.

### **3. 👑 Admin Dashboard Features**

* **Mandatory OTP:** Admin login is always protected by 2FA.
* **Account Control:** Ability to **Freeze** or **Unfreeze** any customer account (blocking login and transactions).
* **Auditing:** View a global log of *all* transactions from *all* users across the system.
* **Admin Management:** Securely create new admin accounts.

---

## 🏗️ Technical Architecture

This project strictly adheres to the professional **3-Tier Architecture** model.

| Layer | Key Files | Purpose |
| :--- | :--- | :--- |
| **1. Presentation (App)** | `BankingConsoleApp.java` | Prints menus and takes `Scanner` input. Contains **NO** business logic. |
| **2. Service (Logic)** | `AccountManager.java` | The **Brains**—coordinates all validation, security checks, and transaction logic. |
| **3. Data Access (DAO)** | `AccountDao.java`, `AdminDao.java`, etc. | Translates Java objects into SQL and executes database commands. |
| **Models** | `Account.java`, `Admin.java`, etc. | **Lombok-Enhanced** data blueprints (no manual getters/setters). |

---

## 🛠️ Setup and Installation Guide

### **1. Prerequisites**
* Java JDK 17+
* Apache Maven
* MySQL Server
* A **Twilio** account (with your personal mobile number verified).

### **2. Setup Instructions**

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/your-username/banking-simulator.git](https://github.com/your-username/banking-simulator.git)
    cd banking-simulator
    ```

2.  **Set up the Database:**
    * Open your MySQL client and run the full SQL script to create the database and all four tables (`users`, `admins`, `transactions`, `payees`).

3.  **Configure `db.properties`:**
    * Go to `src/main/resources/` and **create a file** named `db.properties`.
    * **CRITICAL:** Paste your MySQL and Twilio API keys into this file. (Remember: This file **MUST NOT** be committed to GitHub.)

4.  **Insert the Initial Admin:**
    * Run the `HashGenerator.java` utility to get a fresh hash for the password `admin123`.
    * Run the `INSERT INTO admins` SQL query in your MySQL client, using that new hash and your **verified Twilio phone number**.

### **3. Running the Application**

1.  Open the project in your IDE (IntelliJ/Eclipse).
2.  Run the `main()` method located in: `src/main/java/com/yourbank/app/BankingConsoleApp.java`.
3.  Log in as `admin` / `admin123` to access the Admin Dashboard.

---

## 🔜 Milestone 2: Future Development

* **Implement Payee Management:** Complete the logic for adding, viewing, and deleting saved beneficiaries.
