/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A borrowing transaction. Calculates the due date, overdue days and penalty.
 */
public class Loan extends BorrowingRecord {
    public static final int LOAN_PERIOD_DAYS = 7;
    public static final double PENALTY_PER_DAY = 5.00; // PHP

    private final LocalDate dueDate;
    private LocalDate returnDate;
    private double penaltyCharged;

    public Loan(Member member, Book book, LocalDate borrowDate) {
        super("LOAN", member, book, borrowDate, RecordStatus.ON_LOAN);
        this.dueDate = borrowDate.plusDays(LOAN_PERIOD_DAYS);
    }

    public LocalDate getBorrowDate() { return getDateCreated(); }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getPenaltyCharged() { return penaltyCharged; }

    @Override
    public boolean isActive() {
        return getStatus() == RecordStatus.ON_LOAN;
    }

    public long getDaysRemaining(LocalDate today) {
        return ChronoUnit.DAYS.between(today, dueDate);
    }

    public long getDaysOverdue(LocalDate asOf) {
        LocalDate reference = (returnDate != null) ? returnDate : asOf;
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, reference));
    }

    public double calculatePenalty(LocalDate asOf) {
        return getDaysOverdue(asOf) * PENALTY_PER_DAY;
    }

    /** Completes the loan and returns the penalty charged (0 if on time). */
    public double complete(LocalDate returnDate) {
        this.returnDate = returnDate;
        this.penaltyCharged = calculatePenalty(returnDate);
        setStatus(RecordStatus.RETURNED);
        return penaltyCharged;
    }

    @Override
    public String getRecordSummary() {
        LocalDate today = LocalDate.now();
        if (!isActive()) {
            return super.getBaseInfo() + "\n   Borrowed: " + DateUtil.format(getBorrowDate())
                    + " | Returned: " + DateUtil.format(returnDate)
                    + String.format(" | Penalty: PHP %.2f", penaltyCharged);
        }
        long remaining = getDaysRemaining(today);
        String timing;
        if (remaining > 0) {
            timing = remaining + " day(s) remaining";
        } else if (remaining == 0) {
            timing = "due today";
        } else {
            timing = Math.abs(remaining) + " day(s) overdue";
        }
        String state = remaining < 0 ? "OVERDUE" : "ON LOAN";
        return super.getBaseInfo()
                + "\n   Borrowed: " + DateUtil.format(getBorrowDate())
                + " | Due: " + DateUtil.format(dueDate) + " | " + timing
                + String.format("%n   Status: %s | Penalty: PHP %.2f", state, calculatePenalty(today));
    }
}