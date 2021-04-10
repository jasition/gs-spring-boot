package tenx.banking.transfer.core.command.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tenx.banking.transfer.core.command.TransferFundCommand;
import tenx.banking.transfer.core.entity.Account;
import tenx.banking.transfer.core.event.TransactionAcceptedEvent;
import tenx.banking.transfer.core.repository.AccountGetter;

import javax.money.MonetaryAmount;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransferFundCommandValidator {
    private final ApplicationEventPublisher publisher;
    private final AccountGetter accountGetter;

    public Result validate(TransferFundCommand command) {
        if (isSameAccount(command)) return Result.SAME_ACCOUNT;

        Optional<Account> source = accountGetter.get(command.getPendingTransaction().getSource());
        Optional<Account> target = accountGetter.get(command.getPendingTransaction().getTarget());
        if (!source.isPresent() || !target.isPresent()) {
            return Result.ACCOUNT_DO_NOT_EXIST;
        }

        if (hasEnoughBalance(command, source.get())) {
            TransactionAcceptedEvent event = command.accepted();
            publisher.publishEvent(event);

            return Result.ACCEPTED;
        }
        return Result.INSUFFICIENT_BALANCE_AT_SOURCE;
    }

    private boolean hasEnoughBalance(TransferFundCommand command, Account account) {
        MonetaryAmount amount = command.getPendingTransaction().getAmount();
        MonetaryAmount balance = account.getBalance();

        if (!amount.getCurrency().equals(balance.getCurrency())) {
            throw new UnsupportedOperationException("International fund transfer not supported yet");
        }

        return balance.subtract(amount).isPositiveOrZero();
    }

    private boolean isSameAccount(TransferFundCommand command) {
        return command.getPendingTransaction().getSource().equals(
                command.getPendingTransaction().getTarget());
    }

    public enum Result {
        ACCEPTED,
        INSUFFICIENT_BALANCE_AT_SOURCE,
        SAME_ACCOUNT,
        ACCOUNT_DO_NOT_EXIST
    }
}
