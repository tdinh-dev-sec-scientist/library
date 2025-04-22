package library.csci2010;

public class ReferenceBook extends Book {

    private static final long serialVersionUID = 1L; // Reference Book Serial Version UID
    private String category; // Reference Book Category

    // Constructor
    public ReferenceBook(String isbn, String title, String author, int publicationYear, String category) {
        super(isbn, title, author, publicationYear);
        this.category = category;
    }

    // Getters
    public String getCategory() {
        return category;
    }
    @Override
    public String getBookTypeDetails() {
        return "Reference - Category: " + category;
    }

    // toString method
    @Override
    public String toString() {
        return super.toString() + " | " + getBookTypeDetails();
    }

    public void setCategory(String specificValue) {
        this.category = specificValue;
        }
}
