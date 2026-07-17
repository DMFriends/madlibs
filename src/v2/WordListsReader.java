package src.v2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordListsReader {

    private static final String WORDLISTS = "wordlists.txt";

    // Maps blank names that don't match a wordlists.txt header verbatim
    // (e.g. phrasings used by the madlibs-api) to the header they belong to.
    private static final Map<String, String> SUBJECT_ALIASES = new HashMap<>();
    static {
        SUBJECT_ALIASES.put("verb ending in -ing", "-ing verb");
        SUBJECT_ALIASES.put("verb ending in 'ing'", "-ing verb");
        SUBJECT_ALIASES.put("verb ending in ing", "-ing verb");
        SUBJECT_ALIASES.put("another body part", "body part");
        SUBJECT_ALIASES.put("part of body", "body part");
        SUBJECT_ALIASES.put("part of the body", "body part");
        SUBJECT_ALIASES.put("nouns", "plural noun");
        SUBJECT_ALIASES.put("animals", "animal");
        SUBJECT_ALIASES.put("type of job", "occupation");
    }

    // Overrides the label shown to the user for specific blank names, when
    // the raw blank text (e.g. "nouns") wouldn't read well on its own.
    private static final Map<String, String> DISPLAY_LABEL_OVERRIDES = new HashMap<>();
    static {
        DISPLAY_LABEL_OVERRIDES.put("nouns", "Plural noun");
    }

    private final Map<String, List<String>> wordListsByCategory = new HashMap<>();

    public WordListsReader() {
        parseWordLists(loadTemplate(WORDLISTS));
    }

    private void parseWordLists(String contents) {
        String currentCategory = null;
        for (String line : contents.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("===") && line.endsWith("===")) {
                currentCategory = line.replace("=", "").trim().toLowerCase();
                wordListsByCategory.putIfAbsent(currentCategory, new ArrayList<>());
                continue;
            }
            if (currentCategory == null) {
                continue;
            }
            for (String word : line.split(",")) {
                String trimmed = word.trim();
                if (!trimmed.isEmpty()) {
                    wordListsByCategory.get(currentCategory).add(trimmed);
                }
            }
        }
    }

    public String returnRandomWord(String subject) {
        List<String> wordList = resolveWordList(normalize(subject));
        if (wordList == null || wordList.isEmpty()) {
            return "Subject not found.";
        }
        int randomIndex = (int) (Math.random() * wordList.size());
        return wordList.get(randomIndex);
    }

    // Some madlibs-api blanks combine two category names, e.g. "noun; place"
    // or "plural noun; type of job" - the segment after the last ";" is the
    // more specific category, so that's the one we resolve against.
    private List<String> resolveWordList(String key) {
        List<String> wordList = wordListsByCategory.get(key);
        if (wordList != null) {
            return wordList;
        }
        String alias = SUBJECT_ALIASES.get(key);
        if (alias != null) {
            wordList = wordListsByCategory.get(alias);
            if (wordList != null) {
                return wordList;
            }
        }
        int lastSemicolon = key.lastIndexOf(';');
        if (lastSemicolon >= 0) {
            String lastSegment = key.substring(lastSemicolon + 1).trim();
            if (!lastSegment.isEmpty()) {
                return resolveWordList(lastSegment);
            }
        }
        return null;
    }

    // Returns the label override for this blank, or null if the raw blank
    // text should be formatted and displayed as-is.
    public String getDisplayLabelOverride(String subject) {
        return DISPLAY_LABEL_OVERRIDES.get(normalize(subject));
    }

    private String normalize(String subject) {
        return subject == null ? "" : subject.trim().toLowerCase();
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
