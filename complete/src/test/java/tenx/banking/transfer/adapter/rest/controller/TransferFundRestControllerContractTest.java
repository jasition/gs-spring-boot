package tenx.banking.transfer.adapter.rest.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tenx.banking.transfer.adapter.rest.dto.PendingTransactionDto;
import tenx.banking.transfer.core.command.TransferFundCommand;
import tenx.banking.transfer.core.command.validator.TransferFundCommandValidator;
import tenx.banking.transfer.core.command.validator.TransferFundCommandValidator.ValidationResult;
import tenx.banking.transfer.util.DtoToJson;

import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tenx.banking.transfer.core.command.validator.TransferFundCommandValidator.ResultCode;

@SpringBootTest
@AutoConfigureMockMvc
public class TransferFundRestControllerContractTest {
    private static final String PUT_TRANSACTION = "/transactions/transaction/";

    @MockBean
    private TransferFundCommandValidator validator;

    @Autowired
    private MockMvc mvc;

    private static Stream<Arguments> malformedPayloads() {
        return Stream.of(
                Arguments.of("random stuff"),
                Arguments.of("{\"malformed\":\"a400e07e-ffe9-4d7d-ad87-0168a12056d7\"}"),
                Arguments.of("{\"sourceAccountId\":\"05771619-af46-44e6-9168-639f6ba9d77e\",\"targetAccountId\":\"4bee3e87-487d-4446-955a-0a9035f56bf7\",\"amount\":\"sagasd\",\"currency\":\"EUR\"}"),
                Arguments.of("{\"sourceAccountId\":\"05771619ba9d77e\",\"targetAccountId\":\"4bee3e87-487d-4446-955a-0a9035f56bf7\",\"amount\":\"25.0\",\"currency\":\"EUR\"}"),
                Arguments.of("{\"sourceAccountId\":\"05771619-af46-44e6-9168-639f6ba9d77e\",\"targetAccountId\":\"4bee3e87-48-0a9035f56bf7\",\"amount\":\"25.0\",\"currency\":\"EUR\"}")
        );
    }

    @ParameterizedTest
    @MethodSource("malformedPayloads")
    public void return400ForMalformedPayload() throws Exception {
        mvc.perform(put(PUT_TRANSACTION + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"malformed\":\"a400e07e-ffe9-4d7d-ad87-0168a12056d7\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void return400ForMalformedTransactionId() throws Exception {
        mvc.perform(put("/transactions/transaction/malformed")
                .contentType(MediaType.APPLICATION_JSON)
                .content(DtoToJson.convert(getPendingTransactionDto()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void return415ForUnsupportedMediaType() throws Exception {
        mvc.perform(put("/transactions/transaction/" + UUID.randomUUID())
                .contentType(MediaType.TEXT_PLAIN)
                .content(DtoToJson.convert(getPendingTransactionDto()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void return422WhenSourceAndTargetAccountAreIdentical() throws Exception {
        when(validator.validate(any(TransferFundCommand.class)))
                .thenReturn(new ValidationResult(ResultCode.SAME_ACCOUNT));

        mvc.perform(put("/transactions/transaction/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DtoToJson.convert(getPendingTransactionDto()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void return402WhenSourceAccountHasInsufficentFund() throws Exception {
        when(validator.validate(any(TransferFundCommand.class)))
                .thenReturn(new ValidationResult(ResultCode.INSUFFICIENT_BALANCE_AT_SOURCE));

        mvc.perform(put("/transactions/transaction/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DtoToJson.convert(getPendingTransactionDto()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPaymentRequired());
    }

    @Test
    public void return422WhenAccountDoNotExist() throws Exception {
        when(validator.validate(any(TransferFundCommand.class)))
                .thenReturn(new ValidationResult(ResultCode.ACCOUNT_DO_NOT_EXIST));

        mvc.perform(put("/transactions/transaction/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DtoToJson.convert(getPendingTransactionDto()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void return201WhenTransactionAccepted() throws Exception {
        when(validator.validate(any(TransferFundCommand.class)))
                .thenReturn(new ValidationResult(ResultCode.ACCEPTED));

        mvc.perform(put("/transactions/transaction/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DtoToJson.convert(getPendingTransactionDto()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    private PendingTransactionDto getPendingTransactionDto() {
        PendingTransactionDto transaction = new PendingTransactionDto();
        transaction.setSourceAccountId(UUID.randomUUID());
        transaction.setTargetAccountId(UUID.randomUUID());
        transaction.setAmount("20.2");
        transaction.setCurrency("EUR");
        return transaction;
    }
}
