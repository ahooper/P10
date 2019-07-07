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
