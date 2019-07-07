class test.ShortCircuitBoolean

andthen(a boolean, b boolean) boolean
    r boolean : a && b
    return r
.
orelse(a boolean, b boolean) boolean
    r boolean : a || b
    return r
.
loop1(value byte[], index int, b1 byte, b2 byte) int
	limit int : 5
	while index ≤ limit
		if value[index] = b1 && value[index+1] = b2
			return index
		.
		index : index + 1
	.
.
loop2(value byte, index int, b1 byte, b2 byte) int
	limit int : 5
	while index ≤ limit
		if value = b1 && value = b2
			return index
		.
		index : index + 1
	.
.
