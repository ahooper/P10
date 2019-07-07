PROJECT	= $(HOME)/Software/EclipseWorkspace/P10a

SRCDIR	= .
OBJDIR	= ../obj
IMPDIR	= ../imp
LIBDIR	= ..
PSRCEXT	= p
CSRCEXT	= c
CINCEXT	= h
LLSRCEXT	= ll
ASRCEXT	= s
OBJEXT	= o

ANTLR	= $(HOME)/Software/ANTLR/antlr-4.6-complete.jar
P10C	= java -classpath $(PROJECT)/bin:$(ANTLR) $(JFLAGS) ca.nevdull.p10a.compiler.Main $(PFLAGS)
#LLC	= $(HOME)/Software/compilers/llvm-5.0.0.build/bin/llc
LLC	= /usr/local/Cellar/llvm/7.0.1/bin/llc
PFLAGS	= -highlightErrors
CFLAGS	= -Wall -I $(IMPDIR) -I $(SRCDIR) -g
JFLAGS	= -enableassertions
AFLAGS	= -g

PSOURCES:=
PSOURCES+= $(SRCDIR)/std/lang/Object.$(PSRCEXT)
# need std.io.FileWriter before std.lang.System until topScope is shared across subcompiles
PSOURCES+= $(SRCDIR)/std/io/FileWriter.$(PSRCEXT)
PSOURCES+= $(shell find $(SRCDIR)/std/lang -type f -name *.$(PSRCEXT))
#PSOURCES+= $(shell find $(SRCDIR)/std/io -type f -name *.$(PSRCEXT))
#PSOURCES+= $(shell find $(SRCDIR)/std/text -type f -name *.$(PSRCEXT))
PSOURCES+= $(shell find $(SRCDIR)/std -type f -name *.$(PSRCEXT))
CSOURCES:= $(shell find $(SRCDIR)/native -type f -name *.$(CSRCEXT))
POBJECTS:= $(patsubst $(SRCDIR)/%,$(OBJDIR)/%,$(PSOURCES:.$(PSRCEXT)=.$(OBJEXT)))
COBJECTS:= $(patsubst $(SRCDIR)/%,$(OBJDIR)/%,$(CSOURCES:.$(CSRCEXT)=.$(OBJEXT)))
LIBOBJ	:= $(POBJECTS) $(COBJECTS)

#TODO name native objects distinct from P10 objects

P10		= p10
LDLIBS	= -L$(LIBDIR) -l$(P10)
LIBAR	= $(LIBDIR)/lib$(P10).a

lib:		$(LIBAR)
	
print:
	@echo $(CSOURCES)

# Diagnostic shell wrapper http://www.drdobbs.com/tools/debugging-makefiles/197003338?pgno=3
#OLD_SHELL := $(SHELL)
#SHELL = $(warning [$@ ($^) ($?)])$(OLD_SHELL)

.PHONY:	lib native clean

$(OBJDIR)/%.$(OBJEXT):	$(SRCDIR)/%.$(CSRCEXT) $(SRCDIR)/%.$(CINCEXT)
	-mkdir -p $(@D)
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $? -o $@
	ln -f $@ $(patsubst $(subst /,-,$(OBJDIR))-%,$(OBJDIR)/%,$(subst /,-,$@))

$(OBJDIR)/%.$(OBJEXT):	$(SRCDIR)/%.$(PSRCEXT)
	-mkdir -p $(@D)
	$(P10C) -L $(IMPDIR) -d $(IMPDIR) $<
	$(LLC) $(IMPDIR)/$(<:.$(PSRCEXT)=.$(LLSRCEXT))
	@#TODO don't keep .ll and .s intermediates
	$(AS) $(ASFLAGS) -o $@ $(IMPDIR)/$(<:.$(PSRCEXT)=.$(ASRCEXT))
	ln -f $@ $(patsubst $(subst /,-,$(OBJDIR))-%,$(OBJDIR)/%,$(subst /,-,$@))

$(OBJDIR)/native/%.$(OBJEXT):	$(SRCDIR)/native/p10a.h
$(OBJDIR)/native/%.$(OBJEXT):	$(SRCDIR)/native/%.$(CSRCEXT)
	-mkdir -p $(@D)
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $? -o $@
	ln -f $@ $(patsubst $(subst /,-,$(OBJDIR))-%,$(OBJDIR)/%,$(subst /,-,$@))

$(LIBAR):	$(LIBOBJ)
	$(AR) -r $@ $(patsubst $(subst /,-,$(OBJDIR))-%,$(OBJDIR)/%,$(subst /,-,$?))
$(LIBAR).ranlib:	$(LIBAR)
	ranlib $?
	touch $@
	
native:	$(COBJECTS)

clean:	
	rm -f $(LIBOBJ)
	rm -f $(patsubst $(OBJDIR)/%,$(IMPDIR)/%,$(POBJECTS:.$(OBJEXT)=.$(ASRCEXT)))
	rm -f $(patsubst $(OBJDIR)/%,$(IMPDIR)/%,$(POBJECTS:.$(OBJEXT)=.$(LLSRCEXT)))
	rm -f $(patsubst $(OBJDIR)/%,$(IMPDIR)/%,$(POBJECTS:.$(OBJEXT)=.h))
	rm -f $(patsubst $(OBJDIR)/%,$(IMPDIR)/%,$(POBJECTS:.$(OBJEXT)=.imp))
	rm -f $(patsubst $(OBJDIR)/%,$(IMPDIR)/%,$(POBJECTS:.$(OBJEXT)=.tmp))
