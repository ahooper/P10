/*	std_lang_System.c

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
#include "std/lang/System.h"
void std_lang_System__class_initialize();

#include <unistd.h>

int std_lang_System_standardInputRef() {
	std_lang_System__class_initialize();
	return STDIN_FILENO;
}
int std_lang_System_standardOutputRef() {
	std_lang_System__class_initialize();
	return STDOUT_FILENO;
}
int std_lang_System_standardErrorRef() {
	std_lang_System__class_initialize();
	return STDERR_FILENO;
}
