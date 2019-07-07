/*
	P10 hello example
	*/
class test.Hello2
import std.io.Writer
import std.io.FileWriter
import std.io.BufferedWriter
import std.text.Formatter

serial static int : 1

id int : serial
new()
.

main static(args String[]) int
	out FileWriter : FileWriter(1) // STDOUT_FILENO
	s String : "Hello\n"
	out.write(s.value,0,s.byteSize()) // byte array

	bo BufferedWriter : BufferedWriter(out as Writer)
	bo.write("ab\tc\x64e😀∆\u007a\n") // Unicode characters
	bo.write(`X) // single character
	bo.flush()

	// formatting to System.out (should be same writer as out)
	fmt Formatter : Formatter(System.out)
	breakpoint()
	fmt.c(`F)
	fmt.c(`
)
	fmt.s("text").s("right",10).s("left",-10).s("longer",3).s("longer",-3)
	fmt.c(`D)
	fmt.d(12345+123).c(`|).d(1,5).c(`|).d(1,-5).c(`|).d(123456,5).c(`|)
	a Hello2 : Hello2()
	fmt.c(`I).d(a.id).c(`X).x(0x123abc).c(`
)
	// TODO d(int) and x(int) are erroneously matching to d(long) and x(long)
	fmt.d(12345L+123).c(`|).d(1L,5).c(`|).d(1L,-5).c(`|).d(123456L,5).c(`|).c(`
)
	fmt.x(0x12345+0x123).c(`|).x(1,5).c(`|).x(1,-5).c(`|).x(0x123456,5).c(`|).c(`
)
	fmt.x(0x12345L+0x123).c(`|).x(1L,5).c(`|).x(1L,-5).c(`|).x(0x123456L,5).c(`|).c(`
)
	fmt.x(-1).c(` ).x(-1L).c(` ).x(0xF000000<<4).c(` ).x(0xF000000L<<4).c(`
);	fmt.x(-(0xF000000<<4)).c(` ).x(0x123456789ABCDEF0L).c(` ).d(0- 9223372036854775807L - 1).s("\n")
	System.out.flush()
	// small exercise of buffering
	for x int : 0 while x < 50 next x+:1
		for y int : 1 while y ≤ 8 next y+:1
			bo.write('....+....')
			bo.write((y+`0) as char)
		.
		bo.write(`
)
	.
	// class name
	bo.write(fmt.getClass().getName()); bo.write('\n') 	
	bo.close()
.
breakpoint static()
.
