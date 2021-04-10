package tenx.banking.transfer.core.entity;

import lombok.Data;
import lombok.NonNull;

import javax.money.MonetaryAmount;

@Data
public class Transaction {
    @NonNull
    private final TransactionId id;
    @NonNull
    private final AccountId source;
    @NonNull
    private final AccountId target;
    @NonNull
    private final MonetaryAmount amount;

}
