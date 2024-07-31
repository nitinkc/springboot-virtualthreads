package com.virtualthreads.bookstore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.concurrent.StructuredTaskScope.Subtask.State;
import java.util.concurrent.ThreadFactory;

import com.virtualthreads.bookstore.metrics.CallStats;
import com.virtualthreads.bookstore.model.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static java.lang.StringTemplate.STR;

@Service
@Slf4j
public class BookRetrievalService {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public List<Book> getBookFromAllStores(String bookName) throws InterruptedException {
    ThreadFactory threadFactory = Thread.ofVirtual().name("book-store-thr", 0).factory();
    try (var scope = new StructuredTaskScope<Book>("virtual-store", threadFactory)) {
      List<Subtask<Book>> bookTasks = new ArrayList<>();

      // Simulate the data acquisition
      bookTasks.add(scope.fork(() -> getBook(bookName, "store1")));
      bookTasks.add(scope.fork(() -> getBook(bookName, "store2")));

      scope.join(); // wait for all the tasks to finish

      // Dump stacktrace of all failures
      bookTasks.stream()
          .filter(t -> t.state() == State.FAILED)
          .map(Subtask::exception)
          .forEach(e -> e.printStackTrace());

      List<Book> list = bookTasks
              .stream()
              .filter(t -> t.state() == State.SUCCESS)
              .map(Subtask::get)
              .toList();
      return list;
    }
  }

  private Book getBook(String bookName, String storeName) throws IOException, InterruptedException {
    log.info(STR."\{storeName} :: \{Thread.currentThread()}");
    String fileName;
    if (storeName.equals("store1")) {
      fileName = "src/main/resources/store1.json";
    } else {
      fileName = "src/main/resources/store2.json";
    }
    long start = System.currentTimeMillis();
    // Insert random delay to simulate network access
    int randomWaitTime = (int )(Math.random() * 1000);
    Thread.sleep(randomWaitTime);
    List<Book> books = fetchBookFromStore(fileName);
    long end = System.currentTimeMillis();
    CallStats logMap = BookController.CALL_STATS.get();//Scoped Value

    logMap.putIntoLogMap(storeName, String.valueOf(end - start));
    logMap.putIntoLogMap(STR."wait Time for \{storeName}", String.valueOf(randomWaitTime));

    Book book = books.stream()
            .filter(singleBook -> singleBook.getTitle().equalsIgnoreCase(bookName))
            .findFirst()
            .orElseGet(() -> new Book());

    return book;
  }

  private List<Book> fetchBookFromStore(String fileName) {
    log.info(STR."\fetchBookFromStore :: \{Thread.currentThread()}");

    File file = new File(fileName);
    List<Book> books = new ArrayList<>();
    try {
      books = objectMapper.readValue(file, new TypeReference<List<Book>>() {});
    } catch (IOException e) {
      log.info("Error reading");
    }
    return books;
  }
}