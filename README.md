# DBI‑UK‑Army.app – Installations‑ und Benutzerdokumentation (macOS)
## Kurzbeschreibung

DBI‑UK‑Army ist ein JavaFX‑Desktop‑Programm, das eine Verbindung zu einer lokalen PostgreSQL‑Datenbank herstellt. Beim Start erscheint ein Login‑Fenster, in dem Sie Benutzername, Passwort und den gewünschten Tabellen‑Name eingeben. Nach dem Klicken auf Weiter wird ein zweites Fenster geöffnet, in dem alle Daten aus der angegebenen Tabelle angezeigt werden.
Die .jar-file, die benutzt werden kann ist unter /JAR-Files

## Voraussetzungen

* macOS 10.13+ (High Sierra oder neuer)
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
sudo -u postgres createuser newUser --pwprompt
createdb -O newUSer newDB
```
<b>Hinweis</b><br>
newUser ist der Username und kann geändert werden
newDB ist der Datenbankname und kann geänder werden

<b>Hinweis</b><br>
Die Standard‑Portnummer ist 5432. Falls Sie einen anderen Port nutzen, passen Sie die Konfiguration später an.

## Nun kann DBI‑UK‑Army.app oder DBI-UK-Army.jar gestartet werden!
