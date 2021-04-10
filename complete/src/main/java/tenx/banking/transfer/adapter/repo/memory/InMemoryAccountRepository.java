package tenx.banking.transfer.adapter.repo.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tenx.banking.transfer.core.entity.Account;
import tenx.banking.transfer.core.entity.AccountId;
import tenx.banking.transfer.core.repository.AccountBalanceSetter;
import tenx.banking.transfer.core.repository.AccountGetter;
import tenx.banking.transfer.core.repository.AccountSetter;

import javax.money.MonetaryAmount;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class InMemoryAccountRepository
        implements AccountGetter, AccountBalanceSetter, AccountSetter {
    private final ConcurrentMap<AccountId, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void set(AccountId id, MonetaryAmount balance) {
        Account result = accounts.computeIfPresent(
                id,
                (accountId, account) -> account.ofBalance(balance)
        );
        log.info("process=set_account_balance, accountId={}, balance={}, result={}", id, balance, result);
    }

    @Override
    public Optional<Account> get(AccountId id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public void set(Account account) {
        Account previous = accounts.putIfAbsent(account.getId(), account);
        log.info("process=set_account, previous={}, account={}", previous, account);
    }
}
