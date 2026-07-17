package src.v2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

// This is the MadLibFileReader class
// It reads in a madlib text file and converts it into a Java String
// Feel free to explore this code, it uses the Java Files class
// to read in a Mad Lib template from the "madlib.txt" file.
// DO NOT CHANGE THIS CODE, the program may not work if you do.
public class MadLibFileReader 
{
	private static final String FILENAME = "madlibs.txt";
	
	private String template;
	private ArrayList<String> storyTitles;
	private ArrayList<String> stories;
	private int currentStoryIndex;
	
	public MadLibFileReader()
	{
		template = loadTemplate(FILENAME);
		parseStories();
		generateTitles();
		selectRandomStory();
	}
	
	public String getMadLibTemplate()
	{
		return stories.get(currentStoryIndex);
	}
	
	public String getCurrentStoryTitle()
	{
		return storyTitles.get(currentStoryIndex);
	}
	
	public int getCurrentStoryIndex()
	{
		return currentStoryIndex;
	}
	
	public ArrayList<String> getStoryTitles()
	{
		return storyTitles;
	}
	
	public ArrayList<String> getStories()
	{
		return stories;
	}
	
	public String getStoryTitle(int index)
	{
		if (index >= 0 && index < storyTitles.size()) {
			return storyTitles.get(index);
		}
		return "";
	}
	
	public String getStory(int index)
	{
		if (index >= 0 && index < stories.size()) {
			return stories.get(index);
		}
		return "";
	}
	
	private void selectRandomStory()
	{
		Random random = new Random();
		currentStoryIndex = random.nextInt(stories.size());
	}
	
	private void parseStories()
	{
		stories = new ArrayList<>();
		// Split by double newlines to separate stories
		String[] storyArray = template.split("\n\n+");
		for (String story : storyArray) {
			if (!story.trim().isEmpty()) {
				stories.add(story.trim());
			}
		}
	}
	
	private void generateTitles()
	{
		storyTitles = new ArrayList<>();
		
		// Generate titles based on story content
		storyTitles.add("Programming Basics");
		storyTitles.add("The Hackathon");
		storyTitles.add("Space Mission");
		storyTitles.add("Academy Adventures");
		storyTitles.add("Museum Mystery");
		storyTitles.add("Underwater Dining");
		storyTitles.add("The Grand Parade");
		storyTitles.add("Secret Recipe");
		storyTitles.add("Game Night");
	}
	
	private String loadTemplate(String filename)
	{
		String result = "";
		
		// Try to run the following code, but there may be errors
	    // when reading the file.
		try {
			// Read every byte of data in the file, and convert all bytes
			// to a standard Unicode String
			byte[] encoded = Files.readAllBytes(Paths.get(filename));
			result = new String(encoded, StandardCharsets.UTF_8);
		} 
		// If there was an error reading the file, this catch clause will execute.
		catch (IOException e) {
			System.out.println("There was an error while reading the file: " + filename);
			e.printStackTrace();
		}
		
		// Return the result
		return result;
	}
}