@echo off
cd /D "C:\Users\hardi\OneDrive\Desktop\java nsp\New folder"

echo Compiling...
javac -cp "C:\Users\hardi\OneDrive\Desktop\java nsp\New folder;C:\Users\hardi\OneDrive\Desktop\java nsp\New folder\lib\mysql-connector-j-9.2.0.jar" DatabaseHandler.java AirlineReservationSystem.java

echo Launching...
javaw -cp "C:\Users\hardi\OneDrive\Desktop\java nsp\New folder;C:\Users\hardi\OneDrive\Desktop\java nsp\New folder\lib\mysql-connector-j-9.2.0.jar" AirlineReservationSystem
