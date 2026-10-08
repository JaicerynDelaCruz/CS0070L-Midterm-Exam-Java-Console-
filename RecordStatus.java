/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
/**
 * Status shared by all borrowing-related records.
 * Loans use ON_LOAN / RETURNED; reservations use WAITING / READY /
 * FULFILLED / CANCELLED / EXPIRED.
 */
public enum RecordStatus {
    ON_LOAN, RETURNED,
    WAITING, READY, FULFILLED, CANCELLED, EXPIRED
}