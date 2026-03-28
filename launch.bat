@echo off
echo 🌿 STARTING DIGITAL DETOX TRACKER...

:: Check for Maven
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven (mvn) not found! Please install Maven to run the Java backend.
    echo Visit: https://maven.apache.org/download.cgi
    pause
    exit /b
)

:: Check for Node
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Node.js not found! Please install Node.js to run the React frontend.
    pause
    exit /b
)

:: Run Backend
echo.
echo [1/2] Starting Java Backend (Spring Boot)...
start cmd /k "mvn spring-boot:run"

:: Wait for backend to start a bit (optional)
timeout /t 5 /nobreak >nul

:: Run Frontend
echo [2/2] Starting React Frontend (Vite)...
cd frontend
start cmd /k "npm run dev"

echo.
echo ✅ Done! Backend is opening on http://localhost:8080
echo ✅ Done! Frontend is opening on http://localhost:5173
echo.
pause
