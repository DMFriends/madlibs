package src.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;


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

		HBox header = new HBox(titleLabel, spacer, newStoryButton);
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
		storyDisplay.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

		ScrollPane scrollPane = new ScrollPane(storyDisplay);
		scrollPane.setStyle("-fx-control-inner-background: #f5f5dc;");
		scrollPane.setFitToWidth(true);

		frameBox.getChildren().add(scrollPane);
		VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
		VBox.setVgrow(frameBox, javafx.scene.layout.Priority.ALWAYS);

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

		for (String blank : story.getBlanks()) {
			HBox fieldContainer = new HBox();
			fieldContainer.setSpacing(10);
			fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

			Label label = new Label(formatPlaceholder(blank) + ":");
			label.setPrefWidth(150);
			label.setStyle("-fx-font-size: 12px;");

			TextField textField = new TextField();
			textField.setPrefWidth(250);
			textField.setStyle("-fx-font-size: 12px;");

			Button randomWordButton = new Button("Random " + formatPlaceholder(blank));
			randomWordButton.setStyle(
				"-fx-font-size: 12px; " +
				"-fx-padding: 4px 8px; " +
				"-fx-background-color: #8b4513; " +
				"-fx-text-fill: white; " +
				"-fx-font-weight: bold; " +
				"-fx-border-radius: 5;"
			);
			randomWordButton.setOnAction(_ -> {
				String randomWord = wordListReader.returnRandomWord(blank);
				textField.setText(randomWord);
			});

			fieldContainer.getChildren().addAll(label, textField, randomWordButton);
			formPanel.getChildren().add(fieldContainer);

			inputFields.add(textField);
		}

		// Add spacing and generate button
		Separator separator = new Separator();
		formPanel.getChildren().add(separator);

		Button generateButton = new Button("GENERATE STORY");
		generateButton.setPrefWidth(200);
		generateButton.setStyle(
			"-fx-font-size: 14px; " +
			"-fx-padding: 10px; " +
			"-fx-background-color: #ff8c00; " +
			"-fx-text-fill: white; " +
			"-fx-font-weight: bold; " +
			"-fx-border-radius: 5;"
		);

		generateButton.setOnAction(_ -> generateStory());

		VBox buttonContainer = new VBox(generateButton);
		buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
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

	// private void generateRandomWordForField(TextField field, String placeholder) {
	// 	// Placeholder for random word generation logic
	// 	// For now, just fill with a dummy word based on the placeholder
		
	// }

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

		for (int i = 0; i < segments.size(); i++) {
			String segment = segments.get(i);
			if (!segment.isEmpty()) {
				storyDisplay.getChildren().add(plainText(segment));
			}

			// There is one fewer answer than there are segments.
			if (i < answers.size()) {
				String answer = answers.get(i);
				if (startsNewSentence(segment)) {
					answer = capitalizeFirst(answer);
				}
				storyDisplay.getChildren().add(answerText(answer));
			}
		}
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

	/** True if the answer following this segment starts a new sentence. */
	private boolean startsNewSentence(String precedingSegment) {
		String trimmed = precedingSegment.stripTrailing();
		if (trimmed.isEmpty()) {
			// Nothing (yet) before the blank -- it's the very start of the story.
			return true;
		}
		char last = trimmed.charAt(trimmed.length() - 1);
		return last == '.' || last == '!' || last == '?';
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
		alert.showAndWait();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
