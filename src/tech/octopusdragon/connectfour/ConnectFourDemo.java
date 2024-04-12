package tech.octopusdragon.connectfour;

import java.util.Scanner;

/**
 * Tests out the ConnectFour class by allowing the user to play a game in the
 * terminal.
 * @author Alex Gill
 *
 */
public class ConnectFourDemo {
	
	private final static char P1_TOKEN = 'x';	// Character of player 1's token
	private final static char P2_TOKEN = 'o';	// Character of player 2's token
	private final static char EMPTY = ' ';		// Character for empty space
	private final static boolean SINGLE_PLAYER = true;	// Whether single player
	private final static Difficulty DIFFICULTY = Difficulty.HARD;	// Difficulty
	
	private static ConnectFour game;	// The game
	private static Scanner keyboard;	// Scanner for keyboard input
	
	
	public static void main(String[] args) {
		
		// Create Scanner for keyboard input
		keyboard = new Scanner(System.in);
		
		// Keep playing until the user wants to stop
		boolean keepGoing = true;
		do {
			// Play the game
			playGame();
			
			// Get whether the user wants to keep playing or not.
			boolean validInput = false;
			while (!validInput) {
				System.out.print("Would you like to keep playing ('y'/'n')? ");
				String input = keyboard.nextLine();
				try {
					if (input.charAt(0) == 'y' || input.charAt(0) == 'Y') {
						keepGoing = true;
						validInput = true;
					}
					else if (input.charAt(0) == 'n' || input.charAt(0) == 'N') {
						keepGoing = false;
						validInput = true;
					}
					else {
						throw new IllegalArgumentException();
					}
				}
				catch (Exception e) {
					System.out.print("Invalid input. ");
				}
			}
		} while (keepGoing);
		
		// Print a goodbye message
		System.out.println("Thanks for playing!");
		
		// Close the scanner
		keyboard.close();
	}
	
	
	
	/**
	 * Plays a game of ConnectFour.
	 */
	private static void playGame() {
		
		// Instantiate the game
		game = new ConnectFour();
		game.setDifficulty(DIFFICULTY);
		
		// Play the game
		displayGrid();
		while (!game.isOver()) {
			
			// If on single player and it's the computer's turn, have the
			// computer player move.
			if (SINGLE_PLAYER && game.curPlayer() == Token.P2) {
				game.computerTurn();
			}
			
			// Otherwise, it is a player's turn.
			else {
				// Get the column number to drop the token from the current player.
				int column = -1;
				boolean validInput = false;
				while (!validInput) {
					System.out.printf("%s (%c)'s turn. Enter the column to " +
									  "drop (or 'q' to quit): ",
							game.curPlayer() == Token.P1 ? "Player 1":
								"Player 2",
							getToken(game.curPlayer()));
					String input = keyboard.nextLine();
					try {
						if (input.charAt(0) == 'q' || input.charAt(0) == 'Q') {
							System.out.println("Goodbye!");
							System.exit(0);
						}
						column = Integer.parseInt(input) - 1;
						if (!game.validColumn(column))
							throw new IllegalArgumentException();
						validInput = true;
					}
					catch (Exception e) {
						System.out.print("Invalid input. ");
					}
				}
				
				// Drop the token in the given column.
				game.drop(column);
			}
			
			// Display the rack grid
			displayGrid();
		}
		
		// Display the winner
		Token winner = game.getWinner();
		if (winner == Token.P1) {
			System.out.println("Player 1 won!");
		}
		else if (winner == Token.P2) {
			System.out.println("Player 2 won!");
		}
		else {
			System.out.println("There was a tie.");
		}
	}
	
	
	
	/**
	 * Displays the game's rack board.
	 */
	private static void displayGrid() {
		Token[][] grid = game.getGrid();	// Easy access to grid
		
		for (int i = 0; i < ConnectFour.ROWS; i++) {
			// Print horizontal line
			for (int j = 0; j < ConnectFour.COLUMNS; j++) {
				System.out.print("+---");
			}
			System.out.println("+");
			
			// Print holes
			for (int j = 0; j < ConnectFour.COLUMNS; j++) {
				System.out.print("| " + getToken(grid[i][j]) + " ");
			}
			System.out.println("|");
		}
		
		// Print last horizontal line
		for (int j = 0; j < ConnectFour.COLUMNS; j++) {
			System.out.print("+---");
		}
		System.out.println("+");
		
		// Print legs
		System.out.print("|   ");
		for (int i = 0; i < ConnectFour.COLUMNS - 1; i++) {
			System.out.print("    ");
		}
		System.out.println("|");
		
		// Print feet
		System.out.print("⊥   ");
		for (int i = 0; i < ConnectFour.COLUMNS - 1; i++) {
			System.out.print("    ");
		}
		System.out.println("⊥");
	}
	
	
	
	/**
	 * Gets the token of the current player.
	 * @param player The current player.
	 * @return The token.
	 */
	public static char getToken(Token player) {
		if (player == Token.P1)
			return P1_TOKEN;
		else if (player == Token.P2)
			return P2_TOKEN;
		else
			return EMPTY;
	}

}
