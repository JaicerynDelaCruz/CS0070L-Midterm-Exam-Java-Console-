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
 * A charge issued to a member for a late return.
 */
public class Penalty {
    private final String reason;
    private final double amount;
    private final LocalDate dateIssued;
    private boolean paid;

    public Penalty(String reason, double amount, LocalDate dateIssued) {
        this.reason = reason;
        this.amount = amount;
        this.dateIssued = dateIssued;
        this.paid = false;
    }

    public String getReason() { return reason; }
    public double getAmount() { return amount; }
    public boolean isPaid() { return paid; }

    public void markPaid() {
        this.paid = true;
    }

    public void display() {
        System.out.printf("   - PHP %.2f | %s (issued %s)%n", amount, reason, DateUtil.format(dateIssued));
    }
}