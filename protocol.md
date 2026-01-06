# Semester-Projekt: Media Ratings Platform (MRP)

GitHub-Link: https://github.com/if24b009/MRP.git

## Projektbeschreibung

Die **Media Ratings Platform (MRP)** ist ein REST-basierter Java-HTTP-Server, der als Backend für mögliche Frontends (
z.B. Web, Mobile, CLI) dient. Die Anwendung ermöglicht es Usern, Medieninhalte wie Filme, Serien oder Spiele zu
erstellen, zu bewerten und zu verwalten.

> **Hinweis:** Es handelt sich um eine **standalone Java-Anwendung**. Die Implementierung nutzt `HttpServer` und
> speichert Daten in einer **PostgreSQL-Datenbank**.

## Technologien & Tools

| Komponente          | Technologie                         |
|---------------------|-------------------------------------|
| Programmiersprache  | Java (JDK 24)                       |
| HTTP-Server         | `com.sun.net.httpserver.HttpServer` |
| JSON-Serialisierung | Jackson (`com.fasterxml.jackson`)   |
| Datenbank           | PostgreSQL 16 (mit Docker)          |
| Authentifizierung   | Token-basierte Auth                 |
| API Testing         | Postman Collection                  |
| Build Tool          | Maven                               |

## Architektur & Designentscheidungen

### 1. Projektstruktur

```plaintext
📦 src/
├── 📁 database/          → Verwaltung PostgreSQL-Datenbankverbindung
├── 📁 model/             → Datenmodelle (User, MediaEntry, Rating)
├── 📁 repository/        → Datenbankzugriffe (DAO/Repository Pattern)
├── 📁 serverHandler/     → HTTP-Endpunkte & Request-Routing
├── 📁 service/           → Business-Logik
├── 📁 utils/             → Helferklassen
└── Main.java             → Einstiegspunkt & Server-Initialisierung
```

### 2. Architekturprinzipien

#### Layered Architecture

Die Anwendung folgt der **Schichten-Architektur** mit folgenden Layern zur Trennung von Verantwortlichkeiten:

- **Handler-Schicht:**  
  Zuständig für die Verarbeitung eingehender HTTP-Anfragen.  
  Die Schicht enthält das Bereitstellen der Endpoints der Anwendung, Auswerten der Anfrage (z. B. HTTP-Methode und Pfad)
  aus und Aufrufen der passende Service-Methode.
  Außerdem kümmert sie sich um das Senden von HTTP-Antworten (z. B. JSON-Ausgaben, Fehlercodes) – die direkte Arbeit mit
  dem HttpExchange findet ausschließlich hier statt.
  Sie enthält keine Business-Logik, sondern dient als Vermittler zwischen HTTP-Interface und Service-Schicht.  
  Beispiele: `AuthHandler`, `MediaEntryHandler`.

- **Service-Schicht:**  
  Enthält die Kernlogik (Business-Logik). Führt Validierungen durch, steuert den Ablauf und verarbeitet Daten.  
  Beispiele: `UserService`, `AuthService`.

- **Model-Schicht:**  
  Repräsentiert Datenbank-Entitäten. Wird von der Repository-Schicht verwendet.
  Beispiele: `User`, `MediaEntry`, `Rating`.

- **Repository-Schicht:**  
  Verantwortlich für die Kommunikation mit der PostgreSQL-Datenbank. Implementiert Create/Read/Update/Delete (CRUD)
  Methoden.  
  Beispiele: `UserRepository`, `MediaEntryRepository`.

Diese Schichtung macht den Code modular, leicht testbar und wartbar.

Zudem gibt es ebenfalls Packages für:

- **Server-Handler**  
  Das `serverHandler`-Package enthält alle Klassen, die als zentrale **Ansprechpartner für HTTP-Anfragen** fungieren.
  Jede Handler-Klasse ist einer oder mehreren spezifischen Routen (Endpoints) zugeordnet und wird direkt vom Server bei
  eingehenden Requests aufgerufen.  
  Zum Beispiel: `AuthHandler`

- **Utils**  
  Das `utils`-Package enthält **Hilfsklassen**, die allgemeine, wiederverwendbare Funktionen bereitstellen und **nicht
  direkt zur Geschäftslogik** gehören. Diese Klassen unterstützen andere Schichten (Handler, Service, Repository) und
  sorgen für sauberen, wartbaren Code.

