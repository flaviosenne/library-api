package com.udemy.libraryapi.api.resource;

import com.udemy.libraryapi.api.dto.BookDTO;
import com.udemy.libraryapi.api.dto.LoanDTO;
import com.udemy.libraryapi.domain.entity.Book;
import com.udemy.libraryapi.domain.entity.Loan;
import com.udemy.libraryapi.service.BookService;
import com.udemy.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@Api("Book API")
@Slf4j
public class BookController {

    private BookService service;
    private ModelMapper modelMapper;
    private LoanService loanService;

    public BookController(BookService service, ModelMapper modelMapper, LoanService loanService){
        this.service = service;
        this.modelMapper = modelMapper;
        this.loanService = loanService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("CREATE A BOOK")
    public BookDTO create(@RequestBody @Valid BookDTO dto){
        log.info("create a book for isbn: {} ", dto.getIsbn());
        Book entity = modelMapper.map(dto, Book.class);

        entity = service.save(entity);

        return modelMapper.map(entity, BookDTO.class);
    }

    @ApiOperation("OBTAINS A BOOK DETAILS BY ID")
    @GetMapping("{id}")
    public BookDTO get(@PathVariable(value = "id") Long id){
        log.info("obtaining details for book id: {} ", id);
        return service.getById(id)
                .map( book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    @ApiOperation("DELETE A BOOK BY ID")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Book successfully deleted")
    })
    public void delete(@PathVariable Long id){
        log.info("delete book of id: {} ", id);
        Book book = service.getById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));

        service.delete(book);

    }

    @PutMapping("{id}")
    @ApiOperation("UPDATE A BOOK")
    public BookDTO update(@PathVariable Long id, @RequestBody BookDTO dto){
        log.info("update book of id: {} ", id);
        return service.getById(id)
            .map(book -> {

                book.setAuthor(dto.getAuthor());
                book.setTitle(dto.getTitle());
                book = service.update(book);
                return modelMapper.map(book, BookDTO.class);

            })
            .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    @GetMapping
    @ApiOperation("FIND BOOKS BY PARAMS")
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest){
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pageRequest);
        List<BookDTO> list = result.getContent().stream()
                .map( entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable page){
        Book book = service.getById(id).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Page<Loan> result = loanService.getLoansByBook(book, page);

        List<LoanDTO> list = result.getContent()
                .stream()
                .map(loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());

        return new PageImpl<>(list, page, result.getTotalElements());

    }
}
