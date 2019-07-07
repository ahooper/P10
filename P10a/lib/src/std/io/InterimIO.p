class std.io.InterimIO

// Simple interim output interface, will be replaced with std.io

putByte static(b int) int native

putChar static(c int) int
	r int
	if c < 0x80
		return putByte(c)
	elsif c < 0x800
		r : putByte((c >> 6)           | 0xC0)
		if r ≠ 0
			return r
		.
		return putByte((c      & 0x3F) | 0x80)
	elsif c < 0x10000
		r : putByte( (c >> 12)         | 0xE0)
		if r ≠ 0
			return r
		.
		r : putByte(((c >> 6)  & 0x3F) | 0x80)
		if r ≠ 0
			return r
		.
		return putByte((c      & 0x3F) | 0x80)
	elsif c < 0x110000
		r : putByte((c >> 18)          | 0xF0)
		if r ≠ 0
			return r
		.
		r : putByte(((c >> 12) & 0x3F) | 0x80)
		if r ≠ 0
			return r
		.
		r : putByte(((c >> 6)  & 0x3F) | 0x80)
		if r ≠ 0
			return r
		.
		return putByte((c      & 0x3F) | 0x80)
	else
		//TODO throw invalid argument
	.
.

print static(s String)
	bytes int : s.byteSize()
	for x int : 0 while x < bytes next x : x + 1
		putByte(s.byteAt(x))
	.
.

print static(bytes byte[])
	length int : bytes.length
	for x int : 0 while x < length next x : x + 1
		putByte(bytes[x])
	.
.

print static(n int)
	if n < 0
		if n = -2147483648	// edge case for INTMIN, which would overflow if negated below
			print('-2147483648')
			return
		.
		putChar(`-)
		n : - n
	.
	if n < 10  // common cases
		putChar(n % 10 + `0)
	elsif n < 100
		putChar(n / 10 + `0)
		putChar(n % 10 + `0)
	else
		d int : 1000000000
		while d > n
			d : d / 10
		.
		while d > 0
			putChar(n / d + `0)
			n : n % d
			d : d / 10
		.
	.
.
