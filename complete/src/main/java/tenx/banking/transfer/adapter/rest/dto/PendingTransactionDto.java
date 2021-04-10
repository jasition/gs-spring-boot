package tenx.banking.transfer.adapter.rest.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.javamoney.moneta.Money;
import tenx.banking.transfer.core.command.TransferFundCommand;
import tenx.banking.transfer.core.entity.AccountId;
import tenx.banking.transfer.core.entity.Transaction;
import tenx.banking.transfer.core.entity.TransactionId;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@ToString
public class PendingTransactionDto {
    @NotNull
    private UUID sourceAccountId;
    @NotNull
    private UUID targetAccountId;
    @NotNull
    private String amount;
    @NotNull
    private String currency;

    public TransferFundCommand toCommand(UUID transactionId) {
        return new TransferFundCommand(new Transaction(
                new TransactionId(transactionId),
                new AccountId(sourceAccountId),
                new AccountId(targetAccountId),
                Money.of(new BigDecimal(amount),
                        currency)
        ));
    }
}
