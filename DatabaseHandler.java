import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Handles all JDBC operations for the Airline Reservation System.
 * Connects to the local MySQL server and manages tables for Airlines DB.
 */
public class DatabaseHandler {

    private static String host = "localhost";
    private static int port = 3307;
    private static String dbName = "airlines_db";
    private static String user = "root";
    private static String password = "Drasti@2007";
    private static String url = "jdbc:mysql://localhost:3307/airlines_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";

    private static boolean connected = false;
    private static String lastError = "";

    static {
        loadProperties();
    }

    public static void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("db.properties")) {
            props.load(in);
            if (props.containsKey("db.host")) host = props.getProperty("db.host").trim();
            if (props.containsKey("db.port")) port = Integer.parseInt(props.getProperty("db.port").trim());
            if (props.containsKey("db.name")) dbName = props.getProperty("db.name").trim();
            if (props.containsKey("db.user")) user = props.getProperty("db.user").trim();
            if (props.containsKey("db.password")) password = props.getProperty("db.password").trim();
            if (props.containsKey("db.url")) {
                url = props.getProperty("db.url").trim();
            } else {
                url = "jdbc:mysql://" + host + ":" + port + "/" + dbName +
                      "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
            }
        } catch (Exception e) {
            // Use defaults if db.properties cannot be read
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Driver will auto-register in modern JDBC if JAR is on classpath
        }

        // Try primary configured URL
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException primaryEx) {
            // If primary fails and port was 3306, try port 3307 as fallback
            if (port == 3306) {
                String fallbackUrl = "jdbc:mysql://" + host + ":3307/" + dbName +
                                     "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
                try {
                    Connection conn = DriverManager.getConnection(fallbackUrl, user, password);
                    url = fallbackUrl;
                    port = 3307;
                    return conn;
                } catch (SQLException fallbackEx) {
                    // rethrow primary exception
                }
            }
            throw primaryEx;
        }
    }

    public static boolean init() {
        try (Connection conn = getConnection()) {
            connected = true;
            createTablesIfNotExist(conn);
            System.out.println("[DB] Successfully connected to MySQL at " + host + ":" + port + "/" + dbName);
            return true;
        } catch (Exception e) {
            connected = false;
            lastError = e.getMessage();
            System.err.println("[DB Warning] Could not connect to MySQL: " + e.getMessage());
            return false;
        }
    }

    public static boolean isConnected() {
        return connected;
    }

    /** Performs a live ping to verify the connection is still up. */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            connected = conn != null && !conn.isClosed();
        } catch (SQLException e) {
            connected = false;
            lastError = e.getMessage();
        }
        return connected;
    }

    public static String getStatusMessage() {
        if (connected) {
            return "MySQL Connected (" + host + ":" + port + "/" + dbName + ")";
        } else {
            return "DB Offline (" + (lastError.isEmpty() ? "Not connected" : lastError) + ")";
        }
    }

    private static void createTablesIfNotExist(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Flights table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS flights (" +
                "  flight_no VARCHAR(20) PRIMARY KEY," +
                "  source VARCHAR(100) NOT NULL," +
                "  destination VARCHAR(100) NOT NULL," +
                "  departure VARCHAR(20) NOT NULL," +
                "  arrival VARCHAR(20) NOT NULL," +
                "  fare DOUBLE NOT NULL," +
                "  seats_available INT NOT NULL" +
                ")"
            );

            // Passengers table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS passengers (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  name VARCHAR(100) NOT NULL," +
                "  age INT NOT NULL," +
                "  gender VARCHAR(20) NOT NULL," +
                "  contact VARCHAR(30) NOT NULL" +
                ")"
            );

            // Bookings table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS bookings (" +
                "  ticket_id VARCHAR(30) PRIMARY KEY," +
                "  passenger_name VARCHAR(100) NOT NULL," +
                "  flight_no VARCHAR(20) NOT NULL," +
                "  seat_class VARCHAR(50) NOT NULL," +
                "  seat_no VARCHAR(20) NOT NULL," +
                "  cancelled BOOLEAN NOT NULL DEFAULT FALSE," +
                "  booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        }
    }

    public static void seedDefaultFlightsIfEmpty(List<Flight> defaultFlights) {
        if (!connected) return;
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM flights")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String sql = "INSERT INTO flights (flight_no, source, destination, departure, arrival, fare, seats_available) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        for (Flight f : defaultFlights) {
                            ps.setString(1, f.flightNo);
                            ps.setString(2, f.source);
                            ps.setString(3, f.destination);
                            ps.setString(4, f.departure);
                            ps.setString(5, f.arrival);
                            ps.setDouble(6, f.fare);
                            ps.setInt(7, f.seatsAvailable);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                        System.out.println("[DB] Seeded " + defaultFlights.size() + " default flights into MySQL.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB Error] Seeding flights failed: " + e.getMessage());
        }
    }

    public static ArrayList<Flight> loadFlights() {
        ArrayList<Flight> list = new ArrayList<>();
        if (!connected) return list;
        String sql = "SELECT flight_no, source, destination, departure, arrival, fare, seats_available FROM flights ORDER BY flight_no";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Flight(
                    rs.getString("flight_no"),
                    rs.getString("source"),
                    rs.getString("destination"),
                    rs.getString("departure"),
                    rs.getString("arrival"),
                    rs.getDouble("fare"),
                    rs.getInt("seats_available")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DB Error] Loading flights failed: " + e.getMessage());
        }
        return list;
    }

    public static ArrayList<Passenger> loadPassengers() {
        ArrayList<Passenger> list = new ArrayList<>();
        if (!connected) return list;
        String sql = "SELECT id, name, age, gender, contact FROM passengers ORDER BY id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Passenger(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("contact")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DB Error] Loading passengers failed: " + e.getMessage());
        }
        return list;
    }

    public static ArrayList<Booking> loadBookings() {
        ArrayList<Booking> list = new ArrayList<>();
        if (!connected) return list;
        String sql = "SELECT ticket_id, passenger_name, flight_no, seat_class, seat_no, cancelled FROM bookings ORDER BY ticket_id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Booking b = new Booking(
                    rs.getString("ticket_id"),
                    rs.getString("passenger_name"),
                    rs.getString("flight_no"),
                    rs.getString("seat_class"),
                    rs.getString("seat_no")
                );
                b.cancelled = rs.getBoolean("cancelled");
                list.add(b);
            }
        } catch (SQLException e) {
            System.err.println("[DB Error] Loading bookings failed: " + e.getMessage());
        }
        return list;
    }

    public static boolean savePassenger(Passenger p) {
        if (!connected) return false;
        // Let AUTO_INCREMENT assign the id; use INSERT IGNORE to skip duplicates safely
        String sql = "INSERT INTO passengers (name, age, gender, contact) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.name);
            ps.setInt(2, p.age);
            ps.setString(3, p.gender);
            ps.setString(4, p.contact);
            ps.executeUpdate();
            // Update the passenger object with the DB-generated id
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.id = rs.getInt(1);
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[DB Error] Save passenger failed: " + e.getMessage());
            connected = false; // mark as disconnected so UI shows correct status
            return false;
        }
    }

    public static boolean saveBooking(Booking b, String flightNo) {
        if (!connected) return false;
        String insertBookingSql = "INSERT INTO bookings (ticket_id, passenger_name, flight_no, seat_class, seat_no, cancelled) VALUES (?, ?, ?, ?, ?, ?)";
        String updateFlightSql = "UPDATE flights SET seats_available = seats_available - 1 WHERE flight_no = ? AND seats_available > 0";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psFlight = conn.prepareStatement(updateFlightSql)) {
                    psFlight.setString(1, flightNo);
                    int updated = psFlight.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                try (PreparedStatement psBooking = conn.prepareStatement(insertBookingSql)) {
                    psBooking.setString(1, b.ticketId);
                    psBooking.setString(2, b.passengerName);
                    psBooking.setString(3, b.flightNo);
                    psBooking.setString(4, b.seatClass);
                    psBooking.setString(5, b.seatNo);
                    psBooking.setBoolean(6, b.cancelled);
                    psBooking.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[DB Error] Save booking failed: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public static boolean cancelBooking(String ticketId, String flightNo) {
        if (!connected) return false;
        String updateBookingSql = "UPDATE bookings SET cancelled = TRUE WHERE ticket_id = ? AND cancelled = FALSE";
        String updateFlightSql = "UPDATE flights SET seats_available = seats_available + 1 WHERE flight_no = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psBooking = conn.prepareStatement(updateBookingSql)) {
                    psBooking.setString(1, ticketId);
                    int rows = psBooking.executeUpdate();
                    if (rows == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                try (PreparedStatement psFlight = conn.prepareStatement(updateFlightSql)) {
                    psFlight.setString(1, flightNo);
                    psFlight.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[DB Error] Cancel booking failed: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public static int getMaxPassengerId() {
        if (!connected) return 0;
        String sql = "SELECT COALESCE(MAX(id), 0) FROM passengers";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DB Error] Get max passenger ID failed: " + e.getMessage());
        }
        return 0;
    }

    public static int getMaxTicketNumber() {
        if (!connected) return 1000;
        String sql = "SELECT ticket_id FROM bookings";
        int max = 1000;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String tid = rs.getString(1);
                if (tid != null && tid.toUpperCase().startsWith("TKT")) {
                    try {
                        int num = Integer.parseInt(tid.substring(3).trim());
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB Error] Get max ticket number failed: " + e.getMessage());
        }
        return max;
    }

    public static void resetForTesting() {
        if (!connected) return;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM bookings");
            stmt.executeUpdate("DELETE FROM passengers");
            stmt.executeUpdate("ALTER TABLE passengers AUTO_INCREMENT = 1");
            stmt.executeUpdate("UPDATE flights SET seats_available = 40 WHERE flight_no = 'AI-101'");
            stmt.executeUpdate("UPDATE flights SET seats_available = 35 WHERE flight_no = 'AI-202'");
            stmt.executeUpdate("UPDATE flights SET seats_available = 50 WHERE flight_no = 'AI-303'");
            stmt.executeUpdate("UPDATE flights SET seats_available = 45 WHERE flight_no = 'AI-404'");
            stmt.executeUpdate("UPDATE flights SET seats_available = 50 WHERE flight_no = 'AI-505'");
        } catch (SQLException e) {
            System.err.println("[DB Test Reset Error] " + e.getMessage());
        }
    }

    public static int getRowCount(String tableName) {
        if (!connected) return 0;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }
}
