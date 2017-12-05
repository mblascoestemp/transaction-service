package org.mblascoespar.transactionservice.controllers;

import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.services.StatisticsService;
import org.mblascoespar.transactionservice.services.StatisticsServiceV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@RestController
public class StatisticsController {
    private static Logger log = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    StatisticsServiceV2 service;

    @RequestMapping(
            value = "/statistics",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Statistics getStatistics() {
        Instant startTime = Instant.now();
        log.debug("Received request");
        Statistics statistics = service.getStatistics(startTime.toEpochMilli());
        log.info("Processed request in {}", Duration.between(startTime,Instant.now()).toString());
        return statistics;
    }
}