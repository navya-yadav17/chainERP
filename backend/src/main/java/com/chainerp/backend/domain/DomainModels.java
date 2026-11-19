package com.chainerp.backend.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class DomainModels {
    private DomainModels() {}

    public record TransferRequest(
        UUID organizationId,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        String currency,
        String txHash
    ) {}

    public record TreasuryTransaction(
        UUID id,
        UUID organizationId,
        String txHash,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
    ) {}

    public record TreasuryBalance(
        String account,
        String currency,
        BigDecimal recordedNetAmount
    ) {}

    public record PayrollEntryRequest(
        String employeeId,
        BigDecimal grossAmount,
        BigDecimal netAmount,
        BigDecimal taxes
    ) {}

    public record PayrollRunRequest(
        UUID organizationId,
        LocalDate runDate,
        String currency,
        String generatedBy,
        List<PayrollEntryRequest> entries
    ) {}

    public record PayrollEntry(
        UUID id,
        String employeeId,
        BigDecimal grossAmount,
        BigDecimal netAmount,
        BigDecimal taxes,
        String status
    ) {}

    public record PayrollRun(
        UUID id,
        UUID organizationId,
        LocalDate runDate,
        BigDecimal totalAmount,
        String currency,
        String status,
        String generatedBy,
        Instant createdAt,
        List<PayrollEntry> entries
    ) {}

    public record SubscriptionRequest(
        UUID organizationId,
        String subscriber,
        String plan,
        LocalDate nextBillingDate
    ) {}

    public record Subscription(
        UUID id,
        UUID organizationId,
        String subscriber,
        String plan,
        String status,
        LocalDate nextBillingDate,
        Instant createdAt
    ) {}
}
