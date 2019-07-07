#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

int32_t std_io_InterimIO_putByte_Pint(int32_t b) {
	return putc(b,stdout);
}
