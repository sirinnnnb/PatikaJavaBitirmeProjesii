package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class LibraryManagementSystem {
    public static void main(String[] args) {
        new App().run();
    }
}

/** Basit veri modeli: Book (Kitap) */
class Book {
    // --- Alanlar (Fields) ---
    private final int id;            // benzersiz ID
    private final String title;      // başlık
    private final String author;     // yazar
    private final int publishYear;   // yayın yılı
    private boolean borrowed;        // ödünçte mi?

    // --- Kurucu (Constructor) ---
    public Book(int id, String title, String author, int publishYear, boolean borrowed) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publishYear = publishYear;
        this.borrowed = borrowed;
    }

    // --- Getter'lar ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getPublishYear() { return publishYear; }
    public boolean isBorrowed() { return borrowed; }

    // --- İşlevler ---
    public void markBorrowed() { this.borrowed = true; }     // ödünç alındı işaretle
    public void markReturned() { this.borrowed = false; }    // iade edildi işaretle

    // --- Kalıcılık (TSV satırı olarak serileştirme) ---
    public String toTsv() {
        // Uyarı: Basitlik adına TAB karakteri desteklenmiyor.
        String safeTitle = title.replace("\t", " ");
        String safeAuthor = author.replace("\t", " ");
        return id + "\t" + safeTitle + "\t" + safeAuthor + "\t" + publishYear + "\t" + borrowed;
    }

    public static Book fromTsv(String line) {
        // Beklenen format: id \t title \t author \t year \t borrowed
        String[] parts = line.split("\t");
        if (parts.length < 5) return null;
        try {
            int id = Integer.parseInt(parts[0].trim());
            String title = parts[1];
            String author = parts[2];
            int year = Integer.parseInt(parts[3].trim());
            boolean borrowed = Boolean.parseBoolean(parts[4].trim());
            return new Book(id, title, author, year, borrowed);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        // Kullanıcı dostu çıktı (listeleme ve arama için)
        return String.format("#%d | %s — %s (%d) | %s",
                id, title, author, publishYear, borrowed ? "BORROWED" : "AVAILABLE");
    }
}

/** İş kuralları ve depolama: Library (Kütüphane) */
class Library {
    // --- Alanlar ---
    private final List<Book> books = new ArrayList<>();
    private int nextId = 1; // yeni kitaplar için ID
    private final Path dataDir = Paths.get("data");
    private final Path dataFile = dataDir.resolve("books.tsv");

    // --- Kurucu ---
    public Library() {
        loadFromFile(); // uygulama başlarken kayıtlı verileri yükle
    }

    /** Yeni kitap ekle */
    public Book addBook(String title, String author, int publishYear) {
        // ID üret
        int id = nextId++;
        Book book = new Book(id, title, author, publishYear, false);
        books.add(book);
        saveToFile(); // kalıcı hale getir
        return book;
    }

    /** Tüm kitapları döndür (salt okunur görünüm) */
    public List<Book> listBooks() {
        return Collections.unmodifiableList(books);
    }

    /** Başlığa göre arama (case-insensitive, contains) */
    public List<Book> searchByTitle(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        List<Book> result = new ArrayList<>();
        for (Book b : books) {
            if (b.getTitle().toLowerCase(Locale.ROOT).contains(q)) {
                result.add(b);
            }
        }
        return result;
    }

    /** ID ile kitabı ödünç al */
    public String borrowById(int id) {
        Book b = findById(id);
        if (b == null) return "No book found with given ID.";
        if (b.isBorrowed()) return "This book is already borrowed and not yet returned.";
        b.markBorrowed();
        saveToFile();
        return "Borrowed successfully: " + formatShort(b);
    }

    /** ID ile kitabı iade et */
    public String returnById(int id) {
        Book b = findById(id);
        if (b == null) return "No book found with given ID.";
        if (!b.isBorrowed()) return "This book is not currently borrowed.";
        b.markReturned();
        saveToFile();
        return "Returned successfully: " + formatShort(b);
    }

