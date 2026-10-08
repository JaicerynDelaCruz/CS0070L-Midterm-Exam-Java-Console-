/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
import java.time.LocalDate;
import java.util.List;

/**
 * Text-based user interface. Contains menus and prompts only;
 * all rules live in Library and the model classes.
 */
public class ConsoleUI {
    private static final String LINE = "=".repeat(66);
    private static final String THIN = "-".repeat(66);

    private final Library library;
    private final InputHelper input;
    private Member currentMember;

    public ConsoleUI(Library library, InputHelper input) {
        this.library = library;
        this.input = input;
    }

    // =====================================================================
    //  Start-up and main menu
    // =====================================================================
    public void start() {
        System.out.println(LINE);
        System.out.println("      iTAM PROGRAMMING RESOURCE BORROWING SYSTEM");
        System.out.println(LINE);
        System.out.println("Tip: demo account -> email: demo@itam.edu | password: demo123");

        if (!authenticate()) {
            printGoodbye();
            return;
        }
        mainMenu();
    }

    /** @return true once a member is logged in, false if the user chose to exit */
    private boolean authenticate() {
        while (true) {
            System.out.println("\n--- WELCOME ---");
            System.out.println("1. Register a new account");
            System.out.println("2. Log in");
            System.out.println("0. Exit");
            int choice = input.readInt("Choose an option: ", 0, 2);
            switch (choice) {
                case 1 -> {
                    if (register()) {
                        return true;
                    }
                }
                case 2 -> {
                    if (login()) {
                        return true;
                    }
                }
                default -> {
                    if (input.confirm("Are you sure you want to exit?")) {
                        return false;
                    }
                }
            }
        }
    }

