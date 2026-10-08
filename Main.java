/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
/**
 * Entry point of the iTAM Programming Resource Borrowing System.
 */
public class Main {
    public static void main(String[] args) {
        Library library = new Library();
        InputHelper input = new InputHelper();
        new ConsoleUI(library, input).start();
        input.close();
    }
}