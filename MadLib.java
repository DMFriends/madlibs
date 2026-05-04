import java.util.*;

public class MadLib
{
    // Private constants
	private static final char PLACEHOLDER_START_CHARACTER = '[';
	private static final char PLACEHOLDER_END_CHARACTER = ']';
	
	// File reader to get the template from the "madlib.txt" file
	private MadLibFileReader madLibFileReader = new MadLibFileReader();
	
    public static void main(String[] args)
    {
        // Gets the contents of the file "madlib.txt" as a String
        // This will be the template for our Mad Lib Story
        String template = madLibFileReader.getMadLibTemplate();
    
        String replaced = replaceAllPlaceholders(template);
        
        System.out.println(replaced);
    }
    
    private ArrayList<String> getPlaceholders(String template)
    {
        ArrayList<String> result = new ArrayList<String>();
        
        while(template.indexOf(PLACEHOLDER_START_CHARACTER) >= 0)
        {
            result.add(template.substring(template.indexOf(PLACEHOLDER_START_CHARACTER) + 1, template.indexOf(PLACEHOLDER_END_CHARACTER)));
            template = template.substring(template.indexOf(PLACEHOLDER_END_CHARACTER) + 1);
        }
        
        return result;
    }
    
    private ArrayList<String> getReplacements(ArrayList<String> placeholders)
    {
        Scanner input = new Scanner(System.in);
        
        ArrayList<String> result = new ArrayList<String>();
        
        String blank = "";
        
        for(int i = 0; i < placeholders.size(); i++)
        {
            if(placeholders.get(i).startsWith("A") || placeholders.get(i).startsWith("E") || placeholders.get(i).startsWith("I") || placeholders.get(i).startsWith("O") || placeholders.get(i).startsWith("U") || placeholders.get(i).startsWith("-"))
            {
                System.out.print("Enter an " + placeholders.get(i) + ": ");
            }
            else
            {
                System.out.print("Enter a " + placeholders.get(i) + ": ");
            }
            
            blank = input.nextLine();
            
            // validate input based on placeholder
            //if(blank.equals())
            
            result.add(blank);
            
            System.out.println();
        }
        
        return result;
    }
    
    private String replacePlaceholder(String template, String placeholder, String replacement)
    {
        String result = "";
        
        int index = template.indexOf(PLACEHOLDER_START_CHARACTER);
            
        result += template.substring(0, index);
        
        if(result.substring(result.length()-2, result.length()-1).equals(". "))
        {
            result += replacement.toUpperCase();
        }
        else
        {
            result += replacement;
        }
        
        return result;
    }
    
    private String replaceAllPlaceholders(String template)
    {
        String result = "";
        
        ArrayList<String> placeholders = getPlaceholders(template);
        
        ArrayList<String> replacements = getReplacements(placeholders);
        
        for(int i = 0; i < replacements.size(); i++)
        {
            result += replacePlaceholder(template, placeholders.get(i), replacements.get(i));
            template = template.substring(template.indexOf(PLACEHOLDER_END_CHARACTER) + 1);
        }
        
        result += template;
        
        return result;
    }
}
