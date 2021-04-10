package tenx.banking.transfer.core.command;

import lombok.Data;
import tenx.banking.transfer.core.entity.Transaction;
import tenx.banking.transfer.core.event.TransactionAcceptedEvent;

@Data
public class TransferFundCommand {
    private final Transaction pendingTransaction;

    public TransactionAcceptedEvent accepted() {
        return new TransactionAcceptedEvent(this, new Transaction(
                pendingTransaction.getId(),
                pendingTransaction.getSource(),
                pendingTransaction.getTarget(),
                pendingTransaction.getAmount()
        ));
    }
}
