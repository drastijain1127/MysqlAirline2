-- ========================================================
-- Airlines Database Schema (airlines_db)
-- For Airline Reservation & Passenger Management System
-- ========================================================

CREATE DATABASE IF NOT EXISTS airlines_db;
USE airlines_db;

-- 1. Flights Table
CREATE TABLE IF NOT EXISTS flights (
    flight_no VARCHAR(20) PRIMARY KEY,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure VARCHAR(20) NOT NULL,
    arrival VARCHAR(20) NOT NULL,
    fare DOUBLE NOT NULL,
    seats_available INT NOT NULL
);

-- 2. Passengers Table
CREATE TABLE IF NOT EXISTS passengers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    contact VARCHAR(30) NOT NULL
);

-- 3. Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    ticket_id VARCHAR(30) PRIMARY KEY,
    passenger_name VARCHAR(100) NOT NULL,
    flight_no VARCHAR(20) NOT NULL,
    seat_class VARCHAR(50) NOT NULL,
    seat_no VARCHAR(20) NOT NULL,
    cancelled BOOLEAN NOT NULL DEFAULT FALSE,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flight_no) REFERENCES flights(flight_no) ON DELETE CASCADE
);

-- Initial Flight Schedule Data
INSERT INTO flights (flight_no, source, destination, departure, arrival, fare, seats_available)
VALUES 
    ('AI-101', 'Delhi', 'Mumbai', '08:00', '10:00', 4500, 40),
    ('AI-202', 'Mumbai', 'Bangalore', '11:30', '13:15', 3800, 35),
    ('AI-303', 'Delhi', 'Jaipur', '14:00', '15:00', 2200, 50),
    ('AI-404', 'Bangalore', 'Chennai', '17:45', '18:50', 2800, 45),
    ('AI-505', 'Jaipur', 'Delhi', '20:00', '21:00', 2200, 50)
ON DUPLICATE KEY UPDATE flight_no=flight_no;
