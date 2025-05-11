@echo off 

cls 
echo ============ Crazy Eight Interactive Game ============= 
echo. 

:: Ask for game name 
set /p GAME_NAME="Enter game name: "
if "%GAME_NAME%"=="" (
    echo Game name cannot be empty. Exiting...
    exit /b 1
)

:: Ask for initialization of a new game 
set /p INIT_GAME="Is this a new game? (Y/N): "
if /i "%INIT_GAME%"=="Y" (
    java -cp bin CrazyEights --init --game "%GAME_NAME%"
    echo Game initialized: %GAME_NAME%
)