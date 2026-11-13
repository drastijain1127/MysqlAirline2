import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class AirlineReservationSystemTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static void assertEquals(Object expected, Object actual, String testName) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " | Expected: " + expected + ", Got: " + actual);
            testsFailed++;
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " | Expected true, got false");
            testsFailed++;
        }
    }

    // Helper to get private field via reflection
    @SuppressWarnings("unchecked")
    private static <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(target);
    }

    public static void testModelClasses() {
        System.out.println("\n--- Testing Model Classes ---");

        // 1. Flight test
        Flight flight = new Flight("AI-999", "Delhi", "Goa", "09:00", "11:30", 5500.0, 10);
        assertEquals("AI-999", flight.flightNo, "Flight number correctly assigned");
        assertEquals("Delhi", flight.source, "Flight source correctly assigned");
        assertEquals("Goa", flight.destination, "Flight destination correctly assigned");
        assertEquals(5500.0, flight.fare, "Flight fare correctly assigned");
        assertEquals(10, flight.seatsAvailable, "Seats available count correct");
        assertTrue(flight.toString().contains("AI-999") && flight.toString().contains("Delhi -> Goa"), "Flight toString format valid");

        // 2. Passenger test
        Passenger passenger = new Passenger(1, "Alice", 28, "Female", "9876543210");
        assertEquals(1, passenger.id, "Passenger ID correctly assigned");
        assertEquals("Alice", passenger.name, "Passenger Name correctly assigned");
        assertEquals(28, passenger.age, "Passenger Age correctly assigned");
        assertEquals("Female", passenger.gender, "Passenger Gender correctly assigned");
        assertEquals("9876543210", passenger.contact, "Passenger Contact correctly assigned");
        assertTrue(passenger.toString().contains("PID:1") && passenger.toString().contains("Alice"), "Passenger toString format valid");

        // 3. Booking test
        Booking booking = new Booking("TKT1001", "Alice", "AI-999", "Economy", "E10");
        assertEquals("TKT1001", booking.ticketId, "Ticket ID correctly assigned");
        assertEquals("Alice", booking.passengerName, "Booking passenger name correctly assigned");
        assertEquals("AI-999", booking.flightNo, "Booking flight number correctly assigned");
        assertEquals("Economy", booking.seatClass, "Booking seat class correctly assigned");
        assertEquals("E10", booking.seatNo, "Booking seat number correctly assigned");
        assertTrue(!booking.cancelled, "Initial booking status is not cancelled");
        assertTrue(booking.toString().contains("CONFIRMED"), "Initial booking toString says CONFIRMED");

        booking.cancelled = true;
        assertTrue(booking.toString().contains("CANCELLED"), "Cancelled booking toString says CANCELLED");
    }

    public static void testSystemGUIAndLogic() throws Exception {
        System.out.println("\n--- Testing AirlineReservationSystem GUI & Business Logic ---");

        AirlineReservationSystem app = new AirlineReservationSystem();
        app.setVisible(false); // keep invisible during automated testing

        // Verify default flight schedule
        ArrayList<Flight> flights = getPrivateField(app, "flights");
        assertEquals(5, flights.size(), "Default 5 flights loaded in schedule");
        assertEquals("AI-101", flights.get(0).flightNo, "First flight is AI-101");

        // Extract UI controls via reflection
        TextField tfName = getPrivateField(app, "tfName");
        TextField tfAge = getPrivateField(app, "tfAge");
        TextField tfContact = getPrivateField(app, "tfContact");
        Label lblAddStatus = getPrivateField(app, "lblAddStatus");

        TextField tfPassengerName = getPrivateField(app, "tfPassengerName");
        Choice chFlight = getPrivateField(app, "chFlight");
        Choice chClass = getPrivateField(app, "chClass");
        Label lblBookStatus = getPrivateField(app, "lblBookStatus");

        TextField tfTicketId = getPrivateField(app, "tfTicketId");
        Label lblCancelStatus = getPrivateField(app, "lblCancelStatus");
        TextArea taTicketSearchResult = getPrivateField(app, "taTicketSearchResult");
        TextArea taPassengers = getPrivateField(app, "taPassengers");

        // Test 1: Validation failure on blank fields for adding passenger
        tfName.setText("");
        tfAge.setText("");
        tfContact.setText("");
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "SUBMIT_PASSENGER"));
        assertEquals("Please fill all fields.", lblAddStatus.getText(), "Validation fails on empty passenger fields");

        // Test 2: Validation failure on invalid age
        tfName.setText("John Doe");
        tfAge.setText("not-a-number");
        tfContact.setText("9999999999");
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "SUBMIT_PASSENGER"));
        assertEquals("Age must be a number.", lblAddStatus.getText(), "Validation fails on non-numeric age");

        // Test 3: Successfully add passenger
        tfName.setText("John Doe");
        tfAge.setText("30");
        tfContact.setText("9999999999");
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "SUBMIT_PASSENGER"));
        assertTrue(lblAddStatus.getText().contains("Passenger added successfully with ID: 1"), "Passenger 1 successfully added");
        ArrayList<Passenger> passengers = getPrivateField(app, "passengers");
        assertEquals(1, passengers.size(), "Passenger list size is 1");
        assertEquals("John Doe", passengers.get(0).name, "Passenger name saved correctly");

        // Test 4: Book ticket validation failure on empty name
        tfPassengerName.setText("");
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "SUBMIT_BOOKING"));
        assertEquals("Please enter passenger name.", lblBookStatus.getText(), "Booking validation fails on empty name");

        // Test 5: Successful booking
        int initialSeats = flights.get(0).seatsAvailable;
        tfPassengerName.setText("John Doe");
        chFlight.select(0); // AI-101
        chClass.select("Economy");
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "SUBMIT_BOOKING"));
        assertTrue(lblBookStatus.getText().contains("Ticket booked! ID: TKT1001"), "Ticket TKT1001 booked successfully");
        assertEquals(initialSeats - 1, flights.get(0).seatsAvailable, "Flight seats decreased by 1");

        ArrayList<Booking> bookings = getPrivateField(app, "bookings");
        assertEquals(1, bookings.size(), "Bookings list size is 1");
        Booking b1 = bookings.get(0);
        assertEquals("TKT1001", b1.ticketId, "Booking ticket ID is TKT1001");
        assertEquals("John Doe", b1.passengerName, "Booking name is John Doe");

        // Test 6: Refresh passenger & booking view
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "REFRESH_VIEW"));
        assertTrue(taPassengers.getText().contains("John Doe") && taPassengers.getText().contains("TKT1001"), "Refresh view shows passenger and booking");

        // Test 7: Search ticket with empty input
        tfTicketId.setText("");
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "SEARCH_TICKET"));
        assertEquals("Enter a ticket ID to search.", lblCancelStatus.getText(), "Search fails on empty ticket ID");

        // Test 8: Search ticket with non-existing ticket
        tfTicketId.setText("TKT9999");
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "SEARCH_TICKET"));
        assertEquals("Ticket ID not found.", lblCancelStatus.getText(), "Search notifies when ticket ID not found");

        // Test 9: Search ticket with existing ticket
        tfTicketId.setText("TKT1001");
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "SEARCH_TICKET"));
        assertEquals("Ticket found.", lblCancelStatus.getText(), "Ticket TKT1001 successfully found");
        assertTrue(taTicketSearchResult.getText().contains("TKT1001"), "Search result display shows ticket info");

        // Test 10: Cancel ticket
        int seatsBeforeCancel = flights.get(0).seatsAvailable;
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "CANCEL_TICKET_ACTION"));
        assertEquals("Ticket cancelled successfully.", lblCancelStatus.getText(), "Ticket cancelled successfully");
        assertTrue(b1.cancelled, "Booking object status updated to cancelled");
        assertEquals(seatsBeforeCancel + 1, flights.get(0).seatsAvailable, "Flight seat restored upon cancellation");

        // Test 11: Attempt to cancel already cancelled ticket
        app.actionPerformed(new ActionEvent(app, ActionEvent.ACTION_PERFORMED, "CANCEL_TICKET_ACTION"));
        assertEquals("Ticket already cancelled.", lblCancelStatus.getText(), "Prevents re-cancellation of already cancelled ticket");

        // Dispose frame to release native resources
        app.dispose();
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   Airline Reservation System - Automated Test   ");
        System.out.println("=================================================");

        try {
            testModelClasses();
            testSystemGUIAndLogic();
        } catch (Exception e) {
            e.printStackTrace();
            testsFailed++;
        }

        System.out.println("\n=================================================");
        System.out.println("Test Results: " + testsPassed + " Passed, " + testsFailed + " Failed");
        System.out.println("=================================================");

        if (testsFailed > 0) {
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY!");
        }
    }
}
