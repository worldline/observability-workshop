## demo 
Quelques requêtes

Une ou plusieurs erreurs fonctionnelles, techniques


## Logs

le client nous dit qu'il a une erreur fonctionnelle "AMOUNT_EXCEEDED" ou "INVALID CARD NUMBER"

On regarde dans les logs d' easy pay

Authorize payment
CheckLunKEY NOK

Impossible de corréler plusieurs appels

Pas de contexte (fonctionnel)

--> On ne voit pas grand chose dans les logs

--> On ajoute des logs

CheckLunkKey
BankAuthorService

On ne peut pas corréler.

On ajoute un correlation ID

--> manipulation à faire

Erreur technique

dans le fichier easypay:
data.sql

on a dans la ligne POS-02 passer la colonne active à NULL

Exemple avec POS-02 shell 

Exception

--> on a rien

On met une log dans POstValidator 

### Grafana

Les logs sont écrites dans un répertoire 
On utilise Promtail pour les envoyer vers Loki

Demo dans Grafana

Filtrer, recherche via un correlation ID

## Metrics
Pool de base de données, Garbage Collector

Dans smart bank, on présente le cache réalisé.
--> script
Au bout d'un moment, on a un fallback et un Out Of Memory

On regarde la log dans Grafana

On bascule dans le dashboard de métriques
Monitoring de la JVM
Monitoring du moteur de cache
--> Dire qu'on peut le faire avec le pool de BD

On montre et on explique

## Traces

Déjà activé
On relance des requêtes qui posaient problème.

On regarde dans un dashboard Grafana le résultat

##  Corrélation

Utilisation d'un examplar


##
