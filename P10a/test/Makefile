PROJECT	= $(HOME)/Software/EclipseWorkspace/P10a
LIB	= $(PROJECT)/lib
ANTLR	= $(HOME)/Software/ANTLR/antlr-4.6-complete.jar
P10C	= java -classpath $(PROJECT)/bin:$(ANTLR) ca.nevdull.p10a.compiler.Main -L $(LIB)/imp $(PFLAGS)
PFLAGS	= -highlightErrors #-t DefinitionPass
#LLC	= $(HOME)/Software/compilers/llvm-5.0.0.build/bin/llc
LLC	= /usr/local/Cellar/llvm/7.0.1/bin/llc
CFLAGS	= -I $(LIB) -g
ASFLAGS	= -g
P10	= p10
LIBAR	= $(LIB)/lib$(P10).a
#LDLIBS	= -L $(LIB) -l $(P10)
LDLIBS	= $(LIBAR)
LDFLAGS	= -g

# Cancel Pascal built-in implicit rule
%.o:	%.p
%.ll:	%.p $(LIBAR)
	$(P10C) $<
%.s:	%.ll
	$(LLC) $<
%.o:	%.s
	$(AS) $(ASFLAGS) -o $@ $<
# Retain .s intermediates for gdb
.PRECIOUS:	%.s %.ll
Hello1:	Hello1.o $(LIBAR)
	$(CC) $(LDFLAGS) -o $@ $@.o $(LDLIBS) -Wl,-alias,_test_Hello1_main_PString_Y,_which_main_PString_Y
Hello2:	Hello2.o $(LIBAR)
	$(CC) $(LDFLAGS) -o $@ $@.o $(LDLIBS) -Wl,-alias,_test_Hello2_main_PString_Y,_which_main_PString_Y
Simple:	Simple.o $(LIBAR)
	$(CC) $(LDFLAGS) -o $@ $@.o $(LDLIBS)
Various:	Various.o $(LIBAR)
	$(CC) $(LDFLAGS) -o $@ $@.o $(LDLIBS)
clean:	
	rm -f *.o *.s *.ll *.imp
$(LIBAR):	FORCE
	cd ../lib/src && $(MAKE)
FORCE:
