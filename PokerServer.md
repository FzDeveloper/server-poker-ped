# PokerServer REST API #



## Objectif ##
L’objectif de ce guide d’utilisation est de vous guider dans l’installation et l’utilisation de PokerServer REST API.

## Audience ##
Ce guide s’adresse à des développeurs connaissant les principes fondamentaux du web comme l’architecture client-serveur, le protocole http, le langage JSON, le protocole REST, ainsi que le langage de développement Java.

## Pré-requis ##
Un éditeur de texte suffit mais un environnement de développement est conseillé (Eclipse par exemple). <br>
Un navigateur web. <br>
Un serveur web JBoss. <br>

<h2>Présentation</h2>
L’API PokerServer se compose de plusieurs méthodes à appeler et de quelques points terminaux d’API. <br>
Pour exécuter une action à l'aide de l'API PokerServer, vous devez sélectionner une convention d'appel, envoyer une requête à son point terminal en spécifiant une méthode et des arguments, puis vous recevrez une réponse formatée. <br>
Le format de requête REST utilise une liste de paramètres nommés. <br>
Le paramètre OBLIGATOIRE consumerKey permet de spécifier le jeton d’authentification. <br>
Le paramètre OBLIGATOIRE signature permet de spécifier la signature. <br>
Les arguments, réponses et codes d'erreur de chaque méthode sont listés sur la page de données techniques de la méthode. Les méthodes correspondent à des listes sur la page d'index API. <br>
Point terminaux : <br>
<a href='http://localhost:8888/PokerServer/rest/auth'>http://localhost:8888/PokerServer/rest/auth</a> <br>
<a href='http://localhost:8888/PokerServer/rest/game'>http://localhost:8888/PokerServer/rest/game</a> <br>
<a href='http://localhost:8888/PokerServer/rest/player'>http://localhost:8888/PokerServer/rest/player</a> <br>

<h2>Mode d’accès aux services et authentification</h2>

<h2>Format de requête REST</h2>
REST est le format de requête le plus simple à utiliser – c'est aussi simple que les actions HTTP GET ou POST. <br>
<a href='http://localhost/PokerServer/rest/'>http://localhost/PokerServer/rest/</a> <br>
Ecrire un test du service (style echo) pour mettre dans la doc.<br>
<br>
<h2>Format de réponse JSON</h2>
JSON, ou JavaScript Object Notation, est un format simple d'échange de données assimilable par l'ordinateur, qui facilite l'élaboration d'applications avec l’API Pour plus d'informations sur JSON, rendez-vous sur json.org.