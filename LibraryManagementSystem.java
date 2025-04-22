package library.csci2010;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class LibraryManagementSystem {

    public static void main(String[] args) {
        
        // Create library and scanner objects
        Library library = new Library();
        Scanner scanner = new Scanner(System.in);
        int choice;
        // Try to load existing data
        try {
            library.loadBooksFromFile("books.dat");
            System.out.println("Book data loaded successfully!");
        } catch (FileNotFoundException e) {
            System.out.println("No existing book data found. Starting with empty library.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading book data: " + e.getMessage());
        }

        // Main menu
        do {
            System.out.println("\n<WELCOMING MY FRIENDs!>\n");
            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Add a Book");
            System.out.println("2. Remove a Book");
            System.out.println("3. Display All Books");
            System.out.println("4. Search for Books");
            System.out.println("5. Issue Book to Member");
            System.out.println("6. Return Book");
            System.out.println("7. Save Data");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
                choice = 0;
                continue;
            }

            // Switch case for menu options
            switch (choice) {
                case 1:
                    addBook(scanner, library);
                    break;
                case 2:
                    removeBook(scanner, library);
                    break;
                case 3:
                    library.displayAllBooks();
                    break;
                case 4:
                    searchBooks(scanner, library);
                    break;
                case 5:
                    issueBook(scanner, library);
                    break;
                case 6:
                    returnBook(scanner, library);
                    break;
                case 7:
                    try {
                        library.saveBooksToFile("books.dat");
                        System.out.println("Data saved successfully!");
                    } catch (IOException e) {
                        System.out.println("Error saving data: " + e.getMessage());
                    }
                    break;
                case 8:
                    // Save before exit
                    try {
                        library.saveBooksToFile("books.dat");
                        System.out.println("Data saved successfully!");
                    } catch (IOException e) {
                        System.out.println("Error saving data: " + e.getMessage());
                    }
                    System.out.println("Thank you for using Truong Dinh Library Management System!");
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        } while (choice != 8);
    }

    // addBook methods
    private static void addBook(Scanner scanner, Library library) {
        System.out.println("\n===== ADD BOOK =====");
        System.out.println("Select book type:");
        System.out.println("1. Fiction Book");
        System.out.println("2. Non-Fiction Book");
        System.out.println("3. Reference Book");
        System.out.print("Enter choice: ");

        // add a book
        try {
            int bookType = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter ISBN: ");
            String isbn = scanner.nextLine();
            System.out.print("Enter Title: ");
            String title = scanner.nextLine();
            System.out.print("Enter Author: ");
            String author = scanner.nextLine();
            System.out.print("Enter Publication Year: ");
            int year = Integer.parseInt(scanner.nextLine());

            Book book;
            // Create book based on type
            switch (bookType) {
                case 1:
                    System.out.print("Enter Genre: ");
                    String genre = scanner.nextLine();
                    book = new FictionBook(isbn, title, author, year, genre);
                    break;
                case 2:
                    System.out.print("Enter Subject: ");
                    String subject = scanner.nextLine();
                    book = new NonFictionBook(isbn, title, author, year, subject);
                    break;
                case 3:
                    System.out.print("Enter Category: ");
                    String category = scanner.nextLine();
                    book = new ReferenceBook(isbn, title, author, year, category);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid book type");
            }

            // Add book to library
            library.addBook(book);
            System.out.println("Book added successfully!");

        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a valid number.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (BookException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // removeBook method
    private static void removeBook(Scanner scanner, Library library) {
        System.out.println("\n===== REMOVE BOOK =====");
        System.out.print("Enter ISBN of book to remove: ");
        String isbn = scanner.nextLine();

        try {
            library.removeBook(isbn);
            System.out.println("Book removed successfully!");
        } catch (BookNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // searchBooks method
    private static void searchBooks(Scanner scanner, Library library) {
        System.out.println("\n===== SEARCH BOOKS =====");
        System.out.println("Search by:");
        System.out.println("1. ISBN");
        System.out.println("2. Title");
        System.out.println("3. Author");
        System.out.print("Enter choice: ");

        try {
            int searchChoice = Integer.parseInt(scanner.nextLine());
            String searchTerm;
            List<Book> results;

            switch (searchChoice) {
                case 1:
                    System.out.print("Enter ISBN: ");
                    searchTerm = scanner.nextLine();
                    results = library.searchBooksByISBN(searchTerm);
                    break;
                case 2:
                    System.out.print("Enter Title: ");
                    searchTerm = scanner.nextLine();
                    results = library.searchBooksByTitle(searchTerm);
                    break;
                case 3:
                    System.out.print("Enter Author: ");
                    searchTerm = scanner.nextLine();
                    results = library.searchBooksByAuthor(searchTerm);
                    break;
                default:
                    System.out.println("Invalid choice!");
                    return;
            }

            if (results.isEmpty()) {
                System.out.println("No books found matching your search criteria.");
            } else {
                System.out.println("\nSearch Results:");
                for (Book book : results) {
                    System.out.println(book);
                }
                System.out.println("Total books found: " + results.size());
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a valid number.");
        }
    }

    // issueBook method
    private static void issueBook(Scanner scanner, Library library) {
        System.out.println("\n===== ISSUE BOOK =====");
        System.out.print("Enter ISBN of book to issue: ");
        String isbn = scanner.nextLine();
        System.out.print("Enter Member ID: ");
        String memberId = scanner.nextLine();

        try {
            library.issueBook(isbn, memberId);
            System.out.println("Book issued successfully!");
        } catch (BookNotFoundException | BookAlreadyIssuedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // returnBook method
    private static void returnBook(Scanner scanner, Library library) {
        System.out.println("\n===== RETURN BOOK =====");
        System.out.print("Enter ISBN of book to return: ");
        String isbn = scanner.nextLine();

        try {
            library.returnBook(isbn);
            System.out.println("Book returned successfully!");
        } catch (BookNotFoundException | BookNotIssuedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
