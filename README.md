# MAP POINTS TRIMMER
## The convenient handler for your map points with photos
Сlient-server Java application based on Spring to convert custom POI (Points of Interest) from Locus Android application to Google Earth Windows application.

Программа позволяет правильным образом перенести все ваши сохраненные точки (POI) из одной программы работы с картами в другую (например, из Locus Map pro в Google Earth Pro). Если ваши точки с привязанными фотографиями местности, то можно сделать правильный предпросмотр, удалить ненужную историю сохранений, выставить любой путь до фотографий и т.п.

 Parameters - change according to selected export format:

    GPX - GPS eXchange format is an XML based text format used for handling points, tracks, and routes. Locus fully supports all valid tags. A more detailed description of GPX format is available on this Wikipedia page.
        Export only visible - exports only points actually visible on the map
        Share exported data - enables to share the export file in Dropbox or other services available in your device
        Incl. description&attachments - adds icons, generated descriptions and attachments and exports them into a subfolder
        GPX version - optional setting for experienced GPS device users
    KML/KMZ - Keyhole Markup Language is XML based plain text format with a really wide range of usage. Since the version 2.2 KML format is standardized by OGC so it is used by many web/desktop/mobile applications as well as by Locus Map. It is usable for export of both points and tracks.
        parameters are identical to GPX export but one:
        Incl. descriptions&attachments - enables packing data into one KMZ file - icons, photos etc.
