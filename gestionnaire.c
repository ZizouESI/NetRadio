//Importation des bibliothèques 
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <sys/socket.h>
#include <fcntl.h> 
#include <string.h>

///Définition des constantes et variables globales

#define MAX_DIFF 10 
#define MAX_CLIENTS 10
#define MAX_THREAD 10

//Nombre de clients connectés 
int nbClients=0;


//Structure pour sauvgarder les diffuseurs enregistrés
struct Diffuseur{
    char id[9]; //id du diffuseur 
    char ip1[16]; //Adresse IPv4 
    char port1[5]; //port de multi-diffusion
    char ip2[16]; //Adresse IPv4 de la machine où il se trouve le diffuseur
    char port2[5]; //port de reception des messages des utilisateurs

} ;



struct Diffuseur* diffuseurs[MAX_DIFF];
int indice=0;

//Définition d'une variable mutex pour l'exclusion mutuelle (Synchronisation)
pthread_mutex_t  mutex = PTHREAD_MUTEX_INITIALIZER;

//Prototypes des fonctions utilitaires
void initDiff(struct Diffuseur *diff);
int check_message();
void registerDiff(char * message , struct Diffuseur *diff);
int startsWith(const char *pre, const char *str);
int notExist(char *id);
void num_diff(char* i);
//Définition de la routine à appeler au moment de l'acceptation de la connexion des clients
void * routine(void *arg){
    //Déclarations et initailisations
    struct Diffuseur *diff=malloc(sizeof(struct Diffuseur ));
    //initDiff(diff);
    int newSocket = *((int *)arg);
    char message[58]="\0";

  	
  	int NbRead = recv(newSocket , message , 58 , 0);
    //printf("message : %s",message);
    if(NbRead < 0 || ( check_message(message) == 0 ) ){
        printf("Erreur à la recéption de message \n");
        close(newSocket);
        return(0);
    }
    if(strcmp(message,"LIST\r\n") == 0){
        //envoie de la liste de diffuseurs au client actuellement connecté
        //envoie LINB num-diff\r\n
        
        char linb[100]="LINB ";
        char indice_str[4]="";
        num_diff(indice_str);
        
        strcat(linb,indice_str);

        strcat(linb,"\r\n");
        if(send(newSocket,linb,strlen(linb),0) < 0){
            printf("Erreur à l'envoi de LINB message \n");
            close(newSocket);
            return(0);
        }
        for(int k=0;k<indice;k++){
            char item[200]="ITEM ";
            strcat(item,diffuseurs[k]->id);
            strcat(item," ");
            strcat(item,diffuseurs[k]->ip1);
            strcat(item," ");
            strcat(item,diffuseurs[k]->port1);
            strcat(item," ");
            strcat(item,diffuseurs[k]->ip2);
            strcat(item," ");
            strcat(item,diffuseurs[k]->port2);
            strcat(item,"\r\n");
            if(send(newSocket,item,strlen(item),0) < 0){
                printf("Erreur à l'envoi de ITEM message \n");
                close(newSocket);
                return(0);
            }

        }
    }else if(startsWith("REGI",message)){
        if(indice < MAX_DIFF){
            //préparation de la structure diff pour l'ajouter à la liste des diffuseurs
            registerDiff(message,diff);
            char id[9]="";
            strcpy(id,diff->id);
            if(notExist(id)){
                pthread_mutex_lock(&mutex);
                    //ajout au tableau 
                    diffuseurs[indice]=diff;     //section critique
                    indice ++;
                pthread_mutex_unlock(&mutex);
                
                
                //REOK
                //printf("Enregistrement du diffuseur avec succès\n");
                if(send(newSocket,"REOK\r\n",strlen("REOK\r\n"),0) < 0){
                    printf("Erreur à l'envoi de REOK message \n");
                    close(newSocket);
                    return(0);
                }
                printf("DIFF = %s %s %s %s %s\n",diffuseurs[indice-1]->id,diffuseurs[indice-1]->ip1,diffuseurs[indice-1]->port1,diffuseurs[indice-1]->ip2,diffuseurs[indice-1]->port2);
            }else{
                printf("Le diffusuer est enregistré déjà\n");
                if(send(newSocket,"RENO\r\n",strlen("RENO\r\n"),0) < 0){
                    printf("Erreur à l'envoi de RENO message \n");
                    close(newSocket);
                    return(0);
                }
            }
        }else{
            printf("Le nombre maximum de diffuseurs est atteint!\n");
            if(send(newSocket,"RENO\r\n",strlen("RENO\r\n"),0) < 0){
                printf("Erreur à l'envoi de RENO message \n");
                close(newSocket);
                return(0);
            }
        }
    }
    
    
   
    close(newSocket);
  	pthread_exit(NULL);
    
}

//la routine responsable de test de l'existence des diffuseurs (diffuseurs actifs)
void * testDiffActif(){
    printf("création de ce thread");
    pthread_exit(NULL);
}

