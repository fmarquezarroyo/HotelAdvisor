package es.pfm.hoteladvisor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class IO {

	private static IO instance = null;
	private BufferedReader in;

	private IO() {
		in = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public static IO getIO() {
		if (instance == null)
			instance = new IO();
		return instance;
	}
	
	public void write(String msg) {
		System.out.println(msg);
	}

	
	public String read() throws IOException{
		return in.readLine();
	}
	
	public void close() throws IOException {
		if (in != null)
			in.close();
		in = null;
		instance = null;
	}
	
}
