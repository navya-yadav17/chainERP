package com.chainerp.backend.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.chainerp.backend.domain.DomainModels.PayrollEntryRequest;
import com.chainerp.backend.domain.DomainModels.PayrollRunRequest;
import com.chainerp.backend.domain.DomainModels.SubscriptionRequest;
import com.chainerp.backend.domain.DomainModels.TransferRequest;
import com.chainerp.backend.domain.DomainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class DomainControllerTest {
    private final DomainRepository repository = mock(DomainRepository.class);
    private final DomainController controller = new DomainController(repository);

    @Test
    void rejectsZeroValueTreasuryTransfers() {
        TransferRequest request = new TransferRequest(
            null, "account-a", "account-b", BigDecimal.ZERO, "USD", null
        );

        assertThatThrownBy(() -> controller.createTransfer(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("amount must be greater than zero");
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsPayrollRunsWithEmptyEntries() {
        PayrollRunRequest request = new PayrollRunRequest(
            null, LocalDate.now(), "USD", null, List.of()
        );

        assertThatThrownBy(() -> controller.createPayrollRun(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("entries must not be empty");
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsNegativePayrollAmounts() {
        PayrollEntryRequest entry = new PayrollEntryRequest(
            "employee-1", BigDecimal.ONE, BigDecimal.valueOf(-1), BigDecimal.ZERO
        );
        PayrollRunRequest request = new PayrollRunRequest(
            null, LocalDate.now(), "USD", null, List.of(entry)
        );

        assertThatThrownBy(() -> controller.createPayrollRun(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("amounts must not be negative");
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsSubscriptionsWithoutSubscriberOrPlan() {
        SubscriptionRequest request = new SubscriptionRequest(
            null, "", "Pro", null
        );

        assertThatThrownBy(() -> controller.createSubscription(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("subscriber is required");
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsTreasuryBalanceWithoutAccount() {
        assertThatThrownBy(() -> controller.treasuryBalance("", "USD"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("account is required");
        verifyNoInteractions(repository);
    }
}