//le programme principal du serveur
int main(int argc, char const *argv[])
{
    //vérification des entrées
    if (argc != 2){
        printf("Erreur il faut fournir un numero de port\n");
        return 0;
    }

    //Récupération du numéro de port (conversion de types)
    int p = atoi(argv[1]);

    //déclaration des variables et structures 
	int socketServeur, nouvelleSocket;
	struct sockaddr_in serverAddr;
	struct sockaddr_storage serverStorage;
	socklen_t taille_adr;
    
    
    //initialisations
    
    

	//Création de la socket 
	socketServeur = socket(PF_INET, SOCK_STREAM, 0);

	//configuration des paramètres du serveur : serverAddr
	//adress family = Internet
	serverAddr.sin_family = AF_INET;

  	//configuration du port = p 
	serverAddr.sin_port = htons(p);

	//configuration de l'adresse IP 
  	serverAddr.sin_addr.s_addr = htonl(INADDR_ANY); 
	
	//Initialisation des bits du champs "padding" à zeros (pour marquer la fin de l'entête et début des données)
	memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);
	
	//Liaison de la structure d'adresse au socket 
	bind(socketServeur, (struct sockaddr *) &serverAddr, sizeof(serverAddr));

    //L'écoute sur la socket , avec un maximum de demande défini ici à MAX_CLIENTS (on peut le modifier)
	if(listen(socketServeur,MAX_CLIENTS)==0)
		printf("Entrain d'écouter\n");
  	else
		printf("Erreur à l'écoute\n");


    //création d'un thread pour la vérification de l'existence du diffuseurs (diffuseurs actifs)
    //i.e. envoie des messages de type RUOK avec une fréquence fixée
    pthread_t th;
    if(pthread_create(&th,NULL,testDiffActif,NULL) != 0 ){
        printf("Erreur dans la création du thread de test actif/non-actif \n");
    }
    pthread_join(th,NULL);

    //un tableau de threads , ici on va stocker les threads crées (enregistrement de diffuseurs -REGI- et requetes des clients -ex:LIST- )
    pthread_t threadID[MAX_THREAD];

    int i=0;

    //boucle infine 
    while(1){
        //création d'une  socket pour la connexion entrante
		taille_adr = sizeof (serverStorage);
        nouvelleSocket = accept(socketServeur, (struct sockaddr *) &serverStorage, &taille_adr);
        //Pour chaque demande client on crée un thread et on lui assigne la demande client à traiter
		if( pthread_create(&threadID[i++], NULL, routine, &nouvelleSocket) == 0 ){
			printf("Arrivé du client N :%d \n",i);
			pthread_mutex_lock(&mutex);
				nbClients++;  				//Section critique 
  			pthread_mutex_unlock(&mutex);
		}else{
		
			printf("Erreur dans la création du thread \n");
		}
		
		//joindre les threads 
        if( i >= MAX_CLIENTS){
          	i = 0;
        	while(i < MAX_CLIENTS){
        		pthread_join(threadID[i++],NULL);
			}
       		i = 0;
        }
    }
    
    return 0;
}

/***
 *  check_message : Fonction pour vérifier le format des messages recu par le gestionnaire
 *  @entrées : message 
 *  @sorties : un entier (booleen) , 0 si le format n'est pas respécté , 1 sinon
 ***/ 
int check_message(char *message){
    if(strcmp(message,"LIST\r\n") == 0){
        return 1;
    }else if(startsWith("REGI",message)){
        if(strlen(message) != 57){
            printf("len %ld\n",strlen(message));
            return 0;
        }
        if(message[4] != ' ' || message[13] != ' ' ||message[29] != ' ' ||message[34] != ' ' ||message[50] != ' '){
            printf("spaces");
            return 0;
        }
        if(message[55] != '\r' || message[56] != '\n'){
            printf("r et n");
            return 0;
        }
        return 1;
    }else{
        
        return 0;
    }
}

/***
 * num_diff : Fonction pour retourner et standardiser le nombre de diffuseur actuellement enregistrés  
 * @entrees: void
 * @sorties: chaine de caractères (num-diff)
 ***/ 
void num_diff(char *i){
    char nb[4]="0";
    pthread_mutex_lock(&mutex);
        char tmp[4];
        sprintf(tmp,"%d",indice);
        if(indice < 10){
            
            strcat(nb,tmp);
        }else{
            strcpy(nb,"");
            sprintf(nb,"%d",indice);
        }
    pthread_mutex_unlock(&mutex);
    strcpy(i,nb);
    
}
/***
 * initDiff : Procédure pour initialiser diff
 * @entrees: void
 * @sorties: void
 ***/ 
 void initDiff(struct Diffuseur *diff){
    strcpy(diff->id,"\000");
    strcpy(diff->ip1,"\000");
    strcpy(diff->ip2,"\000");
    strcpy(diff->port1,"\000");
    strcpy(diff->port2,"\000");
 }

/***
 * registerDiff : Procedure pour remplir la structure diff 
 * @entrees: message recu (* REGI id ip1 port1 ip2 port2*)
 * @sorties: void
 * 
 ***/
void registerDiff(char * message , struct Diffuseur *diff){
    strncpy(diff->id,&message[5],8);
    strncpy(diff->ip1,&message[14],15);
    strncpy(diff->port1,&message[30],4);
    strncpy(diff->ip2,&message[35],15);
    strncpy(diff->port2,&message[51],4);
}


/***
 * startsWith : Fonction utilitaire pour vérifier si une chaine de caractère 'str' commence bien par une sous chaine 'pre' 
 * @entrees: Deux Chaines de caractères 'pre' et 'str'
 * @sorties: un entier (booléen) , 1 si 'str' commence par 'pre' , 0 sinon
 * 
 ***/
int startsWith(const char *pre, const char *str)
{
    size_t lenpre = strlen(pre),
           lenstr = strlen(str);
    return lenstr < lenpre ? 0 : memcmp(pre, str, lenpre) == 0;
}

/***
 * notExist : Fonction pour detecter si un diffuseur est enregistré déjà ou non
 * @entrees: id diffuseur (chaine de caractères)
 * @sorties: 0 si le diffuseur existe déjà , 1 sinon
 ***/
int notExist(char *id){
    pthread_mutex_lock(&mutex);
        for(int j=0;j<indice && indice< MAX_DIFF;j++){
            if(strcmp(diffuseurs[j]->id,id)==0){
                return 0;
            }
        }
    pthread_mutex_unlock(&mutex);
    return 1;
}