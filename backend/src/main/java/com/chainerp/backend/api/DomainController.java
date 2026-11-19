package com.chainerp.backend.api;

import com.chainerp.backend.domain.DomainModels.*;
import com.chainerp.backend.domain.DomainRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class DomainController {
    private final DomainRepository repository;

    public DomainController(DomainRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/treasury/transactions")
    public List<TreasuryTransaction> treasuryTransactions(
        @RequestParam(required = false) UUID organizationId
    ) {
        return repository.treasuryTransactions(organizationId);
    }

    @PostMapping("/treasury/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TreasuryTransaction createTransfer(@RequestBody TransferRequest request) {
        validateAccount(request.fromAccount(), "fromAccount");
        validateAccount(request.toAccount(), "toAccount");
        validateRequired(request.currency(), "currency");
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw badRequest("amount must be greater than zero");
        }
        return repository.createTreasuryTransaction(request);
    }

    @GetMapping("/treasury/balance")
    public TreasuryBalance treasuryBalance(
        @RequestParam String account,
        @RequestParam(defaultValue = "USD") String currency
    ) {
        validateAccount(account, "account");
        try {
            return repository.treasuryBalance(account, currency);
        } catch (IllegalArgumentException exception) {
            throw badRequest(exception.getMessage());
        }
    }

    @PostMapping("/payroll/run")
    @ResponseStatus(HttpStatus.CREATED)
    public PayrollRun createPayrollRun(@RequestBody PayrollRunRequest request) {
        validateRequired(request.currency(), "currency");
        if (request.runDate() == null) {
            throw badRequest("runDate is required");
        }
        if (request.entries() == null || request.entries().isEmpty()) {
            throw badRequest("Payroll entries must not be empty");
        }
        for (PayrollEntryRequest entry : request.entries()) {
            validateAccount(entry.employeeId(), "employeeId");
            if (entry.grossAmount() == null || entry.grossAmount().signum() < 0
                || entry.netAmount() == null || entry.netAmount().signum() < 0
                || entry.taxes() == null || entry.taxes().signum() < 0) {
                throw badRequest("Payroll amounts must not be negative");
            }
        }
        return repository.createPayrollRun(request);
    }

    @GetMapping("/payroll")
    public List<PayrollRun> payrollRuns(@RequestParam(required = false) UUID organizationId) {
        return repository.payrollRuns(organizationId);
    }

    @GetMapping("/payroll/{id}")
    public PayrollRun payrollRun(@PathVariable UUID id) {
        return repository.findPayrollRun(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payroll run not found")
        );
    }

    @PostMapping("/subscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public Subscription createSubscription(@RequestBody SubscriptionRequest request) {
        validateAccount(request.subscriber(), "subscriber");
        validateRequired(request.plan(), "plan");
        return repository.createSubscription(request);
    }

    @GetMapping("/subscriptions")
    public List<Subscription> subscriptions(@RequestParam(required = false) UUID organizationId) {
        return repository.subscriptions(organizationId);
    }

    @GetMapping("/subscriptions/{id}")
    public Subscription subscription(@PathVariable UUID id) {
        return repository.findSubscription(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
        );
    }

    public record RenewSubscriptionRequest(LocalDate nextBillingDate) {}

    @PostMapping("/subscriptions/{id}/renew")
    public Subscription renewSubscription(
        @PathVariable UUID id,
        @RequestBody RenewSubscriptionRequest request
    ) {
        if (request.nextBillingDate() == null) {
            throw badRequest("nextBillingDate is required");
        }
        try {
            return repository.renewSubscription(id, request.nextBillingDate());
        } catch (java.util.NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found");
        }
    }

    @PostMapping("/subscriptions/{id}/cancel")
    public Subscription cancelSubscription(@PathVariable UUID id) {
        try {
            return repository.cancelSubscription(id);
        } catch (java.util.NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found");
        }
    }

    private void validateRequired(String value, String field) {
        if (value == null || value.isBlank()) throw badRequest(field + " is required");
    }

    private void validateAccount(String value, String field) {
        validateRequired(value, field);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
