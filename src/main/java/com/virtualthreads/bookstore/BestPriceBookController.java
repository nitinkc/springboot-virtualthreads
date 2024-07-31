package com.virtualthreads.bookstore;

import java.util.Comparator;
import java.util.List;

import com.virtualthreads.bookstore.metrics.CallStats;
import com.virtualthreads.bookstore.model.BestPriceResult;
import com.virtualthreads.bookstore.model.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BestPriceBookController {
    public static final ScopedValue<CallStats> CALL_STATS = ScopedValue.newInstance();
    private final BookRetrievalService retrievalService;
    
    @GetMapping("/book")
    public BestPriceResult getBestPriceForBook(@RequestParam String name) {
    	long start = System.currentTimeMillis();
        
        CallStats logStats = new CallStats();
        try {
            List<Book> books = ScopedValue.callWhere(CALL_STATS, logStats, () -> retrievalService.getBookFromAllStores(name));
            
            Book bestPriceBook = books.stream()
                .min(Comparator.comparing(Book::getPrice))
                .orElseThrow();

            return new BestPriceResult(bestPriceBook, books,logStats);
        } catch (Exception e) {
            throw new RuntimeException("Exception while calling getBestPrice",e);
        }
        finally {
        	logStats.putIntoLogMap("Total Time for Best Price Book", String.valueOf(System.currentTimeMillis() - start));
            logStats.dumpTiming();
        }
    }
}
