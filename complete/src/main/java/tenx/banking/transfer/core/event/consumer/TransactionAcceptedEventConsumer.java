package tenx.banking.transfer.core.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tenx.banking.transfer.core.entity.Account;
import tenx.banking.transfer.core.entity.AccountId;
import tenx.banking.transfer.core.event.TransactionAcceptedEvent;
import tenx.banking.transfer.core.repository.AccountBalanceSetter;
import tenx.banking.transfer.core.repository.AccountGetter;

import javax.money.MonetaryAmount;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionAcceptedEventConsumer {
    private final AccountGetter accountGetter;
    private final AccountBalanceSetter accountBalanceSetter;

    @EventListener
    public void onEvent(TransactionAcceptedEvent event) {
        log.info("process=transaction_accepted_event_consumer, event={}", event);

        Account source = getAccount(event.getTransaction().getSource());
        Account target = getAccount(event.getTransaction().getTarget());
        MonetaryAmount amount = event.getTransaction().getAmount();

        accountBalanceSetter.set(source.getId(), source.getBalance().subtract(amount));
        accountBalanceSetter.set(target.getId(), target.getBalance().add(amount));
    }

    private Account getAccount(AccountId accountId) {
        return accountGetter.get(accountId)
                .orElseThrow(() -> new IllegalStateException("Account not found. accountId=" + accountId));
    }
}
