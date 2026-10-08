/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
/**
 * Contract for records that the student is allowed to cancel.
 */
public interface Cancellable {
    /**
     * Cancels the record if it is still eligible for cancellation.
     * @return true if it was cancelled, false if it was not eligible
     */
    boolean cancel();
}