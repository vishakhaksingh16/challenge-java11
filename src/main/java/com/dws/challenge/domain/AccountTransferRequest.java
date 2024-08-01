package com.dws.challenge.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Getter
@Setter
public class AccountTransferRequest {

    public AccountTransferRequest(String accountFromId, String accountToId, BigDecimal transferAmount) {
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.transferAmount = transferAmount;
    }

    @NotNull
    @NotEmpty
    private final String accountFromId;

    @NotNull
    @NotEmpty
    private final String accountToId;

    @NotNull
    @Min(value = 0, message = "Transfer amount must be positive.")
    private BigDecimal transferAmount;
}