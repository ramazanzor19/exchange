package com.exchange.foreign_exchange_api.exception;

public class PageOutOfRangeException extends RuntimeException {
  private final int requestedPage;
  private final int maxPage;

  public PageOutOfRangeException(int requestedPage, int maxPage) {
    super(String.format("Page %d is out of range (max %d)", requestedPage, maxPage));
    this.requestedPage = requestedPage;
    this.maxPage = maxPage;
  }
}
