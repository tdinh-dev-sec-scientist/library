package library.csci2010;

public class FictionBook extends Book {
    private static final long serialVersionUID = 1L; // Fiction Book Serial Version UID
    private String genre; // Fiction Book Genre

    // Constructor
    public FictionBook(String isbn, String title, String author, int publicationYear, String genre) {
        super(isbn, title, author, publicationYear);
        this.genre = genre;
    }

    // Getters
    public String getGenre() {
        return genre;
    }

    @Override
    public String getBookTypeDetails() {
        return "Fiction - Genre: " + genre;
    }

    // toString method
    @Override
    public String toString() {
        return super.toString() + " | " + getBookTypeDetails();
    }

    public void setGenre(String genre) {
      this.genre = genre;
    }

}
