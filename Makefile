package :
	mvn clean
	mvn package

install : package
	mvn install

clean :
	mvn clean

.PHONY : package install clean

