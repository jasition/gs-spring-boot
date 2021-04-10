package tenx.banking.transfer.core.entity;

import lombok.Data;
import lombok.NonNull;

import javax.money.MonetaryAmount;
import java.time.Instant;

@Data
public class Account {
    @NonNull
    private final AccountId id;
    @NonNull
    private final MonetaryAmount balance;
    @NonNull
    private final Instant createdAt;

    public Account ofBalance(MonetaryAmount newBalance) {
        return new Account(id, newBalance, createdAt);
    }
}
