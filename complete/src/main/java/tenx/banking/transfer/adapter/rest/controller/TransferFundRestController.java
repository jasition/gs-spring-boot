package tenx.banking.transfer.adapter.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tenx.banking.transfer.adapter.rest.dto.PendingTransactionDto;
import tenx.banking.transfer.core.command.TransferFundCommand;
import tenx.banking.transfer.core.command.validator.TransferFundCommandValidator;
import tenx.banking.transfer.core.command.validator.TransferFundCommandValidator.Result;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransferFundRestController {
    private final TransferFundCommandValidator validator;

    @PutMapping(
            value = "/transactions/transaction/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> createPendingTransaction(
            @PathVariable(value = "id", required = true) UUID transactionId,
            @Valid @RequestBody PendingTransactionDto pendingTransaction
    ) {
        log.info("process=put_pending_transaction, status=started, transactionId={}, pendingTransaction={}",
                transactionId, pendingTransaction);

        TransferFundCommand command = pendingTransaction.toCommand(transactionId);
        Result result = validator.validate(command);
        HttpStatus status = map(result);

        log.info("process=put_pending_transaction, status={}, transactionId={}, pendingTransaction={}", status,
                transactionId, pendingTransaction);
        return new ResponseEntity<>(null, status);
    }

    private HttpStatus map(Result result) {
        switch (result) {
            case ACCEPTED:
                return HttpStatus.CREATED;
            case SAME_ACCOUNT:
                return HttpStatus.UNPROCESSABLE_ENTITY;
            case ACCOUNT_DO_NOT_EXIST:
                return HttpStatus.NOT_FOUND;
            case INSUFFICIENT_BALANCE_AT_SOURCE:
                return HttpStatus.PAYMENT_REQUIRED;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
