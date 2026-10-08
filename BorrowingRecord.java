
import java.time.LocalDate;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
public abstract class BorrowingRecord {
    private static int nextNumber = 1;

    private final String recordId;
    private final Member member;
    private final Book book;
    private final LocalDate dateCreated;
    private RecordStatus status;

    protected BorrowingRecord(String prefix, Member member, Book book,
                              LocalDate dateCreated, RecordStatus initialStatus) {
        this.recordId = String.format("%s-%04d", prefix, nextNumber++);
        this.member = member;
        this.book = book;
        this.dateCreated = dateCreated;
        this.status = initialStatus;
    }

    public String getRecordId() { return recordId; }
    public Member getMember() { return member; }
    public Book getBook() { return book; }
    public LocalDate getDateCreated() { return dateCreated; }
    public RecordStatus getStatus() { return status; }

    /** Only the record and its subclasses may change its status. */
    protected void setStatus(RecordStatus status) {
        this.status = status;
    }

    /** True while the record still occupies one of the member's active slots. */
    public abstract boolean isActive();

    /** Each subclass describes itself differently (runtime polymorphism). */
    public abstract String getRecordSummary();

    /** Shared first line used by both subclasses' summaries. */
    protected String getBaseInfo() {
        return book.getTitle() + " [" + book.getCategory() + "] (Record ID: " + recordId + ")";
    }

    @Override
    public String toString() {
        return getRecordSummary();
    }
}