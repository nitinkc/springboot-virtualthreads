package com.virtualthreads.bookstore.model;

import com.virtualthreads.bookstore.metrics.CallStats;

import java.util.List;

public record BestPriceResult(Book bestPriceDeal, List<Book> allDeals,CallStats callStatistics) {
}