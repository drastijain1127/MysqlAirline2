@echo off
echo Starting MySQL Server on port 3306...
echo.
echo Checking if MySQL is already running...
"C:\Program Files\MySQL\MySQL Server 26.10\bin\mysqladmin.exe" -u root -pDrasti@2007 -P 3306 ping 2>nul
if %ERRORLEVEL%==0 (
    echo MySQL is already running!
    goto :done
)

echo Starting MySQL as a Windows Service...
net start MySQL >nul 2>&1
if %ERRORLEVEL%==0 (
    echo MySQL Service started successfully on port 3306.
    goto :done
)

echo Trying to start MySQL manually...
start "" "C:\Program Files\MySQL\MySQL Server 26.10\bin\mysqld.exe" --console
timeout /t 3 /nobreak >nul
echo MySQL Server is running in the background on port 3306.

:done
echo.
echo Done. Use run.bat to launch the application.
pause
