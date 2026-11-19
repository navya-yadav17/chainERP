package com.chainerp.backend.domain;

import com.chainerp.backend.domain.DomainModels.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class DomainRepository {
    private final JdbcTemplate jdbcTemplate;

    public DomainRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TreasuryTransaction> treasuryTransactions(UUID organizationId) {
        if (organizationId == null) {
            return jdbcTemplate.query(
                """
                SELECT id, organization_id, tx_hash, from_account, to_account, amount, currency, status, created_at
                FROM treasury_transactions
                ORDER BY created_at DESC
                """,
                (result, row) -> treasuryTransaction(result)
            );
        }
        return jdbcTemplate.query(
            """
            SELECT id, organization_id, tx_hash, from_account, to_account, amount, currency, status, created_at
            FROM treasury_transactions
            WHERE organization_id = ?
            ORDER BY created_at DESC
            """,
            (result, row) -> treasuryTransaction(result),
            organizationId
        );
    }

    public TreasuryBalance treasuryBalance(String account, String currency) {
        if (account == null || account.isBlank()) {
            throw new IllegalArgumentException("account is required");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }
        java.math.BigDecimal net = jdbcTemplate.queryForObject(
            """
            SELECT COALESCE(
                SUM(CASE
                    WHEN to_account = ? AND status <> 'FAILED' THEN amount
                    WHEN from_account = ? AND status = 'SETTLED' THEN -amount
                    ELSE 0
                END),
                0
            )
            FROM treasury_transactions
            WHERE currency = ? AND status <> 'FAILED'
              AND (to_account = ? OR (from_account = ? AND status = 'SETTLED'))
            """,
            java.math.BigDecimal.class,
            account, account, currency.toUpperCase(), account, account
        );
        return new TreasuryBalance(account, currency.toUpperCase(), net);
    }

    @Transactional
    public TreasuryTransaction createTreasuryTransaction(TransferRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (request.fromAccount() == null || request.fromAccount().isBlank()
            || request.toAccount() == null || request.toAccount().isBlank()) {
            throw new IllegalArgumentException("fromAccount and toAccount are required");
        }
        if (request.currency() == null || request.currency().isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            """
            INSERT INTO treasury_transactions
            (id, organization_id, tx_hash, from_account, to_account, amount, currency, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')
            """,
            id, request.organizationId(), request.txHash(), request.fromAccount(),
            request.toAccount(), request.amount(), request.currency()
        );
        List<TreasuryTransaction> transactions = jdbcTemplate.query(
            """
            SELECT id, organization_id, tx_hash, from_account, to_account, amount, currency, status, created_at
            FROM treasury_transactions WHERE id = ?
            """,
            (result, row) -> treasuryTransaction(result),
            id
        );
        return transactions.stream().findFirst().orElseThrow();
    }

    @Transactional
    public PayrollRun createPayrollRun(PayrollRunRequest request) {
        if (request.entries() == null || request.entries().isEmpty()) {
            throw new IllegalArgumentException("Payroll entries must not be empty");
        }
        if (request.currency() == null || request.currency().isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }
        if (request.runDate() == null) {
            throw new IllegalArgumentException("runDate is required");
        }
        for (PayrollEntryRequest entry : request.entries()) {
            if (entry.employeeId() == null || entry.employeeId().isBlank()) {
                throw new IllegalArgumentException("employeeId is required");
            }
            if (entry.grossAmount() == null || entry.grossAmount().signum() < 0
                || entry.netAmount() == null || entry.netAmount().signum() < 0
                || entry.taxes() == null || entry.taxes().signum() < 0) {
                throw new IllegalArgumentException("Payroll amounts must not be negative");
            }
        }
        java.math.BigDecimal total = request.entries().stream()
            .map(PayrollEntryRequest::netAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            """
            INSERT INTO payroll_runs
            (id, organization_id, run_date, total_amount, currency, status, generated_by)
            VALUES (?, ?, ?, ?, ?, 'CREATED', ?)
            """,
            id, request.organizationId(), request.runDate(), total,
            request.currency(), request.generatedBy()
        );
        for (PayrollEntryRequest entry : request.entries()) {
            jdbcTemplate.update(
                """
                INSERT INTO payroll_entries
                (id, payroll_run_id, employee_id, gross_amount, net_amount, taxes, status)
                VALUES (?, ?, ?, ?, ?, ?, 'CREATED')
                """,
                UUID.randomUUID(), id, entry.employeeId(), entry.grossAmount(),
                entry.netAmount(), entry.taxes()
            );
        }
        return findPayrollRun(id).orElseThrow();
    }

    public List<PayrollRun> payrollRuns(UUID organizationId) {
        if (organizationId == null) {
            return jdbcTemplate.query(
                """
                SELECT id, organization_id, run_date, total_amount, currency, status, generated_by, created_at
                FROM payroll_runs
                ORDER BY created_at DESC
                """,
                (result, row) -> payrollRunSummary(result)
            );
        }
        return jdbcTemplate.query(
            """
            SELECT id, organization_id, run_date, total_amount, currency, status, generated_by, created_at
            FROM payroll_runs
            WHERE organization_id = ?
            ORDER BY created_at DESC
            """,
            (result, row) -> payrollRunSummary(result),
            organizationId
        );
    }

    public java.util.Optional<PayrollRun> findPayrollRun(UUID id) {
        List<PayrollRun> runs = jdbcTemplate.query(
            """
            SELECT id, organization_id, run_date, total_amount, currency, status, generated_by, created_at
            FROM payroll_runs WHERE id = ?
            """,
            (result, row) -> payrollRunSummary(result),
            id
        );
        if (runs.isEmpty()) return java.util.Optional.empty();
        PayrollRun run = runs.get(0);
        List<PayrollEntry> entries = jdbcTemplate.query(
            """
            SELECT id, employee_id, gross_amount, net_amount, taxes, status
            FROM payroll_entries WHERE payroll_run_id = ? ORDER BY employee_id
            """,
            (result, row) -> new PayrollEntry(
                (UUID) result.getObject("id"),
                result.getString("employee_id"),
                result.getBigDecimal("gross_amount"),
                result.getBigDecimal("net_amount"),
                result.getBigDecimal("taxes"),
                result.getString("status")
            ),
            id
        );
        return java.util.Optional.of(new PayrollRun(
            run.id(), run.organizationId(), run.runDate(), run.totalAmount(),
            run.currency(), run.status(), run.generatedBy(), run.createdAt(), entries
        ));
    }

    @Transactional
    public Subscription createSubscription(SubscriptionRequest request) {
        if (request.subscriber() == null || request.subscriber().isBlank()) {
            throw new IllegalArgumentException("subscriber is required");
        }
        if (request.plan() == null || request.plan().isBlank()) {
            throw new IllegalArgumentException("plan is required");
        }
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            """
            INSERT INTO subscriptions
            (id, organization_id, subscriber, plan, status, next_billing_date)
            VALUES (?, ?, ?, ?, 'ACTIVE', ?)
            """,
            id, request.organizationId(), request.subscriber(),
            request.plan(), request.nextBillingDate()
        );
        return findSubscription(id).orElseThrow();
    }

    public java.util.Optional<Subscription> findSubscription(UUID id) {
        List<Subscription> subscriptions = jdbcTemplate.query(
            """
            SELECT id, organization_id, subscriber, plan, status, next_billing_date, created_at
            FROM subscriptions WHERE id = ?
            """,
            (result, row) -> subscription(result),
            id
        );
        return subscriptions.stream().findFirst();
    }

    public List<Subscription> subscriptions(UUID organizationId) {
        if (organizationId == null) {
            return jdbcTemplate.query(
                """
                SELECT id, organization_id, subscriber, plan, status, next_billing_date, created_at
                FROM subscriptions
                ORDER BY created_at DESC
                """,
                (result, row) -> subscription(result)
            );
        }
        return jdbcTemplate.query(
            """
            SELECT id, organization_id, subscriber, plan, status, next_billing_date, created_at
            FROM subscriptions
            WHERE organization_id = ?
            ORDER BY created_at DESC
            """,
            (result, row) -> subscription(result),
            organizationId
        );
    }

    @Transactional
    public Subscription renewSubscription(UUID id, LocalDate nextBillingDate) {
        int updated = jdbcTemplate.update(
            "UPDATE subscriptions SET status = 'ACTIVE', next_billing_date = ? WHERE id = ?",
            nextBillingDate, id
        );
        if (updated == 0) throw new java.util.NoSuchElementException("Subscription not found");
        return findSubscription(id).orElseThrow();
    }

    @Transactional
    public Subscription cancelSubscription(UUID id) {
        int updated = jdbcTemplate.update(
            "UPDATE subscriptions SET status = 'CANCELLED' WHERE id = ?",
            id
        );
        if (updated == 0) throw new java.util.NoSuchElementException("Subscription not found");
        return findSubscription(id).orElseThrow();
    }

    private TreasuryTransaction treasuryTransaction(ResultSet result) throws SQLException {
        return new TreasuryTransaction(
            (UUID) result.getObject("id"), (UUID) result.getObject("organization_id"),
            result.getString("tx_hash"), result.getString("from_account"),
            result.getString("to_account"), result.getBigDecimal("amount"),
            result.getString("currency"), result.getString("status"),
            result.getTimestamp("created_at").toInstant()
        );
    }

    private PayrollRun payrollRunSummary(ResultSet result) throws SQLException {
        return new PayrollRun(
            (UUID) result.getObject("id"), (UUID) result.getObject("organization_id"),
            result.getObject("run_date", java.time.LocalDate.class),
            result.getBigDecimal("total_amount"), result.getString("currency"),
            result.getString("status"), result.getString("generated_by"),
            result.getTimestamp("created_at").toInstant(), List.of()
        );
    }

    private Subscription subscription(ResultSet result) throws SQLException {
        return new Subscription(
            (UUID) result.getObject("id"), (UUID) result.getObject("organization_id"),
            result.getString("subscriber"), result.getString("plan"),
            result.getString("status"),
            result.getObject("next_billing_date", java.time.LocalDate.class),
            result.getTimestamp("created_at").toInstant()
        );
    }
}
