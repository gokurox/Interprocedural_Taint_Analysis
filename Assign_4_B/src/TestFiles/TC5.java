package TestFiles;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class TC5 {
	public static void func () throws IOException {
		Scanner in = new Scanner (System.in);
		int A = in.nextInt();
		in.close();
		
		BufferedReader br = new BufferedReader(new FileReader("abc.txt"));
		String B = br.readLine();
		br.close();
		
		System.out.println (A + " " + B);
	}
	
	public static void main(String[] args) throws IOException {
		func();
	}
}
