package tenx.banking.transfer.core.command.validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import tenx.banking.transfer.core.command.TransferFundCommand;
import tenx.banking.transfer.core.entity.Account;
import tenx.banking.transfer.core.repository.AccountGetter;

import javax.money.MonetaryAmount;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransferFundCommandValidator {
    private final AccountGetter accountGetter;

    public ValidationResult validate(TransferFundCommand command) {
        if (isSameAccount(command)) return new ValidationResult(ResultCode.SAME_ACCOUNT);

        Optional<Account> source = accountGetter.get(command.getPendingTransaction().getSource());
        Optional<Account> target = accountGetter.get(command.getPendingTransaction().getTarget());
        if (!source.isPresent() || !target.isPresent()) {
            return new ValidationResult(ResultCode.ACCOUNT_DO_NOT_EXIST);
        }

        if (hasEnoughBalance(command, source.get())) {
            return new ValidationResult(ResultCode.ACCEPTED, command.accepted());
        }
        return new ValidationResult(ResultCode.INSUFFICIENT_BALANCE_AT_SOURCE);
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

    @Getter
    public static class ValidationResult {
        private final ResultCode resultCode;
        private final List<ApplicationEvent> events;

        public ValidationResult(ResultCode resultCode,
                                 ApplicationEvent... events) {
            this.resultCode = resultCode;
            this.events = Arrays.asList(events);
        }
    }

    public enum ResultCode {
        ACCEPTED,
        INSUFFICIENT_BALANCE_AT_SOURCE,
        SAME_ACCOUNT,
        ACCOUNT_DO_NOT_EXIST
    }
}
