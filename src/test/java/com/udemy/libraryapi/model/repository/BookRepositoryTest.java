package com.udemy.libraryapi.model.repository;

import com.udemy.libraryapi.domain.entity.Book;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Should return true when exist book in DB with ISBN provider")
    void ShouldReturnTrueWhenExistBookInDB(){
        // cenario
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // execução
        boolean exist = repository.existsByIsbn(isbn);

        // verificação
        BDDAssertions.assertThat(exist).isTrue();
    }

    public static Book createNewBook(String isbn) {
        return Book.builder().author("Fulano").title("As Aventuras").isbn(isbn).build();
    }

    @Test
    @DisplayName("Should return false when do not exist book in DB with ISBN provider")
    void ShouldReturnFalseWhenDoesExistBookInDB(){
        // cenario
        String isbn = "123";

        // execução
        boolean exist = repository.existsByIsbn(isbn);

        // verificação
        BDDAssertions.assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("Should return book by id")
    void findById(){
        Book book = createNewBook("123");
        entityManager.persist(book);

        Optional<Book> foundBook = repository.findById(book.getId());

        BDDAssertions.assertThat(foundBook).isPresent();

    }

    @Test
    @DisplayName("Should save a book")
    void saveBookTest(){
        Book book = createNewBook("123");

        Book savedBook = repository.save(book);

        BDDAssertions.assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should delete a book")
    void deleteBookTest(){

        Book book = createNewBook("123");
        entityManager.persist(book);

        Book foundBook = entityManager.find(Book.class, book.getId());

        repository.delete(foundBook);

        Book deletedBook = entityManager.find(Book.class, book.getId());
        BDDAssertions.assertThat(deletedBook).isNull();

    }


}
