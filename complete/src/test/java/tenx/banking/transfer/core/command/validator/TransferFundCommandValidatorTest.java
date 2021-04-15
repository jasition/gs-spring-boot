package tenx.banking.transfer.core.command.validator;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tenx.banking.transfer.core.command.TransferFundCommand;
import tenx.banking.transfer.core.command.validator.TransferFundCommandValidator.ResultCode;
import tenx.banking.transfer.core.command.validator.TransferFundCommandValidator.ValidationResult;
import tenx.banking.transfer.core.entity.Account;
import tenx.banking.transfer.core.entity.AccountId;
import tenx.banking.transfer.core.entity.Transaction;
import tenx.banking.transfer.core.entity.TransactionId;
import tenx.banking.transfer.core.repository.AccountGetter;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferFundCommandValidatorTest {
    @Mock
    private AccountGetter accountGetter;

    @InjectMocks
    private TransferFundCommandValidator validator;

    private static Stream<Arguments> nonExistentAccountCombo() {
        return Stream.of(
                Arguments.of(null, mock(Account.class)),
                Arguments.of(mock(Account.class), null),
                Arguments.of(null, null)
        );
    }

    private static Stream<Arguments> insufficientFundAtSourceCombo() {
        return Stream.of(
                Arguments.of(new BigDecimal("200"), new BigDecimal("400")),
                Arguments.of(new BigDecimal("200"), new BigDecimal("201")),
                Arguments.of(new BigDecimal("200"), new BigDecimal("200.01")),
                Arguments.of(new BigDecimal("200"), new BigDecimal("201")),
                Arguments.of(new BigDecimal("0"), new BigDecimal("1")),
                Arguments.of(new BigDecimal("0"), new BigDecimal("0.01"))
        );
    }

    private static Stream<Arguments> multiCurrencyCombo() {
        return Stream.of(
                Arguments.of("EUR", "GBP"),
                Arguments.of("EUR", "USD"),
                Arguments.of("USD", "GBP"),
                Arguments.of("CHE", "GBP"),
                Arguments.of("EUR", "CHE")
        );
    }

    private static Stream<Arguments> successTransferCombo() {
        return Stream.of(
                Arguments.of(new BigDecimal("3072.57"), new BigDecimal("3072.57")),
                Arguments.of(new BigDecimal("255.29"), new BigDecimal("23.2")),
                Arguments.of(new BigDecimal("32.0"), new BigDecimal("15.7"))
        );
    }

    @Test
    public void detectSameAccountTransaction() {
        AccountId sameAccount = new AccountId(UUID.randomUUID());
        TransferFundCommand command = new TransferFundCommand(new Transaction(
                new TransactionId(UUID.randomUUID()),
                sameAccount,
                sameAccount,
                Money.of(new BigDecimal("22.3"), "EUR")
        ));

        ValidationResult result = validator.validate(command);
        assertThat(result.getResultCode()).isEqualTo(ResultCode.SAME_ACCOUNT);
        assertThat(result.getEvents()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("nonExistentAccountCombo")
    public void detectNonExistentAccounts(
            Account sourceAccount,
            Account targetAccount
    ) {
        AccountId sourceAccountId = new AccountId(UUID.randomUUID());
        AccountId targetAccountId = new AccountId(UUID.randomUUID());
        when(accountGetter.get(sourceAccountId)).thenReturn(Optional.ofNullable(sourceAccount));
        when(accountGetter.get(targetAccountId)).thenReturn(Optional.ofNullable(targetAccount));

        TransferFundCommand command = new TransferFundCommand(new Transaction(
                new TransactionId(UUID.randomUUID()),
                sourceAccountId,
                targetAccountId,
                Money.of(new BigDecimal("22.3"), "EUR")
        ));

        ValidationResult result = validator.validate(command);
        assertThat(result.getResultCode()).isEqualTo(ResultCode.ACCOUNT_DO_NOT_EXIST);
        assertThat(result.getEvents()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("insufficientFundAtSourceCombo")
    public void detectInsufficientFundAtSource(
            BigDecimal balance,
            BigDecimal transferAmount
    ) {
        AccountId sourceAccountId = mockAccount(Money.of(balance, "EUR"));
        AccountId targetAccountId = mockAccount(Money.of(BigDecimal.ZERO, "EUR"));

        TransferFundCommand command = new TransferFundCommand(new Transaction(
                new TransactionId(UUID.randomUUID()),
                sourceAccountId,
                targetAccountId,
                Money.of(transferAmount, "EUR")
        ));

        ValidationResult result = validator.validate(command);
        assertThat(result.getResultCode()).isEqualTo(ResultCode.INSUFFICIENT_BALANCE_AT_SOURCE);
        assertThat(result.getEvents()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("multiCurrencyCombo")
    public void doNotSupportMultiCurrencyTransfer(
            String currencyOfBalance,
            String currencyOfTransaction
    ) {
        AccountId sourceAccountId = mockAccount(Money.of(new BigDecimal("200"), currencyOfBalance));
        AccountId targetAccountId = mockAccount(Money.of(BigDecimal.ZERO, currencyOfTransaction));

        TransferFundCommand command = new TransferFundCommand(new Transaction(
                new TransactionId(UUID.randomUUID()),
                sourceAccountId,
                targetAccountId,
                Money.of(new BigDecimal("15.5"), currencyOfTransaction)
        ));

        assertThrows(UnsupportedOperationException.class, () -> validator.validate(command));
    }

    @Test
    public void acceptTransferBetweenTwoAccounts() {
        AccountId sourceAccountId = mockAccount(Money.of(new BigDecimal("200"), "EUR"));
        AccountId targetAccountId = mockAccount(Money.of(BigDecimal.ZERO, "EUR"));

        TransferFundCommand command = new TransferFundCommand(new Transaction(
                new TransactionId(UUID.randomUUID()),
                sourceAccountId,
                targetAccountId,
                Money.of(new BigDecimal("15.5"), "EUR")
        ));

        ValidationResult result = validator.validate(command);
        assertThat(result.getResultCode()).isEqualTo(ResultCode.ACCEPTED);
        assertThat(result.getEvents()).containsExactly(command.accepted());
    }

    private AccountId mockAccount(MonetaryAmount balance) {
        AccountId sourceAccountId = new AccountId(UUID.randomUUID());
        Account sourceAccount = mock(Account.class);
        when(accountGetter.get(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        lenient().when(sourceAccount.getBalance()).thenReturn(balance);
        return sourceAccountId;
    }
}
