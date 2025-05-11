@echo off
setlocal enabledelayedexpansion

echo -------------------------
echo      CrazyEights Game    
echo -------------------------

REM Create bin directory if it doesn't exist
if not exist "bin" (
    echo Creating bin directory...
    mkdir bin
)

REM Compilation step (once at the beginning)
set /p compile_choice="Do you want to compile the project? (y/n): "
if /i "%compile_choice%"=="y" (
    echo Compiling the project...
    javac -d bin src\*.java
    
    REM Check if compilation was successful
    if %ERRORLEVEL% NEQ 0 (
        echo Compilation failed!
        exit /b 1
    )
    echo Compilation successful!
)

REM Clear the screen
cls

:main_loop
echo.
echo CrazyEights - Select an action:
echo 1. Initialize a new game
echo 2. Add a user to a game
echo 3. Remove a user from a game
echo 4. Start a game
echo 5. Get turn order
echo 6. Play a card
echo 7. Get user's cards
echo 8. Draw a card
echo 9. Pass a turn
echo 10. Enter all arguments manually
echo 11. Recompile the project
echo 12. Exit

set /p action_choice="Enter your choice (1-12): "

if "%action_choice%"=="1" (
    REM Initialize game
    call :get_game_name
    set cmd=java -cp bin CrazyEights --init --game !game_name!
) else if "%action_choice%"=="2" (
    REM Add user
    call :get_game_name
    call :get_username
    set cmd=java -cp bin CrazyEights --add-user !username! --game !game_name!
) else if "%action_choice%"=="3" (
    REM Remove user
    call :get_game_name
    call :get_username
    set cmd=java -cp bin CrazyEights --remove-user !username! --game !game_name!
) else if "%action_choice%"=="4" (
    REM Start game
    call :get_game_name
    set cmd=java -cp bin CrazyEights --start --game !game_name!
) else if "%action_choice%"=="5" (
    REM Get turn order
    call :get_game_name
    call :get_username
    set cmd=java -cp bin CrazyEights --order --user !username! --game !game_name!
) else if "%action_choice%"=="6" (
    REM Play card
    call :get_game_name
    call :get_username
    set /p card="Enter card to play (e.g., 'H8' for Eight of Hearts): "
    set cmd=java -cp bin CrazyEights --play !card! --user !username! --game !game_name!
) else if "%action_choice%"=="7" (
    REM Get cards
    call :get_game_name
    call :get_username
    set /p cards_username="Enter username to get cards for: "
    set cmd=java -cp bin CrazyEights --cards !cards_username! --user !username! --game !game_name!
) else if "%action_choice%"=="8" (
    REM Draw card
    call :get_game_name
    call :get_username
    set cmd=java -cp bin CrazyEights --draw --user !username! --game !game_name!
) else if "%action_choice%"=="9" (
    REM Pass turn
    call :get_game_name
    call :get_username
    set cmd=java -cp bin CrazyEights --pass --user !username! --game !game_name!
) else if "%action_choice%"=="10" (
    REM Manual mode
    set /p args="Enter all arguments (e.g. --draw --user alice --game test): "
    set cmd=java -cp bin CrazyEights !args!
) else if "%action_choice%"=="11" (
    REM Recompile
    echo Recompiling the project...
    javac -d bin src\*.java
    
    REM Check if compilation was successful
    if %ERRORLEVEL% EQU 0 (
        echo Recompilation successful!
    ) else (
        echo Recompilation failed!
    )
    goto main_loop
) else if "%action_choice%"=="12" (
    echo Exiting CrazyEights. Goodbye!
    exit /b 0
) else (
    echo Invalid choice! Please try again.
    goto main_loop
)

REM Execute the command for options 1-10
if defined cmd (
    echo Executing: !cmd!
    !cmd!
    set cmd=
)

goto main_loop

:get_game_name
set /p game_name="Enter game name: "
if "!game_name!"=="" (
    echo Game name cannot be empty!
    goto get_game_name
)
exit /b 0

:get_username
set /p username="Enter username: "
if "!username!"=="" (
    echo Username cannot be empty!
    goto get_username
)
exit /b 0