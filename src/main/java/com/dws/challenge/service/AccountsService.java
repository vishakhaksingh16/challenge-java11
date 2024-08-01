package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Getter
  private final NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void transferFunds(String accountFromId, String accountToId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Transfer amount must be positive");
    }
    Account accountFrom = this.accountsRepository.getAccount(accountFromId);
    Account accountTo = this.accountsRepository.getAccount(accountToId);

    this.accountsRepository.transferFunds(accountFrom, accountTo, amount);
    this.notificationService.notifyAboutTransfer(accountFrom, "Transferred " + amount + " to " + accountToId);
    this.notificationService.notifyAboutTransfer(accountTo, "Received " + amount + " from " + accountFromId);
  }
}
