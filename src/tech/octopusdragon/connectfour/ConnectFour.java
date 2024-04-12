package tech.octopusdragon.connectfour;

import java.util.ArrayList;
import java.util.Random;

/**
 * Represents a game of Connect Four. Players can drop tokens down columns of
 * the rack grid.
 * @author Alex Gill
 *
 */
public class ConnectFour {
	
	public static final int ROWS = 6;			// Number of rows on the grid
	public static final int COLUMNS = 7;		// Number of columns on the grid
	public static final int LINE_LENGTH = 4;	// Number in a row needed to win
	public static final int MAX_DEPTH = 5;
	
	private Token[][] grid;		// The rack grid
	private Token curPlayer;	// The current player
	private Difficulty difficulty;	// The computer difficulty
	
	
	/**
	 * This constructor instantiates a game of Connect Four. Since no starting
	 * player is specified, one is picked randomly.
	 */
	public ConnectFour() {
		this(new Random().nextInt(2) == 0 ? Token.P1: Token.P2);
	}
	
	
	/**
	 * The constructor instantiates a game of Connect Four.
	 * @param startingPlayer The player to go first.
	 */
	public ConnectFour(Token startingPlayer) {
		grid = new Token[ROWS][COLUMNS];
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				grid[i][j] = Token.EMPTY;
			}
		}
		curPlayer = startingPlayer;
	}
	
	
	
	/**
	 * The copy constructor makes a deep copy of the given ConnectFour object.
	 * @param object The object to copy.
	 */
	public ConnectFour(ConnectFour object) {
		this.grid = new Token[ROWS][COLUMNS];
		for (int i = 0; i < ROWS; i++)
			for (int j = 0; j < COLUMNS; j++)
				this.grid[i][j] = object.grid[i][j];
		this.curPlayer = object.curPlayer;
		this.difficulty = object.difficulty;
	}
	
	
	
	/**
	 * Sets the difficulty of the computer.
	 * @param difficulty The difficulty of the computer
	 */
	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}
	
	
	
	/**
	 * Drops a token to occupy the lowest available space in the given column
	 * and then switches to the next player. If the game is over after this
	 * move, the over variable will be set to true.
	 * @param column The column to drop the token in.
	 */
	public void drop(int column) {
		if (!isOver() && validColumn(column)) {
			grid[lowestAvailableSpace(column)][column] = curPlayer;
		}
		if (!isOver())
			nextPlayer();
	}
	
	
	
	/**
	 * Moves for the computer player.
	 * @return The column the computer player dropped the Token into.
	 */
	public int computerTurn() {
		int columnToDrop;
		if (difficulty == Difficulty.EASY) {
			columnToDrop = randomComputerMove();
		}
		else if (difficulty == Difficulty.MEDIUM) {
			Random rand = new Random();
			if (rand.nextBoolean())
				columnToDrop = randomComputerMove();
			else
				columnToDrop = aiComputerMove();
		}
		else {
			columnToDrop = aiComputerMove();
		}
		drop(columnToDrop);
		return columnToDrop;
	}
	
	
	/**
	 * Returns a column for the computer to drop a token in. It is randomly
	 * selected among the valid moves.
	 * @return The column the computer player is to drop the Token into.
	 */
	private int randomComputerMove() {
		Random rand = new Random();
		ArrayList<Integer> validColumns = new ArrayList<>();
		for (int i = 0; i < COLUMNS; i++)
			if (validColumn(i))
				validColumns.add(i);
		return validColumns.get(rand.nextInt(validColumns.size()));
	}
	
	
	
	/**
	 * Returns a column for the computer to drop a token in. It is selected
	 * by a minimax algorithm.
	 * @return The column the computer player is to drop the Token into.
	 */
	private int aiComputerMove() {
		// Column of best move
		ArrayList<Integer> bestMoveCols = new ArrayList<>();
		bestMoveCols.add(0);
		int a = Integer.MIN_VALUE;
		int b = Integer.MAX_VALUE;
		int maxEval = Integer.MIN_VALUE;
		for (int i = 0; i < COLUMNS; i++) {
			if (validColumn(i)) {
				ConnectFour newTempGame = new ConnectFour(this);
				newTempGame.drop(i);
				int eval = minimax(newTempGame, 0, a, b, false);
				System.out.printf("%12d", eval);
				if (eval > maxEval) {
					maxEval = eval;
					bestMoveCols.clear();
					bestMoveCols.add(i);
				}
				else if (eval == maxEval) {
					bestMoveCols.add(i);
				}
				// Prune away unnecessary branches
				a = Math.max(a, maxEval);
				if (maxEval >= b)
					break;
			}
		}
		System.out.println();

		// Return the move
		Random rand = new Random();
		return bestMoveCols.get(rand.nextInt(bestMoveCols.size()));
	}
	
	
	
	/**
	 * A function returning an evaluation using the minimax algorithm. It is enhanced
	 * by also using alpha-beta pruning.
	 * @param tempGrid A temporary grid to keep track of hypothetical moves.
	 * @param depth The depth of the move.
	 * @param a The alpha value.
	 * @param b The beta value.
	 * @param maximizingPlayer If the hypothetical player is the maximizing player
	 * @return
	 */
	private int minimax(ConnectFour tempGame, int depth, int a, int b, boolean maximizingPlayer) {
		// Return the heuristic value if node is leaf
		Token result = tempGame.isOver() ? tempGame.getWinner(): null;
		if (result != null) {
			int eval;
			if (result.equals(curPlayer))
				eval = Integer.MAX_VALUE - depth;
			else if (result.equals(curPlayer == Token.P1 ? Token.P2: Token.P1))
				eval = Integer.MIN_VALUE + depth;
			else {
				eval = 0;
			}
			return eval;
		}
		else if (depth >= MAX_DEPTH) {
			int score = tempGame.scoreBoard(LINE_LENGTH, 0);
			return score;
		}
		
		// Maximizing player
		if (maximizingPlayer) {
			int maxEval = Integer.MIN_VALUE;
			for (int i = 0; i < COLUMNS; i++) {
				if (tempGame.validColumn(i)) {
					ConnectFour newTempGame = new ConnectFour(tempGame);
					newTempGame.drop(i);
					int eval = minimax(newTempGame, depth + 1, a, b, false);
					maxEval = Math.max(maxEval, eval);
					// Prune away unnecessary branches
					a = Math.max(a, maxEval);
					if (maxEval >= b)
						break;
				}
			}
			return maxEval;
		}
		
		// Minimizing player
		else {
			int maxEval = Integer.MAX_VALUE;
			for (int i = 0; i < COLUMNS; i++) {
				if (tempGame.validColumn(i)) {
					ConnectFour newTempGame = new ConnectFour(tempGame);
					newTempGame.drop(i);
					int eval = minimax(newTempGame, depth + 1, a, b, true);
					maxEval = Math.min(maxEval, eval);
					// Prune away unnecessary branches
					b = Math.min(b, maxEval);
					if (maxEval <= a)
						break;
				}
			}
			return maxEval;
		}
	}
	
	
	
	private int scoreBoard(int inARow, int points) {
		Token lastPlayer;	// To hold the current player token
		int inARowCount;	// To hold the number of same tokens in a row
		
		int pointValue = 1000;
		for (int i = inARow; i < LINE_LENGTH; i++)
			pointValue /= 10;
		
		// Check the rows
		for (int row = 0; row < ROWS; row++) {
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			for (int column = 0; column < COLUMNS; column++) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == inARow) {
					points += pointValue;
				}
			}
		}
		
		// Check the columns
		for (int column = 0; column < COLUMNS; column++) {
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			for (int row = 0; row < ROWS; row++) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == inARow) {
					points += pointValue;
				}
			}
		}
		
		// Check the diagonals
		for (int i = ROWS - LINE_LENGTH; i > 0; i--) {
			int row = i;
			int column = 0;
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			
			while (row < ROWS && column < COLUMNS) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == inARow) {
					points += pointValue;
				}
				
				row++;
				column++;
			}
		}
		for (int i = 0; i <= COLUMNS - LINE_LENGTH; i++) {
			int row = 0;
			int column = i;
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			
			while (row < ROWS && column < COLUMNS) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == inARow) {
					points += pointValue;
				}
				
				row++;
				column++;
			}
		}
		for (int i = ROWS - LINE_LENGTH; i > 0; i--) {
			int row = i;
			int column = COLUMNS - 1;
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			
			while (row < ROWS && column >= 0) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == inARow) {
					points += pointValue;
				}
				
				row++;
				column--;
			}
		}
		for (int i = COLUMNS - 1; i >= LINE_LENGTH; i--) {
			int row = 0;
			int column = i;
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			
			while (row < ROWS && column >= 0) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == inARow) {
					points += pointValue;
				}
				
				row++;
				column--;
			}
		}
		
		// Return the score
		if (inARow == 1)
			return points;
		else
			return scoreBoard(inARow - 1, points);
	}
	
	
	
	/**
	 * Returns whether the given column can be played on.
	 * @param column The column.
	 * @return Whether the given column can be played on.
	 */
	public boolean validColumn(int column) {
		boolean validColumn = true;
		
		// Column is not valid if less than 0
		if (column < 0)
			validColumn = false;
		
		// Column is not valid if more than the highest index
		else if (column >= COLUMNS)
			validColumn = false;
		
		// Column is not valid if already full
		else if (lowestAvailableSpace(column) < 0)
			validColumn = false;
		
		return validColumn;
	}
	
	
	
	/**
	 * Returns the rack grid.
	 * @return The rack grid.
	 */
	public Token[][] getGrid() {
		return grid;
	}
	
	
	
	/**
	 * Returns the Token of the current player.
	 * @return The Token of the current player.
	 */
	public Token curPlayer() {
		return curPlayer;
	}
	
	
	
	/**
	 * Checks to see if there is a winner. Returns P1 if player 1 has won.
	 * Returns P2 if player 2 has won. Returns EMPTY if no one has won yet.
	 * @return The Token type of the winner or EMPTY if no one has won yet.
	 */
	public Token getWinner() {
		Token lastPlayer;	// To hold the current player token
		int inARowCount;	// To hold the number of same tokens in a row
		
		// Check the rows
		for (int row = 0; row < ROWS; row++) {
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			for (int column = 0; column < COLUMNS; column++) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == LINE_LENGTH) {
					return lastPlayer;
				}
			}
		}
		
		// Check the columns
		for (int column = 0; column < COLUMNS; column++) {
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			for (int row = 0; row < ROWS; row++) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == LINE_LENGTH) {
					return lastPlayer;
				}
			}
		}
		
		// Check the diagonals
		for (int i = ROWS - LINE_LENGTH; i > 0; i--) {
			int row = i;
			int column = 0;
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			
			while (row < ROWS && column < COLUMNS) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == LINE_LENGTH) {
					return lastPlayer;
				}
				
				row++;
				column++;
			}
		}
		for (int i = 0; i <= COLUMNS - LINE_LENGTH; i++) {
			int row = 0;
			int column = i;
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			
			while (row < ROWS && column < COLUMNS) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == LINE_LENGTH) {
					return lastPlayer;
				}
				
				row++;
				column++;
			}
		}
		for (int i = ROWS - LINE_LENGTH; i > 0; i--) {
			int row = i;
			int column = COLUMNS - 1;
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			
			while (row < ROWS && column >= 0) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == LINE_LENGTH) {
					return lastPlayer;
				}
				
				row++;
				column--;
			}
		}
		for (int i = COLUMNS - 1; i >= LINE_LENGTH; i--) {
			int row = 0;
			int column = i;
			lastPlayer = Token.EMPTY;
			inARowCount = 0;
			
			while (row < ROWS && column >= 0) {
				if (grid[row][column] != lastPlayer) {
					inARowCount = 0;
					lastPlayer = grid[row][column];
				}
				if (grid[row][column] != Token.EMPTY) {
					inARowCount++;
				}
				if (inARowCount == LINE_LENGTH) {
					return lastPlayer;
				}
				
				row++;
				column--;
			}
		}
		
		// Return empty if no one has won yet.
		return Token.EMPTY;
	}
	
	
	
	/**
	 * Checks to see if the game is over.
	 * @return true if the game is over or false otherwise.
	 */
	public boolean isOver() {
		boolean isOver = false;
		
		// Find out if a player has won. If so, the game is over
		if (getWinner() != Token.EMPTY) {
			isOver = true;
		}
		
		// Find out if the board is full and there is a tied game. If so,
		// the game is over.
		else {
			isOver = true;
			outerLoop: for (int i = 0; i < ROWS; i++) {
				for (int j = 0; j < COLUMNS; j++ ) {
					if (grid[i][j] == Token.EMPTY) {
						isOver = false;
						break outerLoop;
					}
				}
			}
		}
		
		// Return the finding.
		return isOver;
	}
	
	
	
	/**
	 * Returns the lowest available space within the given column.
	 * @param column The column.
	 * @return The lowest available row or -1 if full.
	 */
	public int lowestAvailableSpace(int column) {
		int space = ROWS - 1;
		
		while (space >= 0 && grid[space][column] != Token.EMPTY) {
			space--;
		}
		
		return space;
	}
	
	
	
	/**
	 * Changes the current player to the opposite player.
	 */
	private void nextPlayer() {
		if (curPlayer == Token.P1) {
			curPlayer = Token.P2;
		}
		else {
			curPlayer = Token.P1;
		}
	}
	
}
