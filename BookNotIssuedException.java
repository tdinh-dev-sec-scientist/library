package library.csci2010;

public class BookNotIssuedException extends BookException {
  public BookNotIssuedException(String isbn) {
    super("Book with ISBN " + isbn + " has not currently been issued to any members.");
  }
  
}
