# DBI‑UK‑Army.app – Installations‑ und Benutzerdokumentation (macOS)
## Kurzbeschreibung

DBI‑UK‑Army ist ein JavaFX‑Desktop‑Programm, das eine Verbindung zu einer lokalen PostgreSQL‑Datenbank herstellt. Beim Start erscheint ein Login‑Fenster, in dem Sie Benutzername, Passwort, den gewünschten Tabellen‑Name eingeben und den Port. Nach dem Klicken auf Weiter wird ein zweites Fenster geöffnet, in dem alle Daten aus der angegebenen Tabelle angezeigt werden.
Die .jar-file, die benutzt werden kann, ist unter releases.

## Voraussetzungen
* MacOS
* Java 17 JDK (oder höher)
* PostgreSQL 13+ (lokale Instanz, bereits installiert und konfiguriert)
* Homebrew (zum installieren, fallss Java oder PostgreSQL nicht richtig installiert sind)

Beim ersten Start von Apps, die nicht aus dem App‑Store stammen, kann macOS blockieren. Rechtsklick → Öffnen und bestätigen. Es kann auch die .jar file benutzt werden

## Homebrew installieren, falls nicht installiert:

[Homebrew](https://brew.sh/)

```
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```


## Voraussetzungen prüfen
Java installieren (falls noch nicht vorhanden)

### Überprüfen, ob Java installiert ist
```
java --version
```

Falls nicht installiert, so installieren:
```
brew install openjdk@17
``` 

### Pfad setzen (optional)
```
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### Version prüfen
```
java -version   # Ausgabe: openjdk 17.x.x …
```

### Überprüfen, ob PostgreSQL installiert ist

```
psql --version
```
Falls nicht installiert, so installieren:

```
brew install postgresql@13   # oder neuere Version
```

### Service starten
```
brew services start postgresql@13
```

### Benutzer & Datenbank erstellen (falls nicht bereits vorhanden)
```
sudo -u postgres createuser username --pwprompt
createdb -O username userdb
```
<b>Hinweis</b><br>
username ist der Username und kann geändert werden
userdb ist der Datenbankname und kann geänder werden

### Tabellen erstellen
SQL file von SQL Ordner runterladen

```
\i /pfad/zu/schema.sql
```

### jar file ausführen
### Es gibt zwei arten, die Jar file auszuführen

1. CLI (schneller, empfohlen)

  die .jar von der releases runterladen und ausführen mit 

  ```
  cd Pfad/zu/DBI-UK-Army.jar
  java -jar DBI-UK-Army.jar
  ```

2. GUI

Wenn man im Finder auf DBI-UK-Army.jar doppelklickt, kommt folgendes Pop-Up:

<img width="372" height="362" alt="Screenshot 2025-11-24 at 3 58 44 pm" src="https://github.com/user-attachments/assets/6c2f0dd7-8066-4b64-89cc-7704f0c2b878" />

Um das zu Umgehen, muss man die Systemeinstellungen öffnen und unter Privacy & Security runterscrollen, bis man folgendes sieht:

<img width="835" height="806" alt="Screenshot 2025-11-24 at 4 02 02 pm" src="https://github.com/user-attachments/assets/9d57bf49-e580-4048-adf7-091a2b702e28" />

Hier clickt man dann "Open Anyway". Dann kommt folgendes Fenster:

<img width="328" height="418" alt="Screenshot 2025-11-24 at 4 04 42 pm 1" src="https://github.com/user-attachments/assets/9fe1cf53-9721-4309-97bf-8d3d10213b68" />

Da clickt man dann "Open Anyway", dann gibt man das Password ein oder scannt den Fingerabdruck und nun kann man die .jar File öffnen

