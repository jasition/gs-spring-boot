package tenx.banking.transfer.adapter.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tenx.banking.transfer.adapter.rest.dto.PendingTransactionDto;
import tenx.banking.transfer.util.DtoToJson;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tenx.banking.Application.FIRST_TESTING_ACCOUNT_ID;
import static tenx.banking.Application.SECOND_TESTING_ACCOUNT_ID;

@SpringBootTest
@AutoConfigureMockMvc
public class TransferFundRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void putPendingTransaction() throws Exception {
        PendingTransactionDto transaction = new PendingTransactionDto();
        transaction.setSourceAccountId(FIRST_TESTING_ACCOUNT_ID.getId());
        transaction.setTargetAccountId(SECOND_TESTING_ACCOUNT_ID.getId());
        transaction.setAmount("20.2");
        transaction.setCurrency("EUR");

        mvc.perform(put("/transactions/transaction/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DtoToJson.convert(transaction))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}
