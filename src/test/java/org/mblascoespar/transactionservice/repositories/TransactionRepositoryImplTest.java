package org.mblascoespar.transactionservice.repositories;

import org.junit.Test;
import org.mblascoespar.transactionservice.model.Transaction;
import org.mblascoespar.transactionservice.repository.TransactionRepository;
import org.mblascoespar.transactionservice.repository.TransactionRepositoryImpl;

import java.util.Calendar;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TransactionRepositoryImplTest {

    TransactionRepository transactionRepository = new TransactionRepositoryImpl();

    @Test
    public void insertTransactionSuccessfully() throws Exception {
        

        Transaction transaction = new Transaction(1.0f, Calendar.getInstance().getTimeInMillis());
        transactionRepository.insertOrUpdate(transaction);

        assertThat(transactionRepository.getAll(), contains(transaction));

    }

    @Test(expected = IllegalArgumentException.class)
    public void insertNull() throws Exception {
        

        transactionRepository.insertOrUpdate(null);
        assertThat(transactionRepository.getAll(), empty());
    }

    @Test
    public void insertTwice() throws Exception {
        

        Transaction transaction = new Transaction(1.0f, Calendar.getInstance().getTimeInMillis());
        transactionRepository.insertOrUpdate(transaction);
        transactionRepository.insertOrUpdate(transaction);

        assertThat(transactionRepository.getAll(), hasSize(1));
        assertThat(transactionRepository.getAll(), hasItem(transaction));

    }

    @Test
    public void removeTransactionSuccessfully() throws Exception {
        


        Transaction transaction = new Transaction(1.0f, Calendar.getInstance().getTimeInMillis());
        transactionRepository.insertOrUpdate(transaction);
        transactionRepository.remove(transaction);
        assertThat(transactionRepository.getAll(), hasSize(0));


    }

    @Test
    public void removeNonExistentTransaction() throws Exception {
        

        Transaction transaction = new Transaction(1.0f, Calendar.getInstance().getTimeInMillis());
        transactionRepository.remove(transaction);
        assertThat(transactionRepository.getAll(), hasSize(0));

    }

    @Test
    public void removeNull() throws Exception {
        

        Transaction transaction = new Transaction(1.0f, Calendar.getInstance().getTimeInMillis());
        transactionRepository.remove(transaction);
        assertThat(transactionRepository.getAll(), hasSize(0));

    }

    @Test
    public void getTransactions() throws Exception {
        

        Transaction transaction = new Transaction(1.0f, Calendar.getInstance().getTimeInMillis());
        transactionRepository.insertOrUpdate(transaction);

        assertThat(transactionRepository.getAll(), contains(transaction));
        assertThat(transactionRepository.getAll(), hasSize(1));
    }

    @Test
    public void getEmptyTransactionsMap() throws Exception {
        
        assertThat(transactionRepository.getAll(), hasSize(0));
    }

}
