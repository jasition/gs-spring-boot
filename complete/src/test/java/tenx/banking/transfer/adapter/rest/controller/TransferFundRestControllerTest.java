package tenx.banking.transfer.adapter.rest.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import tenx.banking.transfer.adapter.rest.dto.PendingTransactionDto;
import tenx.banking.transfer.core.command.TransferFundCommand;
import tenx.banking.transfer.core.command.validator.TransferFundCommandValidator;
import tenx.banking.transfer.core.command.validator.TransferFundCommandValidator.ValidationResult;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static tenx.banking.Application.FIRST_TESTING_ACCOUNT_ID;
import static tenx.banking.Application.SECOND_TESTING_ACCOUNT_ID;
import static tenx.banking.transfer.core.command.validator.TransferFundCommandValidator.ResultCode;

@ExtendWith(MockitoExtension.class)
class TransferFundRestControllerTest {
    @Mock
    private TransferFundCommandValidator validator;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private ApplicationEvent event1;

    @Mock
    private ApplicationEvent event2;

    @InjectMocks
    private TransferFundRestController controller;

    @Test
    public void publishTransactionAcceptedEvent() {
        PendingTransactionDto transaction = new PendingTransactionDto();
        transaction.setSourceAccountId(FIRST_TESTING_ACCOUNT_ID.getId());
        transaction.setTargetAccountId(SECOND_TESTING_ACCOUNT_ID.getId());
        transaction.setAmount("20.2");
        transaction.setCurrency("EUR");

        when(validator.validate(any(TransferFundCommand.class)))
                .thenReturn(new ValidationResult(ResultCode.ACCEPTED, event1, event2));

        controller.createPendingTransaction(UUID.randomUUID(), transaction);

        verify(publisher).publishEvent(event1);
        verify(publisher).publishEvent(event2);
        verifyNoMoreInteractions(publisher);
    }
}
