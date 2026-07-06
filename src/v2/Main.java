package src.v2;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.util.ArrayList;

public class Main extends Application {
	
	public static final String APP_VERSION = "v2.0";
	
	private MadLibs madLibs = new MadLibs();
	private ArrayList<String> placeholders;
	private ArrayList<TextField> inputFields;
	private TextFlow storyDisplay;

	@Override
	public void start(Stage primaryStage) {
		// Get placeholders from the template
		String template = MadLibs.madLibFileReader.getMadLibTemplate();
		placeholders = MadLibs.getPlaceholders(template);
		
		// Create the main layout
		BorderPane root = new BorderPane();
		
		// Create left panel with form inputs
		VBox leftPanel = createInputForm();
		ScrollPane scrollPane = new ScrollPane(leftPanel);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		scrollPane.setStyle("-fx-font-size: 12px; -fx-padding: 0; -fx-control-inner-background: #ffe4b5;");
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		
		// Create right panel for story display
		VBox storyPanel = createStoryPanel();
		
		// Create center split pane
		SplitPane splitPane = new SplitPane();
		splitPane.setDividerPositions(0.4);
		splitPane.getItems().addAll(scrollPane, storyPanel);
		
		root.setCenter(splitPane);
		
		// Create scene and show stage
		Scene scene = new Scene(root, 1000, 600);
		primaryStage.setTitle("MadLibs " + APP_VERSION);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	private VBox createStoryPanel() {
		VBox storyPanel = new VBox();
		storyPanel.setPadding(new Insets(20));
		storyPanel.setSpacing(15);
		storyPanel.setStyle("-fx-background-color: #fffacd;");
		
		// Add title
		Label titleLabel = new Label("YOUR SILLY STORY:");
		titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #8b4513;");
		storyPanel.getChildren().add(titleLabel);
		
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
		Label titleLabel = new Label("STORY " + MadLibs.madLibFileReader.getCurrentStoryIndex() + ": " + MadLibs.madLibFileReader.getCurrentStoryTitle());
		titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8b4513;");
		formPanel.getChildren().add(titleLabel);
		
		Label inputsLabel = new Label("Inputs:");
		inputsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
		formPanel.getChildren().add(inputsLabel);
		
		// Create input fields
		inputFields = new ArrayList<>();
		
		for (String placeholder : placeholders) {
			HBox fieldContainer = new HBox();
			fieldContainer.setSpacing(10);
			fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
			
			Label label = new Label(formatPlaceholder(placeholder) + ":");
			label.setPrefWidth(150);
			label.setStyle("-fx-font-size: 12px;");
			
			TextField textField = new TextField();
			textField.setPrefWidth(250);
			textField.setStyle("-fx-font-size: 12px;");
			
			fieldContainer.getChildren().addAll(label, textField);
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
		
		generateButton.setOnAction(e -> generateStory());
		
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
			result.append(word.substring(0, 1).toUpperCase())
				  .append(word.substring(1).toLowerCase())
				  .append(" ");
		}
		
		return result.toString().trim();
	}
	
	private void generateStory() {
		// Collect all inputs from text fields
		ArrayList<String> replacements = new ArrayList<>();
		
		for (TextField field : inputFields) {
			String input = field.getText().trim();
			if (input.isEmpty()) {
				showAlert("Please fill in all fields!");
				return;
			}
			replacements.add(input);
		}
		
		// Generate the story
		String template = madLibs.madLibFileReader.getMadLibTemplate();
		buildStoryTextFlow(template, replacements);
	}
	
	private void buildStoryTextFlow(String template, ArrayList<String> replacements) {
		storyDisplay.getChildren().clear();
		
		String result = template;
		int replacementIndex = 0;
		
		for (int i = 0; i < replacements.size(); i++) {
			String placeholder = placeholders.get(i);
			String replacement = replacements.get(i);
			String placeholderText = "[" + placeholder + "]";
			
			// Check if the placeholder is at the start of a sentence
			int index = result.indexOf(placeholderText);
			if (index > 0 && result.charAt(index - 1) == ' ' && 
			    index >= 2 && result.charAt(index - 2) == '.') {
				replacement = replacement.substring(0, 1).toUpperCase() + replacement.substring(1);
			}
			
			result = result.replace(placeholderText, "|||" + replacement + "|||");
		}
		
		// Split by the delimiter and build TextFlow
		String[] parts = result.split("\\|\\|\\|");
		
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].isEmpty()) continue;
			
			Text text = new Text(parts[i]);
			text.setStyle("-fx-font-size: 18px;");
			
			// Odd indices are user-entered words (replacements)
			if (i % 2 == 1) {
				text.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #ff6b35;");
			}
			
			storyDisplay.getChildren().add(text);
		}
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
