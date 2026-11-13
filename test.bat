@echo off
echo Compiling tests with MySQL Driver...
javac -cp ".;lib/*" AirlineReservationSystemTest.java DatabaseHandler.java AirlineReservationSystem.java
if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Running tests...
java -cp ".;lib/*" AirlineReservationSystemTest
pause
