package tenx.banking.transfer.core.repository;

import tenx.banking.transfer.core.entity.AccountId;

import javax.money.MonetaryAmount;

public interface AccountBalanceSetter {
    void set(AccountId id, MonetaryAmount balance);
}
