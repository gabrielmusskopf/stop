package br.com.gabrielmusskopf.stop.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
	NAME(1),        //000000001
	OBJECT(2),     //000000010
	LOCATION(4),    //000000100
	COLOR(8),        //000001000
	ANIMAL(16),        //000010000
	WORD(32);        //000100000

	private final int code;
}
