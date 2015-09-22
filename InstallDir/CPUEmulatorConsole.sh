#!/bin/sh
cp=`dirname $0`
java -classpath "${CLASSPATH}:$cp/bin/classes:$cp/bin/lib/Hack.jar:$cp/bin/lib/HackGUI.jar:$cp/bin/lib/Simulators.jar:$cp/bin/lib/SimulatorsGUI.jar:$cp/bin/lib/Compilers.jar" CPUEmulatorConsole $1
