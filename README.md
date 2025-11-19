# DBI‑UK‑Army.app – Installations‑ und Benutzerdokumentation (macOS)
## Kurzbeschreibung

DBI‑UK‑Army ist ein JavaFX‑Desktop‑Programm, das eine Verbindung zu einer lokalen PostgreSQL‑Datenbank herstellt. Beim Start erscheint ein Login‑Fenster, in dem Sie Benutzername, Passwort und den gewünschten Tabellen‑Name eingeben. Nach dem Klicken auf Weiter wird ein zweites Fenster geöffnet, in dem alle Daten aus der angegebenen Tabelle angezeigt werden.
Die .jar-file, die benutzt werden kann ist unter /JAR-Files

## Voraussetzungen
* MacOS
* Java 17 JDK (oder höher)
* PostgreSQL 13+ (lokale Instanz, bereits installiert und konfiguriert)

Beim ersten Start von Apps, die nicht aus dem App‑Store stammen, kann macOS blockieren. Rechtsklick → Öffnen und bestätigen. Es kann auch die .jar file benutzt werden

## Voraussetzungen prüfen
Java installieren (falls noch nicht vorhanden)
#### Homebrew → Java 17 JDK
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
### PostgreSQL installieren (falls noch nicht vorhanden)
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
SQL file von SQL Ordner runterlade

```
\i /pfad/zur/datei.sql
```

### jar file ausführen
die .jar von der releases runterladen und ausführen mit 

```
cd Pfad/zur/jar/file
java -jar DBI-UK-Army.jar
```
