package com.udemy.libraryapi.api.resource;

import com.udemy.libraryapi.api.dto.BookDTO;
import com.udemy.libraryapi.api.dto.LoanDTO;
import com.udemy.libraryapi.api.dto.LoanFilterDto;
import com.udemy.libraryapi.api.dto.ReturnedLoanDto;
import com.udemy.libraryapi.domain.entity.Book;
import com.udemy.libraryapi.domain.entity.Loan;
import com.udemy.libraryapi.service.BookService;
import com.udemy.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService service;
    private final ModelMapper model;
    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody @Valid LoanDTO dto){
        Book book = bookService.getBookByIsbn(dto.getIsbn())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Book not found for passed isbn"));

        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = service.save(entity);

        return entity.getId();
    }

    @PatchMapping("{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDto dto){
        Loan loan = service.getById(id).orElseThrow(()->
                new ResponseStatusException(HttpStatus.NOT_FOUND));

        loan.setReturned(dto.getReturned());
        service.update(loan);

    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDto dto, Pageable page){
        Page<Loan> result = service.find(dto, page);
        List<LoanDTO> loans = result
                .getContent()
                .stream()
                .map(entity -> {

                    Book book = entity.getBook();
                    BookDTO bookDto = model.map(book, BookDTO.class);
                    LoanDTO loanDto =  model.map(entity, LoanDTO.class);
                    loanDto.setBook(bookDto);

                    return loanDto;
                }).collect(Collectors.toList());

        return new PageImpl<LoanDTO>(loans, page, result.getTotalElements());

    }

}
