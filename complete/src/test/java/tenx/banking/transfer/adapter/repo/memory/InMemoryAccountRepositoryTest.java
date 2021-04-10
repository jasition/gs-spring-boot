package tenx.banking.transfer.adapter.repo.memory;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import tenx.banking.transfer.core.entity.Account;
import tenx.banking.transfer.core.entity.AccountId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryAccountRepositoryTest {
    private InMemoryAccountRepository repository = new InMemoryAccountRepository();

    @Test
    public void returnOptionalEmptyForNonExistentAccount() {
        assertThat(repository.get(new AccountId(UUID.randomUUID()))).isEmpty();
    }

    @Test
    public void previouslyCreatedAccountCanBeRetrieved() {
        Account account = new Account(
                new AccountId(UUID.randomUUID()),
                Money.of(new BigDecimal("1235.2"), "EUR"),
                Instant.now()
        );
        repository.set(account);

        assertThat(repository.get(account.getId())).isEqualTo(Optional.of(account));
    }

    @Test
    public void setAccountIsIdempotent() {
        Account account = new Account(
                new AccountId(UUID.randomUUID()),
                Money.of(new BigDecimal("1235.2"), "EUR"),
                Instant.now()
        );
        repository.set(account);
        repository.set(account);

        assertThat(repository.get(account.getId())).isEqualTo(Optional.of(account));
    }

    @Test
    public void previouslyCreatedAccountCanSetBalance() {
        Account account = new Account(
                new AccountId(UUID.randomUUID()),
                Money.of(new BigDecimal("1235.2"), "EUR"),
                Instant.now()
        );
        Money newBalance = Money.of(new BigDecimal("2335.2"), "EUR");
        repository.set(account);

        repository.set(account.getId(), newBalance);

        assertThat(repository.get(account.getId())).isEqualTo(Optional.of(account.ofBalance(newBalance)));
    }

    @Test
    public void setAccountBalanceIsIdempotent() {
        Account account = new Account(
                new AccountId(UUID.randomUUID()),
                Money.of(new BigDecimal("1235.2"), "EUR"),
                Instant.now()
        );
        Money newBalance = Money.of(new BigDecimal("2335.2"), "EUR");
        repository.set(account);

        repository.set(account.getId(), newBalance);
        repository.set(account.getId(), newBalance);

        assertThat(repository.get(account.getId())).isEqualTo(Optional.of(account.ofBalance(newBalance)));
    }
}
