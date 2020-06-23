# JKU Multimediasysteme Projekt

## Videokonvertierung

**Contributors:** 
* Thomas Grininger
* Alexander Höbart
* Lukas Wagner
* Joel Klimont

### Kurzbeschreibung
Es soll eine Desktop-Applikation entwickelt werden, mit der man Videos konvertieren und komprimieren kann. Diese Anwendung
ist für Einzel-Benutzer gedacht und unterstützt die einfache Umwandlung von Video-Dateien in alle gängige Formate.

### Anforderungen
* Die Anwendung soll im Hintergrund [FFmpeg](https://ffmpeg.org/) verwenden.
* Die GUI soll es ermöglichen alle gängigen Einstellungen vorzunehmen, woraufhin die Verarbeitung des Videos mit
den jeweiligen getroffenen Optionen durchgeführt wird.
* Es soll vordefinierte Profile geben die optimierte Einstellungen für Android Handys, Desktop PCs etc. angeben.
Solche Profile sollen auch selber angelegt, gespeichert und verändert werden können.
* Es soll möglich sein, mehrere Aufträge in einer Warteschlange abzulegen, die z.B. über die Nacht verarbeitet werden
können. Ein Auftrag besteht aus einem Video und bestimmten Einstellungen, mit denen es verarbeitet werden soll. Der Benutzer kann jederzeit neue Aufträge zu der Warteschlange hinzufügen/ entfernen/ stoppen etc.

### Ziele
* Der Benutzer kann mittels der GUI ein Video von einem Format zu einem anderen Konvertieren.
* Der Benutzer kann eine Warteschlange erstellen, in der zu jedem Video Einstellungen definiert sind, die über einen
längeren Zeitraum verarbeitet werden.
* Der Benutzer kann per Drag&Drop oder durch Pfadauswahl Video-Dateien spezifizieren.
* Der Benutzer kann alle Tasks in der Warteschlange jederzeit stoppen und beliebige entfernen.
* Der Benutzer kann für jeden Task einen Zielordner angeben, wo das verarbeitete Video gespeichert wird.
* Der Benutzer kann Profile erstellen, in denen Einstellungen gespeichert werden können.
* Der Benutzer kann diese Profile auch bearbeiten/ entfernen etc.
* Dem Benutzer werden ein paar optimierte Profile für bestimmte Plattformen (z.B. für Android) bereitgestellt, die er
zum Video verarbeiten verwenden kann
* Der Benutzer kann Audiospuren aus dem Video entfernen/ neue hinzufügen.
#### Nicht-Ziele
* Der Benutzer kann **nicht** mittels des Tools Videos zusammenschneiden.
* Der Benutzer kann **keine** Videos bearbeiten (z.B. neue Animationen hinzufügen).
### Realisierung
Das Projekt wird in JAVA realisiert und für die GUI wird [JavaFX](https://openjfx.io/) benutzt. Um die Kommunikation zwischen FFmpeg
und dem Programm zu realisieren wird der [ffmpeg-cli-wrapper](https://github.com/bramp/ffmpeg-cli-wrapper) verwendet. Die Profile werden in einer [SQLite-Datenbank](https://www.sqlite.org/index.html) lokal auf dem Rechner gespeichert und sollen auch
importierbar / exportierbar sein. Zur Versionskontrolle und gemeinsamen Zusammenarbeit soll GitHub verwendet
werden.
