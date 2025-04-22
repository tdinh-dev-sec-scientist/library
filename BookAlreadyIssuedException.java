package library.csci2010;

public class BookAlreadyIssuedException extends BookException {
  public BookAlreadyIssuedException(String isbn) {
    super("Book with ISBN " + isbn + " has already been issued to other members.");
  }
  
}
