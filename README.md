# MAP POINTS TRIMMER
**The convenient handler for your map points with photos**

Программа позволяет переносить все ваши сохраненные точки на карте (POI) с вложенными фотографиями местности из одной программы работы с картами в другую (например, из Locus Map Pro в Google Earth Pro).
Фотографиям можно сделать правильный предпросмотр, удалить ненужную историю сохранений, выставить любой путь до фотографий и т.п.
 
## Как использовать
Скачать готовую для автозапуска программу можно по ссылке.

После запуска в приложение заходите через любой браузер по адресу: [Map-Points-Trimmer](http://localhost:8088/trimmer/)

**По завершению работы не забудьте его выключить!!** (Иначе сервер так и останется висеть у вас в памяти.)

На данный момент программа работает только с .kml и .kmz файлами, как наиболее правильно и точно поддерживающие сохранение и передачу фотографий внутри точек.

Поддержка формата точек .gpx рассматривается в будущем.

## Минимальные системные требования
* Windows 7/Linux
* Intel/AMD Dual Core CPU with 2.0GHz+
* 4Gb+ RAM
* Preinstalled minimum [Java SE Runtime Environment 9+]( https://www.oracle.com/java/technologies/javase/javase9-archive-downloads.html "Where to download and install") or the latest [Java SE/SDK 14+](https://www.oracle.com/java/technologies/javase-downloads.html)
 
 Parameters - change according to selected export format:

    GPX - GPS eXchange format is an XML based text format used for handling points, tracks, and routes. Locus fully supports all valid tags. A more detailed description of GPX format is available on this Wikipedia page.
        Export only visible - exports only points actually visible on the map
        Share exported data - enables to share the export file in Dropbox or other services available in your device
        Incl. description&attachments - adds icons, generated descriptions and attachments and exports them into a subfolder
        GPX version - optional setting for experienced GPS device users
    KML/KMZ - Keyhole Markup Language is XML based plain text format with a really wide range of usage. Since the version 2.2 KML format is standardized by OGC so it is used by many web/desktop/mobile applications as well as by Locus Map. It is usable for export of both points and tracks.
        parameters are identical to GPX export but one:
        Incl. descriptions&attachments - enables packing data into one KMZ file - icons, photos etc.
