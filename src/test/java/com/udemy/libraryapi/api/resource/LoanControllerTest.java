package com.udemy.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udemy.libraryapi.api.dto.LoanDTO;
import com.udemy.libraryapi.api.dto.LoanFilterDto;
import com.udemy.libraryapi.api.dto.ReturnedLoanDto;
import com.udemy.libraryapi.domain.entity.Book;
import com.udemy.libraryapi.domain.entity.Loan;
import com.udemy.libraryapi.exception.BusinessException;
import com.udemy.libraryapi.service.BookService;
import com.udemy.libraryapi.service.LoanService;
import com.udemy.libraryapi.service.LoanServiceTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = LoanController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LoanControllerTest {

    static final String LOAN_API = "/api/loans";
    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("Should make loan when success request")
    void createLoanTest() throws Exception{
        Long id = 1l;

        LoanDTO dto = LoanDTO.builder().isbn("123").email("customer@email.com").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(id).isbn("123").build();
        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));

        Loan loan = Loan.builder().id(id).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
        BDDMockito.given(loanService.save(any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);


        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Should return a error when loan create is fail")
    void invalidIsbnCreateLoanTest() throws Exception{

        LoanDTO dto = LoanDTO.builder().isbn("123").email("customer@email.com").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);


        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));

    }

    @Test
    @DisplayName("Should return a error when book error in loan create is fail")
    void loanedBookErrorOnCreateLoanTest() throws Exception{

        LoanDTO dto = LoanDTO.builder().isbn("123").email("customer@email.com").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));

        BDDMockito.given(loanService.save(any(Loan.class)))
                .willThrow(new BusinessException("Book already loaned"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);


        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));

    }

    @Test
    @DisplayName("Should return a book")
    void returnBookTest() throws Exception{
        ReturnedLoanDto dto = ReturnedLoanDto.builder().returned(true).build();
        Loan loan = Loan.builder().id(1l).build();
        when(loanService.getById(anyLong())).thenReturn(Optional.of(loan));
        String json = new ObjectMapper().writeValueAsString(dto);


        mvc.perform(
                patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isOk());

        verify(loanService, times(1)).update(loan);
    }

    @Test
    @DisplayName("Should return 404 when not found book")
    void NotFoundBookTest() throws Exception{
        ReturnedLoanDto dto = ReturnedLoanDto.builder().returned(true).build();
        when(loanService.getById(anyLong())).thenReturn(Optional.empty());
        String json = new ObjectMapper().writeValueAsString(dto);


        mvc.perform(
                patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Should filter loans")
    void findLoanTest() throws Exception {
        Long id = 1l;

        Book book = Book.builder().id(1l).isbn("321").build();
        Loan loan = LoanServiceTest.createLoan();
        loan.setId(id);
        loan.setBook(book);


        BDDMockito.given(loanService.find(any(LoanFilterDto.class), any(Pageable.class)))
                .willReturn(
                        new PageImpl<Loan>( Arrays.asList(loan),
                                PageRequest.of(0,10),
                                1));

        String queryString = String.format(
                "?isbn=%s&customer=%s&page=0&size=10",
                book.getIsbn(), loan.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform( request )
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

}
