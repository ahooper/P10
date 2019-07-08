/*	main.c

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
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <string.h>
#include <execinfo.h>
#define _XOPEN_SOURCE
#include <ucontext.h>
#include <unistd.h>

//TODO parse options, convert arguments to String objects

// NB: must alias desired main method with linker -alias _test_Hello_main_PString_Y _which_main_PString_Y
// TODO a way to dynamically select this (maybe dynamic linker calls)
extern int which_main_PString_Y(void *);
void critical_error(int sig_num, siginfo_t * info, void * ucontext);

int main(int argc, char *argv[], char *env[]) {
	// Handle fatal signals to produce a call trace
#if defined(_XOPEN_UNIX)
	// alternate signal handling stack in case of stack overflow SIGSEGV
	stack_t sigstk = {
		.ss_size = SIGSTKSZ,
		.ss_flags = 0
	};
	if ((sigstk.ss_sp = valloc(sigstk.ss_size)) == NULL) {
		fprintf(stderr, "Unable to allocate alternate signal handling stack (%d)\n",
					SIGSTKSZ);
		// carry on without protection
	} else if (sigaltstack(&sigstk,(stack_t *)0) < 0) {
		perror("Unable to set alternate signal handling stack: ");
		// carry on without protection
	}
	
    struct sigaction sigact = {
		.sa_sigaction = critical_error,
	    .sa_flags = SA_RESTART | SA_SIGINFO
    };
    if (sigaction(SIGSEGV, &sigact, (struct sigaction *)NULL) != 0) {
		fprintf(stderr, "Error setting signal handler for %d (%s)\n",
					SIGSEGV, strsignal(SIGSEGV));
		exit(EXIT_FAILURE);
	}
    if (sigaction(SIGBUS, &sigact, (struct sigaction *)NULL) != 0) {
		fprintf(stderr, "Error setting signal handler for %d (%s)\n",
					SIGBUS, strsignal(SIGBUS));
		exit(EXIT_FAILURE);
	}
    if (sigaction(SIGFPE, &sigact, (struct sigaction *)NULL) != 0) {
		fprintf(stderr, "Error setting signal handler for %d (%s)\n",
					SIGFPE, strsignal(SIGFPE));
		exit(EXIT_FAILURE);
	}
#endif
	int ret = which_main_PString_Y((void*)0);
#if defined(_XOPEN_UNIX)
	if (sigstk.ss_sp != NULL) {
		free(sigstk.ss_sp);
	}
#endif
	return ret;
}

#if defined(_XOPEN_UNIX)
//https://stackoverflow.com/a/1925461

#define BACKTRACE_LIMIT	50
void critical_error(int sig_num, siginfo_t* info, void* ucontext) {
	void* array[BACKTRACE_LIMIT];
	int size;
	void* caller_address;

	 /* Get the address at the time the signal was raised */
#if defined(__APPLE__) && defined(__MACH__) && defined(__LP64__)
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wint-conversion"
	caller_address = ((ucontext_t *)ucontext)->uc_mcontext->__ss.__rip;
  #pragma clang diagnostic pop
#elif defined(__i386__) // gcc specific
	 caller_address = ((ucontext_t*)ucontext)->uc_mcontext.eip; // EIP: x86 specific
#elif defined(__x86_64__) // gcc specific
	 caller_address = ((ucontext_t*)ucontext)->uc_mcontext.rip; // RIP: x86_64 specific
#else
#error Unsupported architecture for signal caller address. // TODO: Add support for other arch.
#endif
	fprintf(stderr, "Signal %d (%s), address is %p from %p\n", 
				sig_num, strsignal(sig_num), info->si_addr, 
				(void *)caller_address);
	size = backtrace(array, BACKTRACE_LIMIT);
	array[1] = caller_address; // overwrite sigaction with caller's address
	backtrace_symbols_fd(array+1, size-1, STDERR_FILENO); // omit self address
	exit(EXIT_FAILURE);
}
#endif
