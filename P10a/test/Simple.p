/*	Class: simple
		exercise simple features
*/
class test.Simple
extends std.lang.Object
import std.lang.Throw

content char[]
length int
offset int

new()
	content : null
	length : 0
	offset : 0
.

/*	Method: index
	Parameters:
		c char - character to be searched
	Return: position of character in content, or -1 if not present
*/
index(c char) int
    for x int : 0 while x < length next x : x+1
	    if this.content[offset+x] = c
	    	return x
    	.
   	.
    return -1
.
index() int
	return index(\0)
.

/*	Method: various
		exercise static calls
*/
various()
	s int : sequence()
	n Simple : Simple.new()
	t int : n.sequence()
	if content = null
		Throw.nullPointerFault()
	.
	if s < 0 || s ≥ length
		Throw.arrayIndexFault(s)
	.
.

/*	Field: serial */
serial static int : 0

/*	Method: sequence
		exercise static local variable
*/
sequence static() int
	s int : serial
	serial : serial+1
	return s
.	
