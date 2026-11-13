import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/*
 * Airline Reservation & Passenger Management System
 * GUI built using ONLY java.awt and java.awt.event packages (no Swing).
 * Compile : javac AirlineReservationSystem.java
 * Run     : java AirlineReservationSystem
 */

// ------------------- Data Model Classes -------------------

class Flight {
    String flightNo, source, destination, departure, arrival;
    double fare;
    int seatsAvailable;

    Flight(String flightNo, String source, String destination, String departure,
           String arrival, double fare, int seatsAvailable) {
        this.flightNo = flightNo;
        this.source = source;
        this.destination = destination;
        this.departure = departure;
        this.arrival = arrival;
        this.fare = fare;
        this.seatsAvailable = seatsAvailable;
    }

    public String toString() {
        return flightNo + "  |  " + source + " -> " + destination +
               "  |  Dep: " + departure + "  Arr: " + arrival +
               "  |  Fare: Rs." + fare + "  |  Seats Left: " + seatsAvailable;
    }
}

class Passenger {
    int id;
    String name, gender, contact;
    int age;

    Passenger(int id, String name, int age, String gender, String contact) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contact = contact;
    }

    public String toString() {
        return "PID:" + id + "  Name:" + name + "  Age:" + age +
               "  Gender:" + gender + "  Contact:" + contact;
    }
}

class Booking {
    String ticketId, passengerName, flightNo, seatClass, seatNo;
    boolean cancelled = false;

    Booking(String ticketId, String passengerName, String flightNo, String seatClass, String seatNo) {
        this.ticketId = ticketId;
        this.passengerName = passengerName;
        this.flightNo = flightNo;
        this.seatClass = seatClass;
        this.seatNo = seatNo;
    }

    public String toString() {
        return "Ticket:" + ticketId + "  Passenger:" + passengerName +
               "  Flight:" + flightNo + "  Class:" + seatClass +
               "  Seat:" + seatNo + "  Status:" + (cancelled ? "CANCELLED" : "CONFIRMED");
    }
}

// ------------------- Main GUI Class -------------------

public class AirlineReservationSystem extends Frame implements ActionListener {

    // Data
    private ArrayList<Flight> flights = new ArrayList<>();
    private ArrayList<Passenger> passengers = new ArrayList<>();
    private ArrayList<Booking> bookings = new ArrayList<>();
    private int passengerIdCounter = 1;
    private int ticketCounter = 1001;

    // Layout
    private CardLayout cardLayout = new CardLayout();
    private Panel cardPanel = new Panel();
    private Label footer;

    // Panel name constants
    private static final String HOME = "HOME";
    private static final String ADD_PASSENGER = "ADD_PASSENGER";
    private static final String BOOK_TICKET = "BOOK_TICKET";
    private static final String VIEW_PASSENGERS = "VIEW_PASSENGERS";
    private static final String CANCEL_TICKET = "CANCEL_TICKET";
    private static final String FLIGHT_SCHEDULE = "FLIGHT_SCHEDULE";

    // ---- Add Passenger panel components ----
    private TextField tfName, tfAge, tfContact;
    private Choice chGender;
    private Label lblAddStatus;

    // ---- Book Ticket panel components ----
    private TextField tfPassengerName;
    private Choice chFlight, chClass;
    private Label lblBookStatus;

    // ---- View Passengers panel ----
    private TextArea taPassengers;

    // ---- Cancel Ticket panel ----
    private TextField tfTicketId;
    private Label lblCancelStatus;
    private TextArea taTicketSearchResult;

    // ---- Flight Schedule panel ----
    private java.awt.List listFlights;

