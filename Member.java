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
import java.util.Collections;
import java.util.List;

/**
 * A registered student. Keeps their loans, reservations and penalties.
 */
public class Member {
    public static final int MAX_ACTIVE_SLOTS = 3;

    private final String memberId;
    private final String name;
    private final String email;
    private final String password; // private: can be checked but never read
    private final List<Loan> loans = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<Penalty> penalties = new ArrayList<>();

    public Member(String memberId, String name, String email, String password) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public boolean verifyPassword(String attempt) {
        return password.equals(attempt);
    }

    // ---- loans ----
    public void addLoan(Loan loan) {
        loans.add(loan);
    }

    public List<Loan> getActiveLoans() {
        List<Loan> active = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isActive()) {
                active.add(loan);
            }
        }
        return active;
    }

    /** Total books borrowed across completed and active loans. */
    public int getTotalBooksBorrowed() {
        return loans.size();
    }

    public boolean hasOverdueBook(LocalDate today) {
        for (Loan loan : getActiveLoans()) {
            if (loan.getDaysOverdue(today) > 0) {
                return true;
            }
        }
        return false;
    }

    // ---- reservations ----
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    public List<Reservation> getActiveReservations() {
        List<Reservation> active = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.isActive()) {
                active.add(reservation);
            }
        }
        return active;
    }

    public boolean hasReservationFor(Book book) {
        for (Reservation reservation : getActiveReservations()) {
            if (reservation.getBook() == book) {
                return true;
            }
        }
        return false;
    }

    // ---- slots (polymorphic: every active BorrowingRecord uses one slot) ----
    public List<BorrowingRecord> getActiveRecords() {
        List<BorrowingRecord> active = new ArrayList<>();
        active.addAll(getActiveLoans());
        active.addAll(getActiveReservations());
        return Collections.unmodifiableList(active);
    }

    public int getBorrowedCount() { return getActiveLoans().size(); }
    public int getReservationCount() { return getActiveReservations().size(); }
    public int getActiveSlotsUsed() { return getActiveRecords().size(); }
    public int getAvailableSlots() { return MAX_ACTIVE_SLOTS - getActiveSlotsUsed(); }
    public boolean hasFreeSlot() { return getAvailableSlots() > 0; }

    // ---- penalties and account status ----
    public void addPenalty(Penalty penalty) {
        penalties.add(penalty);
    }

    public List<Penalty> getUnpaidPenalties() {
        List<Penalty> unpaid = new ArrayList<>();
        for (Penalty penalty : penalties) {
            if (!penalty.isPaid()) {
                unpaid.add(penalty);
            }
        }
        return unpaid;
    }

    public double getOutstandingPenaltyTotal() {
        double total = 0;
        for (Penalty penalty : getUnpaidPenalties()) {
            total += penalty.getAmount();
        }
        return total;
    }

    public void settleAllPenalties() {
        for (Penalty penalty : penalties) {
            if (!penalty.isPaid()) {
                penalty.markPaid();
            }
        }
    }

    public AccountStatus getAccountStatus() {
        boolean restricted = hasOverdueBook(LocalDate.now()) || getOutstandingPenaltyTotal() > 0;
        return restricted ? AccountStatus.RESTRICTED : AccountStatus.ACTIVE;
    }

    public String getRestrictionReason() {
        List<String> reasons = new ArrayList<>();
        if (hasOverdueBook(LocalDate.now())) {
            reasons.add("overdue book(s) not yet returned");
        }
        if (getOutstandingPenaltyTotal() > 0) {
            reasons.add(String.format("unpaid penalties of PHP %.2f", getOutstandingPenaltyTotal()));
        }
        return String.join("; ", reasons);
    }

    // ---- display ----
    public void displaySlotSummary() {
        System.out.println("RULE: Every member may hold a maximum of " + MAX_ACTIVE_SLOTS
                + " active book slots, shared between borrowed and reserved books.");
        System.out.println("      Example: 2 borrowed + 1 reserved = 3/3 slots used.");
        System.out.printf("Borrowed: %d | Reserved: %d | Active slots used: %d/%d | Slots available: %d%n",
                getBorrowedCount(), getReservationCount(), getActiveSlotsUsed(),
                MAX_ACTIVE_SLOTS, getAvailableSlots());
    }

    public void display() {
        AccountStatus status = getAccountStatus();
        System.out.println("Member ID        : " + memberId);
        System.out.println("Name             : " + name);
        System.out.println("Email            : " + email);
        System.out.println("Account status   : " + status);
        System.out.println("Borrowed books   : " + getBorrowedCount());
        System.out.println("Active reserv.   : " + getReservationCount());
        System.out.printf("Outstanding fines: PHP %.2f%n", getOutstandingPenaltyTotal());
        List<Penalty> unpaid = getUnpaidPenalties();
        if (!unpaid.isEmpty()) {
            System.out.println("Penalty details:");
            for (Penalty penalty : unpaid) {
                penalty.display();
            }
        }
        if (status == AccountStatus.RESTRICTED) {
            System.out.println("RESTRICTED because of: " + getRestrictionReason());
            System.out.println("Restricted accounts cannot borrow or reserve books.");
        }
    }
}