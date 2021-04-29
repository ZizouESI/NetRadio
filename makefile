compil:
	javac Diffuseur.java
	javac Client.java
	gcc -Wall -pthread gestionnaire.c -o gest

diff:
	java Diffuseur diff-config.txt
cl:
	java Client cl_config.txt

gest:
	./gest 5858

clear:
	rm *.class
	rm gest