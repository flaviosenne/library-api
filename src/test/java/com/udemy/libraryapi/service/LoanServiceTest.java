package com.udemy.libraryapi.service;

import com.udemy.libraryapi.api.dto.LoanFilterDto;
import com.udemy.libraryapi.domain.entity.Book;
import com.udemy.libraryapi.domain.entity.Loan;
import com.udemy.libraryapi.exception.BusinessException;
import com.udemy.libraryapi.model.repository.LoanRepository;
import com.udemy.libraryapi.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    @MockBean
    LoanRepository repository;

    LoanService service;

    @BeforeEach
    void setup(){
        service = new LoanServiceImpl(repository);
    }


    @Test
    @DisplayName("Should save a loan")
    void saveLoanTest(){

        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan = createLoan();

        Loan savedLoan = Loan.builder()
                .id(1l)
                .book(book)
                .loanDate(LocalDate.now())
                .customer(customer)
                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(false);

        when(repository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan= service.save(savingLoan);

        BDDAssertions.assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        BDDAssertions.assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        BDDAssertions.assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        BDDAssertions.assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Should throw exception when save a loan already loan")
    void saveLoanFailTest(){

        ;Book book = Book.builder().id(1l).build();

        Loan savingLoan = createLoan();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() ->service.save(savingLoan));

        BDDAssertions.assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        verify(repository, never()).save(savingLoan);
    }

    @Test
    @DisplayName("Should get information of loan by id")
    void getLoanDetailsTest(){
        Long id = 1l;
        Loan loan = createLoan();
        loan.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = service.getById(id);

        BDDAssertions.assertThat(result).isPresent();
        BDDAssertions.assertThat(result.get().getId()).isEqualTo(id);
        BDDAssertions.assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        BDDAssertions.assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        BDDAssertions.assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Should update loan")
    void updateLoanTest(){
        Loan loan = createLoan();
        loan.setId(1l);
        loan.setReturned(true);

        when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        BDDAssertions.assertThat(updatedLoan.getReturned()).isTrue();
        verify(repository).save(loan);
    }


    @Test
    @DisplayName("Should filter loans by properties")
    void findLoanTest(){
        LoanFilterDto loanFilterDto = LoanFilterDto.builder()
                .customer("Fulano")
                .isbn("321")
                .build();


        Loan loan = createLoan();
        loan.setId(1l);
        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Loan> list = Collections.singletonList(loan);

        Page<Loan> page = new PageImpl<Loan>(list ,pageRequest,list.size());

        Mockito.when(repository.findByBookIsbnOrCustomer(
                anyString(), anyString(), any(PageRequest.class)))
                .thenReturn(page);

        Page<Loan> result = service.find(loanFilterDto, pageRequest);

        BDDAssertions.assertThat(result.getTotalElements()).isEqualTo(1);
        BDDAssertions.assertThat(result.getContent()).isEqualTo( list );
        BDDAssertions.assertThat(result.getPageable().getPageNumber()).isEqualTo( 0 );
        BDDAssertions.assertThat(result.getPageable().getPageSize()).isEqualTo( 10 );

    }


    public static Loan createLoan(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        return Loan.builder()
                .book(book)
                .loanDate(LocalDate.now())
                .customer(customer)
                .build();
    }
}