    // --- Yardımcılar ---
    private Book findById(int id) {
        for (Book b : books) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    private String formatShort(Book b) {
        return String.format("#%d | %s", b.getId(), b.getTitle());
    }

    /** Dosyadan yükleme */
    private void loadFromFile() {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir); // data/ klasörünü oluştur
            }
            if (!Files.exists(dataFile)) {
                // Dosya yoksa başlangıç için boş liste, nextId=1
                return;
            }
            List<String> lines = Files.readAllLines(dataFile);
            int maxId = 0;
            for (String line : lines) {
                if (line.isBlank()) continue;
                Book b = Book.fromTsv(line);
                if (b != null) {
                    books.add(b);
                    maxId = Math.max(maxId, b.getId());
                }
            }
            nextId = maxId + 1; // mevcut en büyük ID'nin bir fazlası
        } catch (IOException e) {
            System.err.println("Failed to load data file: " + e.getMessage());
        }
    }

    /** Dosyaya kaydetme */
    private void saveToFile() {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            try (BufferedWriter bw = Files.newBufferedWriter(dataFile)) {
                for (Book b : books) {
                    bw.write(b.toTsv());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to save data file: " + e.getMessage());
        }
    }
}

/** Uygulama akışı ve menü: App */
class App {
    private final Scanner in = new Scanner(System.in);
    private final Library library = new Library();

    public void run() {
        // Sürekli döngü: kullanıcı çıkış deyinceye kadar
        while (true) {
            printMenu();
            String choice = in.nextLine().trim();
            switch (choice) {
                case "1" -> addBookFlow();
                case "2" -> listBooksFlow();
                case "3" -> searchFlow();
                case "4" -> borrowFlow();
                case "5" -> returnFlow();
                case "0" -> {
                    System.out.println("Exiting the program. Goodbye!");
                    return; // programdan çık
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
            // Her işlemden sonra menü doğal olarak tekrar gösterilecek (döngü başına dönüyoruz)
        }
    }

    // --- Menü yazdır ---
    private void printMenu() {
        System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
        System.out.println("1) Add a new book");
        System.out.println("2) List all books");
        System.out.println("3) Search by title");
        System.out.println("4) Borrow a book");
        System.out.println("5) Return a book");
        System.out.println("0) Exit");
        System.out.print("Select an option: ");
    }

    // --- Akışlar ---
    private void addBookFlow() {
        try {
            // Kullanıcıdan kitap bilgilerini al
            System.out.print("Title: ");
            String title = in.nextLine().trim();

            System.out.print("Author: ");
            String author = in.nextLine().trim();

            System.out.print("Publish year (e.g., 2020): ");
            String yearStr = in.nextLine().trim();
            int year = Integer.parseInt(yearStr);

            // Ekleyip kaydet
            Book added = library.addBook(title, author, year);
            System.out.println("Book added successfully: " + added);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid year. Please enter a valid number.");
        }
    }

    private void listBooksFlow() {
        List<Book> all = library.listBooks();
        if (all.isEmpty()) {
            System.out.println("No books in the library yet.");
            return;
        }
        System.out.println("\n--- ALL BOOKS ---");
        for (Book b : all) {
            System.out.println(b);
        }
    }

    private void searchFlow() {
        System.out.print("Enter a title keyword: ");
        String q = in.nextLine().trim();
        List<Book> found = library.searchByTitle(q);
        if (found.isEmpty()) {
            System.out.println("No matching books found.");
            return;
        }
        System.out.println("\n--- SEARCH RESULTS ---");
        for (Book b : found) {
            System.out.println(b);
        }
    }

    private void borrowFlow() {
        try {
            System.out.print("Enter book ID to borrow: ");
            int id = Integer.parseInt(in.nextLine().trim());
            String msg = library.borrowById(id);
            System.out.println(msg);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid ID. Please enter a valid number.");
        }
    }

    private void returnFlow() {
        try {
            System.out.print("Enter book ID to return: ");
            int id = Integer.parseInt(in.nextLine().trim());
            String msg = library.returnById(id);
            System.out.println(msg);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid ID. Please enter a valid number.");
        }
    }
}

