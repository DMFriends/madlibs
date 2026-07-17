package src.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Main extends Application {

	public static final String APP_VERSION = "v2.0";

	private Story story;
	private String storySource;
	
	private ArrayList<TextField> inputFields;
	private TextFlow storyDisplay;
	private ScrollPane inputScroll;
	private Button newStoryButton;
	private WordListsReader wordListReader;

	@Override
	public void start(Stage primaryStage) {
		// Load a story from the API, falling back to the bundled file if the
		// service can't be reached.
		wordListReader = new WordListsReader();

		story = loadStory();

		// Create the main layout
		BorderPane root = new BorderPane();

		// Create left panel with form inputs
		inputScroll = new ScrollPane(createInputForm());
		inputScroll.setFitToWidth(true);
		inputScroll.setFitToHeight(true);
		inputScroll.setStyle("-fx-font-size: 12px; -fx-padding: 0; -fx-control-inner-background: #ffe4b5;");
		inputScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		inputScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

		// Create right panel for story display
		VBox storyPanel = createStoryPanel();

		// Create center split pane
		SplitPane splitPane = new SplitPane();
		splitPane.setDividerPositions(0.4);
		splitPane.getItems().addAll(inputScroll, storyPanel);

		root.setCenter(splitPane);

		// Create scene and show stage
		Scene scene = new Scene(root, 1000, 600);
		primaryStage.setTitle("MadLibs " + APP_VERSION);

		primaryStage.getIcons().add(
			    new Image(
			        Objects.requireNonNull(
			            getClass().getResourceAsStream("resources/madlibs.png")
			        )
			    )
			);

		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.setResizable(false);
		primaryStage.show();

		// Lock the divider in place so the user can't drag it. The divider nodes
		// only exist once CSS has been applied (after the stage is shown), so we
		// look them up here and make them ignore mouse input. A listener also
		// pins the position in case a resize tries to nudge it.
		splitPane.lookupAll(".split-pane-divider")
			.forEach(divider -> divider.setMouseTransparent(true));
		splitPane.getDividers().get(0).positionProperty().addListener(
			(_, _, _) -> splitPane.setDividerPositions(0.4));
	}

	/**
	 * Fetches a random story from the madlibs-api. If the API is unreachable
	 * (offline, timeout, error), falls back to a random story from the bundled
	 * {@code madlibs.txt} file. {@link #storySource} records which path was used.
	 */
	private Story loadStory() {
		try {
			Story fromApi = new MadLibApiReader().getRandom();
			storySource = "Online";
			return fromApi;
		} catch (Exception e) {
			System.out.println("Could not reach madlibs-api, using local stories: " + e.getMessage());
			storySource = "Offline";
			// A fresh reader re-picks a random story each time, so clicking
			// "New Story" still varies the story while offline.
			MadLibFileReader reader = new MadLibFileReader();
			return Story.fromTemplate(reader.getCurrentStoryTitle(), reader.getMadLibTemplate());
		}
	}
	
	private String copyStory()
	{
		StringBuilder sb = new StringBuilder();
	    for (Node node : storyDisplay.getChildren()) {
	        if (node instanceof Text) {
	            sb.append(((Text) node).getText());
	        }
	    }
	    
	    return sb.toString();
	}
	
	/**
	 * Copies the currently rendered story to the system clipboard.
	 * {@link Clipboard#getSystemClipboard()} delegates to the native clipboard
	 * on Windows, macOS, and Linux, so no platform-specific handling is needed.
	 */
	private void copyStoryToClipboard() {
		String story = copyStory();
		
		if (story.isEmpty()) {
			showAlert("Generate a story first!");
			return;
		}

		ClipboardContent content = new ClipboardContent();
		content.putString(story);
		Clipboard.getSystemClipboard().setContent(content);
	}

	private boolean isReadyToCopy() {
		String story = copyStory();
		
		if (story.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Loads a fresh story in the background (so the UI stays responsive), then
	 * rebuilds the input form and clears the previous result on the FX thread.
	 */
	private void loadNewStory() {
		newStoryButton.setDisable(true);
		newStoryButton.setText("LOADING...");

		Thread worker = new Thread(() -> {
			Story fresh = loadStory();
			Platform.runLater(() -> {
				story = fresh;
				inputScroll.setContent(createInputForm());
				storyDisplay.getChildren().clear();
				newStoryButton.setText("NEW STORY");
				newStoryButton.setDisable(false);
			});
		});
		worker.setDaemon(true);
		worker.start();
	}

	private VBox createStoryPanel() {
		VBox storyPanel = new VBox();
		storyPanel.setPadding(new Insets(20));
		storyPanel.setSpacing(15);
		storyPanel.setStyle("-fx-background-color: #fffacd;");

		// Add title row with a "New Story" button on the right
		Label titleLabel = new Label("YOUR SILLY STORY:");
		titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #8b4513;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		newStoryButton = new Button("NEW STORY");
		newStoryButton.setStyle(
			"-fx-font-size: 13px; " +
			"-fx-padding: 6px 12px; " +
			"-fx-background-color: #8b4513; " +
			"-fx-text-fill: white; " +
			"-fx-font-weight: bold; " +
			"-fx-border-radius: 5;"
		);
		newStoryButton.setOnAction(_ -> loadNewStory());

		Button copyStoryButton = new Button("COPY STORY");
		String copyNormalStyle = 
			    "-fx-font-size: 13px; " +
			    "-fx-padding: 6px 12px; " +
			    "-fx-background-color: #8b4513; " + 
			    "-fx-text-fill: white; " +
			    "-fx-font-weight: bold; " +
			    "-fx-background-radius: 5; " +      
			    "-fx-border-radius: 5;";

			String copyHoverStyle = 
			    "-fx-font-size: 13px; " +
			    "-fx-padding: 6px 12px; " +
			    "-fx-background-color: #a0522d; " + 
			    "-fx-text-fill: white; " +
			    "-fx-font-weight: bold; " +
			    "-fx-background-radius: 5; " +
			    "-fx-border-radius: 5;";

			String copyPressedStyle = 
			    "-fx-font-size: 13px; " +
			    "-fx-padding: 6px 12px; " +
			    "-fx-background-color: #00a31b; " + 
			    "-fx-text-fill: white; " +
			    "-fx-font-weight: bold; " +
			    "-fx-background-radius: 5; " +
			    "-fx-border-radius: 5; " +
			    "-fx-scale-x: 1; " + 
			    "-fx-scale-y: 1;";

			copyStoryButton.setStyle(copyNormalStyle);

			PauseTransition textDelay = new PauseTransition(Duration.seconds(1.5));
			textDelay.setOnFinished(_ -> {
			    copyStoryButton.setText("COPY STORY");
			    // If the mouse is still hovering when the timer finishes, keep the hover style
			    if (copyStoryButton.isHover()) {
			        copyStoryButton.setStyle(copyHoverStyle);
			    } else {
			        copyStoryButton.setStyle(copyNormalStyle);
			    }
			});

			copyStoryButton.setOnMouseEntered(_ -> {
			    // Only apply hover style if the "Copied!" timer isn't running
			    if (textDelay.getStatus() != javafx.animation.Animation.Status.RUNNING) {
			        copyStoryButton.setStyle(copyHoverStyle);
			    }
			});

			copyStoryButton.setOnMouseExited(_ -> {
			    // Only return to completely normal if the timer isn't running
			    if (textDelay.getStatus() != javafx.animation.Animation.Status.RUNNING) {
			        copyStoryButton.setStyle(copyNormalStyle);
			    }
			    copyStoryButton.setScaleX(1.0); 
			    copyStoryButton.setScaleY(1.0);
			});

			copyStoryButton.setOnMousePressed(_ -> {
				if(isReadyToCopy())
				{
					textDelay.stop(); // Reset the timer if they click it multiple times rapidly
			    	copyStoryButton.setStyle(copyPressedStyle);
			    	copyStoryButton.setText("COPIED!");  
				}    
			});

			copyStoryButton.setOnMouseReleased(_ -> {
				if(isReadyToCopy())
				{
			    copyStoryButton.setScaleX(1.0); // Snap the size back up right away
			    copyStoryButton.setScaleY(1.0);
			    copyStoryButton.setStyle(copyPressedStyle); // Stay highlighted
				}
			    textDelay.play(); // Start the 1.5-second countdown to switch the text back
			});

		copyStoryButton.setOnAction(_ -> copyStoryToClipboard());

		HBox header = new HBox(titleLabel, spacer, copyStoryButton, newStoryButton);
		header.setSpacing(10);
		header.setAlignment(Pos.CENTER_LEFT);
		storyPanel.getChildren().add(header);

		// Create framed story display
		VBox frameBox = new VBox();
		frameBox.setStyle(
			"-fx-border-color: #d4af37; " +
			"-fx-border-width: 8; " +
			"-fx-background-color: #f5f5dc; " +
			"-fx-padding: 20;"
		);
		frameBox.setAlignment(Pos.CENTER);

		storyDisplay = new TextFlow();
		storyDisplay.setStyle(
			"-fx-font-size: 18px; " +
			"-fx-text-fill: #333333; " +
			"-fx-padding: 10;"
		);
		storyDisplay.setLineSpacing(5);
		storyDisplay.setTextAlignment(TextAlignment.CENTER);

		ScrollPane scrollPane = new ScrollPane(storyDisplay);
		scrollPane.setStyle("-fx-control-inner-background: #f5f5dc;");
		scrollPane.setFitToWidth(true);

		frameBox.getChildren().add(scrollPane);
		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		VBox.setVgrow(frameBox, Priority.ALWAYS);

		storyPanel.getChildren().add(frameBox);

		return storyPanel;
	}

	private VBox createInputForm() {
		VBox formPanel = new VBox();
		formPanel.setPadding(new Insets(20));
		formPanel.setSpacing(15);
		formPanel.setStyle("-fx-background-color: #ffe4b5;");

		// Add title
		Label titleLabel = new Label(story.getTitle() + "  (" + storySource + ")");
		titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8b4513;");
		formPanel.getChildren().add(titleLabel);

		Label inputsLabel = new Label("Inputs:");
		inputsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
		formPanel.getChildren().add(inputsLabel);

		// Create input fields
		inputFields = new ArrayList<>();
		
		// Create random word buttons list (empty)
		ArrayList<Button> randomWordButtons = new ArrayList<>();

		for (String blank : story.getBlanks()) {
			
			HBox fieldContainer = new HBox();
			fieldContainer.setSpacing(10);
			fieldContainer.setAlignment(Pos.CENTER_LEFT);

			String displayLabel = wordListReader.getDisplayLabelOverride(blank);
			if (displayLabel == null) {
				displayLabel = formatPlaceholder(blank);
			}

			Label label = new Label(displayLabel + ":");
			label.setPrefWidth(150);
			label.setStyle("-fx-font-size: 12px;");

			TextField textField = new TextField();
			textField.setPrefWidth(250);
			textField.setStyle("-fx-font-size: 12px;");

			Button randomWordButton = new Button("Random " + displayLabel);
			String normalStyle = 
			    "-fx-font-size: 12px; " +
			    "-fx-padding: 4px 8px; " +
			    "-fx-background-color: #8b4513; " +
			    "-fx-text-fill: white; " +
			    "-fx-font-weight: bold; " +
			    "-fx-background-radius: 5; " + // Tip: JavaFX buttons use background-radius for curves
			    "-fx-border-radius: 5;";

			String hoverStyle = 
			    "-fx-font-size: 12px; " +
			    "-fx-padding: 4px 8px; " +
			    "-fx-background-color: #a0522d; " + // Lighter brown
			    "-fx-text-fill: white; " +
			    "-fx-font-weight: bold; " +
			    "-fx-background-radius: 5; " +
			    "-fx-border-radius: 5;";

			String pressedStyle = 
			    "-fx-font-size: 12px; " +
			    "-fx-padding: 4px 8px; " +
			    "-fx-background-color: #5c2d0c; " + // Darker brown
			    "-fx-text-fill: white; " +
			    "-fx-font-weight: bold; " +
			    "-fx-background-radius: 5; " +
			    "-fx-border-radius: 5; " +
			    "-fx-scale-x: 0.95; " +             // Shrink slightly
			    "-fx-scale-y: 0.95;";

			randomWordButton.setStyle(normalStyle);

			randomWordButton.setOnMouseEntered(_ -> randomWordButton.setStyle(hoverStyle));

			randomWordButton.setOnMouseExited(_ -> {
			    randomWordButton.setStyle(normalStyle);
			    randomWordButton.setScaleX(1.0); // Reset scale in case they drag away while clicking
			    randomWordButton.setScaleY(1.0);
			});

			randomWordButton.setOnMousePressed(_ -> randomWordButton.setStyle(pressedStyle));

			randomWordButton.setOnMouseReleased(_ -> randomWordButton.setStyle(hoverStyle));

			fieldContainer.getChildren().addAll(label, textField, randomWordButton);
			formPanel.getChildren().add(fieldContainer);

			inputFields.add(textField);
			randomWordButtons.add(randomWordButton);
		}

		// Set up event handlers for text fields and random word buttons
		for(int i = 0; i < inputFields.size(); i++) {
			int nextIndex = i + 1;
			TextField currentField = inputFields.get(i);
			Button currentRandomButton = randomWordButtons.get(i);
			String currentBlank = story.getBlanks().get(i);
			currentField.setOnAction(_ -> {
				if (nextIndex < inputFields.size()) {
					inputFields.get(nextIndex).requestFocus(); //moves to next field
				} else {
					currentField.getParent().requestFocus(); //removes focus from last field
				}
			});
			currentRandomButton.setOnAction(_ -> {
				if (nextIndex < inputFields.size()) {
					String randomWord = wordListReader.returnRandomWord(currentBlank);
					currentField.setText(randomWord);
					inputFields.get(nextIndex).requestFocus(); //moves to next field
				} else {
					String randomWord = wordListReader.returnRandomWord(currentBlank);
					currentField.setText(randomWord);
					currentField.getParent().requestFocus(); //removes focus from last field
				}
			});
		}

		// Add spacing and generate button
		Separator separator = new Separator();
		formPanel.getChildren().add(separator);

		Button generateButton = new Button("GENERATE STORY");
		generateButton.setPrefWidth(200);
		String generateNormalStyle = 
		    "-fx-font-size: 14px; " +
		    "-fx-padding: 10px; " +
		    "-fx-background-color: #ff8c00; " + // Base orange
		    "-fx-text-fill: white; " +
		    "-fx-font-weight: bold; " +
		    "-fx-background-radius: 5; " +    // Added for smooth corners
		    "-fx-border-radius: 5;";

		String generateHoverStyle = 
		    "-fx-font-size: 14px; " +
		    "-fx-padding: 10px; " +
		    "-fx-background-color: #ffa502; " + // Slightly lighter orange for hover
		    "-fx-text-fill: white; " +
		    "-fx-font-weight: bold; " +
		    "-fx-background-radius: 5; " +
		    "-fx-border-radius: 5;";

		String generatePressedStyle = 
		    "-fx-font-size: 14px; " +
		    "-fx-padding: 10px; " +
		    "-fx-background-color: #d35400; " + // Darker orange for click
		    "-fx-text-fill: white; " +
		    "-fx-font-weight: bold; " +
		    "-fx-background-radius: 5; " +
		    "-fx-border-radius: 5; " +
		    "-fx-scale-x: 0.95; " +             // Shrink slightly
		    "-fx-scale-y: 0.95;";

		
		generateButton.setStyle(generateNormalStyle);

		
		generateButton.setOnMouseEntered(_ -> generateButton.setStyle(generateHoverStyle));

		generateButton.setOnMouseExited(_ -> {
		    generateButton.setStyle(generateNormalStyle);
		    generateButton.setScaleX(1.0); // Reset scale
		    generateButton.setScaleY(1.0);
		});

		generateButton.setOnMousePressed(_ -> generateButton.setStyle(generatePressedStyle));

		generateButton.setOnMouseReleased(_ -> generateButton.setStyle(generateHoverStyle));

		

		generateButton.setOnAction(_ -> generateStory());

		VBox buttonContainer = new VBox(generateButton);
		buttonContainer.setAlignment(Pos.CENTER);
		buttonContainer.setPadding(new Insets(10, 0, 0, 0));
		formPanel.getChildren().add(buttonContainer);

		return formPanel;
	}

	private String formatPlaceholder(String placeholder) {
		// Convert placeholder like "ADJECTIVE" to "Adjective"
		String[] words = placeholder.split(" ");
		StringBuilder result = new StringBuilder();

		for (String word : words) {
			if (word.isEmpty()) {
				continue;
			}
			result.append(word.substring(0, 1).toUpperCase())
				  .append(word.substring(1).toLowerCase())
				  .append(" ");
		}

		return result.toString().trim();
	}

	private void generateStory() {
		// Collect all inputs from text fields
		ArrayList<String> answers = new ArrayList<>();

		for (TextField field : inputFields) {
			String input = field.getText().trim();
			if (input.isEmpty()) {
				showAlert("Please fill in all fields!");
				return;
			}
			answers.add(input);
		}

		// Render the completed story
		renderStory(answers);
	}

	/**
	 * Rebuilds the story display by interleaving the fixed text segments with
	 * the user's answers. Answers are highlighted, and are capitalized when they
	 * begin a new sentence.
	 */
	private void renderStory(List<String> answers) {
		storyDisplay.getChildren().clear();

		List<String> segments = story.getSegments();

		// Tracks the last character actually emitted so we can detect when a
		// segment and an answer are glued together with no whitespace between
		// them (a data-quality quirk of the source templates) and fix it up.
		char lastChar = '\0';

		for (int i = 0; i < segments.size(); i++) {
			String segment = ensureLeadingSpace(lastChar, segments.get(i));
			if (!segment.isEmpty()) {
				storyDisplay.getChildren().add(plainText(segment));
				lastChar = segment.charAt(segment.length() - 1);
			}

			// There is one fewer answer than there are segments.
			if (i < answers.size()) {
				String answer = answers.get(i);
				if (startsNewSentence(lastChar)) {
					answer = capitalizeFirst(answer);
				}
				answer = ensureLeadingSpace(lastChar, answer);
				if (!answer.isEmpty()) {
					storyDisplay.getChildren().add(answerText(answer));
					lastChar = answer.charAt(answer.length() - 1);
				}
			}
		}
	}

	/**
	 * Prefixes {@code text} with a space if it would otherwise run directly
	 * into the preceding character with no whitespace between them -- e.g. a
	 * segment like "with a dog" glued straight onto an answer like "zoom".
	 */
	private String ensureLeadingSpace(char precedingChar, String text) {
		if (text.isEmpty() || precedingChar == '\0') {
			return text;
		}
		if (Character.isLetterOrDigit(precedingChar) && Character.isLetterOrDigit(text.charAt(0))) {
			return " " + text;
		}
		return text;
	}

	private Text plainText(String content) {
		Text text = new Text(content);
		text.setStyle("-fx-font-size: 18px;");
		return text;
	}

	private Text answerText(String content) {
		Text text = new Text(content);
		text.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #ff6b35;");
		return text;
	}

	/**
	 * True if an answer follows the given last-emitted character at the start
	 * of a new sentence: either nothing has been emitted yet ({@code '\0'}),
	 * or the last real character was sentence-ending punctuation. Using the
	 * last *emitted* character (rather than just the immediately preceding
	 * segment's own text) means whitespace-only segments between two blanks
	 * don't get mistaken for the start of the story.
	 */
	private boolean startsNewSentence(char lastEmittedChar) {
		return lastEmittedChar == '\0'
				|| lastEmittedChar == '.'
				|| lastEmittedChar == '!'
				|| lastEmittedChar == '?';
	}

	private String capitalizeFirst(String value) {
		if (value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Input Required");
		alert.setHeaderText(null);
		alert.setContentText(message);

		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(
			new Image(
				Objects.requireNonNull(
					getClass().getResourceAsStream("resources/madlibs.png")
				)
			)
		);

		alert.showAndWait();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
