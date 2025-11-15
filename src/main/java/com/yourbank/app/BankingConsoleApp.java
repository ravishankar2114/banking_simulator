package com.yourbank.app;

import com.yourbank.exception.AccountNotFoundException;
import com.yourbank.exception.InsufficientFundsException;
import com.yourbank.exception.ValidationException;
import com.yourbank.model.Account;
import com.yourbank.model.Admin;
import com.yourbank.model.Payee;
import com.yourbank.model.TransactionRecord;
import com.yourbank.service.AccountManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
public class BankingConsoleApp {


    private static AccountManager manager = new AccountManager();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to the Banking Simulator!");
        mainMenuLoop();
        System.out.println("Thank you for banking with us. Goodbye!");
    }

    private static void mainMenuLoop() {
        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Create New Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Please choose an option (1-3): ");

            int choice = readIntInput();

            switch (choice) {
                case 1:
                    handleCreateAccount();
                    break;
                case 2:
                    handleLogin();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }


    private static void handleCreateAccount() {
        System.out.println("\n--- Create Account ---");
        System.out.println("1. Create Normal User Account");
        System.out.println("2. Back to Main Menu");
        System.out.print("Please choose an option: ");

        int choice = readIntInput();

        switch (choice) {
            case 1:
                handleCreateNormalAccount();
                break;
            case 2:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }


    private static void handleCreateNormalAccount() {
        try {
            System.out.println("\n--- New User Registration ---");

            String name = readString("Enter full name: ");
            String email = readString("Enter email: ");
            String phone = readString("Enter 10-digit phone number (must be verified in Twilio for OTP to work): ");
            String password = readString("Create a password (min 8 chars, 1 number, 1 special char): ");
            String address = readString("Enter full address: ");
            String pan = readString("Enter PAN card number (e.g., ABCDE1234F): ");
            String aadhar = readString("Enter 12-digit Aadhar number: ");
            String ifsc = readString("Enter home branch IFSC code (e.g., SBIN0123456): ");
            
            Account.AccountType type;
            if (readString("Enter account type (S for Savings, C for Checking): ").equalsIgnoreCase("C")) {
                type = Account.AccountType.CHECKING;
            } else {
                type = Account.AccountType.SAVINGS;
            }

            Account.SecurityLevel securityLevel;
            if (readString("Enable 'Morely Secured' (OTP) login? (Y/N): ").equalsIgnoreCase("Y")) {
                securityLevel = Account.SecurityLevel.SECURE_OTP;
            } else {
                securityLevel = Account.SecurityLevel.STANDARD;
            }

            Account newAccount = manager.createAccount(name, email, password, phone, address, pan, aadhar, ifsc, type, securityLevel);
            
            System.out.println("\n--- ACCOUNT CREATED SUCCESSFULLY! ---");
            System.out.println("Your new Account Number is: " + newAccount.getAccountNumber());
            System.out.println("Please log in to use your account.");

        } catch (ValidationException | SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }


    private static void handleLogin() {
        System.out.println("\n--- Login ---");
        System.out.println("1. Normal User Login");
        System.out.println("2. Admin Login");
        System.out.println("3. Back to Main Menu");
        System.out.print("Please choose an option: ");

        int choice = readIntInput();

        switch (choice) {
            case 1:
                handleNormalLogin();
                break;
            case 2:
                handleAdminLogin();
                break;
            case 3:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void handleNormalLogin() {
        try {
            String accNum = readString("Enter your Account Number: ");
            String pass = readString("Enter your Password: ");


            Account account = manager.login(accNum, pass);

            if (account.getSecurityLevel() == Account.SecurityLevel.SECURE_OTP) {
                System.out.println("--- Secure Login Detected ---");
                if (!handleOtpVerification(account.getPhoneNumber())) {
                    System.err.println("OTP verification failed. Logging out.");
                    return;
                }
                System.out.println("OTP Verified!");
            }

            System.out.println("\nWelcome, " + account.getHolderName() + "!");

            userDashboardLoop(account);

        } catch (AccountNotFoundException | ValidationException e) {
            System.err.println("Login Failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected login error occurred: " + e.getMessage());
        }
    }


    private static void handleAdminLogin() {
        try {
            String user = readString("Enter admin username: ");
            String pass = readString("Enter admin password: ");


            Admin admin = manager.adminLogin(user, pass);

            System.out.println("--- Admin Login Detected ---");

            String phone = admin.getPhoneNumber();


            if (phone == null || phone.isEmpty()) {
                System.err.println("ERROR: This admin account does not have a phone number set in the database.");
                System.err.println("Please contact your database administrator.");
                return;
            }

            if (!handleOtpVerification(phone)) {
                System.err.println("OTP verification failed. Logging out.");
                return;
            }

            System.out.println("OTP Verified!");
            System.out.println("\nWelcome, Admin " + admin.getUsername() + "!");


            adminDashboardLoop(admin);

        } catch (AccountNotFoundException | ValidationException e) {
            System.err.println("Login Failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected login error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean handleOtpVerification(String phoneNumber) {
        try {
            System.out.println("Sending OTP to ......" + phoneNumber.substring(phoneNumber.length() - 4));

            String sentOtp = manager.sendOtp(phoneNumber);

            String userOtp = readString("Please enter the 6-digit OTP you received: ");
            return userOtp.equals(sentOtp);

        } catch (Exception e) {
            System.err.println("Could not send or verify OTP: " + e.getMessage());
            return false;
        }
    }

    private static void userDashboardLoop(Account currentUser) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- User Dashboard (Account: " + currentUser.getAccountNumber() + ") ---");
            System.out.println("== Core Banking ==");
            System.out.println(" 1. Check Balance");
            System.out.println(" 2. Deposit Funds");
            System.out.println(" 3. Withdraw Cash");
            System.out.println(" 4. Transfer to New Account");
            System.out.println(" 5. Manage Payees (Beneficiaries)");
            System.out.println("== Statements & History ==");
            System.out.println(" 6. View Mini Statement (Last 5)");
            System.out.println(" 7. View Full Transaction History");
            System.out.println("== My Account & Profile ==");
            System.out.println(" 8. View My Profile");
            System.out.println(" 9. Update Contact Information");
            System.out.println("== Security Center ==");
            System.out.println("10. Change Login Password");
            System.out.println("== Services & Loans (COMING SOON) ==");
            System.out.println("11. Apply for a Personal Loan");
            System.out.println("12. Open a Fixed Deposit (FD)");
            System.out.println("== Exit ==");
            System.out.println("13. Logout");
            System.out.print("Please choose an option (1-13): ");

            int choice = readIntInput();

            switch (choice) {
                case 1:
                    System.out.println("Your current balance is: $" + manager.getBalance(currentUser));
                    break;
                case 2:
                    handleDeposit(currentUser);
                    break;
                case 3:
                    handleWithdraw(currentUser);
                    break;
                case 4:
                    handleTransfer(currentUser);
                    break;
                case 5:
                    handleManagePayees(currentUser);
                    break;
                case 6:
                    handleViewMiniStatement(currentUser);
                    break;
                case 7:
                    handleViewFullHistory(currentUser);
                    break;
                case 8:
                    handleViewProfile(currentUser);
                    break;
                case 9:
                    handleUpdateProfile(currentUser);
                    break;
                case 10:
                    try {
                        handleChangePassword(currentUser);
                        System.out.println("Password changed. Please log in again.");
                        loggedIn = false;
                    } catch (ValidationException | SQLException e) {
                        System.err.println("Password change failed: " + e.getMessage());
                    }
                    break;
                case 11:
                case 12:
                    System.out.println("This feature is still in implementation. Coming soon!");
                    break;
                case 13:
                    System.out.println("Logging out...");
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void handleManagePayees(Account currentUser) {
        while (true) {
            System.out.println("\n--- Manage Payees ---");
            System.out.println("1. Add New Payee");
            System.out.println("2. View My Payees");
            System.out.println("3. Delete a Payee");
            System.out.println("4. Back to Dashboard");
            System.out.print("Please choose an option (1-4): ");

            int choice = readIntInput();

            switch (choice) {
                case 1:
                    handleAddPayee(currentUser);
                    break;
                case 2:
                    handleViewPayees(currentUser);
                    break;
                case 3:
                    handleDeletePayee(currentUser);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }


    private static void handleAddPayee(Account currentUser) {
        try {
            System.out.println("\n--- Add New Payee ---");
            String payeeName = readString("Enter payee's name (e.g., 'Landlord'): ");
            String payeeAccNum = readString("Enter payee's 12-digit account number: ").trim();
            String payeeIfsc = readString("Enter payee's IFSC code: ").trim();

            manager.addPayee(currentUser, payeeName, payeeAccNum, payeeIfsc);
            System.out.println("Success! Payee '" + payeeName + "' has been added.");

        } catch (ValidationException | SQLException e) {
            System.err.println("Failed to add payee: " + e.getMessage());
        }
    }


    private static boolean handleViewPayees(Account currentUser) {
        List<Payee> payees = manager.getPayeesForAccount(currentUser);

        if (payees.isEmpty()) {
            System.out.println("You have no saved payees.");
            return false;
        }

        System.out.println("\n--- Your Saved Payees ---");
        System.out.printf("%-5s | %-20s | %-14s | %s\n",
                "ID", "Payee Name", "Account Number", "IFSC Code");
        System.out.println(new String(new char[60]).replace("\0", "-"));

        for (Payee p : payees) {
            System.out.printf("%-5d | %-20s | %-14s | %s\n",
                    p.getPayeeId(),
                    p.getPayeeName(),
                    p.getPayeeAccountNumber(),
                    p.getPayeeIfscCode()
            );
        }
        return true;
    }

    private static void handleDeletePayee(Account currentUser) {
        System.out.println("\n--- Delete a Payee ---");

        boolean hasPayees = handleViewPayees(currentUser);

        if (!hasPayees) {
            return;
        }

        try {
            int payeeId = readIntInput("Enter the 'ID' of the payee you wish to delete: ");
            manager.deletePayee(currentUser, payeeId);
            System.out.println("Payee has been successfully deleted.");
        } catch (ValidationException | SQLException e) {
            System.err.println("Failed to delete payee: " + e.getMessage());
        }
    }

    private static int readIntInput(String s) {
        System.out.println(s);
        while (true) {
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                return choice;
            } catch (InputMismatchException e) {
                System.err.print("Invalid input. Please enter a number: ");
                scanner.nextLine();
            }
        }
    }

    private static void handleDeposit(Account account) {
        try {
            BigDecimal amount = readBigDecimal("Enter amount to deposit: ");
            manager.deposit(account, amount);
            System.out.println("Deposit successful.");
            System.out.println("Your new balance is: $" + account.getBalance());
        } catch (ValidationException | SQLException e) {
            System.err.println("Deposit failed: " + e.getMessage());
        }
    }
    
    private static void handleWithdraw(Account account) {
        try {
            BigDecimal amount = readBigDecimal("Enter amount to withdraw: ");
            manager.withdraw(account, amount);
            System.out.println("Withdrawal successful.");
            System.out.println("Your new balance is: $" + account.getBalance());
        } catch (ValidationException | InsufficientFundsException | SQLException e) {
            System.err.println("Withdrawal failed: " + e.getMessage());
        }
    }
    
    private static void handleTransfer(Account fromAccount) {
        try {
            String toAccNum = readString("Enter recipient's account number: ");
            BigDecimal amount = readBigDecimal("Enter amount to transfer: ");
            
            manager.transfer(fromAccount, toAccNum, amount);
            System.out.println("Transfer successful.");
            System.out.println("Your new balance is: $" + fromAccount.getBalance());
            
        } catch (ValidationException | InsufficientFundsException | AccountNotFoundException e) {
            System.err.println("Transfer failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("A system error occurred during transfer. Transaction rolled back.");
        }
    }
    
    private static void handleViewMiniStatement(Account account) {
        try {
            List<TransactionRecord> statement = manager.getMiniStatement(account.getAccountNumber());
            printTransactionList(statement, "Mini Statement (Last 5 Transactions)");
        } catch (Exception e) {
            System.err.println("Could not retrieve statement: " + e.getMessage());
        }
    }

    private static void handleViewFullHistory(Account account) {
        try {
            List<TransactionRecord> history = manager.getFullTransactionHistory(account.getAccountNumber());
            printTransactionList(history, "Full Transaction History");
        } catch (Exception e) {
            System.err.println("Could not retrieve history: " + e.getMessage());
        }
    }

    private static void handleViewProfile(Account account) {
        Account details = manager.getAccountDetails(account);
        System.out.println("\n--- My Profile ---");
        System.out.println("Account Number: " + details.getAccountNumber());
        System.out.println("Holder Name:    " + details.getHolderName());
        System.out.println("Email:          " + details.getEmail());
        System.out.println("Phone Number:   " + details.getPhoneNumber());
        System.out.println("Address:        " + details.getFullAddress());
        System.out.println("PAN Card:       " + details.getPanCardNumber());
        System.out.println("Aadhar Card:    " + details.getAadharCardNumber());
        System.out.println("Account Type:   " + details.getAccountType());
        System.out.println("Account Status: " + details.getAccountStatus());
        System.out.println("Security Level: " + details.getSecurityLevel());
    }

    private static void handleUpdateProfile(Account account) {
        try {
            System.out.println("\n--- Update Contact Info ---");
            System.out.println("Leave a field blank to keep it unchanged.");

            String newEmail = readString("Enter new email (current: " + account.getEmail() + "): ");
            String newPhone = readString("Enter new phone (current: " + account.getPhoneNumber() + "): ");
            String newAddress = readString("Enter new address (current: " + account.getFullAddress() + "): ");


            if (newEmail.trim().isEmpty()) newEmail = account.getEmail();
            if (newPhone.trim().isEmpty()) newPhone = account.getPhoneNumber();
            if (newAddress.trim().isEmpty()) newAddress = account.getFullAddress();


            System.out.println("--- Security Settings ---");
            Account.SecurityLevel currentLevel = account.getSecurityLevel();
            Account.SecurityLevel newLevel = currentLevel;

            if (currentLevel == Account.SecurityLevel.STANDARD) {

                String upgrade = readString("Your account is currently STANDARD. Would you like to upgrade to SECURE_OTP? (Y/N): ");
                if (upgrade.equalsIgnoreCase("Y")) {
                    newLevel = Account.SecurityLevel.SECURE_OTP;
                    System.out.println("Security level will be upgraded to SECURE_OTP.");
                } else {
                    System.out.println("Security level will remain STANDARD.");
                }
            } else {

                String downgrade = readString("Your account is currently SECURE_OTP. Would you like to downgrade to STANDARD? (Y/N): ");
                if (downgrade.equalsIgnoreCase("Y")) {
                    newLevel = Account.SecurityLevel.STANDARD;
                    System.out.println("Security level will be downgraded to STANDARD.");
                } else {
                    System.out.println("Security level will remain SECURE_OTP.");
                }
            }

            manager.updateProfile(account, newEmail, newPhone, newAddress, newLevel);
            System.out.println("Profile updated successfully!");

        } catch (ValidationException | SQLException e) {
            System.err.println("Update failed: " + e.getMessage());
        }
    }
    private static void handleChangePassword(Account account) throws ValidationException, SQLException {

        System.out.println("\n--- Change Password ---");
        String oldPass = readString("Enter your CURRENT password: ");
        String newPass = readString("Enter your NEW password: ");
        String confirmPass = readString("Confirm your NEW password: ");
        
        if (!newPass.equals(confirmPass)) {
            throw new ValidationException("New passwords do not match.");
        }
        
        manager.changePassword(account, oldPass, newPass);
    }

    private static void adminDashboardLoop(Admin currentAdmin) {
        while (true) {
            System.out.println("\n--- Admin Control Dashboard (User: " + currentAdmin.getUsername() + ") ---");
            System.out.println("== Customer Account Management ==");
            System.out.println(" 1. List All Customer Accounts");
            System.out.println(" 2. Search for a Customer (and manage)");
            System.out.println("== System-Wide Monitoring ==");
            System.out.println(" 3. View All Transactions (Global Log)");
            System.out.println("== Admin & Security Management ==");
            System.out.println(" 4. Create New Admin User");
            System.out.println("== Exit ==");
            System.out.println(" 5. Logout");
            System.out.print("Please choose an option (1-5): ");
            
            int choice = readIntInput();

            switch (choice) {
                case 1:
                    handleListAllAccounts();
                    break;
                case 2:
                    handleSearchAndManageAccount();
                    break;
                case 3:
                    handleViewAllTransactions();
                    break;
                case 4:
                    handleCreateNewAdmin();
                    break;
                case 5:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void handleListAllAccounts() {
        try {
            List<Account> accounts = manager.listAllCustomerAccounts();
            if (accounts.isEmpty()) {
                System.out.println("No customer accounts found in the system.");
                return;
            }
            
            System.out.println("\n--- All Customer Accounts ---");
            System.out.printf("%-14s | %-20s | %-25s | %-10s | %s\n", 
                "Account #", "Holder Name", "Email", "Status", "Balance");
            System.out.println(new String(new char[80]).replace("\0", "-"));
            
            for (Account acc : accounts) {
                System.out.printf("%-14s | %-20s | %-25s | %-10s | $%.2f\n",
                    acc.getAccountNumber(),
                    acc.getHolderName(),
                    acc.getEmail(),
                    acc.getAccountStatus(),
                    acc.getBalance()
                );
            }
        } catch (Exception e) {
            System.err.println("Error listing accounts: " + e.getMessage());
        }
    }

    private static void handleSearchAndManageAccount() {
        try {
            String accNum = readString("Enter account number to manage: ");
            Account account = manager.searchAccountByNumber(accNum);
            
            System.out.println("\n--- Managing Account: " + accNum + " ---");
            System.out.println("Holder: " + account.getHolderName());
            System.out.println("Status: " + account.getAccountStatus());
            
            System.out.println("\nChoose an action:");
            System.out.println("1. Freeze Account");
            System.out.println("2. Unfreeze Account");
            System.out.println("3. View Full Profile");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Action: ");
            
            int choice = readIntInput();
            switch (choice) {
                case 1:
                    manager.freezeAccount(account);
                    System.out.println("Account " + accNum + " has been FROZEN.");
                    break;
                case 2:
                    manager.unfreezeAccount(account);
                    System.out.println("Account " + accNum + " has been set to ACTIVE.");
                    break;
                case 3:
                    handleViewProfile(account);
                    break;
                case 4:
                    break;
                default:
                    System.out.println("Invalid action.");
            }
            
        } catch (AccountNotFoundException e) {
            System.err.println("Search failed: " + e.getMessage());
        } catch (ValidationException | SQLException e) {
            System.err.println("Action failed: " + e.getMessage());
        }
    }

    private static void handleViewAllTransactions() {
        try {
            List<TransactionRecord> history = manager.getAllTransactions();
            printTransactionList(history, "Global Transaction Log");
        } catch (Exception e) {
            System.err.println("Could not retrieve log: " + e.getMessage());
        }
    }

    private static void handleCreateNewAdmin() {
        try {
            System.out.println("\n--- Create New Admin ---");
            String user = readString("Enter new admin username: ");
            String phone = readString("Enter your mobile phone number (for OTP): ");
            String email = readString("Enter admin email: ");
            String pass = readString("Enter temporary password: ");
            String role = readString("Enter role (e.g., Manager, Auditor): ");
            String ifsc = readString("Enter branch IFSC: ");
            
            Admin newAdmin = manager.createNewAdmin(user, pass, email,phone, role, ifsc);
            System.out.println("Admin user created successfully!");
            System.out.println("Admin ID: " + newAdmin.getAdminId());
            System.out.println("Username: " + newAdmin.getUsername());
            
        } catch (ValidationException | SQLException e) {
            System.err.println("Failed to create admin: " + e.getMessage());
        }
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }


    private static int readIntInput() {
        while (true) {
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                return choice;
            } catch (InputMismatchException e) {
                System.err.print("Invalid input. Please enter a number: ");
                scanner.nextLine();
            }
        }
    }


    private static BigDecimal readBigDecimal(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                BigDecimal amount = scanner.nextBigDecimal();
                scanner.nextLine();
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    System.err.println("Amount cannot be negative.");
                } else {
                    return amount;
                }
            } catch (InputMismatchException e) {
                System.err.print("Invalid input. Please enter a numerical amount (e.g., 50.25): ");
                scanner.nextLine();
            }
        }
    }
    

    private static void printTransactionList(List<TransactionRecord> transactions, String title) {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        
        System.out.println("\n--- " + title + " ---");
        System.out.printf("%-12s | %-10s | %-12s | %-14s | %-14s\n",
            "Date", "Type", "Amount", "From Account", "To Account");
        System.out.println(new String(new char[70]).replace("\0", "-"));
            
        for (TransactionRecord tx : transactions) {
            System.out.printf("%-12s | %-10s | $%-11.2f | %-14s | %-14s\n",
                tx.getCreatedAt().toLocalDate(),
                tx.getTxType(),
                tx.getAmount(),
                tx.getFromAccountNumber() != null ? tx.getFromAccountNumber() : "N/A",
                tx.getToAccountNumber() != null ? tx.getToAccountNumber() : "N/A"
            );
        }
    }
}