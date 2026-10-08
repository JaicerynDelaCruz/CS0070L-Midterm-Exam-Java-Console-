/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
import java.time.LocalDate;

/**
 * A place in a book's reservation queue. Manages its queue position and
 * expiration, and can be cancelled by the student.
 */
public class Reservation extends BorrowingRecord implements Cancellable {
    /** Days a READY book is held for the reserver before the reservation expires. */
    public static final int HOLD_DAYS = 3;

    private LocalDate expiryDate; // set only once the reservation becomes READY

    public Reservation(Member member, Book book, LocalDate reservationDate) {
        super("RSV", member, book, reservationDate, RecordStatus.WAITING);
    }

    public LocalDate getReservationDate() { return getDateCreated(); }
    public LocalDate getExpiryDate() { return expiryDate; }

    @Override
    public boolean isActive() {
        return getStatus() == RecordStatus.WAITING || getStatus() == RecordStatus.READY;
    }

    public boolean isReady() {
        return getStatus() == RecordStatus.READY;
    }

    public int getQueuePosition() {
        return getBook().getQueuePosition(this);
    }

    /** The book has come back: hold it for this member until the expiry date. */
    public void markReady(LocalDate today) {
        setStatus(RecordStatus.READY);
        expiryDate = today.plusDays(HOLD_DAYS);
    }

    public boolean isExpired(LocalDate today) {
        return isReady() && today.isAfter(expiryDate);
    }

    public void markExpired() {
        setStatus(RecordStatus.EXPIRED);
    }

    public void markFulfilled() {
        setStatus(RecordStatus.FULFILLED);
    }

    /** Only WAITING or READY reservations can still be cancelled. */
    @Override
    public boolean cancel() {
        if (!isActive()) {
            return false;
        }
        setStatus(RecordStatus.CANCELLED);
        return true;
    }

    @Override
    public String getRecordSummary() {
        if (!isActive()) {
            return super.getBaseInfo() + "\n   Reserved on: " + DateUtil.format(getReservationDate())
                    + " | Status: " + getStatus();
        }
        int position = getQueuePosition();
        String availability;
        if (isReady()) {
            availability = "NOW - pick it up by " + DateUtil.format(expiryDate)
                    + " (Borrow a Book, ID " + getBook().getBookId() + ")";
        } else {
            availability = DateUtil.format(getBook().getExpectedAvailabilityForPosition(position));
        }
        return super.getBaseInfo()
                + "\n   Reserved on: " + DateUtil.format(getReservationDate())
                + " | Queue position: " + position + " | Status: " + getStatus()
                + "\n   Expected availability: " + availability;
    }
}