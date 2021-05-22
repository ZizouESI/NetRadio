Compilation , execution et architecture de l'application réseau NET RADIO 

1. Compilation :
	Pour compiler le projet, un makefile est mis à disposition de l'utilisateur , pour cela il lui faut juste de taper la commande `make` pour compiler le projet (première entrée du makefile)
	
2. Execution :
	a. Lancement du gestionaire :
		- ./gest num-port ( où num-port un numéro de port d'écoute du gestionnaire , num-port=5858 , donc ./gest 5858)
	b. Lancement du client :
		- make cl
	c. Lancement du diffuseur :
		-make diff

3. Architecture :
	En ce qui concerne l'architecture de l'application , veuillez voir le dossier architecture
	le dossier architecture comporte :
	-Un diagrame de circulation de flux (données) TCP/UDP entre les différentes entités (Diffuseur , Client et gestionnaire )
	-Un diagramme de classes qui touche les deux entités (Diffuseur et Client) développées en JAVA
	
	le gestionnaire est développé avec le langage C  , il a une architecture d'un serveur multi-thread en C.
