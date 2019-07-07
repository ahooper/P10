package ca.nevdull.p10a.experiments;

public class testUnicode {

	public static void main(String[] args) {
		String u = " \uD7FF \uDC00 \uE123 😀 ∆";
		for (char c : u.toCharArray()) System.out.format(" %04x", (int)c);
		System.out.println();
		for (byte b : u.getBytes(java.nio.charset.StandardCharsets.UTF_8)) System.out.format(" %02x", b);
		System.out.println();
	}

}
