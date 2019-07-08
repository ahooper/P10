/*	std_lang_Throw.c

	Copyright 2019 Andrew Hooper

	This file is part of the P10 Compiler.

	The P10 Compiler is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
#include "native/p10a.h"
#include "std/lang/Throw.h"

#include <stdint.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#define UNW_LOCAL_ONLY
#include <libunwind.h>

void std_lang_Throw_backtrace();

//TEMPORARY

_Noreturn void std_lang_Throw_arrayIndexFault_Pint(int32_t index) {
	fprintf(stderr,"Array index fault value=%d\n",index);
	std_lang_Throw_backtrace();
	exit(EXIT_FAILURE);
}
_Noreturn void std_lang_Throw_nullPointerFault() {
	fprintf(stderr,"Null pointer fault\n");
	std_lang_Throw_backtrace();
	exit(EXIT_FAILURE);
}
_Noreturn void std_lang_Throw_switchIndexFault_Pint(int32_t index) {
	fprintf(stderr,"Switch index fault value=%d\n",index);
	std_lang_Throw_backtrace();
	exit(EXIT_FAILURE);
}
_Noreturn void std_lang_Throw_stringIndexOutOfBoundsFault_Pint(int32_t index) {
	fprintf(stderr,"String index out of bounds fault value=%d\n",index);
	std_lang_Throw_backtrace();
	exit(EXIT_FAILURE);
}

// https://eli.thegreenplace.net/2015/programmatic-access-to-the-call-stack-in-c/

// Call this function to get a backtrace.
void std_lang_Throw_backtrace() {
	unw_cursor_t cursor;
	unw_context_t context;

	// Initialize cursor to current frame for local unwinding.
	unw_getcontext(&context);
	unw_init_local(&cursor, &context);

	// Unwind frames one by one, going up the frame stack.
	while (unw_step(&cursor) > 0) {
		unw_word_t offset, pc;
		unw_get_reg(&cursor, UNW_REG_IP, &pc);
		if (pc == 0) {
			break;
		}
		printf("0x%"PRIxPTR":", pc);

		char sym[256];
		if (unw_get_proc_name(&cursor, sym, sizeof(sym), &offset) == 0) {
			//TODO decode method name
			printf(" (%s+0x%"PRIxPTR")\n", sym, offset);
		} else {
			printf(" -- error: unable to obtain symbol name for this frame\n");
		}
	}
}
