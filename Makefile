package :
	mvn clean
	mvn package

install : package
	mvn install

clean :
	mvn clean

tgz : clean
	cd ../.. ; tar -zcvf javamisc_trunk.tgz javamisc/trunk
	mv ../../javamisc_trunk.tgz .

.PHONY : package install clean

