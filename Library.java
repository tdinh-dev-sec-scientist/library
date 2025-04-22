package library.csci2010;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Library {
    
    // Array List Declaration
    private ArrayList<Book> books;
    private ArrayList<LoanRecord> loanHistory;


    // Constructor
    public Library() {
        this.books = new ArrayList<>();
    }
    // addBook method
    public void addBook(Book book) throws BookException {
        for (Book b : books) {
            if (b.getIsbn().equals(book.getIsbn())) {
                throw new BookException("Book with ISBN " + book.getIsbn() + " already exists in the library.");
            }
        }
        books.add(book);

    }

    // removeBook method
    public void removeBook(String isbn) throws BookNotFoundException {
        Book bookToRemove = findBookByISBN(isbn);
        if (bookToRemove == null) {
            throw new BookNotFoundException(isbn);
        }
        books.remove(bookToRemove);
    }
    
    // displayAllBooks method
    public void displayAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books in the library.");
            return;
        }
        System.out.println("\n===== LIBRARY INVENTORY =====");
        System.out.println("Total Books: " + books.size());

        // Group books by type
        Map<String, List<Book>> booksByType = new HashMap<>();
        for (Book book : books) {
            String type = book.getClass().getSimpleName();
            if (!booksByType.containsKey(type)) {
                booksByType.put(type, new ArrayList<>());
            }
            booksByType.get(type).add(book);
        }

        // Display books by type
        for (Map.Entry<String, List<Book>> entry : booksByType.entrySet()) {
            System.out.println("\n" + entry.getKey() + " (" + entry.getValue().size() + " books):");
            for (Book book : entry.getValue()) {
                System.out.println(book);
            }
        }
    }

    // searchBooksByISBN method
    public List<Book> searchBooksByISBN(String isbn) {
        return searchBooks(isbn, SearchType.ISBN);
    }

    // searchBooksByTitle method
    public List<Book> searchBooksByTitle(String title) {
        return searchBooks(title, SearchType.TITLE);
    }

    // searchBooksByAuthor method
    public List<Book> searchBooksByAuthor(String author) {
        return searchBooks(author, SearchType.AUTHOR);
    }

    // searchBooks method
    private List<Book> searchBooks(String criteria, SearchType type) {
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.matchesSearchCriteria(criteria, type)) {
                results.add(book);
            }
        }
        return results;
    }

    // findBookByISBN method
    private Book findBookByISBN(String isbn) {
        for (Book book : books) {
            if (book.getIsbn().equals(isbn)) {
                return book;
            }
        }
        return null;
    }

    // issueBook method
    public void issueBook(String isbn, String memberId)
            throws BookNotFoundException, BookAlreadyIssuedException {
        Book book = findBookByISBN(isbn);
        if (book == null) {
            throw new BookNotFoundException(isbn);
        }
        book.issueBook(memberId);
    }

    //  returnBook method
    public void returnBook(String isbn)
            throws BookNotFoundException, BookNotIssuedException {
        Book book = findBookByISBN(isbn);
        if (book == null) {
            throw new BookNotFoundException(isbn);
        }
        book.returnBook();
    }

    // saveBooksToFile method
    public void saveBooksToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filename))) {
            oos.writeObject(books);
            oos.writeObject(loanHistory);
        }
    }

    // loadBooksFromFile method
    public void loadBooksFromFile(String filename)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filename))) {
            books = (ArrayList<Book>) ois.readObject();
            loanHistory = (ArrayList<LoanRecord>) ois.readObject();
        }
    }
    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    
    }
    public void addLoanRecord(Book book) {
        LocalDate returnDate = LocalDate.now();
        loanHistory.add(new LoanRecord(book, returnDate));
    }
   
}
