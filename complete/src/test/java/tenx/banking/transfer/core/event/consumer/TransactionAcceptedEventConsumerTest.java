package tenx.banking.transfer.core.event.consumer;

import lombok.Builder;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tenx.banking.transfer.core.entity.Account;
import tenx.banking.transfer.core.entity.AccountId;
import tenx.banking.transfer.core.entity.Transaction;
import tenx.banking.transfer.core.entity.TransactionId;
import tenx.banking.transfer.core.event.TransactionAcceptedEvent;
import tenx.banking.transfer.core.repository.AccountBalanceSetter;
import tenx.banking.transfer.core.repository.AccountGetter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class TransactionAcceptedEventConsumerTest {
    public static final String CURRENCY = "EUR";
    @Mock
    private AccountGetter accountGetter;

    @Mock
    private AccountBalanceSetter accountBalanceSetter;

    @InjectMocks
    private TransactionAcceptedEventConsumer consumer;

    private static Stream<Arguments> nonExistentAccountCombo() {
        return Stream.of(
                Arguments.of(null, mock(Account.class)),
                Arguments.of(mock(Account.class), null),
                Arguments.of(null, null)
        );
    }

    private static Stream<Arguments> successTransferCombo() {
        return Stream.of(
                Arguments.of(SuccessTransferCase.builder()
                        .balanceAtSource(new BigDecimal("200.00"))
                        .balanceAtTarget(new BigDecimal("100.00"))
                        .transferAmount(new BigDecimal("50.00"))
                        .newBalanceAtSource(new BigDecimal("150.00"))
                        .newBalanceAtTarget(new BigDecimal("150.00"))
                        .build()
                ),
                Arguments.of(SuccessTransferCase.builder()
                        .balanceAtSource(new BigDecimal("43.70"))
                        .balanceAtTarget(new BigDecimal("56.90"))
                        .transferAmount(new BigDecimal("1.37"))
                        .newBalanceAtSource(new BigDecimal("42.33"))
                        .newBalanceAtTarget(new BigDecimal("58.27"))
                        .build()
                ),
                Arguments.of(SuccessTransferCase.builder()
                        .balanceAtSource(new BigDecimal("43.70"))
                        .balanceAtTarget(new BigDecimal("56.90"))
                        .transferAmount(new BigDecimal("43.70"))
                        .newBalanceAtSource(new BigDecimal("0.00"))
                        .newBalanceAtTarget(new BigDecimal("100.6"))
                        .build()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("nonExistentAccountCombo")
    public void exceptionIfSourceAccountNotFound(
            Account sourceAccount,
            Account targetAccount
    ) {
        AccountId sourceAccountId = new AccountId(UUID.randomUUID());
        AccountId targetAccountId = new AccountId(UUID.randomUUID());
        lenient().when(accountGetter.get(sourceAccountId)).thenReturn(Optional.ofNullable(sourceAccount));
        lenient().when(accountGetter.get(targetAccountId)).thenReturn(Optional.ofNullable(targetAccount));

        TransactionAcceptedEvent event = new TransactionAcceptedEvent(
                this,
                new Transaction(
                        new TransactionId(UUID.randomUUID()),
                        sourceAccountId,
                        targetAccountId,
                        Money.of(BigDecimal.TEN, CURRENCY)
                )
        );

        assertThrows(IllegalStateException.class, () -> consumer.onEvent(event));
        verifyNoMoreInteractions(accountBalanceSetter);
    }


    @ParameterizedTest
    @MethodSource("successTransferCombo")
    public void sourceDebitedAndTargetCredited(
            SuccessTransferCase testCase) {
        Account sourceAccount = new Account(new AccountId(UUID.randomUUID()),
                Money.of(testCase.balanceAtSource, CURRENCY),
                Instant.now());
        Account targetAccount = new Account(new AccountId(UUID.randomUUID()),
                Money.of(testCase.balanceAtTarget, CURRENCY),
                Instant.now());
        lenient().when(accountGetter.get(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        lenient().when(accountGetter.get(targetAccount.getId())).thenReturn(Optional.of(targetAccount));

        TransactionAcceptedEvent event = new TransactionAcceptedEvent(
                this,
                new Transaction(
                        new TransactionId(UUID.randomUUID()),
                        sourceAccount.getId(),
                        targetAccount.getId(),
                        Money.of(testCase.transferAmount, CURRENCY)
                )
        );

        consumer.onEvent(event);

        InOrder inOrder = inOrder(accountBalanceSetter);
        inOrder.verify(accountBalanceSetter).set(sourceAccount.getId(), Money.of(testCase.newBalanceAtSource, CURRENCY));
        inOrder.verify(accountBalanceSetter).set(targetAccount.getId(), Money.of(testCase.newBalanceAtTarget, CURRENCY));
        verifyNoMoreInteractions(accountBalanceSetter);
    }

    @Builder
    static class SuccessTransferCase {
        private final BigDecimal balanceAtSource;
        private final BigDecimal balanceAtTarget;
        private final BigDecimal transferAmount;
        private final BigDecimal newBalanceAtSource;
        private final BigDecimal newBalanceAtTarget;
    }
}