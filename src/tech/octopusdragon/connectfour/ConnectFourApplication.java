package tech.octopusdragon.connectfour;

import java.util.Random;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ConnectFourApplication extends Application {
	
	// Constants
	private final double DEF_LEN = 8.0;	// Default length of bars
	private final double DIALOG_WIDTH = 500.0;	// Width of new game dialog box
	private final double DIALOG_HEIGHT = 200.0;	// Height of new game dialog box
	private final int WAIT_TIME = 500;	// Millis to wait before computer moves
	
	// Variables
	private ConnectFour game;	// The game
	private boolean playing;	// Indicates whether an animation is playing
	private boolean singlePlayer;	// Indicates single player game
	private Token playerToken;	// Player's token in a single player game
	private int selectedDifficulty = 1;	// Index of selected difficulty
	private int selectedPlayerColor = 0;	// Index of selected player color
	private int selectedFirstPlayer = 2;	// Index of selected first player
	
	// GUI components
	private Stage primaryStage;	// The stage
	private Scene scene;	// The scene
	private StackPane root;	// The root
	private Pane canvas;	// The space with the content
	private StackPane stackPane;	// The board + frontPanes + backImages
	private GridPane boardGridPane;	// The board
	private Pane[][] frontPanes;
	private ImageView[][] backImages;
	private GridPane rackVBox;
	private ImageView curPlayerImageView;	// To hold the current player
	private final Image P1_IMAGE = new Image(ConnectFourApplication.class.getClassLoader().getResourceAsStream("p1_token.png"));
	private final String P1_COLOR = "Red";
	private final Image P2_IMAGE = new Image(ConnectFourApplication.class.getClassLoader().getResourceAsStream("p2_token.png"));
	private final String P2_COLOR = "Yellow";
	private final Image BACKGROUND_IMAGE = new Image(ConnectFourApplication.class.getClassLoader().getResourceAsStream("background.jpg"));
	private Image holeImage = new Image(ConnectFourApplication.class.getClassLoader().getResourceAsStream("hole.png"));
	private MediaPlayer clinkSound = new MediaPlayer(new Media(ConnectFourApplication.class.getClassLoader().getResource("clink.wav").toExternalForm()));
	private MediaPlayer cheerSound = new MediaPlayer(new Media(ConnectFourApplication.class.getClassLoader().getResource("cheer.wav").toExternalForm()));
	private MediaPlayer lossSound = new MediaPlayer(new Media(ConnectFourApplication.class.getClassLoader().getResource("loss.wav").toExternalForm()));
	
	
	@Override
	public void init() {
		
		// Instantiate the first game.
		game = new ConnectFour();
		
		// Set up sounds
		clinkSound.setOnEndOfMedia(() -> {
			clinkSound.stop();
		});
		cheerSound.setOnEndOfMedia(() -> {
			cheerSound.stop();
		});
		lossSound.setOnEndOfMedia(() -> {
			lossSound.stop();
		});
		
		// No animation is playing yet
		playing = false;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Create GridPane
		boardGridPane = new GridPane();
		
		// Create the corners of the board
		Image topLeftCornerImage = new Image(getClass().getClassLoader().getResourceAsStream("top_left_corner.png"));
		ImageView topLeftCorner = new ImageView(topLeftCornerImage);
		boardGridPane.add(topLeftCorner, 0, 0);
		Image topRightCornerImage = new Image(getClass().getClassLoader().getResourceAsStream("top_right_corner.png"));
		ImageView topRightCorner = new ImageView(topRightCornerImage);
		boardGridPane.add(topRightCorner, ConnectFour.COLUMNS * 2 + 2, 0);
		Image bottomLeftCornerImage = new Image(getClass().getClassLoader().getResourceAsStream("bottom_left_corner.png"));
		ImageView bottomLeftCorner = new ImageView(bottomLeftCornerImage);
		boardGridPane.add(bottomLeftCorner, 0, ConnectFour.ROWS * 2 + 2);
		Image bottomRightCornerImage = new Image(getClass().getClassLoader().getResourceAsStream("bottom_right_corner.png"));
		ImageView bottomRightCorner = new ImageView(bottomRightCornerImage);
		boardGridPane.add(bottomRightCorner, ConnectFour.COLUMNS * 2 + 2, ConnectFour.ROWS * 2 + 2);
		
		// Create the inner areas of the board
		for (int i = 1; i < ConnectFour.ROWS * 2 + 2; i++) {
			for (int j = 1; j < ConnectFour.COLUMNS * 2 + 2; j++) {
				if (i % 2 == 1 || j % 2 == 1) {
					Pane pane = new Pane();
					pane.setBackground(new Background(new BackgroundFill(Color.web("00137F"), null, null)));
					boardGridPane.add(pane, j, i);
				}
			}
		}
		
		// Create the holes of the board
		ImageView[][] holes = new ImageView[ConnectFour.ROWS][ConnectFour.COLUMNS];
		for (int i = 0; i < ConnectFour.ROWS; i++) {
			for (int j = 0; j < ConnectFour.COLUMNS; j++) {
				holes[i][j] = new ImageView(holeImage);
				boardGridPane.add(holes[i][j], j * 2 + 2, i * 2 + 2);
			}
		}
		
		// Create row and column constraints
		RowConstraints[] rows = new RowConstraints[GridPane.getRowIndex(bottomLeftCorner) + 1];
		ColumnConstraints[] columns = new ColumnConstraints[GridPane.getColumnIndex(topRightCorner) + 1];
		for (int i = 0; i <= ConnectFour.ROWS * 2 + 2; i++) {
			if (i == 0 || i == ConnectFour.ROWS * 2 + 2) {
				rows[i] = new RowConstraints(topLeftCornerImage.getWidth());
			}
			else if (i % 2 == 0) {
				rows[i] = new RowConstraints(holeImage.getWidth());
			}
			else {
				rows[i] = new RowConstraints(DEF_LEN);
			}
			boardGridPane.getRowConstraints().add(rows[i]);
		}
		for (int j = 0; j <= ConnectFour.COLUMNS * 2 + 2; j++) {
			if (j == 0 || j == ConnectFour.COLUMNS * 2 + 2) {
				columns[j] = new ColumnConstraints(topLeftCornerImage.getHeight());
			}
			else if (j % 2 == 0) {
				columns[j] = new ColumnConstraints(holeImage.getHeight());
			}
			else {
				columns[j] = new ColumnConstraints(DEF_LEN);
			}
			boardGridPane.getColumnConstraints().add(columns[j]);
		}
		
		// Create the edges of the boar
		double fitWidth = 0.0;
		for (int i = 0; i < ConnectFour.COLUMNS; i++)
			fitWidth += holeImage.getWidth();
		for (int i = 1; i <= ConnectFour.COLUMNS * 2 + 1; i += 2)
			fitWidth += DEF_LEN;
		double fitHeight = 0.0;
		for (int i = 0; i < ConnectFour.ROWS; i++)
			fitHeight += holeImage.getHeight();
		for (int i = 1; i <= ConnectFour.ROWS * 2 + 1; i += 2)
			fitHeight += DEF_LEN;
		Image topEdgeImage = new Image(getClass().getClassLoader().getResourceAsStream("top_edge.png"));
		ImageView topEdge = new ImageView(topEdgeImage);
		Pane topEdgePane = new Pane(topEdge);
		boardGridPane.add(topEdgePane, 1, 0);
		GridPane.setColumnSpan(topEdgePane, ConnectFour.COLUMNS * 2 + 1);
		topEdge.setFitWidth(fitWidth);
		Image leftEdgeImage = new Image(getClass().getClassLoader().getResourceAsStream("left_edge.png"));
		ImageView leftEdge = new ImageView(leftEdgeImage);
		Pane leftEdgePane = new Pane(leftEdge);
		boardGridPane.add(leftEdgePane, 0, 1);
		GridPane.setRowSpan(leftEdgePane, ConnectFour.ROWS * 2 + 1);
		leftEdge.setFitHeight(fitHeight);
		Image bottomEdgeImage = new Image(getClass().getClassLoader().getResourceAsStream("bottom_edge.png"));
		ImageView bottomEdge = new ImageView(bottomEdgeImage);
		Pane bottomEdgePane = new Pane(bottomEdge);
		boardGridPane.add(bottomEdgePane, 1, ConnectFour.ROWS * 2 + 2);
		GridPane.setColumnSpan(bottomEdgePane, ConnectFour.COLUMNS * 2 + 1);
		bottomEdge.setFitWidth(fitWidth);
		Image rightEdgeImage = new Image(getClass().getClassLoader().getResourceAsStream("right_edge.png"));
		ImageView rightEdge = new ImageView(rightEdgeImage);
		Pane rightEdgePane = new Pane(rightEdge);
		boardGridPane.add(rightEdgePane, ConnectFour.COLUMNS * 2 + 2, 1);
		GridPane.setRowSpan(rightEdgePane, ConnectFour.ROWS * 2 + 1);
		rightEdge.setFitHeight(fitHeight);
		
		// Create the GridPane to go behind the board
		backImages = new ImageView[ConnectFour.ROWS][ConnectFour.COLUMNS];
		GridPane back = new GridPane();
		for (int i = 0; i <= ConnectFour.ROWS * 2 + 2; i++) {
			back.getRowConstraints().add(rows[i]);
		}
		for (int j = 0; j <= ConnectFour.COLUMNS * 2 + 2; j++) {
			back.getColumnConstraints().add(columns[j]);
		}
		for (int i = 0; i <= ConnectFour.ROWS * 2 + 2; i++) {
			for (int j = 0; j <= ConnectFour.COLUMNS * 2 + 2; j++) {
				ImageView curImageView = new ImageView();
				if ((i > 0 && i < ConnectFour.ROWS * 2 + 2 && i % 2 == 0) &&
					(j > 0 && j < ConnectFour.COLUMNS * 2 + 2 && j % 2 == 0)) {
					backImages[i / 2 - 1][j / 2 - 1] = curImageView;
				}
				back.add(curImageView, j, i);
			}
		}
		
		// Create the GridPane to go behind the board
		frontPanes = new Pane[rows.length][columns.length];
		GridPane front = new GridPane();
		for (int i = 0; i <= ConnectFour.ROWS * 2 + 2; i++) {
			front.getRowConstraints().add(rows[i]);
		}
		for (int j = 0; j <= ConnectFour.COLUMNS * 2 + 2; j++) {
			front.getColumnConstraints().add(columns[j]);
		}
		for (int i = 0; i <= ConnectFour.ROWS * 2 + 2; i++) {
			for (int j = 0; j <= ConnectFour.COLUMNS * 2 + 2; j++) {
				frontPanes[i][j] = new Pane();
				if (j > 0 && j < ConnectFour.COLUMNS * 2 + 2 && j % 2 == 0) {
					frontPanes[i][j].setOnMouseClicked(new ClickHandler());
					frontPanes[i][j].setOnMouseEntered(new MouseEnterHandler());
					frontPanes[i][j].setOnMouseExited(new MouseExitHandler());
				}
				front.add(frontPanes[i][j], j, i);
			}
		}
		
		// Put the layers in a StackPane
		stackPane = new StackPane(back, boardGridPane, front);
		
		// Create the stands
		Image leftStand = new Image(getClass().getClassLoader().getResourceAsStream("left_stand.png"));
		ImageView standLeft = new ImageView(leftStand);
		Image rightStand = new Image(getClass().getClassLoader().getResourceAsStream("right_stand.png"));
		ImageView standRight = new ImageView(rightStand);
		rackVBox = new GridPane();
		rackVBox.add(stackPane, 0, 0);
		GridPane.setColumnSpan(stackPane, 2);
		rackVBox.add(standLeft, 0, 1);
		GridPane.setHalignment(standLeft, HPos.LEFT);
		rackVBox.add(standRight, 1, 1);
		GridPane.setHalignment(standRight, HPos.RIGHT);
		
		// Put a background and the StackPane in a Pane
		ImageView background = new ImageView(BACKGROUND_IMAGE);
		canvas = new Pane(background, rackVBox);
		canvas.setMinSize(BACKGROUND_IMAGE.getWidth(), BACKGROUND_IMAGE.getHeight());
		canvas.setPrefSize(BACKGROUND_IMAGE.getWidth(), BACKGROUND_IMAGE.getHeight());
		canvas.setMaxSize(BACKGROUND_IMAGE.getWidth(), BACKGROUND_IMAGE.getHeight());
		
		// Position the rack on the background
		primaryStage.setOnShown(event ->{
			rackVBox.setLayoutX(BACKGROUND_IMAGE.getWidth() / 2 - rackVBox.getWidth() / 2);
		});
		rackVBox.setLayoutY(80.0);
		
		// Create a small pane to show the current player.
		VBox curPlayerBox = new VBox();
		curPlayerBox.setBackground(new Background(new BackgroundFill(Color.web("white", 0.75), new CornerRadii(10.0), null)));
		curPlayerBox.setMinSize(200, 150);
		curPlayerBox.setPrefSize(200, 150);
		curPlayerBox.setMaxSize(200, 150);
		curPlayerBox.setAlignment(Pos.CENTER);
		curPlayerBox.setSpacing(17.0);
		StackPane.setAlignment(curPlayerBox, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(curPlayerBox, new Insets(20.0));
		Label curPlayerLabel = new Label("CURRENT PLAYER");
		curPlayerLabel.setFont(new Font("Century Gothic Bold", 19));
		curPlayerBox.getChildren().add(curPlayerLabel);
		curPlayerImageView = new ImageView(game.curPlayer() == Token.P1 ? P1_IMAGE: P2_IMAGE);
		curPlayerBox.getChildren().add(curPlayerImageView);
		
		// Put the Pane in the root.
		root = new StackPane();
		root.getChildren().addAll(canvas, curPlayerBox);
		root.setAlignment(Pos.CENTER);
		root.setBackground(new Background(new BackgroundFill(Color.DARKGREEN, null, null)));
		
		// Set the scene
		scene = new Scene(root, BACKGROUND_IMAGE.getWidth(), BACKGROUND_IMAGE.getHeight());
		this.primaryStage = primaryStage;
		primaryStage.setScene(scene);
		primaryStage.setTitle("Connect 4");
		primaryStage.setResizable(false);
		
		// Show the dialog to start a game
		newGameDialog();
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	/**
	 * Allows a user to move when a tile is clicked.
	 * @author Alex Gill
	 *
	 */
	public class ClickHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			
			// If it is the computer player's turn, do nothing
			if (singlePlayer && game.curPlayer() != playerToken) return;
			
			// Get the column of the clicked tile.
			int column = GridPane.getColumnIndex((Node)event.getSource());
			column = column / 2 - 1;
			
			// Drop a token in the column.
			dropToken(column);
		}
	}
	
	/**
	 * Highlights the tile's column when the user hovers the mouse over it.
	 * @author Alex Gill
	 *
	 */
	public class MouseEnterHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			
			// Get the column of the entered tile.
			int column = GridPane.getColumnIndex((Node)event.getSource());
			
			// Highlight all of the tiles in the column and
			// neighboring columns.
			for (int i = 0; i < frontPanes.length; i++) {
				frontPanes[i][column - 1].setStyle("-fx-background-color: rgba(255, 255, 0, 0.25);");
				frontPanes[i][column].setStyle("-fx-background-color: rgba(255, 255, 0, 0.25);");
				frontPanes[i][column + 1].setStyle("-fx-background-color: rgba(255, 255, 0, 0.25);");
			}
			
			// Change the cursor to hand
			scene.setCursor(Cursor.HAND);
		}
	}
	
	/**
	 * Changes the cursor back and stops highlighting the column when the mouse
	 * exits the tile.
	 * @author Alex Gill
	 *
	 */
	public class MouseExitHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			
			// Get the column of the entered tile.
			int column = GridPane.getColumnIndex((Node)event.getSource());
			
			// Un-highlight all of the tiles in the column and
			// neighboring columns.
			for (int i = 0; i < frontPanes.length; i++) {
				frontPanes[i][column - 1].setStyle("");
				frontPanes[i][column].setStyle("");
				frontPanes[i][column + 1].setStyle("");
			}
			
			// Change the cursor to hand
			scene.setCursor(Cursor.DEFAULT);
		}
	}
	
	/**
	 * Drops the current player's token in the given column and displays it.
	 * @param column The column to drop the token into.
	 */
	public void dropToken(int column) {
		
		// If the game is already over or not a valid column, do nothing
		if (game.isOver() || !game.validColumn(column) || playing) return;
		
		// Get the information of the token being dropped.
		Token curToken = game.curPlayer();
		int row = game.lowestAvailableSpace(column);
		
		// Drop the token
		game.drop(column);
		
		// Create the animation
		ImageView imageView = new ImageView(curToken == Token.P1 ? P1_IMAGE : P2_IMAGE);
		double x = rackVBox.getLayoutX() + backImages[row][column].getLayoutX();
		double startY = 0.0;
		double endY = rackVBox.getLayoutY() + backImages[row][column].getLayoutY() - holeImage.getWidth() / 2; // still don't know why i have to subtract half hole height
		TranslateTransition ttrans = new TranslateTransition(Duration.millis(1.5 * endY), imageView);
		ttrans.setFromX(x);
		ttrans.setFromY(startY);
		ttrans.setToX(x);
		ttrans.setToY(endY);
		
		// Display the updated rack grid after the animation
		ttrans.setOnFinished(e -> {
			
			// Play sound
			if (!game.isOver())
				clinkSound.play();
			else
				cheerSound.play();
			
			// Remove the animated image and place permanent image.
			backImages[row][column].setImage(curToken == Token.P1 ? P1_IMAGE : P2_IMAGE);
			canvas.getChildren().remove(imageView);
			playing = false;
			
			// Display the next player in the current player box if the game is not over
			if (!game.isOver()) {
				curPlayerImageView.setImage(curToken == Token.P1 ? P2_IMAGE : P1_IMAGE);
				
				// If it is now the computer player's turn, move for the computer.
				if (singlePlayer) {
					new Thread(new Task<Void>() {
						@Override
						public Void call() throws InterruptedException {
							Thread.sleep(WAIT_TIME);
							computerDropToken();
							return null;
						}
					}).start();
				}
			}
			
			// If the game is over, show a play again dialog
			else {
				Platform.runLater(() -> {
					newGameDialog();
				});
			}
		});
		
		// Start the animation
		canvas.getChildren().add(1, imageView);
		ttrans.play();
		playing = true;
	}
	
	
	public void computerDropToken() {
		
		// Get the token being dropped.
		Token curToken = game.curPlayer();
		
		// Drop the computer's token and get the droped column
		int column = game.computerTurn();
		
		// Calculate the row of the dropped Token
		int row = game.lowestAvailableSpace(column) + 1;
		
		// Create the animation
		Platform.runLater(() -> {
			ImageView imageView = new ImageView(curToken == Token.P1 ? P1_IMAGE : P2_IMAGE);
			double x = rackVBox.getLayoutX() + backImages[row][column].getLayoutX();
			double startY = 0.0;
			double endY = rackVBox.getLayoutY() + backImages[row][column].getLayoutY() - holeImage.getWidth() / 2; // still don't know why i have to subtract half hole height
			TranslateTransition ttrans = new TranslateTransition(Duration.millis(1.5 * endY), imageView);
			ttrans.setFromX(x);
			ttrans.setFromY(startY);
			ttrans.setToX(x);
			ttrans.setToY(endY);
			
			// Display the updated rack grid after the animation
			ttrans.setOnFinished(e -> {
				
				// Play sound
				if (!game.isOver())
					clinkSound.play();
				else if (game.getWinner() != playerToken)
					lossSound.play();
				else
					cheerSound.play();
				
				// Remove the animated image and place permanent image.
				backImages[row][column].setImage(curToken == Token.P1 ? P1_IMAGE : P2_IMAGE);
				canvas.getChildren().remove(imageView);
				playing = false;
				
				// Display the next player in the current player box if the game is not over
				if (!game.isOver()) {
					curPlayerImageView.setImage(curToken == Token.P1 ? P2_IMAGE : P1_IMAGE);
				}
				
				// If the game is over, show a play again dialog
				else {
					Platform.runLater(() -> {
						newGameDialog();
					});
				}
			});
			
			// Start the animation
			canvas.getChildren().add(1, imageView);
			ttrans.play();
			playing = true;
		});
	}
	
	
	/**
	 * Show the user a dialog that can start a new game or exit the program.
	 */
	public void newGameDialog() {
		
		// Determine the message and graphic
		String title;
		String message;
		ImageView graphic;
		String prompt;
		if (!primaryStage.isShowing()) {
			/*title = "Connect 4";
			message = "Welcome to Connect 4!";
			graphic = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("token.png")));
			prompt = "What type of game would you like to play?";*/
			singlePlayer = false;
			primaryStage.show();
			newGame();
			return;
		}
		else if (singlePlayer && game.getWinner() == playerToken) {
			title = "Game Over";
			message = "You won!";
			graphic = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("winner.png")));
			prompt = "Would you like to play again?";
		}
		else if (singlePlayer && game.getWinner() == (playerToken == Token.P1 ? Token.P2: Token.P1)) {
			title = "Game Over";
			message = "You lost... Better luck next time.";
			graphic = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("loser.png")));
			prompt = "Would you like to play again?";
		}
		else if (game.getWinner() == Token.P1) {
			title = "Game Over";
			message = P1_COLOR + " won!";
			graphic = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("winner.png")));
			prompt = "Would you like to play again?";
		}
		else if (game.getWinner() == Token.P2) {
			title = "Game Over";
			message = P2_COLOR + " won!";
			graphic = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("winner.png")));
			prompt = "Would you like to play again?";
		}
		else {
			title = "Game Over";
			message = "It was a draw!";
			graphic = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("tie.png")));
			prompt = "Would you like to play again?";
		}
		
		// Create the buttons
		/*ButtonType singlePlayerButtonType = new ButtonType("Single-Player Game", ButtonData.OK_DONE);*/
		ButtonType twoPlayerButtonType = new ButtonType("Play", ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("Exit", ButtonData.CANCEL_CLOSE);
		
		// Create the dialog
		Alert dialog = new Alert(AlertType.CONFIRMATION, prompt, /*singlePlayerButtonType,*/ twoPlayerButtonType, cancelButtonType);
		dialog.setTitle(title);
		dialog.setHeaderText(message);
		dialog.setGraphic(graphic);
		dialog.getDialogPane().setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		
		// Standby and act depending on the user's choice
		dialog.showAndWait().ifPresent(response -> {
			
			/*if (response == singlePlayerButtonType) {
				singlePlayerGameDialog();
			}
			else*/ if (response == twoPlayerButtonType) {
				
				// If it is the first game, show the stage which is not visible
				if (!primaryStage.isShowing()) {
					primaryStage.show();
				}
				singlePlayer = false;
				newGame();
			}
			else {
				Platform.exit();
			}
		});
	}
	
	
	public void singlePlayerGameDialog() {
		
		// Create the buttons
		ButtonType playButtonType = new ButtonType("Play", ButtonData.OK_DONE);
		ButtonType backButtonType = new ButtonType("Back", ButtonData.CANCEL_CLOSE);
		
		// Create the settings radio buttons
		Label difficultyLabel = new Label("Computer Difficulty");
		ToggleGroup difficultyToggleGroup = new ToggleGroup();
		RadioButton easyRadioButton = new RadioButton("Easy");
		easyRadioButton.setToggleGroup(difficultyToggleGroup);
		RadioButton mediumRadioButton = new RadioButton("Medium");
		mediumRadioButton.setToggleGroup(difficultyToggleGroup);
		RadioButton hardRadioButton = new RadioButton("Hard");
		hardRadioButton.setToggleGroup(difficultyToggleGroup);
		difficultyToggleGroup.selectToggle(difficultyToggleGroup.getToggles().get(selectedDifficulty));
		
		Label playerColorLabel = new Label("Your Color");
		ToggleGroup playerColorToggleGroup = new ToggleGroup();
		RadioButton redRadioButton = new RadioButton("Red");
		redRadioButton.setToggleGroup(playerColorToggleGroup);
		RadioButton yellowRadioButton = new RadioButton("Yellow");
		yellowRadioButton.setToggleGroup(playerColorToggleGroup);
		RadioButton randomColorRadioButton = new RadioButton("Random");
		randomColorRadioButton.setToggleGroup(playerColorToggleGroup);
		playerColorToggleGroup.selectToggle(playerColorToggleGroup.getToggles().get(selectedPlayerColor));
		
		Label firstPlayerLabel = new Label("First Move");
		ToggleGroup firstPlayerToggleGroup = new ToggleGroup();
		RadioButton youRadioButton = new RadioButton("You");
		youRadioButton.setToggleGroup(firstPlayerToggleGroup);
		RadioButton computerRadioButton = new RadioButton("Computer");
		computerRadioButton.setToggleGroup(firstPlayerToggleGroup);
		RadioButton randomFirstPlayerRadioButton = new RadioButton("Random");
		randomFirstPlayerRadioButton.setToggleGroup(firstPlayerToggleGroup);
		firstPlayerToggleGroup.selectToggle(firstPlayerToggleGroup.getToggles().get(selectedFirstPlayer));
		
		GridPane radioBox = new GridPane();
		radioBox.add(difficultyLabel, 0, 0);
		radioBox.add(easyRadioButton, 0, 1);
		radioBox.add(mediumRadioButton, 1, 1);
		radioBox.add(hardRadioButton, 2, 1);
		radioBox.add(playerColorLabel, 0, 3);
		radioBox.add(redRadioButton, 0, 4);
		radioBox.add(yellowRadioButton, 1, 4);
		radioBox.add(randomColorRadioButton, 2, 4);
		radioBox.add(firstPlayerLabel, 0, 6);
		radioBox.add(youRadioButton, 0, 7);
		radioBox.add(computerRadioButton, 1, 7);
		radioBox.add(randomFirstPlayerRadioButton, 2, 7);
		radioBox.setHgap(10.0);
		radioBox.setVgap(10.0);
		
		// Create the dialog
		Alert dialog = new Alert(AlertType.CONFIRMATION, null, playButtonType, backButtonType);
		dialog.setTitle("Single-Player Game");
		dialog.setGraphic(new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("computer.png"))));
		dialog.setHeaderText("Set the settings to your liking and press Play when ready.");
		dialog.getDialogPane().setContent(radioBox);
		dialog.getDialogPane().setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		
		// Standby and act depending on the user's choice
		dialog.showAndWait().ifPresent(response -> {
			
			if (response == playButtonType) {
				singlePlayer = true;
				
				if (redRadioButton.isSelected()) {
					playerToken = Token.P1;
					selectedPlayerColor = 0;
				}
				else if (yellowRadioButton.isSelected()) {
					playerToken = Token.P2;
					selectedPlayerColor = 1;
				}
				else {
					Random rand = new Random();
					if (rand.nextBoolean())
						playerToken = Token.P1;
					else
						playerToken = Token.P2;
					selectedPlayerColor = 2;
				}
				
				if (youRadioButton.isSelected()) {
					newGame(playerToken);
					selectedFirstPlayer = 0;
				}
				else if (computerRadioButton.isSelected()) {
					newGame(playerToken == Token.P1 ? Token.P2: Token.P1);
					selectedFirstPlayer = 1;
				}
				else {
					newGame();
					selectedFirstPlayer = 2;
				}
				
				if (easyRadioButton.isSelected()) {
					game.setDifficulty(Difficulty.EASY);
					selectedDifficulty = 0;
				}
				else if (mediumRadioButton.isSelected()) {
					game.setDifficulty(Difficulty.MEDIUM);
					selectedDifficulty = 1;
				}
				else {
					game.setDifficulty(Difficulty.HARD);
					selectedDifficulty = 2;
				}
				
				// If it is the first game, show the stage which is not visible
				if (!primaryStage.isShowing()) {
					primaryStage.show();
				}
				
				// If it is the computer's move, move for the computer
				if (game.curPlayer() != playerToken)
					computerDropToken();
			}
			else {
				newGameDialog();
			}
		});
	}
	
	
	/**
	 * Starts a new game
	 */
	public void newGame() {
		
		// Instantiate the new game.
		game = new ConnectFour();
		
		// Remove token images from backImages
		for (int i = 0; i < ConnectFour.ROWS; i++) {
			for (int j = 0; j < ConnectFour.COLUMNS; j++) {
				backImages[i][j].setImage(null);
			}
		}
		
		// Set the correct "current player" pane
		curPlayerImageView.setImage(game.curPlayer() == Token.P1 ? P1_IMAGE: P2_IMAGE);
	}
	
	
	/**
	 * Starts a new game that starts with the specified player
	 */
	public void newGame(Token firstPlayer) {
		
		// Instantiate the new game.
		game = new ConnectFour(firstPlayer);
		
		// Remove token images from backImages
		for (int i = 0; i < ConnectFour.ROWS; i++) {
			for (int j = 0; j < ConnectFour.COLUMNS; j++) {
				backImages[i][j].setImage(null);
			}
		}
		
		// Set the correct "current player" pane
		curPlayerImageView.setImage(game.curPlayer() == Token.P1 ? P1_IMAGE: P2_IMAGE);
	}

}
