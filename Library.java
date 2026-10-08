/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Core business logic: owns the catalog, topics and members, and enforces
 * every borrowing, reservation, return and penalty rule.
 */
public final class Library {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final List<Book> books = new ArrayList<>();
    private final List<Topic> topics = new ArrayList<>();
    private final Map<String, Member> membersById = new HashMap<>();
    private final Map<String, Member> membersByEmail = new HashMap<>();

    public Library() {
        seedTopics();
        seedBooks();
        seedDemoData();
    }

    public LocalDate getToday() {
        return LocalDate.now();
    }

    // =====================================================================
    //  Members
    // =====================================================================
    public Member registerMember(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        String emailKey = email.trim().toLowerCase();
        if (membersByEmail.containsKey(emailKey)) {
            throw new IllegalArgumentException("That email is already registered.");
        }
        String memberId = generateMemberId(name, emailKey, password);
        Member member = new Member(memberId, name.trim(), emailKey, password);
        membersById.put(memberId, member);
        membersByEmail.put(emailKey, member);
        return member;
    }

    public Member login(String email, String password) {
        Member member = membersByEmail.get(email.trim().toLowerCase());
        if (member != null && member.verifyPassword(password)) {
            return member;
        }
        return null;
    }

    /** Member ID = ITAM-<initials>-<4 digits derived from name, email and password>, made unique. */
    private String generateMemberId(String name, String email, String password) {
        StringBuilder initials = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            if (!part.isEmpty() && initials.length() < 3) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        int number = Math.abs(Objects.hash(name.trim().toLowerCase(), email, password) % 10000);
        String id;
        do {
            id = String.format("ITAM-%s-%04d", initials, number);
            number = (number + 1) % 10000;
        } while (membersById.containsKey(id));
        return id;
    }