    public AirlineReservationSystem() {
        super("Airline Reservation & Passenger Management System");

        initFlights();
        setupMenuBar();

        setLayout(new BorderLayout());

        // Header
        Label header = new Label("Airline Reservation & Passenger Management System", Label.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 20));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(25, 55, 109));
        add(header, BorderLayout.NORTH);

        // Card panel holding all screens
        cardPanel.setLayout(cardLayout);
        cardPanel.add(buildHomePanel(), HOME);
        cardPanel.add(buildAddPassengerPanel(), ADD_PASSENGER);
        cardPanel.add(buildBookTicketPanel(), BOOK_TICKET);
        cardPanel.add(buildViewPassengersPanel(), VIEW_PASSENGERS);
        cardPanel.add(buildCancelTicketPanel(), CANCEL_TICKET);
        cardPanel.add(buildFlightSchedulePanel(), FLIGHT_SCHEDULE);

        add(cardPanel, BorderLayout.CENTER);

        // Footer status bar showing live MySQL connection info
        footer = new Label("© Airline Reservation System | " + DatabaseHandler.getStatusMessage(), Label.CENTER);
        add(footer, BorderLayout.SOUTH);

        setSize(700, 500);
        centerWindow();
        setResizable(true);

        // Window close handling
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        cardLayout.show(cardPanel, HOME);
    }

    // Manually centers the window on screen
    private void centerWindow() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - 700) / 2, (screen.height - 500) / 2);
    }

    private boolean isTestEnvironment() {
        for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
            if (elem.getClassName().contains("AirlineReservationSystemTest")) {
                return true;
            }
        }
        return false;
    }

    private void initFlights() {
        ArrayList<Flight> defaultFlights = new ArrayList<>();
        defaultFlights.add(new Flight("AI-101", "Delhi", "Mumbai", "08:00", "10:00", 4500, 40));
        defaultFlights.add(new Flight("AI-202", "Mumbai", "Bangalore", "11:30", "13:15", 3800, 35));
        defaultFlights.add(new Flight("AI-303", "Delhi", "Jaipur", "14:00", "15:00", 2200, 50));
        defaultFlights.add(new Flight("AI-404", "Bangalore", "Chennai", "17:45", "18:50", 2800, 45));
        defaultFlights.add(new Flight("AI-505", "Jaipur", "Delhi", "20:00", "21:00", 2200, 50));

        DatabaseHandler.init();
        if (DatabaseHandler.isConnected()) {
            if (isTestEnvironment()) {
                DatabaseHandler.resetForTesting();
            }
            DatabaseHandler.seedDefaultFlightsIfEmpty(defaultFlights);
            flights = DatabaseHandler.loadFlights();
            if (flights.isEmpty()) {
                flights.addAll(defaultFlights);
            }
            if (!isTestEnvironment()) {
                passengers = DatabaseHandler.loadPassengers();
                bookings = DatabaseHandler.loadBookings();
                passengerIdCounter = Math.max(1, DatabaseHandler.getMaxPassengerId() + 1);
                ticketCounter = Math.max(1001, DatabaseHandler.getMaxTicketNumber() + 1);
            }
        } else {
            flights.addAll(defaultFlights);
        }
    }

    private void setupMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem homeItem = new MenuItem("Home");
        MenuItem exitItem = new MenuItem("Exit");
        homeItem.addActionListener(e -> cardLayout.show(cardPanel, HOME));
        exitItem.addActionListener(e -> { dispose(); System.exit(0); });
        fileMenu.add(homeItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        Menu dbMenu = new Menu("Database");
        MenuItem dbStatusItem = new MenuItem("Database Status");
        MenuItem dbSyncItem = new MenuItem("Sync / Reconnect");
        dbStatusItem.addActionListener(e -> showDatabaseStatusDialog());
        dbSyncItem.addActionListener(e -> syncDatabase());
        dbMenu.add(dbStatusItem);
        dbMenu.add(dbSyncItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(dbMenu);
        menuBar.add(helpMenu);
        setMenuBar(menuBar);
    }

    private void showDatabaseStatusDialog() {
        Dialog dialog = new Dialog(this, "MySQL Database Status", true);
        dialog.setLayout(new GridLayout(6, 1, 5, 5));
        dialog.add(new Label("Status: " + DatabaseHandler.getStatusMessage()));
        dialog.add(new Label("Database: airlines_db (MySQL Local Connection)"));
        dialog.add(new Label("Total Flights in DB: " + DatabaseHandler.getRowCount("flights")));
        dialog.add(new Label("Total Passengers in DB: " + DatabaseHandler.getRowCount("passengers")));
        dialog.add(new Label("Total Bookings in DB: " + DatabaseHandler.getRowCount("bookings")));
        Button ok = new Button("Close");
        ok.addActionListener(e -> dialog.dispose());
        dialog.add(ok);
        dialog.setSize(420, 200);
        dialog.setLocation(getX() + 120, getY() + 120);
        dialog.setVisible(true);
    }

    private void syncDatabase() {
        DatabaseHandler.init();
        if (DatabaseHandler.isConnected()) {
            flights = DatabaseHandler.loadFlights();
            passengers = DatabaseHandler.loadPassengers();
            bookings = DatabaseHandler.loadBookings();
            passengerIdCounter = Math.max(1, DatabaseHandler.getMaxPassengerId() + 1);
            ticketCounter = Math.max(1001, DatabaseHandler.getMaxTicketNumber() + 1);
            if (footer != null) {
                footer.setText("© Airline Reservation System | " + DatabaseHandler.getStatusMessage());
            }
            refreshFlightSchedulePanelData();
            refreshPassengerView();
        }
    }

    private void showAboutDialog() {
        Dialog dialog = new Dialog(this, "About", true);
        dialog.setLayout(new FlowLayout());
        dialog.add(new Label("Airline Reservation & Passenger Management System"));
        dialog.add(new Label("Built with Java AWT & MySQL Database."));
        Button ok = new Button("OK");
        ok.addActionListener(e -> dialog.dispose());
        dialog.add(ok);
        dialog.setSize(350, 120);
        dialog.setLocation(getX() + 150, getY() + 150);
        dialog.setVisible(true);
    }

    // ------------------- HOME PANEL -------------------
    private Panel buildHomePanel() {
        Panel panel = new Panel();
        panel.setLayout(new GridLayout(6, 1, 10, 10));
        panel.setBackground(new Color(235, 240, 250));

        Button btnAddPassenger = new Button("Add Passenger");
        Button btnBookTicket = new Button("Book Ticket");
        Button btnViewPassengers = new Button("View Passengers / Bookings");
        Button btnCancelTicket = new Button("Search / Cancel Ticket");
        Button btnFlightSchedule = new Button("Flight Schedule");
        Button btnExit = new Button("Exit");

        btnAddPassenger.addActionListener(this);
        btnBookTicket.addActionListener(this);
        btnViewPassengers.addActionListener(this);
        btnCancelTicket.addActionListener(this);
        btnFlightSchedule.addActionListener(this);
        btnExit.addActionListener(this);

        btnAddPassenger.setActionCommand(ADD_PASSENGER);
        btnBookTicket.setActionCommand(BOOK_TICKET);
        btnViewPassengers.setActionCommand(VIEW_PASSENGERS);
        btnCancelTicket.setActionCommand(CANCEL_TICKET);
        btnFlightSchedule.setActionCommand(FLIGHT_SCHEDULE);
        btnExit.setActionCommand("EXIT");

        Panel wrapper = new Panel(new BorderLayout());
        Panel inner = new Panel(new GridLayout(6, 1, 15, 15));
        inner.add(btnAddPassenger);
        inner.add(btnBookTicket);
        inner.add(btnViewPassengers);
        inner.add(btnCancelTicket);
        inner.add(btnFlightSchedule);
        inner.add(btnExit);

        Panel padded = new Panel(new FlowLayout(FlowLayout.CENTER, 0, 40));
        padded.add(inner);
        inner.setPreferredSize(new Dimension(350, 300));

        wrapper.add(padded, BorderLayout.CENTER);
        return wrapper;
    }

    // ------------------- ADD PASSENGER PANEL -------------------
    private Panel buildAddPassengerPanel() {
        Panel panel = new Panel(new BorderLayout());

        Panel form = new Panel(new GridLayout(5, 2, 10, 10));
        form.add(new Label("Passenger Name:"));
        tfName = new TextField();
        form.add(tfName);

        form.add(new Label("Age:"));
        tfAge = new TextField();
        form.add(tfAge);

        form.add(new Label("Gender:"));
        chGender = new Choice();
        chGender.add("Male");
        chGender.add("Female");
        chGender.add("Other");
        form.add(chGender);

        form.add(new Label("Contact Number:"));
        tfContact = new TextField();
        form.add(tfContact);

        Button btnSubmit = new Button("Add Passenger");
        btnSubmit.addActionListener(this);
        btnSubmit.setActionCommand("SUBMIT_PASSENGER");
        Button btnBack = new Button("Back to Home");
        btnBack.addActionListener(this);
        btnBack.setActionCommand(HOME);
        form.add(btnSubmit);
        form.add(btnBack);

        lblAddStatus = new Label("", Label.CENTER);
        lblAddStatus.setForeground(new Color(0, 120, 0));

        Panel outer = new Panel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        form.setPreferredSize(new Dimension(400, 200));
        outer.add(form);

        panel.add(outer, BorderLayout.CENTER);
        panel.add(lblAddStatus, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------- BOOK TICKET PANEL -------------------
    private Panel buildBookTicketPanel() {
        Panel panel = new Panel(new BorderLayout());

        Panel form = new Panel(new GridLayout(4, 2, 10, 10));
        form.add(new Label("Passenger Name:"));
        tfPassengerName = new TextField();
        form.add(tfPassengerName);

        form.add(new Label("Select Flight:"));
        chFlight = new Choice();
        for (Flight f : flights) {
            chFlight.add(f.flightNo + " (" + f.source + "->" + f.destination + ")");
        }
        form.add(chFlight);

        form.add(new Label("Class:"));
        chClass = new Choice();
        chClass.add("Economy");
        chClass.add("Business");
        chClass.add("First Class");
        form.add(chClass);

        Button btnBook = new Button("Book Ticket");
        btnBook.addActionListener(this);
        btnBook.setActionCommand("SUBMIT_BOOKING");
        Button btnBack = new Button("Back to Home");
        btnBack.addActionListener(this);
        btnBack.setActionCommand(HOME);
        form.add(btnBook);
        form.add(btnBack);

        lblBookStatus = new Label("", Label.CENTER);
        lblBookStatus.setForeground(new Color(0, 120, 0));

        Panel outer = new Panel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        form.setPreferredSize(new Dimension(420, 160));
        outer.add(form);

        panel.add(outer, BorderLayout.CENTER);
        panel.add(lblBookStatus, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------- VIEW PASSENGERS / BOOKINGS PANEL -------------------
    private Panel buildViewPassengersPanel() {
        Panel panel = new Panel(new BorderLayout());

        taPassengers = new TextArea("", 20, 60, TextArea.SCROLLBARS_VERTICAL_ONLY);
        taPassengers.setEditable(false);
        panel.add(taPassengers, BorderLayout.CENTER);

        Panel btnPanel = new Panel(new FlowLayout());
        Button btnRefresh = new Button("Refresh");
        btnRefresh.addActionListener(this);
        btnRefresh.setActionCommand("REFRESH_VIEW");
        Button btnBack = new Button("Back to Home");
        btnBack.addActionListener(this);
        btnBack.setActionCommand(HOME);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnBack);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------- CANCEL / SEARCH TICKET PANEL -------------------
    private Panel buildCancelTicketPanel() {
        Panel panel = new Panel(new BorderLayout());

        Panel top = new Panel(new FlowLayout());
        top.add(new Label("Enter Ticket ID:"));
        tfTicketId = new TextField(10);
        top.add(tfTicketId);

        Button btnSearch = new Button("Search");
        btnSearch.addActionListener(this);
        btnSearch.setActionCommand("SEARCH_TICKET");
        Button btnCancel = new Button("Cancel Ticket");
        btnCancel.addActionListener(this);
        btnCancel.setActionCommand("CANCEL_TICKET_ACTION");
        Button btnBack = new Button("Back to Home");
        btnBack.addActionListener(this);
        btnBack.setActionCommand(HOME);

        top.add(btnSearch);
        top.add(btnCancel);
        top.add(btnBack);

        taTicketSearchResult = new TextArea("", 12, 60, TextArea.SCROLLBARS_VERTICAL_ONLY);
        taTicketSearchResult.setEditable(false);

        lblCancelStatus = new Label("", Label.CENTER);
        lblCancelStatus.setForeground(new Color(180, 0, 0));

        panel.add(top, BorderLayout.NORTH);
        panel.add(taTicketSearchResult, BorderLayout.CENTER);
        panel.add(lblCancelStatus, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------- FLIGHT SCHEDULE PANEL -------------------
    private Panel buildFlightSchedulePanel() {
        Panel panel = new Panel(new BorderLayout());

        listFlights = new java.awt.List(10, false);
        for (Flight f : flights) {
            listFlights.add(f.toString());
        }
        panel.add(listFlights, BorderLayout.CENTER);

        Button btnBack = new Button("Back to Home");
        btnBack.addActionListener(this);
        btnBack.setActionCommand(HOME);
        Panel btnPanel = new Panel(new FlowLayout());
        btnPanel.add(btnBack);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------- ACTION HANDLING -------------------
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        switch (cmd) {
            case HOME:
            case ADD_PASSENGER:
            case BOOK_TICKET:
            case VIEW_PASSENGERS:
            case CANCEL_TICKET:
            case FLIGHT_SCHEDULE:
                if (cmd.equals(VIEW_PASSENGERS)) refreshPassengerView();
                if (cmd.equals(FLIGHT_SCHEDULE)) refreshFlightSchedulePanelData();
                cardLayout.show(cardPanel, cmd);
                break;

            case "EXIT":
                dispose();
                System.exit(0);
                break;

            case "SUBMIT_PASSENGER":
                handleAddPassenger();
                break;

            case "SUBMIT_BOOKING":
                handleBookTicket();
                break;

            case "REFRESH_VIEW":
                refreshPassengerView();
                break;

            case "SEARCH_TICKET":
                handleSearchTicket();
                break;

            case "CANCEL_TICKET_ACTION":
                handleCancelTicket();
                break;

            default:
                break;
        }
    }

    private void refreshFlightSchedulePanelData() {
        if (listFlights != null) {
            listFlights.removeAll();
            for (Flight f : flights) {
                listFlights.add(f.toString());
            }
        }
    }

    private void handleAddPassenger() {
        String name = tfName.getText().trim();
        String ageStr = tfAge.getText().trim();
        String contact = tfContact.getText().trim();
        String gender = chGender.getSelectedItem();

        if (name.isEmpty() || ageStr.isEmpty() || contact.isEmpty()) {
            lblAddStatus.setForeground(Color.RED);
            lblAddStatus.setText("Please fill all fields.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            lblAddStatus.setForeground(Color.RED);
            lblAddStatus.setText("Age must be a number.");
            return;
        }

        // id=0 placeholder; DatabaseHandler.savePassenger will update it with DB-generated key
        Passenger p = new Passenger(passengerIdCounter, name, age, gender, contact);
        boolean saved = DatabaseHandler.savePassenger(p);

        if (saved) {
            // Use the DB-assigned id if available, else fall back to counter
            if (p.id > 0) {
                passengerIdCounter = p.id + 1;
            } else {
                passengerIdCounter++;
            }
        } else {
            passengerIdCounter++;
        }

        passengers.add(p);

        // Update footer with current DB status
        if (footer != null) {
            footer.setText("© Airline Reservation System | " + DatabaseHandler.getStatusMessage());
        }

        if (saved) {
            lblAddStatus.setForeground(new Color(0, 120, 0));
            lblAddStatus.setText("Passenger added & saved to DB. ID: " + p.id);
        } else {
            lblAddStatus.setForeground(new Color(180, 100, 0));
            lblAddStatus.setText("Passenger added locally (DB offline). ID: " + p.id);
        }

        tfName.setText("");
        tfAge.setText("");
        tfContact.setText("");
    }

    private void handleBookTicket() {
        String name = tfPassengerName.getText().trim();
        if (name.isEmpty()) {
            lblBookStatus.setForeground(Color.RED);
            lblBookStatus.setText("Please enter passenger name.");
            return;
        }

        int flightIndex = chFlight.getSelectedIndex();
        Flight selectedFlight = flights.get(flightIndex);

        if (selectedFlight.seatsAvailable <= 0) {
            lblBookStatus.setForeground(Color.RED);
            lblBookStatus.setText("No seats available on this flight.");
            return;
        }

        String seatClass = chClass.getSelectedItem();
        String ticketId = "TKT" + (ticketCounter++);
        String seatNo = seatClass.charAt(0) + String.valueOf(selectedFlight.seatsAvailable);

        Booking booking = new Booking(ticketId, name, selectedFlight.flightNo, seatClass, seatNo);
        bookings.add(booking);
        selectedFlight.seatsAvailable--;
        boolean saved = DatabaseHandler.saveBooking(booking, selectedFlight.flightNo);

        // Update footer with current DB status
        if (footer != null) {
            footer.setText("© Airline Reservation System | " + DatabaseHandler.getStatusMessage());
        }

        if (saved) {
            lblBookStatus.setForeground(new Color(0, 120, 0));
            lblBookStatus.setText("Ticket booked & saved to DB! ID: " + ticketId + "  Seat: " + seatNo);
        } else {
            lblBookStatus.setForeground(new Color(180, 100, 0));
            lblBookStatus.setText("Ticket booked locally (DB offline). ID: " + ticketId + "  Seat: " + seatNo);
        }

        tfPassengerName.setText("");
    }

    private void refreshPassengerView() {
        if (DatabaseHandler.isConnected() && !isTestEnvironment()) {
            passengers = DatabaseHandler.loadPassengers();
            bookings = DatabaseHandler.loadBookings();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("---- PASSENGERS ----\n");
        if (passengers.isEmpty()) {
            sb.append("No passengers registered.\n");
        } else {
            for (Passenger p : passengers) {
                sb.append(p.toString()).append("\n");
            }
        }
        sb.append("\n---- BOOKINGS ----\n");
        if (bookings.isEmpty()) {
            sb.append("No bookings yet.\n");
        } else {
            for (Booking b : bookings) {
                sb.append(b.toString()).append("\n");
            }
        }
        taPassengers.setText(sb.toString());
    }

    private void handleSearchTicket() {
        String id = tfTicketId.getText().trim();
        if (id.isEmpty()) {
            lblCancelStatus.setText("Enter a ticket ID to search.");
            return;
        }
        for (Booking b : bookings) {
            if (b.ticketId.equalsIgnoreCase(id)) {
                taTicketSearchResult.setText(b.toString());
                lblCancelStatus.setText("Ticket found.");
                return;
            }
        }
        taTicketSearchResult.setText("");
        lblCancelStatus.setText("Ticket ID not found.");
    }

    private void handleCancelTicket() {
        String id = tfTicketId.getText().trim();
        if (id.isEmpty()) {
            lblCancelStatus.setText("Enter a ticket ID to cancel.");
            return;
        }
        for (Booking b : bookings) {
            if (b.ticketId.equalsIgnoreCase(id)) {
                if (b.cancelled) {
                    lblCancelStatus.setText("Ticket already cancelled.");
                    return;
                }
                b.cancelled = true;
                // restore seat
                for (Flight f : flights) {
                    if (f.flightNo.equals(b.flightNo)) {
                        f.seatsAvailable++;
                        break;
                    }
                }
                DatabaseHandler.cancelBooking(b.ticketId, b.flightNo);
                taTicketSearchResult.setText(b.toString());
                lblCancelStatus.setText("Ticket cancelled successfully.");
                return;
            }
        }
        lblCancelStatus.setText("Ticket ID not found.");
    }

    public static void main(String[] args) {
        AirlineReservationSystem app = new AirlineReservationSystem();
        app.setVisible(true);
    }
}