- **Database**  
  Das `database`-Package enthält die zentrale Datenbankklasse, die für die **Verwaltung der Verbindung zur
  PostgreSQL-Datenbank** verantwortlich ist.

### 3. SOLID Prinzipien

Die Anwendung implementiert mehrere **SOLID-Prinzipien**, um wartbaren und erweiterbaren Code zu gewährleisten:

#### Single Responsibility Principle (SRP)

Jede Klasse hat genau eine klar definierte Verantwortung:

- **`AuthService`**: Kümmert sich ausschließlich um die Authentifizierung (Register/Login)
- **`TokenValidation`**: Ausschließlich für die Token-Validierung verantwortlich
- **`PathParameterExtraction`**: Nur für die URL-Parameter-Extraktion

**Beispiel aus `AuthService.java`:**

```java
public class AuthService {
    private final UserRepository userRepository;

    //Nur Authentifizierungs-Logik
    public Map<String, Object> register(String username, String password) { ...}

    public Map<String, Object> login(String username, String password) { ...}
}
```

#### Open/Closed Principle (OCP)

Das **Generic Repository Pattern** ermöglicht Erweiterung ohne Modifikation bestehenden Codes:

**`Repository<T>` Interface:**

```java
public interface Repository<T> {
    UUID save(T object) throws SQLException;

    ResultSet findById(UUID id) throws SQLException;

    int delete(UUID id) throws SQLException;

    ResultSet findAll() throws SQLException;
}
```

**Implementierungen:**

- `UserRepository implements Repository<User>`
- `MediaEntryRepository implements Repository<MediaEntry>`
- `RatingRepository implements Repository<Rating>`

Neue Repositories können hinzugefügt werden, ohne das Interface oder bestehende Implementierungen zu ändern.

#### Dependency Inversion Principle (DIP)

Betreffend der Unit Tests werden die Services dort wiefolgt umgesetzt, dass sie **Constructor Injection** für
Abhängigkeiten nutzen, was lose Kopplung und Testbarkeit ermöglicht:

**Beispiel aus `AuthService.java`:**

