/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * One of the 25 catalog books. Manages its own status and reservation queue.
 */
public class Book {
    private final String bookId;
    private final String title;
    private final String author;
    private final String category;
    private final Difficulty difficulty;
    private final String description;
    private BookStatus status;
    private LocalDate expectedAvailableDate;
    private final LinkedList<Reservation> reservationQueue = new LinkedList<>();

    public Book(String bookId, String title, String author, String category,
                Difficulty difficulty, String description) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.difficulty = difficulty;
        this.description = description;
        this.status = BookStatus.AVAILABLE;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public Difficulty getDifficulty() { return difficulty; }
    public String getDescription() { return description; }
    public BookStatus getStatus() { return status; }
    public LocalDate getExpectedAvailableDate() { return expectedAvailableDate; }
    public int getQueueSize() { return reservationQueue.size(); }

    public List<Reservation> getReservationQueue() {
        return Collections.unmodifiableList(reservationQueue);
    }

    public boolean isAvailable() {
        return status == BookStatus.AVAILABLE;
    }

    /** True when the book is being held for this member (first in queue, status RESERVED). */
    public boolean isReadyFor(Member member) {
        return status == BookStatus.RESERVED
                && !reservationQueue.isEmpty()
                && reservationQueue.getFirst().getMember() == member;
    }

    public int getQueuePosition(Reservation reservation) {
        return reservationQueue.indexOf(reservation) + 1;
    }

    /** Estimated date the book reaches the given queue position (1 = first in line). */
    public LocalDate getExpectedAvailabilityForPosition(int position) {
        LocalDate base = (expectedAvailableDate != null) ? expectedAvailableDate : LocalDate.now();
        return base.plusDays((long) (position - 1) * Loan.LOAN_PERIOD_DAYS);
    }

    public void markBorrowed(LocalDate dueDate) {
        this.status = BookStatus.BORROWED;
        this.expectedAvailableDate = dueDate;
    }

    public void enqueue(Reservation reservation) {
        reservationQueue.addLast(reservation);
    }

    /** Removes and returns the head of the queue (when the reserver picks the book up). */
    public Reservation claimReservation() {
        return reservationQueue.pollFirst();
    }

    /** After a return: book goes to the next reserver (RESERVED) or becomes AVAILABLE. */
    public Reservation releaseAfterReturn(LocalDate today) {
        return promoteNextOrRelease(today);
    }

    public void removeReservation(Reservation reservation, LocalDate today) {
        boolean wasHead = !reservationQueue.isEmpty() && reservationQueue.getFirst() == reservation;
        reservationQueue.remove(reservation);
        if (wasHead && status == BookStatus.RESERVED) {
            promoteNextOrRelease(today);
        }
    }

    private Reservation promoteNextOrRelease(LocalDate today) {
        if (reservationQueue.isEmpty()) {
            status = BookStatus.AVAILABLE;
            expectedAvailableDate = null;
            return null;
        }
        Reservation next = reservationQueue.getFirst();
        next.markReady(today);
        status = BookStatus.RESERVED;
        expectedAvailableDate = today;
        return next;
    }

    public void display() {
        System.out.printf("[%s] %s%n", bookId, title);
        System.out.printf("     Author: %s | Category: %s | Level: %s%n", author, category, difficulty);
        System.out.printf("     %s%n", description);
        StringBuilder line = new StringBuilder("     Status: ").append(status);
        if (status == BookStatus.BORROWED) {
            line.append(" | Expected available: ")
                .append(DateUtil.format(getExpectedAvailabilityForPosition(1)));
        } else if (status == BookStatus.RESERVED) {
            line.append(" (held for the next member in queue) | Expected available: ")
                .append(DateUtil.format(getExpectedAvailabilityForPosition(2)));
        }
        if (!reservationQueue.isEmpty()) {
            line.append(" | In queue: ").append(reservationQueue.size());
        }
        System.out.println(line);
    }
}