package com.homework.utils;

public class RandomUtils {

	public static int random(int min, int max) {
		return (int) (Math.random() * (max - min + 1)) + min;
	}
}