    private boolean register() {
        System.out.println("\n--- REGISTER ---");
        String name = input.readNonEmpty("Full name: ");
        String email = input.readNonEmpty("Email: ");
        String password = input.readNonEmpty("Password (min. 6 characters): ");
        try {
            currentMember = library.registerMember(name, email, password);
            System.out.println("\nAccount created! Welcome, " + currentMember.getName() + ".");
            System.out.println("Your unique Member ID is: " + currentMember.getMemberId());
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    private boolean login() {
        System.out.println("\n--- LOG IN ---");
        String email = input.readNonEmpty("Email: ");
        String password = input.readNonEmpty("Password: ");
        Member member = library.login(email, password);
        if (member == null) {
            System.out.println("Incorrect email or password.");
            return false;
        }
        currentMember = member;
        System.out.println("\nWelcome back, " + member.getName() + "! (Member ID: " + member.getMemberId() + ")");
        return true;
    }

    private void mainMenu() {
        boolean running = true;
        while (running) {
            notifyExpiredReservations();
            System.out.println("\n" + LINE);
            System.out.println("                         MAIN MENU");
            System.out.println(LINE);
            System.out.println("1. Explore Programming Topics");
            System.out.println("2. Find a Book");
            System.out.println("3. Borrow a Book");
            System.out.println("4. My Borrowed Books");
            System.out.println("5. Return a Book");
            System.out.println("6. My Reservations");
            System.out.println("7. My Account");
            System.out.println("0. Exit");
            int choice = input.readInt("Enter your choice: ", 0, 7);
            switch (choice) {
                case 1 -> exploreTopics();
                case 2 -> findBook();
                case 3 -> borrowBookMenu();
                case 4 -> viewBorrowedBooks();
                case 5 -> returnBook();
                case 6 -> viewReservations();
                case 7 -> viewAccount();
                default -> running = !confirmExit();
            }
        }
    }

    /** Runtime polymorphism: each record prints its own summary via getRecordSummary(). */
    private void printRecords(List<? extends BorrowingRecord> records) {
        for (int i = 0; i < records.size(); i++) {
            System.out.println((i + 1) + ". " + records.get(i).getRecordSummary());
        }
    }

    private void notifyExpiredReservations() {
        for (Reservation expired : library.processExpiredReservations()) {
            if (expired.getMember() == currentMember) {
                System.out.println("\nNOTICE: Your reservation for \"" + expired.getBook().getTitle()
                        + "\" has EXPIRED because it was not picked up within "
                        + Reservation.HOLD_DAYS + " days.");
            }
        }
    }

    private boolean confirmExit() {
        if (input.confirm("Are you sure you want to exit?")) {
            printGoodbye();
            return true;
        }
        System.out.println("Returning to the main menu.");
        return false;
    }

    private void printGoodbye() {
        System.out.println("\nThank you for using the iTAM Programming Resource Borrowing System.");
        System.out.println("Keep learning and happy coding! Goodbye.");
    }

    // =====================================================================
    //  1. Explore Programming Topics
    // =====================================================================
    private void exploreTopics() {
        while (true) {
            System.out.println("\n=== EXPLORE PROGRAMMING TOPICS ===");
            Topic topic = chooseTopic("Select a topic to learn more (0 to go back): ");
            if (topic == null) {
                return;
            }
            topic.display();
            if (input.confirm("\nView the books under " + topic.getName() + "?")) {
                printBookList(library.getBooksByCategory(topic.getName()));
                offerBookAction();
            }
        }
    }

    /** @return the chosen topic, or null if the user chose 0 */
    private Topic chooseTopic(String prompt) {
        List<Topic> topics = library.getTopics();
        for (int i = 0; i < topics.size(); i++) {
            System.out.println((i + 1) + ". " + topics.get(i).getName());
        }
        int choice = input.readInt(prompt, 0, topics.size());
        return choice == 0 ? null : topics.get(choice - 1);
    }

    // =====================================================================
    //  2. Find a Book
    // =====================================================================
    private void findBook() {
        while (true) {
            System.out.println("\n=== FIND A BOOK ===");
            System.out.println("1. Browse by category");
            System.out.println("2. View all 25 books");
            System.out.println("3. Search by title, author, category or level");
            System.out.println("0. Back to main menu");
            int choice = input.readInt("Choose an option: ", 0, 3);
            switch (choice) {
                case 1 -> {
                    Topic topic = chooseTopic("Select a category (0 to cancel): ");
                    if (topic != null) {
                        printBookList(library.getBooksByCategory(topic.getName()));
                        offerBookAction();
                    }
                }
                case 2 -> {
                    printBookList(library.getBooks());
                    offerBookAction();
                }
                case 3 -> {
                    String keyword = input.readNonEmpty("Enter a keyword: ");
                    printBookList(library.searchBooks(keyword));
                    offerBookAction();
                }
                default -> {
                    return;
                }
            }
        }
    }

    private void printBookList(List<Book> list) {
        System.out.println("\n" + THIN);
        if (list.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        for (Book book : list) {
            book.display();
            System.out.println();
        }
        System.out.println(THIN);
    }

    /** After a list is shown, let the user jump straight to borrowing or reserving. */
    private void offerBookAction() {
        String id = input.readLine("Enter a Book ID to borrow/reserve it (or press Enter to go back): ").trim();
        if (id.isEmpty()) {
            return;
        }
        Book book = library.findBookById(id);
        if (book == null) {
            System.out.println("No book found with ID \"" + id + "\".");
            return;
        }
        handleBookSelection(book);
    }

    // =====================================================================
    //  3. Borrow a Book (and reserve if unavailable)
    // =====================================================================
    private void borrowBookMenu() {
        System.out.println("\n=== BORROW A BOOK ===");
        while (true) {
            String id = input.readLine("Enter Book ID (type LIST to view all books, Enter to cancel): ").trim();
            if (id.isEmpty()) {
                return;
            }
            if (id.equalsIgnoreCase("LIST")) {
                printBookList(library.getBooks());
                continue;
            }
            Book book = library.findBookById(id);
            if (book == null) {
                System.out.println("No book found with ID \"" + id + "\". Try again.");
                continue;
            }
            handleBookSelection(book);
            return;
        }
    }

    private void handleBookSelection(Book book) {
        System.out.println();
        book.display();
        if (book.isAvailable() || book.isReadyFor(currentMember)) {
            borrowFlow(book);
        } else {
            reserveFlow(book);
        }
    }

    private void borrowFlow(Book book) {
        String problem = library.checkBorrowEligibility(currentMember, book);
        if (problem != null) {
            System.out.println("\nCannot borrow: " + problem);
            return;
        }
        boolean heldForMe = book.isReadyFor(currentMember);
        int slotsAfter = heldForMe ? currentMember.getActiveSlotsUsed() : currentMember.getActiveSlotsUsed() + 1;
        System.out.println("\n" + THIN);
        System.out.println("Borrow date : " + DateUtil.format(library.getToday())
                + " (today)");
        System.out.println("Due date    : " + DateUtil.format(library.previewDueDate())
                + " (" + Loan.LOAN_PERIOD_DAYS + "-day loan)");
        System.out.println("Slots used  : " + currentMember.getActiveSlotsUsed() + "/" + Member.MAX_ACTIVE_SLOTS
                + " -> " + slotsAfter + "/" + Member.MAX_ACTIVE_SLOTS);
        System.out.println(THIN);
        if (!input.confirm("Confirm borrowing \"" + book.getTitle() + "\"?")) {
            System.out.println("Borrowing cancelled.");
            return;
        }
        Loan record = library.borrowBook(currentMember, book);
        System.out.println("\nSUCCESS! You borrowed \"" + book.getTitle() + "\".");
        System.out.println("Please return it on or before " + DateUtil.format(record.getDueDate())
                + " to avoid a penalty of PHP " + String.format("%.2f", Loan.PENALTY_PER_DAY) + " per day.");
        System.out.println("Book status is now: " + book.getStatus());
    }

    private void reserveFlow(Book book) {
        System.out.println("\nThis book is currently unavailable, but you may join its reservation queue.");
        String problem = library.checkReserveEligibility(currentMember, book);
        if (problem != null) {
            System.out.println("Cannot reserve: " + problem);
            return;
        }
        int position = book.getQueueSize() + 1;
        System.out.println(THIN);
        System.out.println("Your queue position   : #" + position);
        System.out.println("Expected availability : "
                + DateUtil.format(book.getExpectedAvailabilityForPosition(position)));
        System.out.println("Slots used            : " + currentMember.getActiveSlotsUsed() + "/" + Member.MAX_ACTIVE_SLOTS
                + " -> " + (currentMember.getActiveSlotsUsed() + 1) + "/" + Member.MAX_ACTIVE_SLOTS);
        System.out.println(THIN);
        if (!input.confirm("Reserve \"" + book.getTitle() + "\"?")) {
            System.out.println("Reservation cancelled.");
            return;
        }
        library.reserveBook(currentMember, book);
        System.out.println("\nSUCCESS! You are #" + position + " in the queue for \"" + book.getTitle() + "\".");
        System.out.println("This reservation now uses one of your " + Member.MAX_ACTIVE_SLOTS + " active slots ("
                + currentMember.getActiveSlotsUsed() + "/" + Member.MAX_ACTIVE_SLOTS + ").");
    }

    // =====================================================================
    //  4. My Borrowed Books
    // =====================================================================
    private void viewBorrowedBooks() {
        System.out.println("\n=== MY BORROWED BOOKS ===");
        currentMember.displaySlotSummary();
        System.out.println(THIN);
        List<Loan> active = currentMember.getActiveLoans();
        if (active.isEmpty()) {
            System.out.println("You have no borrowed books right now.");
        } else {
            printRecords(active);
        }
        System.out.println(THIN);
        System.out.println("Total Books Borrowed (completed + active): " + currentMember.getTotalBooksBorrowed());
        input.pause();
    }

    // =====================================================================
    //  5. Return a Book
    // =====================================================================
    private void returnBook() {
        System.out.println("\n=== RETURN A BOOK ===");
        List<Loan> active = currentMember.getActiveLoans();
        if (active.isEmpty()) {
            System.out.println("You have no borrowed books to return.");
            return;
        }
        printRecords(active);
        int choice = input.readInt("Select the book to return (0 to cancel): ", 0, active.size());
        if (choice == 0) {
            return;
        }
        Loan record = active.get(choice - 1);
        if (!input.confirm("Return \"" + record.getBook().getTitle() + "\" now?")) {
            System.out.println("Return cancelled.");
            return;
        }
        Reservation nextInLine = library.returnBook(record);
        LocalDate returned = record.getReturnDate();
        System.out.println("\n" + THIN);
        System.out.println("Book         : " + record.getBook().getTitle());
        System.out.println("Due date     : " + DateUtil.format(record.getDueDate()));
        System.out.println("Return date  : " + DateUtil.format(returned));
        long late = record.getDaysOverdue(returned);
        if (late > 0) {
            System.out.println("Result       : OVERDUE by " + late + " day(s)");
            System.out.printf("Penalty      : PHP %.2f (added to your account as an outstanding penalty)%n",
                    record.getPenaltyCharged());
        } else {
            System.out.println("Result       : Returned on time");
            System.out.println("Penalty      : PHP 0.00");
        }
        System.out.println(THIN);
        System.out.println("Transaction completed. Slots used: " + currentMember.getActiveSlotsUsed()
                + "/" + Member.MAX_ACTIVE_SLOTS);
        if (nextInLine != null) {
            System.out.println("The book is now RESERVED for the next eligible member in its queue.");
        } else {
            System.out.println("The book is now AVAILABLE.");
        }
    }

    // =====================================================================
    //  6. My Reservations
    // =====================================================================
    private void viewReservations() {
        System.out.println("\n=== MY RESERVATIONS ===");
        currentMember.displaySlotSummary();
        System.out.println(THIN);
        List<Reservation> list = currentMember.getActiveReservations();
        if (list.isEmpty()) {
            System.out.println("You have no active reservations.");
            return;
        }
        printRecords(list);
        System.out.println(THIN);
        int choice = input.readInt("Enter a reservation number to cancel (0 to go back): ", 0, list.size());
        if (choice == 0) {
            return;
        }
        Reservation reservation = list.get(choice - 1);
        if (!input.confirm("Cancel your reservation for \"" + reservation.getBook().getTitle() + "\"?")) {
            System.out.println("Cancellation aborted.");
            return;
        }
        if (!library.cancelReservation(reservation)) {
            System.out.println("This reservation can no longer be cancelled.");
            return;
        }
        System.out.println("Reservation cancelled. Slots used: " + currentMember.getActiveSlotsUsed()
                + "/" + Member.MAX_ACTIVE_SLOTS);
    }

    // =====================================================================
    //  7. My Account
    // =====================================================================
    private void viewAccount() {
        System.out.println("\n=== MY ACCOUNT ===");
        currentMember.display();
        if (currentMember.getOutstandingPenaltyTotal() > 0
                && input.confirm("\nSettle all outstanding penalties now (simulated counter payment)?")) {
            currentMember.settleAllPenalties();
            System.out.println("Penalties settled. Account status: " + currentMember.getAccountStatus());
        }
        input.pause();
    }
}