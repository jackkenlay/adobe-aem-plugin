#!/bin/bash

echo "Installing Plugin"

#TODO
#installation dir as var

#local install thing as var, basically its a copy and delete.
#find the local installation of intelliJ

if [ -d "~/Library/Application\ Support/IdeaIC2018.1/" ]; then
    	echo "Plugin Directory Found"
	echo "Copying File"
	cp ./build/libs/adobe-aem-plugin-1.0-SNAPSHOT.jar ./adobe-aem-plugin.jar
	cp ./adobe-aem-plugin.jar "~/Library/Application Support/IdeaIC2018.1/"
	echo "File copied, please restart IntelliJ"
fi
