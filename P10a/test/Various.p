/*
	P10 simple example
	*/
class Various
import std.lang.StdIO

n int	          	// define module field

f(n int)	       	// define f in module
    StdIO.print(n)		// access parameter; prints 10
    y int : 2	  	// create local variable
    //y int : 22	// duplicate definition
.

g()	        		// define g in module
    n : 3      		// set module field
.

frotz(args String[]) int 	
	StdIO.print("Hello")
	f(10)
	g()
	StdIO.print(n)		// prints 3 (g alters field value)
	StdIO.print(factorial(5))
.

factorial static(n int) int  	// factorial function
    if n <= 0 
    	return 1
   	.
    return n * factorial(n - 1)
.

content char[]
offset int
length int
serial static int : 5

// return position of character in content, or -1 if not present
index(c char) int
	s int : serial+1
    for x int : 0 while x < length next x : x+1
	    if content[offset+x] = c
	    	return x
    	.
   	.
    return -1
.
//copy static(model This) This 
//	x int : serial++
//	return new This()
//.

trySwitch(x char)
	switch x
	case \a
		StdIO.print('one')
	case \b, \c
		StdIO.print('two')
	.
	factorial("wrong type","excessive argument")
	factorial() // missing arguments
.
