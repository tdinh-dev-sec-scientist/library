package library.csci2010;

import java.io.Serializable;
import java.time.LocalDate;

public abstract class Book implements Searchable, Borrowable, Serializable {

  private static final long serialVersionUID = 1L;
  protected String isbn; // International Standard Book Number
  protected String title; // Title of the book
  protected String author; // Author of the book
  protected int publicationYear; // Year of publication
  protected String issuedTo; // memberId of the member who has borrowed the book
  private LocalDate loanDate;
  private LocalDate dueDate;

  // Constructor
  public Book(String isbn, String title, String author, int publicationYear) {
    this.isbn = isbn;
    this.title = title;
    this.author = author;
    this.publicationYear = publicationYear;
    this.issuedTo = null;
  }

  // Accessor for all fields
  public String getIsbn() {
    return isbn;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }

  public int getPublicationYear() {
    return publicationYear;
  }

  public LocalDate getLoanDate() {
    return loanDate;
  }

  public void setLoanDate(LocalDate loanDate) {
    this.loanDate = loanDate;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  // Abstract method to get book type details
  @Override
  public boolean matchesSearchCriteria(String criteria, SearchType type) {
    switch (type) {
      case ISBN:
        return isbn.toLowerCase().contains(criteria.toLowerCase());
      case TITLE:
        return title.toLowerCase().contains(criteria.toLowerCase());
      case AUTHOR:
        return author.toLowerCase().contains(criteria.toLowerCase());
      default:
        return false;
    }
  }

  // issueBook method
  @Override
  public void issueBook(String memberId) throws BookAlreadyIssuedException {
    if (!isAvailable()) {
      throw new BookAlreadyIssuedException(isbn);
    }
    this.issuedTo = memberId;
  }

  // returnBook method
  @Override
  public void returnBook() throws BookNotIssuedException {
    if (isAvailable()) {
      throw new BookNotIssuedException(isbn);
    }
    this.issuedTo = null;
  }

  // isAvailable method
  @Override
  public boolean isAvailable() {
    return issuedTo == null;
  }

  // toString method
  @Override
  public String toString() {
    return String.format("ISBN: %s | Title: %s | Author: %s | Year: %d | Status: %s",
        isbn, title, author, publicationYear,
        isAvailable() ? "Available" : "Issued to " + issuedTo);
  }

  // Each book type must implement this to provide Book Type details
  public String getBookTypeDetails() {
    return "";
  }

  protected boolean isOnLoan() {
    return issuedTo != null;
  }

  protected String getBorrower() {
    return issuedTo;
  }

  protected void setOnLoan(boolean b) {
    if (b) {
      issuedTo = "On Loan";
    } else {
      issuedTo = null;
    }
  }

  protected void setBorrower(Object object) {
    issuedTo = (String) object;
  }

  public void setTitle(String title2) {
    this.title = title2;
  }

  public void setAuthor(String author2) {
    this.author = author2;
  }

  public void setYear(int year) {
    this.publicationYear = year;
  }

  public void setIsbn(String isbn2) {
    this.isbn = isbn2;
  }
}
