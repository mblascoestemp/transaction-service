package org.mblascoespar.transactionservice.controllers;


import org.mblascoespar.transactionservice.model.Transaction;
import org.mblascoespar.transactionservice.model.TransactionRequestBody;
import org.mblascoespar.transactionservice.services.StatisticsServiceV2;
import org.mblascoespar.transactionservice.validators.TransactionRequestBodyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;

@RestController
public class TransactionController {
    private static Logger log = LoggerFactory.getLogger(TransactionController.class);


    @Value("${statistics.window.size.in.millis}")
    private long window;

    @Autowired
    StatisticsServiceV2 service;

    @RequestMapping(
            value = "/transactions",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity createTransaction(@Valid @RequestBody TransactionRequestBody transaction, BindingResult bindingResult) {
        log.debug("REceived request with {}", transaction);
        Instant startTime = Instant.now();
        TransactionRequestBodyValidator validator = new TransactionRequestBodyValidator(window);
        if (bindingResult.hasErrors()) {
            log.warn("Problem while receiving POST request for /transactions errors: {}");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        //Validate errors produce NO_CONTENT
        validator.validate(transaction, bindingResult);
        if (bindingResult.hasErrors()) {
            log.warn("Transaction rejected. errors: {}", bindingResult.getAllErrors());
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        log.info("New transaction request received: {}", transaction);
        service.addTransaction(new Transaction(transaction));

        log.info("Processed insert of transaction {} in {}", transaction, Duration.between(startTime, Instant.now()).toString());
        return new ResponseEntity(HttpStatus.CREATED);
    }


}