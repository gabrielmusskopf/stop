package br.com.gabrielmusskopf.stop.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

public class LetterGenerator {

	private static final List<Character> UNUSED_LETTERS = List.of('k', 'x', 'w', 'y', 'z');

	private final List<Character> generatedLetters = new ArrayList<>();

	public char generate() {
		// TODO: implement letter frequency
		char letter;
		do {
			letter = RandomStringUtils.secure().nextAlphabetic(1).toLowerCase().charAt(0);
		} while (UNUSED_LETTERS.contains(letter));
		return letter;
	}

}
