package com.udemy.libraryapi.model.repository;

import com.udemy.libraryapi.domain.entity.Book;
import com.udemy.libraryapi.domain.entity.Loan;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.time.LocalDate;
import java.util.List;

import static com.udemy.libraryapi.model.repository.BookRepositoryTest.createNewBook;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
class LoanRepositoryTest {

    @Autowired
    LoanRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("Should verify already exists loan not returned by book")
    void existsByBookAndNotReturned(){
        Loan loan = createAndPersistLoan(LocalDate.now());

        boolean exists = repository.existsByBookAndNotReturned(loan.getBook());

        BDDAssertions.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should search loan by isbn book or customer")
    void findByBookIsbnOrCustomer(){
        Loan loan = createAndPersistLoan(LocalDate.now());

        Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "Fulano", PageRequest.of(0, 10));

        BDDAssertions.assertThat(result.getContent()).hasSize(1);
        BDDAssertions.assertThat(result.getContent()).contains(loan);
        BDDAssertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        BDDAssertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        BDDAssertions.assertThat(result.getTotalElements()).isEqualTo(1);

    }

    @Test
    @DisplayName("Should get loans by loan date smaller three days ago and not returned")
    void findByLoanDateLessThanAndNotReturned(){
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        BDDAssertions.assertThat(result).hasSize(1).contains(loan);
    }

    @Test
    @DisplayName("Should return empty when do not find late loans")
    void notFindByLoanDateLessThanAndNotReturned(){
        createAndPersistLoan(LocalDate.now());

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        BDDAssertions.assertThat(result).isEmpty();
    }

    Loan createAndPersistLoan(LocalDate loanDate){
        Book book= createNewBook("123");
        entityManager.persist(book);

        Loan loan = Loan.builder()
                .book(book)
                .customer("Fulano")
                .loanDate(loanDate)
                .build();
        entityManager.persist(loan);

        return loan;
    }

}
