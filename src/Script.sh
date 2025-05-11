#!/bin/bash 

echo "-------------------------"
echo "     CrazyEights Game    "
echo "-------------------------"

# Create bin directory if it doesn't exist
if [ ! -d "bin" ]; then
    echo "Creating bin directory..."
    mkdir -p bin
fi

# Compilation step (once at the beginning)
read -p "Do you want to compile the project? (y/n): " compile_choice 
if [[ $compile_choice == "y" || $compile_choice == "Y" ]]; then 
    echo "Compiling the project..."
    javac -d bin src/*.java
    
    # Check if compilation was successful
    if [ $? -eq 0 ]; then
        echo "Compilation successful!"
    else
        echo "Compilation failed!"
        exit 1
    fi
fi

# Function to get game name
get_game_name() {
    read -p "Enter game name: " game_name
    while [ -z "$game_name" ]; do
        echo "Game name cannot be empty!"
        read -p "Enter game name: " game_name
    done
    echo "$game_name"
}

# Function to get username
get_username() {
    read -p "Enter username: " username
    while [ -z "$username" ]; do
        echo "Username cannot be empty!"
        read -p "Enter username: " username
    done
    echo "$username"
}

# Clear the screen
clear

# Main program loop
while true; do
    echo ""
    echo "CrazyEights - Select an action:"
    echo "1. Initialize a new game"
    echo "2. Add a user to a game"
    echo "3. Remove a user from a game"
    echo "4. Start a game"
    echo "5. Get turn order"
    echo "6. Play a card"
    echo "7. Get user's cards"
    echo "8. Draw a card"
    echo "9. Pass a turn"
    echo "10. Enter all arguments manually"
    echo "11. Recompile the project"
    echo "12. Exit"
    
    read -p "Enter your choice (1-12): " action_choice
    
    case $action_choice in
        1)  # Initialize game
            game_name=$(get_game_name)
            cmd="java -cp bin CrazyEights --init --game $game_name"
            ;;
        2)  # Add user
            game_name=$(get_game_name)
            username=$(get_username)
            cmd="java -cp bin CrazyEights --add-user $username --game $game_name"
            ;;
        3)  # Remove user
            game_name=$(get_game_name)
            username=$(get_username)
            cmd="java -cp bin CrazyEights --remove-user $username --game $game_name"
            ;;
        4)  # Start game
            game_name=$(get_game_name)
            cmd="java -cp bin CrazyEights --start --game $game_name"
            ;;
        5)  # Get turn order
            game_name=$(get_game_name)
            username=$(get_username)
            cmd="java -cp bin CrazyEights --order --user $username --game $game_name"
            ;;
        6)  # Play card
            game_name=$(get_game_name)
            username=$(get_username)
            read -p "Enter card to play (e.g., 'H8' for Eight of Hearts): " card
            cmd="java -cp bin CrazyEights --play $card --user $username --game $game_name"
            ;;
        7)  # Get cards
            game_name=$(get_game_name)
            username=$(get_username)
            read -p "Enter username to get cards for: " cards_username
            cmd="java -cp bin CrazyEights --cards $cards_username --user $username --game $game_name"
            ;;
        8)  # Draw card
            game_name=$(get_game_name)
            username=$(get_username)
            cmd="java -cp bin CrazyEights --draw --user $username --game $game_name"
            ;;
        9)  # Pass turn
            game_name=$(get_game_name)
            username=$(get_username)
            cmd="java -cp bin CrazyEights --pass --user $username --game $game_name"
            ;;
        10) # Manual mode
            read -p "Enter all arguments (e.g. --draw --user alice --game test): " args
            cmd="java -cp bin CrazyEights $args"
            ;;
        11) # Recompile
            echo "Recompiling the project..."
            javac -d bin src/*.java
            
            # Check if compilation was successful
            if [ $? -eq 0 ]; then
                echo "Recompilation successful!"
            else
                echo "Recompilation failed!"
            fi
            continue
            ;;
        12) # Exit
            echo "Exiting CrazyEights. Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid choice! Please try again."
            continue
            ;;
    esac
    
    # Execute the command for options 1-10
    if [[ -n "$cmd" ]]; then
        echo "Executing: $cmd"
        eval "$cmd"
        unset cmd  # Clear the command for the next iteration
    fi
done