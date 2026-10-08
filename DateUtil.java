/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Small utility class for formatting dates consistently across the program.
 */
public final class DateUtil {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    private DateUtil() { }

    public static String format(LocalDate date) {
        return date == null ? "N/A" : date.format(FORMAT);
    }
}