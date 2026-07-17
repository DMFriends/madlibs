package src.v2;

import java.util.ArrayList;
import java.util.List;

/**
 * A Mad Lib story in a source-independent form: the fixed {@link #segments}
 * of text, interleaved with the {@link #blanks} the user fills in.
 *
 * The invariant is always {@code segments.size() == blanks.size() + 1}, so the
 * finished story is segments[0] + answer[0] + segments[1] + answer[1] + ...
 *
 * Both the API ({@link MadLibApiReader}) and the local file
 * ({@link MadLibFileReader}) produce stories in this shape.
 */
public class Story {

	private final String title;
	private final List<String> segments;
	private final List<String> blanks;

	public Story(String title, List<String> segments, List<String> blanks) {
		this.title = title;
		this.segments = segments;
		this.blanks = blanks;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getSegments() {
		return segments;
	}

	public List<String> getBlanks() {
		return blanks;
	}

	/**
	 * Builds a Story from the legacy "[PLACEHOLDER]" template format used by
	 * {@link MadLibFileReader}, so the offline fallback produces the same shape
	 * as the API response.
	 */
	public static Story fromTemplate(String title, String template) {
		List<String> segments = new ArrayList<>();
		List<String> blanks = new ArrayList<>();
		StringBuilder segment = new StringBuilder();

		for (int i = 0; i < template.length(); i++) {
			char c = template.charAt(i);
			if (c == '[') {
				segments.add(segment.toString());
				segment.setLength(0);
				int end = template.indexOf(']', i);
				blanks.add(template.substring(i + 1, end));
				i = end;
			} else {
				segment.append(c);
			}
		}
		segments.add(segment.toString());

		return new Story(title, segments, blanks);
	}
}
