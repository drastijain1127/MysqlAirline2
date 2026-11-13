# ✈️ MysqlAirline2 — Airline Reservation & Passenger Management System

A Java AWT desktop application connected to a **MySQL database** for managing airline reservations, passengers, and flight bookings.

---

## 🛠️ Tech Stack
- **Language:** Java (AWT GUI — no Swing)
- **Database:** MySQL 
- **JDBC:** MySQL Connector/J 9.2.0

---

## 📁 Project Structure
```
├── AirlineReservationSystem.java   # Main GUI application
├── DatabaseHandler.java            # JDBC / MySQL operations
├── db.properties                   # DB connection config
├── schema.sql                      # Database schema & seed data
├── run.bat                         # Compile & launch script
├── start_mysql.bat                 # Start MySQL server
├── lib/
│   └── mysql-connector-j-9.2.0.jar # MySQL JDBC driver
```

---

## ⚙️ Setup & Run

### 1. Prerequisites
- Java JDK (17+)
- MySQL Server running on port **3307**
- MySQL Connector/J JAR in `lib/` folder

### 2. Configure Database
Edit `db.properties` with your MySQL credentials:
```properties
db.host=localhost
db.port=3307
db.name=airlines_db
db.user=root
db.password=YOUR_PASSWORD
```

### 3. Create Database
Run `schema.sql` in MySQL Workbench or via CLI:
```bash
mysql -u root -p < schema.sql
```

### 4. Download JDBC Driver
Download [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) and place the JAR in the `lib/` folder.

### 5. Run the App
```cmd
cd "path\to\project"
javac -cp ".;lib\mysql-connector-j-9.2.0.jar" DatabaseHandler.java AirlineReservationSystem.java
java -cp ".;lib\mysql-connector-j-9.2.0.jar" AirlineReservationSystem
```
Or simply **double-click `run.bat`** ✅

---

## ✨ Features
- ➕ **Add Passengers** — Register new passengers with details
- 🎫 **Book Tickets** — Book seats on available flights
- 👁️ **View Passengers & Bookings** — See all records from DB
- ❌ **Cancel Tickets** — Cancel bookings and restore seats
- ✈️ **Flight Schedule** — View all available flights
- 🗄️ **MySQL Integration** — All data persisted in MySQL database

---

## 🗄️ Database Schema
| Table | Description |
|---|---|
| `flights` | Flight schedule with seats |
| `passengers` | Registered passenger details |
| `bookings` | Ticket bookings with status |
