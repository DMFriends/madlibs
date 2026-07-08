package src.v2;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fetches Mad Lib stories from the public madlibs-api
 * (https://github.com/chroline/madlibs-api).
 *
 * The API responds with JSON of the shape:
 * <pre>
 *   { "title": String, "text": [String...], "blanks": [String...] }
 * </pre>
 * where {@code text.length == blanks.length + 1}. That maps directly onto
 * {@link Story} (text -> segments, blanks -> blanks).
 */
public class MadLibApiReader {

	private static final String BASE_URL = "https://madlibs-api.vercel.app/api";
	private static final Duration TIMEOUT = Duration.ofSeconds(10);

	private final HttpClient client = HttpClient.newBuilder()
			.connectTimeout(TIMEOUT)
			.build();

	/** Returns a random story from the API. */
	public Story getRandom() throws Exception {
		return toStory(get(BASE_URL + "/random"));
	}

	/** Returns a specific story by its title. */
	public Story getByTitle(String title) throws Exception {
		// Encode as a path segment: URLEncoder uses '+' for spaces (form
		// encoding), but a URL path needs '%20'.
		String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8).replace("+", "%20");
		return toStory(get(BASE_URL + "/story/" + encoded));
	}

	private String get(String url) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.timeout(TIMEOUT)
				.header("Accept", "application/json")
				.GET()
				.build();

		HttpResponse<String> response =
				client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("madlibs-api returned HTTP " + response.statusCode());
		}
		return response.body();
	}

	@SuppressWarnings("unchecked")
	private Story toStory(String json) {
		Map<String, Object> root = (Map<String, Object>) Json.parse(json);
		String title = String.valueOf(root.get("title"));
		List<String> segments = toStringList((List<Object>) root.get("text"));
		List<String> blanks = toStringList((List<Object>) root.get("blanks"));
		return new Story(title, segments, blanks);
	}

	private List<String> toStringList(List<Object> raw) {
		List<String> out = new ArrayList<>(raw.size());
		for (Object o : raw) {
			out.add(String.valueOf(o));
		}
		return out;
	}
}
