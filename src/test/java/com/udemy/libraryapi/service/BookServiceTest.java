package com.udemy.libraryapi.service;

import com.udemy.libraryapi.domain.entity.Book;
import com.udemy.libraryapi.exception.BusinessException;
import com.udemy.libraryapi.model.repository.BookRepository;
import com.udemy.libraryapi.service.impl.BookServiceImpl;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    void setup(){
        this.service = new BookServiceImpl(repository);
    }

    Book createBook(){
        return Book.builder()
                .isbn("123")
                .author("Fulano")
                .title("As aventuras")
                .build();
    }
    @Test
    @DisplayName("Should save a book")
    void saveBookTest(){
        Book book =  createBook();
        when(repository.existsByIsbn(anyString())).thenReturn(false);

        when(repository.save(book)).thenReturn(Book.builder()
                .id(1l)
                .isbn("123")
                .title("As aventuras")
                .author("Fulano")
                .build());

        Book savedBook = service.save(book);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
    }

    @Test
    @DisplayName("Should return a book when id is provider in DB")
    void getById(){
        Long id = 1l;
        Book book = createBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> foundBook = service.getById(id);

        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());

    }

    @Test
    @DisplayName("Should return empty when id is not provider in DB")
    void getByIdNotFound(){
        Long id = 1l;
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> book = service.getById(id);

        assertThat(book).isNotPresent();
    }

    @Test
    @DisplayName("Should throw error exception when try save isbn duplicated")
    void ShouldNotSaveSaveBookWithDuplicatedISBN(){
        Book book = createBook();
        when(repository.existsByIsbn(anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        BDDAssertions.assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Should delete a book when id is provider")
    void deleteBook(){
        Book book = Book.builder().id(1l).build();

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(()->service.delete(book));

        verify(repository, times(1)).delete(book);
    }

    @Test
    @DisplayName("Not Should delete a book when id is not provider")
    void deleteBookFail(){
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                ()->service.delete(book));

        verify(repository, never()).delete(book);
    }

    @Test
    @DisplayName("Not Should update a book when id is not provider")
    void updateBookFail(){
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                ()->service.update(book));

        verify(repository, never()).save(book);
    }

    @Test
    @DisplayName("Should filter books by properties")
    void findBookTest(){
        Book book = createBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Book> list = Collections.singletonList(book);

        Page<Book> page = new PageImpl<Book>(
                list
                ,pageRequest,
                1);

        Mockito.when(repository.findAll(
                any(Example.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> result = service.find(book, pageRequest);

        BDDAssertions.assertThat(result.getTotalElements()).isEqualTo(1);
        BDDAssertions.assertThat(result.getContent()).isEqualTo( list );
        BDDAssertions.assertThat(result.getPageable().getPageNumber()).isEqualTo( 0 );
        BDDAssertions.assertThat(result.getPageable().getPageSize()).isEqualTo( 10 );

    }

    @Test
    @DisplayName("Should update a book when id is provider")
    void updateBookTest(){
        Long id = 1l;
        Book updatingBook = Book.builder().id(id).build();

        Book updatedBook = createBook();
        updatedBook.setId(id);

        when(repository.save(updatingBook)).thenReturn(updatedBook);

        Book book = service.update(updatingBook);

        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());


    }

    @Test
    @DisplayName("Should return a book by isbn")
    void getBookByIsbn(){
        String isbn = "1230";

        when(repository.findByIsbn(isbn))
                .thenReturn(Optional.of(Book.builder().id(1l).isbn(isbn).build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        BDDAssertions.assertThat(book).isPresent();
        BDDAssertions.assertThat(book.get().getId()).isEqualTo(1l);
        BDDAssertions.assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(repository, times(1)).findByIsbn(isbn);
    }
}
