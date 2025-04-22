package library.csci2010;

public class NonFictionBook extends Book{
  private static final long serialVersionUID = 1L;
  private String subject;
  
  public NonFictionBook(String isbn, String title, String author, int publicationYear, String subject) {
    super(isbn, title, author, publicationYear);
    this.subject = subject;
  }
  // getter
  public String getSubject() {
    return subject;
  }
  @Override
    public String getBookTypeDetails() {
        return "Non-Fiction - Subject: " + subject;
    }
    
    @Override
    public String toString() {
        return super.toString() + " | " + getBookTypeDetails();
    }
    public void setSubject(String specificValue) {
      this.subject = specificValue;
    }

  }