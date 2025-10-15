# Semester-Projekt: Media Ratings Platform (MRP)

## Projektbeschreibung


Die **Media Ratings Platform (MRP)** ist ein REST-basierter Java-HTTP-Server, der als Backend für mögliche Frontends (z.B. Web, Mobile, CLI) dient. Die Anwendung ermöglicht es Usern, Medieninhalte wie Filme, Serien oder Spiele zu erstellen, zu bewerten und zu verwalten.

> **Hinweis:** Es handelt sich um eine **standalone Java-Anwendung** (ohne Spring, JSP, JSF oder ASP.NET). Die Implementierung nutzt `HttpServer` und speichert Daten in einer **PostgreSQL-Datenbank**.


## Technologien & Tools

| Komponente       | Technologie                         |
|------------------|-------------------------------------|
| Programmiersprache | Java (JDK 24)                       |
| HTTP-Server      | `com.sun.net.httpserver.HttpServer` |
| JSON-Serialisierung | Jackson (`com.fasterxml.jackson`)   |
| Datenbank        | PostgreSQL 16 (mit Docker)          |
| Authentifizierung | Token-basierte Auth                 |
| API Testing      | Postman Collection                  |
| Build Tool       | Maven                               |



## Architektur & Designentscheidungen

### 1. Projektstruktur

```plaintext
📦 src/
├── 📁 database/          → Verwaltung PostgreSQL-Datenbankverbindung
├── 📁 model/             → Datenmodelle (User, MediaEntry, Rating)
├── 📁 repository/        → Datenbankzugriffe (DAO/Repository Pattern)
├── 📁 serverHandler/     → HTTP-Endpunkte & Request-Routing
├── 📁 service/           → Business-Logik
├── 📁 transferObjects/   → X
├── 📁 util/              → Helferklassen (z.B. TokenHandler)
└── Main.java             → Einstiegspunkt & Server-Initialisierung
```

### 2. Architekturprinzipien

#### Layered Architecture

Die Anwendung folgt der **Schichten-Architektur** mit folgenden Layern zur Trennung von Verantwortlichkeiten:

- **Service-Schicht:**  
  Enthält die Kernlogik (Business-Logik). Führt Validierungen durch, steuert den Ablauf und verarbeitet Daten.  
  Beispiele: `UserService`, `MediaService`.

- **Model-Schicht:**  
  Nimmt HTTP-Anfragen entgegen, verarbeitet Header/Parameter und gibt HTTP-Antworten zurück.  
  Beispiele: `UserController`, `MediaController`, `RatingController`.

- **Repository-Schicht:**  
  Verantwortlich für die Kommunikation mit der PostgreSQL-Datenbank. Implementiert Create/Read/Update/Delete (CRUD) Methoden.  
  Beispiele: `UserRepository`, `MediaRepository`.

Diese Schichtung macht den Code modular, leicht testbar und wartbar.

Zudem gibt es ebenfalls Packages für:

- **Server-Handler**  
  Das `serverHandler`-Package enthält alle Klassen, die als zentrale **Ansprechpartner für HTTP-Anfragen** fungieren. Jede Handler-Klasse ist einer oder mehreren spezifischen Routen (Endpoints) zugeordnet und wird direkt vom Server bei eingehenden Requests aufgerufen.  
  Zum Beispiel: `AuthHandler`

- **Transfer Objects**  
  Das `transferObjects`-Package enthält alle **Data Transfer Objects (DTOs)**, die für die Kommunikation zwischen Client (z.B. Postman, Frontend) und Server verwendet werden. Diese Objekte dienen als **strukturierte Datencontainer**, um eingehende und ausgehende JSON-Daten vom/zum Server zu serialisieren bzw. deserialisieren.

- **Utils**  
  Das `utils`-Package enthält **Hilfsklassen**, die allgemeine, wiederverwendbare Funktionen bereitstellen und **nicht direkt zur Geschäftslogik** gehören. Diese Klassen unterstützen andere Schichten (Handler, Service, Repository) und sorgen für sauberen, wartbaren Code.

- **Database**  
  Das `database`-Package enthält die zentrale Datenbankklasse, die für die **Verwaltung der Verbindung zur PostgreSQL-Datenbank** verantwortlich ist.  


### 3. Token-basierte Authentifizierung

Für die Autorisierung wird ein **Token-basiertes Authentifizierungssystem** verwendet:

- Nach dem Login wird ein eindeutiger Token generiert.
- Dieser Token wird in der Datenbank in Bezug zu dem User gespeichert.
- Bei einer erneuten Anmeldung des Users wird sein Token in der Datenbank überschrieben.


### 5. UML

Zur besseren Darstellung der Systemarchitektur zeigt das folgende UML-Diagramm die zentralen Komponenten und Layer und deren Interaktionen.

![UML-Diagramm von MRP](./mrp_uml.svg)