    // =====================================================================
    //  Catalog queries
    // =====================================================================
    public List<Topic> getTopics() {
        return topics;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Book> getBooksByCategory(String category) {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getCategory().equalsIgnoreCase(category)) {
                result.add(book);
            }
        }
        return result;
    }

    public Book findBookById(String bookId) {
        for (Book book : books) {
            if (book.getBookId().equalsIgnoreCase(bookId.trim())) {
                return book;
            }
        }
        return null;
    }

    public List<Book> searchBooks(String keyword) {
        String key = keyword.trim().toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(key)
                    || book.getAuthor().toLowerCase().contains(key)
                    || book.getCategory().toLowerCase().contains(key)
                    || book.getDifficulty().toString().toLowerCase().contains(key)) {
                result.add(book);
            }
        }
        return result;
    }

    // =====================================================================
    //  Borrowing
    // =====================================================================
    /** Returns null when the member may borrow, otherwise the reason they may not. */
    public String checkBorrowEligibility(Member member, Book book) {
        if (member.getAccountStatus() == AccountStatus.RESTRICTED) {
            return "Your account is RESTRICTED (" + member.getRestrictionReason()
                    + "). Settle this first.";
        }
        if (hasActiveLoan(member, book)) {
            return "You are already borrowing this book.";
        }
        boolean heldForMember = book.isReadyFor(member);
        if (!book.isAvailable() && !heldForMember) {
            return "This book is currently " + book.getStatus() + " and cannot be borrowed.";
        }
        if (!heldForMember && !member.hasFreeSlot()) {
            return "All " + Member.MAX_ACTIVE_SLOTS + " active book slots are in use. "
                    + "Return a book or cancel a reservation first.";
        }
        return null;
    }

    public LocalDate previewDueDate() {
        return getToday().plusDays(Loan.LOAN_PERIOD_DAYS);
    }

    public Loan borrowBook(Member member, Book book) {
        if (book.isReadyFor(member)) {
            // The reservation is converted into a loan, so it frees its slot.
            Reservation claimed = book.claimReservation();
            claimed.markFulfilled();
        }
        Loan loan = new Loan(member, book, getToday());
        book.markBorrowed(loan.getDueDate());
        member.addLoan(loan);
        return loan;
    }

    private boolean hasActiveLoan(Member member, Book book) {
        for (Loan loan : member.getActiveLoans()) {
            if (loan.getBook() == book) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================
    //  Reservations
    // =====================================================================
    public String checkReserveEligibility(Member member, Book book) {
        if (member.getAccountStatus() == AccountStatus.RESTRICTED) {
            return "Your account is RESTRICTED (" + member.getRestrictionReason()
                    + "). Settle this first.";
        }
        if (book.isAvailable()) {
            return "This book is AVAILABLE - you can borrow it directly.";
        }
        if (hasActiveLoan(member, book)) {
            return "You are currently borrowing this book.";
        }
        if (member.hasReservationFor(book)) {
            return "You already have a reservation for this book.";
        }
        if (!member.hasFreeSlot()) {
            return "All " + Member.MAX_ACTIVE_SLOTS + " active book slots are in use. "
                    + "Return a book or cancel a reservation first.";
        }
        return null;
    }

    public Reservation reserveBook(Member member, Book book) {
        Reservation reservation = new Reservation(member, book, getToday());
        book.enqueue(reservation);
        member.addReservation(reservation);
        return reservation;
    }

    /** @return true if cancelled, false if the reservation was no longer eligible */
    public boolean cancelReservation(Reservation reservation) {
        if (!reservation.cancel()) {
            return false;
        }
        reservation.getBook().removeReservation(reservation, getToday());
        return true;
    }

    /**
     * Expires READY reservations that were not picked up in time and passes
     * the book on to the next member in the queue.
     * @return the reservations that expired during this check
     */
    public List<Reservation> processExpiredReservations() {
        LocalDate today = getToday();
        List<Reservation> expired = new ArrayList<>();
        for (Book book : books) {
            while (book.getStatus() == BookStatus.RESERVED && !book.getReservationQueue().isEmpty()) {
                Reservation head = book.getReservationQueue().get(0);
                if (!head.isExpired(today)) {
                    break;
                }
                head.markExpired();
                book.removeReservation(head, today);
                expired.add(head);
            }
        }
        return expired;
    }

    // =====================================================================
    //  Returns
    // =====================================================================
    /**
     * Completes the loan, issues a penalty if late, and updates the book.
     * @return the reservation promoted to READY, or null if nobody was waiting
     */
    public Reservation returnBook(Loan loan) {
        LocalDate today = getToday();
        double penalty = loan.complete(today);
        if (penalty > 0) {
            String reason = String.format("\"%s\" returned %d day(s) late (PHP %.2f/day)",
                    loan.getBook().getTitle(), loan.getDaysOverdue(today), Loan.PENALTY_PER_DAY);
            loan.getMember().addPenalty(new Penalty(reason, penalty, today));
        }
        return loan.getBook().releaseAfterReturn(today);
    }

    // =====================================================================
    //  Seed data
    // =====================================================================
    private void seedTopics() {
        topics.add(new Topic("C++",
                "A fast, compiled language that gives programmers close control over memory and performance.",
                "Game engines, operating systems, embedded systems, competitive programming.",
                "Intermediate to Advanced (beginner-friendly books are available)"));
        topics.add(new Topic("JavaScript",
                "The language of the web, used to make pages interactive and to build full applications.",
                "Interactive websites, web apps, server-side development (Node.js), mobile apps.",
                "Beginner to Intermediate"));
        topics.add(new Topic("Python",
                "A readable, general-purpose language known for its simple syntax and huge library ecosystem.",
                "Automation, data science, artificial intelligence, web back ends, scripting.",
                "Beginner-friendly"));
        topics.add(new Topic("HTML",
                "The markup language that defines the structure and content of every web page.",
                "Web page structure, forms, accessibility, search-engine-friendly content.",
                "Beginner (the first step in web development)"));
        topics.add(new Topic("CSS",
                "The style language that controls the layout, colors and appearance of web pages.",
                "Page layout, responsive design, animations, theming.",
                "Beginner to Intermediate"));
    }

    private void addBook(String category, String id, String title, String author,
                         Difficulty difficulty, String description) {
        books.add(new Book(id, title, author, category, difficulty, description));
    }

    private void seedBooks() {
        // ---- C++ ----
        addBook("C++", "CPP-01", "Programming: Principles and Practice Using C++", "Bjarne Stroustrup",
                Difficulty.BEGINNER, "A beginner-friendly introduction to programming concepts using C++ by its creator.");
        addBook("C++", "CPP-02", "C++ Crash Course", "Josh Lospinoso",
                Difficulty.BEGINNER, "A fast-paced, project-oriented introduction to modern C++.");
        addBook("C++", "CPP-03", "C++ Primer", "Stanley B. Lippman, Josee Lajoie, Barbara E. Moo",
                Difficulty.INTERMEDIATE, "A thorough, widely used guide to core C++ and the standard library.");
        addBook("C++", "CPP-04", "Effective Modern C++", "Scott Meyers",
                Difficulty.ADVANCED, "Practical guidelines for writing better C++11 and C++14 code.");
        addBook("C++", "CPP-05", "The C++ Programming Language", "Bjarne Stroustrup",
                Difficulty.ADVANCED, "The definitive reference to the language and its design, for experienced programmers.");

        // ---- JavaScript ----
        addBook("JavaScript", "JS-01", "Eloquent JavaScript", "Marijn Haverbeke",
                Difficulty.BEGINNER, "A modern introduction to programming through JavaScript, with exercises.");
        addBook("JavaScript", "JS-02", "JavaScript and jQuery: Interactive Front-End Web Development", "Jon Duckett",
                Difficulty.BEGINNER, "A visually rich guide to building interactive web pages.");
        addBook("JavaScript", "JS-03", "You Don't Know JS Yet", "Kyle Simpson",
                Difficulty.INTERMEDIATE, "A deep dive into how the core mechanisms of JavaScript really work.");
        addBook("JavaScript", "JS-04", "JavaScript: The Good Parts", "Douglas Crockford",
                Difficulty.INTERMEDIATE, "A concise look at the most reliable and elegant features of JavaScript.");
        addBook("JavaScript", "JS-05", "JavaScript: The Definitive Guide", "David Flanagan",
                Difficulty.ADVANCED, "An exhaustive reference to the language and the web platform APIs.");

        // ---- Python ----
        addBook("Python", "PY-01", "Python Crash Course", "Eric Matthes",
                Difficulty.BEGINNER, "A hands-on introduction with projects such as a game and a web app.");
        addBook("Python", "PY-02", "Automate the Boring Stuff with Python", "Al Sweigart",
                Difficulty.BEGINNER, "Practical programs that automate everyday computer tasks.");
        addBook("Python", "PY-03", "Python Cookbook", "David Beazley, Brian K. Jones",
                Difficulty.INTERMEDIATE, "Recipes for solving common problems with modern Python.");
        addBook("Python", "PY-04", "Effective Python", "Brett Slatkin",
                Difficulty.INTERMEDIATE, "Practical, bite-sized advice for writing better Python code.");
        addBook("Python", "PY-05", "Fluent Python", "Luciano Ramalho",
                Difficulty.ADVANCED, "An in-depth look at Python's features and idioms for experienced developers.");

        // ---- HTML ----
        addBook("HTML", "HTML-01", "HTML and CSS: Design and Build Websites", "Jon Duckett",
                Difficulty.BEGINNER, "A beautifully illustrated introduction to building web pages.");
        addBook("HTML", "HTML-02", "Head First HTML and CSS", "Elisabeth Robson, Eric Freeman",
                Difficulty.BEGINNER, "A visual, beginner-friendly approach to HTML and CSS fundamentals.");
        addBook("HTML", "HTML-03", "HTML5: The Missing Manual", "Matthew MacDonald",
                Difficulty.INTERMEDIATE, "A clear guide to modern HTML5 features and techniques.");
        addBook("HTML", "HTML-04", "HTML5 for Web Designers", "Jeremy Keith",
                Difficulty.INTERMEDIATE, "A short, practical guide to using HTML5 elements well.");
        addBook("HTML", "HTML-05", "Inclusive Components", "Heydon Pickering",
                Difficulty.ADVANCED, "Building accessible, robust interface components with semantic HTML.");

        // ---- CSS ----
        addBook("CSS", "CSS-01", "CSS: The Missing Manual", "David Sawyer McFarland",
                Difficulty.BEGINNER, "A friendly, step-by-step guide to styling web pages.");
        addBook("CSS", "CSS-02", "CSS Pocket Reference", "Eric A. Meyer",
                Difficulty.BEGINNER, "A quick reference to CSS properties and syntax.");
        addBook("CSS", "CSS-03", "Responsive Web Design", "Ethan Marcotte",
                Difficulty.INTERMEDIATE, "The foundational book on flexible layouts and media queries.");
        addBook("CSS", "CSS-04", "CSS in Depth", "Keith J. Grant",
                Difficulty.INTERMEDIATE, "A deeper look at layout, Flexbox, Grid and CSS architecture.");
        addBook("CSS", "CSS-05", "CSS Secrets", "Lea Verou",
                Difficulty.ADVANCED, "Tips and tricks for solving common CSS problems creatively.");
    }

    /**
     * Demo members so that some books start as BORROWED / RESERVED and the queue,
     * overdue and penalty features can be tried right away.
     * Demo login: demo@itam.edu / demo123 (has one overdue book and one reservation).
     */
    private void seedDemoData() {
        Member maria = registerMember("Maria Santos", "maria@itam.edu", "maria123");
        Member juan = registerMember("Juan Dela Cruz", "juan@itam.edu", "juan123");
        Member demo = registerMember("Demo Student", "demo@itam.edu", "demo123");

        seedBorrow(maria, "CPP-02", 3);
        seedBorrow(maria, "JS-01", 2);
        seedBorrow(juan, "PY-03", 5);
        seedBorrow(juan, "CSS-04", 1);
        seedReservation(juan, "JS-01", 1);
        seedBorrow(demo, "HTML-01", 12);      // loan period 7 days -> 5 days overdue
        seedReservation(demo, "CPP-02", 1);
    }

    private void seedBorrow(Member member, String bookId, int daysAgo) {
        Book book = findBookById(bookId);
        Loan loan = new Loan(member, book, getToday().minusDays(daysAgo));
        book.markBorrowed(loan.getDueDate());
        member.addLoan(loan);
    }

    private void seedReservation(Member member, String bookId, int daysAgo) {
        Book book = findBookById(bookId);
        Reservation reservation = new Reservation(member, book, getToday().minusDays(daysAgo));
        book.enqueue(reservation);
        member.addReservation(reservation);
    }
}