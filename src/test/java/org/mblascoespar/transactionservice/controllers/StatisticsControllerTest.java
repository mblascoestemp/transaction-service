package org.mblascoespar.transactionservice.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.services.StatisticsService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(StatisticsController.class)
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Mock
    StatisticsService service;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(service.getStatistics()).thenReturn(new Statistics(0,0,0,0,0));

    }

    @Test
    public void getStatisticsHappyCase() throws Exception {

        this.mvc.perform(get("/statistics")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("count", is(0)))
        .andExpect(jsonPath("timestamp").doesNotExist());
    }

}
