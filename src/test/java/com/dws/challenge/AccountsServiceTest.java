package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @MockBean
  private NotificationService notificationService;

  private Account accountFrom;
  private Account accountTo;

  @BeforeEach
  void prepareAccounts() {
    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();

    // Create account objects for transfer tests
    accountFrom = new Account("1", BigDecimal.valueOf(100));
    accountTo = new Account("2", BigDecimal.valueOf(50));
  }

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  void transferMoney_successfulTransfer() {
    this.accountsService.createAccount(accountFrom);
    this.accountsService.createAccount(accountTo);
    this.accountsService.transferFunds(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(25));
    assertThat(this.accountsService.getAccount(accountFrom.getAccountId()).getBalance()).isEqualTo(BigDecimal.valueOf(75));
    assertThat(this.accountsService.getAccount(accountTo.getAccountId()).getBalance()).isEqualTo(BigDecimal.valueOf(75));
    verify(notificationService).notifyAboutTransfer(accountFrom, "Transferred 25 to 2");
    verify(notificationService).notifyAboutTransfer(accountTo, "Received 25 from 1");
  }

  @Test
  void transferFunds_insufficientFunds() {
    this.accountsService.createAccount(accountFrom);
    this.accountsService.createAccount(accountTo);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.accountsService.transferFunds(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(101)));
    assertEquals("Insufficient funds in the account", exception.getMessage());
  }

  @Test
  void transferFunds_invalidAmount() {
    this.accountsService.createAccount(accountFrom);
    this.accountsService.createAccount(accountTo);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.accountsService.transferFunds(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(-10)));

    assertEquals("Transfer amount must be positive", exception.getMessage());
  }

  @Test
  void transferFunds_accountNotFound() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> accountsService.transferFunds("1", "2", BigDecimal.valueOf(10)));

    assertEquals("Both accounts must exist", exception.getMessage());
  }
}
