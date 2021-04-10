package tenx.banking.transfer.core.repository;

import tenx.banking.transfer.core.entity.Account;
import tenx.banking.transfer.core.entity.AccountId;

import java.util.Optional;

public interface AccountGetter {
    Optional<Account> get(AccountId id);
}
