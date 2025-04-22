package library.csci2010;

public interface Borrowable {

  public void issueBook(String memberId) throws BookAlreadyIssuedException;

  public void returnBook() throws BookNotIssuedException;

  public boolean isAvailable();
}
