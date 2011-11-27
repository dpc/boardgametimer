
all: buildd

buildd:
	ant debug

buildr:
	ant release

installd: buildd
	ant installd

installr: buildr
	ant installr

install: installd

.phony: all install build buildr buildd installr installd
