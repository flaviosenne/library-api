package com.udemy.libraryapi.service;

import com.udemy.libraryapi.api.dto.LoanFilterDto;
import com.udemy.libraryapi.api.resource.BookController;
import com.udemy.libraryapi.domain.entity.Book;
import com.udemy.libraryapi.domain.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoanService {
    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDto filterDTO, Pageable page);

    Page<Loan> getLoansByBook(Book book, Pageable page);

    List<Loan> getAllLateLoans();
}
