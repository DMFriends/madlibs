package src.v2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class WordListsReader {
    // Class implementation goes here

    private static final String WORDLISTS = "wordlists.txt";
    private String wordlists = loadTemplate(WORDLISTS);
    private static ArrayList<ArrayList<String>> wordListArr = new ArrayList<ArrayList<String>>();
    private HashMap<String, Integer> getWordListIndexMap;

    public WordListsReader() {
        getWordListIndexMap = new HashMap<>();
        fillWordListArr();
        getWordListArr();
        fillWordListIndexMap();
        
    }

    private void fillWordListArr() {
        String[] wordLists = wordlists.split("\n");
        for (String wordList : wordLists) {
            String[] words = wordList.split(",");
            ArrayList<String> wordListArray = new ArrayList<String>();
            for (String word : words) {
                wordListArray.add(word.trim());
            }
            wordListArr.add(wordListArray);
        }
    }

    public static void getWordListArr() {
        for (ArrayList<String> row : wordListArr) {
            for (String item : row) {
                System.out.print(item + "\t"); // \t adds a tab space between columns
            }
            System.out.println(); // Moves to the next line after each row
        }
    }

    public String returnRandomWord(String subject) {
        
        Integer index = getWordListIndexMap.get(subject);
        if (index != null) {
            ArrayList<String> wordList = wordListArr.get(index);
            int randomIndex = (int) (Math.random() * wordList.size());
            return wordList.get(randomIndex);
        } else {
            return "Subject not found.";
        }
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

   private void fillWordListIndexMap() {
        for(int i = 0; i < wordListArr.size(); i+=2) {
            ArrayList<String> wordList = wordListArr.get(i);
            String word = wordList.get(0).replace("=", "").trim(); // Assuming the first word in each list is the key
            getWordListIndexMap.put(word, i+1); // Store the index of the corresponding word list

        }
   }  
}


