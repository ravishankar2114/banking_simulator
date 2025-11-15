package com.yourbank.service;

import com.yourbank.dao.AccountDao;
import com.yourbank.dao.AdminDao;
import com.yourbank.dao.PayeeDao;
import com.yourbank.dao.TransactionDao;
import com.yourbank.exception.AccountNotFoundException;
import com.yourbank.exception.InsufficientFundsException;
import com.yourbank.exception.ValidationException;
import com.yourbank.model.Account;
import com.yourbank.model.Account.AccountStatus;
import com.yourbank.model.Account.SecurityLevel;
import com.yourbank.model.Admin;
import com.yourbank.model.Payee;
import com.yourbank.model.TransactionRecord;
import com.yourbank.model.TransactionRecord.TxType;
import com.yourbank.util.DatabaseUtil;
import com.yourbank.util.OtpService;
import com.yourbank.util.SecurityUtil;
import com.yourbank.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class AccountManager {

    private AccountDao accountDao;
    private AdminDao adminDao;
    private TransactionDao transactionDao;
    private PayeeDao payeeDao;
    private Connection connection;


    public AccountManager() {

        this.accountDao = new AccountDao();
        this.adminDao = new AdminDao();
        this.transactionDao = new TransactionDao();
        this.payeeDao = new PayeeDao();
        try {
            this.connection = DatabaseUtil.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get DB connection for manager", e);
        }
    }




    public Account createAccount(String holderName, String email, String password, String phone,
                                 String address, String pan, String aadhar, String ifsc,
                                 Account.AccountType type, Account.SecurityLevel securityLevel)
            throws ValidationException, SQLException {


        if (phone.length() == 10 && ValidationUtil.isValidPhoneNumber(phone)) {
            phone = "+91" + phone;
        } else if (phone.startsWith("+") && phone.length() > 10) {
        } else {

            throw new ValidationException("Invalid phone number. Must be 10 digits or in international format (e.g., +91...).");
        }


        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Invalid email format.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new ValidationException("Password must be at least 8 chars, with a number and special char.");
        }
        if (pan != null && !pan.isEmpty() && !ValidationUtil.isValidPan(pan)) {
            throw new ValidationException("Invalid PAN card format.");
        }
        if (aadhar != null && !aadhar.isEmpty() && !ValidationUtil.isValidAadhar(aadhar)) {
            throw new ValidationException("Invalid Aadhar card format. Must be 12 digits.");
        }
        if (ifsc != null && !ifsc.isEmpty() && !ValidationUtil.isValidIfsc(ifsc)) {
            throw new ValidationException("Invalid IFSC code format.");
        }

        String accountNumber = generateUniqueAccountNumber();
        String passwordHash = SecurityUtil.hashPassword(password);


        Account newAccount = new Account();
        newAccount.setAccountNumber(accountNumber);
        newAccount.setHolderName(holderName);
        newAccount.setPasswordHash(passwordHash);
        newAccount.setEmail(email);
        newAccount.setPhoneNumber(phone);
        newAccount.setFullAddress(address);
        newAccount.setPanCardNumber(pan);
        newAccount.setAadharCardNumber(aadhar);
        newAccount.setIfscCode(ifsc);
        newAccount.setAccountType(type);
        newAccount.setSecurityLevel(securityLevel);
        newAccount.setAccountStatus(AccountStatus.ACTIVE);
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setCreatedAt(LocalDateTime.now());

        accountDao.createAccount(newAccount);

        return newAccount;
    }


    public Account login(String accountNumber, String password)
            throws AccountNotFoundException, ValidationException {


        Account account;
        try {
            account = accountDao.findAccountByNumber(accountNumber);
        } catch (SQLException e) {
            throw new RuntimeException("Database error during login.", e);
        }

        if (account == null) {
            throw new AccountNotFoundException("No account found with that number.");
        }


        if (account.getAccountStatus() == AccountStatus.FROZEN ||
                account.getAccountStatus() == AccountStatus.CLOSED) {
            throw new ValidationException("This account is " + account.getAccountStatus() + ". Please contact the bank.");
        }


        if (!SecurityUtil.checkPassword(password, account.getPasswordHash())) {
            throw new ValidationException("Invalid account number or password.");
        }


        return account;
    }


    public Admin adminLogin(String username, String password)
            throws AccountNotFoundException, ValidationException {


        System.out.println("\n--- DEBUGGING ADMIN LOGIN ---");
        System.out.println("Attempting login for username: [" + username + "]");

        Admin admin;
        try {
            admin = adminDao.findAdminByUsername(username);
        } catch (SQLException e) {
            throw new RuntimeException("Database error during admin login.", e);
        }


        if (admin == null) {
            System.out.println("DEBUG: findAdminByUsername returned NULL.");
            System.out.println("This means the username was not found in the DB.");
            System.out.println("--- END DEBUG ---");
            throw new AccountNotFoundException("Invalid admin username or password.");
        }

        System.out.println("DEBUG: User '" + admin.getUsername() + "' was found successfully.");


        String hashFromDB = admin.getPasswordHash();
        System.out.println("DEBUG: Hash from DB is: [" + hashFromDB + "]");

        boolean passwordMatches = SecurityUtil.checkPassword(password, hashFromDB);

        System.out.println("DEBUG: Plain-text password is: [" + password + "]");
        System.out.println("DEBUG: Does password match hash? --> " + passwordMatches);
        System.out.println("--- END DEBUG ---");

        if (!passwordMatches) {
            throw new ValidationException("Invalid admin username or password.");
        }

        return admin;
    }


    public String sendOtp(String phoneNumber) {
        return OtpService.generateAndSendOtp(phoneNumber);
    }

    public BigDecimal getBalance(Account currentUser) {
        return currentUser.getBalance();
    }


    public void deposit(Account account, BigDecimal amount)
            throws ValidationException, SQLException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Deposit amount must be positive.");
        }

        account.setBalance(account.getBalance().add(amount));
        accountDao.updateAccount(account);
        logTransaction(TxType.DEPOSIT, amount, null, account.getAccountNumber());
    }


    public void withdraw(Account account, BigDecimal amount)
            throws ValidationException, InsufficientFundsException, SQLException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Withdrawal amount must be positive.");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Your balance is: " + account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountDao.updateAccount(account);
        logTransaction(TxType.WITHDRAW, amount, account.getAccountNumber(), null);
    }

    public void transfer(Account fromAccount, String toAccountNumber, BigDecimal amount)
            throws ValidationException, InsufficientFundsException, AccountNotFoundException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Transfer amount must be positive.");
        }
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Your balance is: " + fromAccount.getBalance());
        }
        if (fromAccount.getAccountNumber().equals(toAccountNumber)) {
            throw new ValidationException("Cannot transfer money to yourself.");
        }

        try {
            Account toAccount = accountDao.findAccountByNumber(toAccountNumber);
            if (toAccount == null) {
                throw new AccountNotFoundException("The recipient account number does not exist.");
            }
            if (toAccount.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new ValidationException("The recipient account is not active.");
            }

            connection.setAutoCommit(false);


            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            accountDao.updateAccount(fromAccount);


            toAccount.setBalance(toAccount.getBalance().add(amount));
            accountDao.updateAccount(toAccount);


            logTransaction(TxType.TRANSFER, amount, fromAccount.getAccountNumber(), toAccountNumber);

            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Transfer failed due to a database error. Transaction was rolled back.", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public List<TransactionRecord> getMiniStatement(String accountNumber) {
        final int MINI_STATEMENT_LIMIT = 5;
        try {
            return transactionDao.findMiniStatement(accountNumber, MINI_STATEMENT_LIMIT);
        } catch (SQLException e) {
            System.err.println("Database error retrieving mini statement: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public List<TransactionRecord> getFullTransactionHistory(String accountNumber) {
        try {
            return transactionDao.findTransactionsForAccount(accountNumber);
        } catch (SQLException e) {
            System.err.println("Database error retrieving full transaction history: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public Account getAccountDetails(Account currentUser) {
        return currentUser;
    }


    public void updateProfile(Account currentUser, String newEmail, String newPhone, String newAddress,
                              Account.SecurityLevel newSecurityLevel)
            throws ValidationException, SQLException {


        if (newPhone.length() == 10 && ValidationUtil.isValidPhoneNumber(newPhone)) {
            newPhone = "+91" + newPhone;
        } else if (newPhone.startsWith("+") && newPhone.length() > 10) {

        } else {
            throw new ValidationException("Invalid phone number. Must be 10 digits or in international format (e.g., +91...).");
        }

        if (!ValidationUtil.isValidEmail(newEmail)) {
            throw new ValidationException("Invalid email format.");
        }
        if (newAddress == null || newAddress.trim().isEmpty()) {
            throw new ValidationException("Address cannot be empty.");
        }


        currentUser.setEmail(newEmail);
        currentUser.setPhoneNumber(newPhone);
        currentUser.setFullAddress(newAddress);
        currentUser.setSecurityLevel(newSecurityLevel);


        accountDao.updateAccount(currentUser);
    }

    public void changePassword(Account currentUser, String oldPassword, String newPassword)
            throws ValidationException, SQLException {

        if (!SecurityUtil.checkPassword(oldPassword, currentUser.getPasswordHash())) {
            throw new ValidationException("Incorrect old password.");
        }
        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new ValidationException("New password is too weak. Must be 8+ chars, 1 number, 1 special char.");
        }

        String newPasswordHash = SecurityUtil.hashPassword(newPassword);
        currentUser.setPasswordHash(newPasswordHash);

        accountDao.updateAccount(currentUser);
    }


    public List<Account> listAllCustomerAccounts() {
        try {
            return accountDao.listAllAccounts();
        } catch (SQLException e) {
            System.err.println("Database error listing all accounts: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public Account searchAccountByNumber(String accountNumber)
            throws AccountNotFoundException {
        try {
            Account account = accountDao.findAccountByNumber(accountNumber);
            if (account == null) {
                throw new AccountNotFoundException("No account found with number: " + accountNumber);
            }
            return account;
        } catch (SQLException e) {
            System.err.println("Database error searching for account: " + e.getMessage());
            throw new AccountNotFoundException("A database error occurred. Please try again.");
        }
    }

    public void freezeAccount(Account accountToFreeze) throws ValidationException, SQLException {
        if (accountToFreeze.getAccountStatus() == AccountStatus.FROZEN) {
            throw new ValidationException("This account is already frozen.");
        }
        accountToFreeze.setAccountStatus(AccountStatus.FROZEN);
        accountDao.updateAccount(accountToFreeze);
    }


    public void unfreezeAccount(Account accountToUnfreeze) throws ValidationException, SQLException {
        if (accountToUnfreeze.getAccountStatus() != AccountStatus.FROZEN) {
            throw new ValidationException("This account is not frozen.");
        }
        accountToUnfreeze.setAccountStatus(AccountStatus.ACTIVE);
        accountDao.updateAccount(accountToUnfreeze);
    }


    public List<TransactionRecord> getAllTransactions() {
        try {
            return transactionDao.findAllTransactions();
        } catch (SQLException e) {
            System.err.println("Database error listing all transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public Admin createNewAdmin(String username, String password, String email,
                                String phone, String role, String bankBranchIfsc)
            throws ValidationException, SQLException {


        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Invalid email format.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new ValidationException("Password is too weak. Must be 8+ chars, 1 number, 1 special char.");
        }

        if (phone.length() == 10 && ValidationUtil.isValidPhoneNumber(phone)) {
            phone = "+91" + phone;
        } else if (phone.startsWith("+") && phone.length() > 10) {
        } else {
            throw new ValidationException("Invalid admin phone number. Must be 10 digits or in +91 format.");
        }

        if (adminDao.findAdminByUsername(username) != null) {
            throw new ValidationException("Username '" + username + "' is already taken.");
        }


        String adminId = "admin_" + username.toLowerCase();
        String passwordHash = SecurityUtil.hashPassword(password);
        String bankName = DatabaseUtil.getProperty("BANK_NAME", "Global Bank Inc.");


        Admin newAdmin = new Admin(
                adminId,
                username,
                passwordHash,
                email,
                phone,
                role,
                bankName,
                bankBranchIfsc
        );


        adminDao.createAdmin(newAdmin);

        return newAdmin;
    }



    private String generateUniqueAccountNumber() {
        long first11 = ThreadLocalRandom.current().nextLong(100_000_000_00L, 999_999_999_99L);
        int lastDigit = ThreadLocalRandom.current().nextInt(10);
        return "" + first11 + lastDigit;
    }

     void logTransaction(TxType type, BigDecimal amount, String fromAcct, String toAcct)
            throws SQLException {

        TransactionRecord tx = new TransactionRecord(
                UUID.randomUUID().toString(),
                type,
                amount,
                fromAcct,
                toAcct,
                LocalDateTime.now()
        );
        transactionDao.createTransaction(tx);
    }

    public List<Payee> getPayeesForAccount(Account currentUser) {
        try {
            return payeeDao.findPayeesForAccount(currentUser.getAccountNumber());
        } catch (SQLException e) {
            System.err.println("Database error retrieving payees: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public void addPayee(Account currentUser, String payeeName, String payeeAccNum, String payeeIfsc)
            throws ValidationException, SQLException {


        if (!ValidationUtil.isValidIfsc(payeeIfsc)) {
            throw new ValidationException("Invalid IFSC code format.");
        }
        if (payeeAccNum.equals(currentUser.getAccountNumber())) {
            throw new ValidationException("You cannot add yourself as a payee.");
        }
        if (payeeName == null || payeeName.trim().isEmpty()) {
            throw new ValidationException("Payee name cannot be empty.");
        }

        Account payeeAccount = accountDao.findAccountByNumber(payeeAccNum);
        if (payeeAccount == null) {
            throw new ValidationException("The payee account number " + payeeAccNum + " does not exist.");
        }


        Payee newPayee = new Payee();
        newPayee.setOwnerAccountNumber(currentUser.getAccountNumber());
        newPayee.setPayeeName(payeeName);
        newPayee.setPayeeAccountNumber(payeeAccNum);
        newPayee.setPayeeIfscCode(payeeIfsc);

        try {
            payeeDao.createPayee(newPayee);
        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {
                throw new ValidationException("You have already added this payee.");
            }
            throw e;
        }
    }


    public void deletePayee(Account currentUser, int payeeId)
            throws ValidationException, SQLException {

        boolean success = payeeDao.deletePayee(payeeId, currentUser.getAccountNumber());

        if (!success) {

            throw new ValidationException("Delete failed: Payee ID not found or you do not own this payee.");
        }
    }
}