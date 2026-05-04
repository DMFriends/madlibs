import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

// This is the MadLibFileReader class
// It reads in a madlib text file and converts it into a Java String
// Feel free to explore this code, it uses the Java Files class
// to read in a Mad Lib template from the "madlib.txt" file.
// DO NOT CHANGE THIS CODE, the program may not work if you do.
public class MadLibFileReader 
{
	private static final String FILENAME = "madlib.txt";
	
	private String template;
	
	public MadLibFileReader()
	{
		template = loadTemplate(FILENAME);
	}
	
	public String getMadLibTemplate()
	{
		return template;
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