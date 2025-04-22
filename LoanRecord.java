package library.csci2010;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LoanRecord implements Serializable {
  private String bookIsbn;
  private String bookTitle;
  private String borrower;
  private LocalDate loanDate;
  private LocalDate dueDate;
  private LocalDate returnDate;
  private boolean isOverdue;
  
  // Constructor, getters, setters
  public LoanRecord(Book book, LocalDate returnDate) {
      this.bookIsbn = book.getIsbn();
      this.bookTitle = book.getTitle();
      this.borrower = book.getBorrower();
      this.loanDate = book.getLoanDate();
      this.dueDate = book.getDueDate();
      this.returnDate = returnDate;
      this.isOverdue = returnDate.isAfter(dueDate);
  
}
 public String getISBN(){
        return bookIsbn;
    }
    public String getBookTitle() {
        return bookTitle;
    }
    public String getBorrower() {
        return borrower;
    }
    public LocalDate getLoanDate() {
        return loanDate;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public LocalDate getReturnDate() {
        return returnDate;
 }
  public String getStatus() {
      if (isOverdue) {
          long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
          return "Overdue (" + daysOverdue + " days)";
      }
      return "Returned on time";
  }
  
}
