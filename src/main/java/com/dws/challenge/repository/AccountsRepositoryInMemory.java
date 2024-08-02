package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    // Map to hold locks for each account
    private final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();
    // Get or create a lock for a specific account
    private Lock getLock(String accountId) {
        return locks.computeIfAbsent(accountId, id -> new ReentrantLock());
    }

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    @Override
    public void transferFunds(Account accountFrom, Account accountTo, BigDecimal amount) {
        if (Objects.isNull(accountFrom) || Objects.isNull(accountTo)) {
            throw new IllegalArgumentException("Both accounts must exist");
        }
        if (accountFrom.equals(accountTo)) {
            throw new IllegalArgumentException("Cannot transfer funds between the same account");
        }

        String idFrom = accountFrom.getAccountId();
        String idTo = accountTo.getAccountId();

        // consistent order of locks considering order of account ids
        Lock lock1 = getLock(idFrom);
        Lock lock2 = getLock(idTo);
        Lock firstLock = idFrom.compareTo(idTo) < 0 ? lock1 : lock2;
        Lock secondLock = idFrom.compareTo(idTo) < 0 ? lock2 : lock1;

        firstLock.lock();
        try {
            secondLock.lock();
            try {
                if (accountFrom.getBalance().compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Insufficient funds in the account");
                }
                accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
                accountTo.setBalance(accountTo.getBalance().add(amount));
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }
    }
    }

