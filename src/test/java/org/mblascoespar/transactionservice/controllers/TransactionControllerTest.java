package org.mblascoespar.transactionservice.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mvc;


    @Test
    public void createTransactionHappyCase() throws Exception {

        final String requestBodyJson = String.format("{\"amount\":%f,\"timestamp\":%d}",100.0f, Instant.now().toEpochMilli());
        testRequest(requestBodyJson).andExpect(status().isCreated());
    }

    @Test
    public void createTransactionMissingAmount() throws Exception {

        final String requestBodyJson = String.format("{\"timestamp\":%d}", Instant.now().toEpochMilli());
        testRequest(requestBodyJson).andExpect(status().isBadRequest());
    }

    @Test
    public void createTransactionMissingTimestamp() throws Exception {

        final String requestBodyJson = String.format("\"timestamp\":%d}",Instant.now().toEpochMilli());
        testRequest(requestBodyJson).andExpect(status().isBadRequest());
    }

    @Test
    public void createTransactionEmptyBody() throws Exception {

        final String requestBodyJson = String.format("{}",100.0f,Instant.now().toEpochMilli());
        testRequest(requestBodyJson).andExpect(status().isBadRequest());
    }

    @Test
    public void createTransactionAmountWrongType() throws Exception {

        final String requestBodyJson = String.format("{\"amount\":\"%s\",\"timestamp\":%d}","oops",Instant.now().toEpochMilli());
        testRequest(requestBodyJson).andExpect(status().isBadRequest());
    }

    @Test
    public void createTransactionTimestampWrongType() throws Exception {

        final String requestBodyJson = String.format("{\"amount\":%f,\"timestamp\":\"%s\"}",100.0f,"oops");
        testRequest(requestBodyJson).andExpect(status().isBadRequest());
    }

    @Test
    public void createTransactionIgnoreAdditionalFields() throws Exception {

        final String requestBodyJson = String.format("{\"amount\":%f,\"timestamp\":%d,\"other\":\"value\"}",100.0f,Instant.now().toEpochMilli());
        testRequest(requestBodyJson).andExpect(status().isCreated());
    }

    @Test
    public void createTransactionTimestampFromTheFuture() throws Exception {

        final String requestBodyJson = String.format("{\"amount\":%f,\"timestamp\":\"%s\"}", 100.0f, Instant.now().toEpochMilli()+10000L);
        testRequest(requestBodyJson).andExpect(status().isNoContent());
    }

    @Test
    public void createTransactionNegativeTimestamp() throws Exception {

        final String requestBodyJson = String.format("{\"amount\":%f,\"timestamp\":\"%s\"}", 100.0f, -3695);
        testRequest(requestBodyJson).andExpect(status().isNoContent());
    }

    @Test
    public void createTransactionTimestampTooOldAfter60Seconds() throws Exception {
        final String requestBodyJson = String.format("{\"amount\":%f,\"timestamp\":%d,\"other\":\"value\"}",100.0f, Instant.now().minusMillis(66666666).toEpochMilli());
        testRequest(requestBodyJson).andExpect(status().isNoContent());
    }

    @Test
    public void createTransactionZeroAmount() throws Exception {

        final String requestBodyJson = String.format("{\"amount\":%f,\"timestamp\":\"%s\"}",0.0f,Instant.now().toEpochMilli());
        testRequest(requestBodyJson).andExpect(status().isCreated());
    }

        private ResultActions testRequest(String requestBodyJson) throws Exception {
            return this.mvc.perform(post("/transactions").content(requestBodyJson).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE));
        }


}
