package com.udemy.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udemy.libraryapi.api.dto.BookDTO;
import com.udemy.libraryapi.domain.entity.Book;
import com.udemy.libraryapi.exception.BusinessException;
import com.udemy.libraryapi.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Should create a book when is success.")
    void createBookTest() throws Exception{

        BookDTO dto = createNewBook();

        Book savedBook = Book.builder()
                .id(10l)
                .author("Joao")
                .title("As aventuras")
                .isbn("001")
                .build();

        BDDMockito.given(service.save(any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);


        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").isNotEmpty())
                .andExpect( MockMvcResultMatchers.jsonPath("title").value(dto.getTitle()))
                .andExpect( MockMvcResultMatchers.jsonPath("author").value(dto.getAuthor()))
                .andExpect( MockMvcResultMatchers.jsonPath("isbn").value(dto.getIsbn()));

    }


    @Test
    @DisplayName("Should throws bad request exception when don't exists data valid.")
    void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);


        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Should throw error exception when duplicated isbn is provider")
    void createBookWithDuplicatedIsbn() throws Exception{
        BookDTO dto = createNewBook();

        String json = new ObjectMapper().writeValueAsString(dto);

        String messageError = "Isbn já cadastrado.";
        BDDMockito.given(service.save(any()))
                .willThrow(new BusinessException(messageError));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform( request )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(messageError));


    }

    @Test
    @DisplayName("Should get book detail")
    void getBookDetail() throws Exception{
        // cenario (given)
        Long id = 1l;

        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        // execução (when)

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // verificação

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
                .andExpect( MockMvcResultMatchers.jsonPath("title").value(createNewBook().getTitle()))
                .andExpect( MockMvcResultMatchers.jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect( MockMvcResultMatchers.jsonPath("isbn").value(createNewBook().getIsbn()));

    }

    @Test
    @DisplayName("Should return not found exception when book not found in DB")
    void bookNotFoundTest() throws Exception{

        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete a book")
    void deleteBookTest() throws Exception{

        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.of(Book.builder().id(1l).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1));

        mvc.perform( request )
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Should return resource not found when do not exists book in DB")
    void deleteNotExistBookTest() throws Exception{

        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1));

        mvc.perform( request )
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Should update a book")
    void updateBookTest() throws Exception {
        Long id = 1l;
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book bookUpdate = Book.builder().id(id).title("some title").author("some author").isbn("123").build();

        Book updatedBook = Book.builder()
                .id(1l)
                .author("Joao")
                .title("As aventuras")
                .isbn("123")
                .build();

        BDDMockito.given( service.getById(anyLong()) )
                .willReturn( Optional.of(bookUpdate) );
        BDDMockito.given( service.update( bookUpdate ))
                .willReturn( updatedBook );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform( request )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
                .andExpect( MockMvcResultMatchers.jsonPath("title").value(createNewBook().getTitle()))
                .andExpect( MockMvcResultMatchers.jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect( MockMvcResultMatchers.jsonPath("isbn").value("123"));
        ;
    }

    @Test
    @DisplayName("Should return 404 when book do not exists in DB")
    void updateNotExistBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given( service.getById(anyLong()) )
                .willReturn( Optional.empty() );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform( request )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should filter books")
    void findBookTest() throws Exception {
        Long id = 1l;

        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(
                        new PageImpl<Book>( Arrays.asList(book),
                        PageRequest.of(0,100),
                        1));

        String queryString = String.format(
                "?title=%s&author=%s&page=0&size=100",
                book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform( request )
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

    private BookDTO createNewBook(){
        return BookDTO.builder()
                .author("Joao")
                .title("As aventuras")
                .isbn("001")
                .build();
    }

}
