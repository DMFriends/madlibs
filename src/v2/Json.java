package src.v2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimal, dependency-free JSON parser -- just enough to read the
 * madlibs-api response ({@code {title, text[], blanks[]}}) without pulling in
 * a third-party library such as Gson.
 *
 * {@link #parse(String)} returns nested {@link Map}s (objects),
 * {@link List}s (arrays), {@link String}s, {@link Double}s, {@link Boolean}s
 * and {@code null}. It handles standard string escapes including {@code \\uXXXX}.
 */
final class Json {

	private final String s;
	private int i;

	private Json(String s) {
		this.s = s;
	}

	static Object parse(String text) {
		Json json = new Json(text);
		json.ws();
		Object value = json.value();
		json.ws();
		return value;
	}

	private Object value() {
		char c = s.charAt(i);
		switch (c) {
			case '{': return object();
			case '[': return array();
			case '"': return string();
			case 't': i += 4; return Boolean.TRUE;   // true
			case 'f': i += 5; return Boolean.FALSE;  // false
			case 'n': i += 4; return null;           // null
			default:  return number();
		}
	}

	private Map<String, Object> object() {
		Map<String, Object> map = new LinkedHashMap<>();
		i++; // consume '{'
		ws();
		if (s.charAt(i) == '}') {
			i++;
			return map;
		}
		while (true) {
			ws();
			String key = string();
			ws();
			i++; // consume ':'
			ws();
			map.put(key, value());
			ws();
			char c = s.charAt(i++);
			if (c == '}') {
				break;
			}
			// otherwise c == ',' -- continue with the next member
		}
		return map;
	}

	private List<Object> array() {
		List<Object> list = new ArrayList<>();
		i++; // consume '['
		ws();
		if (s.charAt(i) == ']') {
			i++;
			return list;
		}
		while (true) {
			ws();
			list.add(value());
			ws();
			char c = s.charAt(i++);
			if (c == ']') {
				break;
			}
			// otherwise c == ',' -- continue with the next element
		}
		return list;
	}

	private String string() {
		StringBuilder sb = new StringBuilder();
		i++; // consume opening quote
		while (true) {
			char c = s.charAt(i++);
			if (c == '"') {
				break;
			}
			if (c == '\\') {
				char e = s.charAt(i++);
				switch (e) {
					case '"':  sb.append('"');  break;
					case '\\': sb.append('\\'); break;
					case '/':  sb.append('/');  break;
					case 'b':  sb.append('\b'); break;
					case 'f':  sb.append('\f'); break;
					case 'n':  sb.append('\n'); break;
					case 'r':  sb.append('\r'); break;
					case 't':  sb.append('\t'); break;
					case 'u':
						sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
						i += 4;
						break;
					default:   sb.append(e);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private Number number() {
		int start = i;
		while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
			i++;
		}
		return Double.parseDouble(s.substring(start, i));
	}

	private void ws() {
		while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
			i++;
		}
	}
}
