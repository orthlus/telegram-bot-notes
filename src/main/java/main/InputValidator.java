package main;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InputValidator {
	private final List<String> stopWords = List.of("drop", "delete", "select", "insert", "update", "alter");

	public String getStopWords() {
		return String.join(", ", stopWords);
	}

	public boolean isValid(String input) {
		String s = input.toLowerCase();
		for (String stopWord : stopWords) {
			if (s.contains(stopWord)) {
				return false;
			}
		}
		return true;
	}
}