```java
public class AuthService {
    private final UserRepository userRepository;

    //Standard-Konstruktor für Produktion
    public AuthService() {
        this.userRepository = new UserRepository();
    }

    //Konstruktor für Testing mit Mock-Repository (Unit Tests)
    AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

Dies ermöglicht das Einsetzen von Mock-Objekten in Unit-Tests, ohne den Produktionscode zu verändern.

### 4. Token-basierte Authentifizierung

Für die Autorisierung wird ein **Token-basiertes Authentifizierungssystem** verwendet:

- Nach dem Login wird ein eindeutiger Token generiert.
- Dieser Token wird in der Datenbank in Bezug zu dem User gespeichert.
- Bei einer erneuten Anmeldung des Users wird sein Token in der Datenbank überschrieben.

### 5. UML

Zur besseren Darstellung der Systemarchitektur zeigt das folgende UML-Diagramm die zentralen Komponenten und Layer und
deren Interaktionen.

![UML-Diagramm von MRP](./mrp_uml.svg)

## Endpoints

Folgend eine Liste aller im Projekt relevanten Endpoints:

| Methode | Endpoint                      | Beschreibung                                                                                                                                      |
|---------|-------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| POST    | `/register`                   | Registrierung                                                                                                                                     |
| POST    | `/login`                      | Login & Token erhalten                                                                                                                            |
| GET     | `/mediaEntry`                 | Liste aller Medieninhalte                                                                                                                         |
|         | `/mediaEntry?{filter}`        | Liste gefilterter Medieninhalte                                                                                                                   |
|         |                               | **Filtermöglichkeiten:** `<filter>=<wert>`<br>- `genre=ACTION`<br>- `type=MOVIE`<br>- `releaseYear=2024`<br>- `ageRestriction=16`<br>- `rating=8` |
|         |                               | **Sortierung:** `<sortBy>=<feld>`<br>- `sortBy=title`<br>- `sortBy=year`<br>- `sortBy=score`                                                      |
| POST    | `/mediaEntry`                 | Neues Medium erstellen                                                                                                                            |
| PUT     | `/mediaEntry/{id}`            | Medium bearbeiten (nur Creator)                                                                                                                   |
| DELETE  | `/mediaEntry/{id}`            | Medium löschen                                                                                                                                    |
| POST    | `/mediaEntry/{id}/favorite`   | Medium als Favorite markieren                                                                                                                     |
| DELETE  | `/mediaEntry/{id}/favorite`   | Medium aus Favorites entfernen                                                                                                                    |
| POST    | `/rating/{id}`                | Bewertung erstellen                                                                                                                               |
| PUT     | `/rating/{id}`                | Bewertung bearbeiten                                                                                                                              |
| DELETE  | `/rating/{id}`                | Bewertung löschen                                                                                                                                 |
| POST    | `/rating/{id}/like`           | Bewertung liken                                                                                                                                   |
| POST    | `/rating/{id}/unlike`         | Bewertung nicht mehr liken                                                                                                                        |
| POST    | `/rating/{id}/confirm`        | Rating-Kommentar öffentlich schalten                                                                                                              |
| GET     | `/users/{username}/profile`   | Profil mit Statistiken                                                                                                                            |
| GET     | `/users/{username}/favorites` | Userspezifische Favoriten                                                                                                                         |
| GET     | `/users/{username}/ratings`   | Userspezifische Bewertungen                                                                                                                       |
| GET     | `/users/leaderboard`          | Leaderboard eines Users                                                                                                                           |
| GET     | `/users/recommendations`      | Empfehlungsfunktion                                                                                                                               |

## Testing

Das Projekt verwendet eine **zweistufige Teststrategie** mit Unit-Tests für die Business-Logik und Integrationstests
mittels Postman für End-to-End-Szenarien.

### Unit-Tests

Die Unit-Tests validieren die **Business-Logik in der Service-Schicht** unter Verwendung von **JUnit 5** und **Mockito**
für das Mocking von Abhängigkeiten.

#### Teststrategie

Die Unit-Tests beziehen sich auf die Service-Schicht mit der Business-Logik:

- **Input-Validierung**: Sicherstellen korrekter Eingaben (z.B. Username-Länge, Star-Rating 1-5)
- **Autorisierung**: Prüfung von Ownership-Regeln (nur Creator kann bearbeiten/löschen)
- **Fehlerfälle**: Testen von Exceptions bei ungültigen Zuständen
- **Happy-Path**: Validierung korrekter Durchläufe (normaler, idealer Ablauf mit ausschließlich richtigem Input)

#### Testklassen und Coverage

| Testklasse              | Tests  | Getestete Logik                                         |
|-------------------------|--------|---------------------------------------------------------|
| `AuthServiceTest`       | 5      | Registration, Login-Validierung, Passwort-Anforderungen |
| `UserServiceTest`       | 5      | Profil-Abruf, Update-Autorisierung, Recommendations     |
| `MediaEntryServiceTest` | 6      | CRUD-Operationen, Ownership, Favoriten-Logik            |
| `RatingServiceTest`     | 6      | Star-Validierung (1-5), Like/Unlike, Autorisierung      |
| **Gesamt**              | **22** |                                                         |

#### Beispiel: Testing mit Mockito

```java

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
    }

    @Test
    void register_ShouldThrowException_WhenUsernameTooShort() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("ab", "password123")
        );
        assertEquals("Username must be between 3 and 50 characters",
                exception.getMessage());
    }
}
```

#### Warum diese Logik getestet wird

- **Validierung**: Verhindert ungültige Daten in der Datenbank (z.B. leere Titel, ungültige Ratings)
- **Autorisierung**: Sicherheitskritisch - nur berechtigte User dürfen Ressourcen ändern
- **Edge Cases**: Duplikate erkennen (z.B. doppelte Likes, bereits favorisiert)
- **State-Transitions**: Like/Unlike, Confirm/Unconfirm korrekt umsetzen

### Integrationstests (Postman)

Die Integrationstests sind mittels Postman umgesetzt und validieren die **komplette API End-to-End**:

| Bereich               | Abgedeckt durch Postman Tests |
|-----------------------|-------------------------------|
| Registrierung / Login | ✅                             |
| Authentifizierung     | ✅                             |
| Profil                | ✅                             |
| CRUD Media            | ✅                             |
| Ratings               | ✅                             |
| Likes                 | ✅                             |
| Favoriten             | ✅                             |
| Leaderboard           | ✅                             |
| Recommendations       | ✅                             |
| Fehlerfälle (4xx/5xx) | ✅                             |

**Postman Collection Inhalt von `MRP-IntegrationTests`:**

- `01_Authentication`: Registration und Login
- `02_MediaEntry-CRUD`: Create, Update, Read, Delete eines Media Entrys
- `03_Favorites`: Add, Remove, Get User's Favorites
- `04_Ratings`: Comment-Visibility, Like/Unlike, CRUD
- `05_Leaderboard`: User-Rankings
- `06_Profile`: Profile-Statistiken des Users lesen und ändern
- `07_Recommendations`: User-Recommendations


## Zeitaufwand (geschätzt)

*Nachdem es sich hierbei um die Zwischenabgabe handelt, sind ausschließlich bereits erledigte Aufgaben zeitlich
geschätzt:*

| Aufgabe                                 | Stunden |
|-----------------------------------------|---------|
| Setup (Projekt-Grundgerüst, DB, Docker) | 20 h    |
| User Authentifizierung                  | 5 h     |
| User Profil                             | 5 h     |
| Media-Entry CRUD                        | 18 h    |
| Ratings + Comments + Likes              | 10 h    |
| Sortieren + Filter                      | 5 h     |
| Favoriten                               | 5 h     |
| Empfehlungen                            | 5 h     |
| Leaderboard                             | 2 h     |
| Postman Tests & Debugging               | 18 h    |
| Unit Tests                              | 8 h     |
| Dokumentation (README & Protocol)       | 4 h     |
| **Gesamt**                              | 105 h   |

## Probleme & Lösungen

Im bisherigen Projektverlauf sind keine gravierenden technischen Probleme aufgetreten.
Einige Punkte, die potenziell fehleranfällig wären, konnten durch saubere Planung und Strukturierung vermieden werden:

- **Datenbankverbindung:**  
  Die Verwendung einer zentralen `Database`-Klasse im `database`-Package führt dazu, dass die Verbindung stabil und
  übersichtlich bleibt, ohne Probleme mit mehreren Verbindungen.

- **Routen-Handling:**  
  Eine konsistente Struktur der Handler sorgt für Übersichtlichkeit und einfache Erweiterbarkeit.


## Lessons Learned

Im Laufe des Projektes sind folgende wichtige Erkenntnisse gewonnen worden:

### Architektur & Design

- **Schichten-Architektur lohnt sich:** Die strikte Trennung in Handler, Service und Repository macht den Code übersichtlich und wartbar. Änderungen in einer Schicht haben minimale Auswirkungen auf andere Schichten.

- **Repository Pattern für Flexibilität:** Das generische `Repository<T>`-Interface ermöglicht es, neue Entitäten hinzuzufügen, ohne bestehenden Code zu ändern. Alle Model-Repositories basierend auf dem `Repository<T>`-Interface.

- **Constructor Injection für Testbarkeit:** Die Entscheidung, Abhängigkeiten über Konstruktoren zu injizieren, hat das Unit-Testing vereinfacht. Mock-Objekte können direkt eingesetzt werden, ohne den Produktionscode anzupassen.

### Testing

- **Früh testen:** Das Schreiben von Unit-Tests parallel zur Implementierung hilft, Fehler frühzeitig zu erkennen. Besonders bei der Validierungslogik und Autorisierung haben Tests Bugs aufgedeckt, bevor sie in die Integrationstests gelangt sind.

- **Mockito bietet umfangreiche Möglichkeiten:** Das Mocking von Repository-Abhängigkeiten ermöglicht isolierte Tests der Business-Logik ohne Datenbankzugriffe. Dies beschleunigt die Testausführung deutlich.

### Optimierungsmöglichkeiten

- **Mehr Exceptions früher definieren:** Custom-Exceptions wie `ForbiddenException` und `DuplicateResourceException` sind erst spät eingeführt worden. Eine frühere Definition hätte die Fehlerbehandlung konsistenter gemacht.

- **Konfiguration auslagern:** Datenbankverbindungsdaten könnten in eine separate Konfigurationsdatei ausgelagert werden, statt im Code zu stehen.

### Gewonnene Fähigkeiten

- Praktische Erfahrung mit dem `com.sun.net.httpserver.HttpServer` ohne Framework
- Tieferes Verständnis von REST-Prinzipien und HTTP-Statuscodes
- Effektives Testen mit JUnit 5 und Mockito
- Anwendung von SOLID-Prinzipien in einem realen Projekt