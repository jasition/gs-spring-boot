package tenx.banking.transfer.core.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import tenx.banking.transfer.core.entity.Transaction;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class TransactionAcceptedEvent extends ApplicationEvent {
    private final Transaction transaction;

    public TransactionAcceptedEvent(Object source, Transaction transaction) {
        super(source);

        this.transaction = transaction;
    }
}
