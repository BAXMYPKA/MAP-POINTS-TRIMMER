[RUSSIAN](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/README_ru.md)

# MAP POINTS TRIMMER
**The convenient handler for your map points with photos**

The aim of this utility is to help transfer your map points with photos in their descriptions from one program to another one so that embedded photos will be properly displayed according to your wishes and screen resolution as preview and have the ability to be magnified by click. Also you can set any path to your photos within any device, delete outdated garbage from descriptions and so forth.

For export to Google Earth the application proposes options for selecting size, color and opacity of icons and points names to be displayed on the map. 

First of all it's been developed for Locus Map Pro and Google Earth Pro for desktop.

The examples of map points displaying for **Locus Map Pro** and **Google Earth Pro**:

<p align="center"> <img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/locusMapPhotoPoint.jpg" width="260px" alt="Locus Map point with the photo in description" title="How Locus Map can display a photo right in a point description"> <img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/googleEarthPhoto.jpg" width=310px" alt="How Google Earth Pro displays a point description with the photo inside it after it was clicked" title="How Google Earth Pro displays a point description with the photo inside it after it was clicked"> <img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/googleEarthPoints.jpg" width="310px" alt="An example of points icons and names with various colors and sizes on Google Earth satellite map" title="An example of points icons and names with various colors and sizes on Google Earth satellite map"></p>

## How to use it

Download the ready to use [application here](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/releases)

When the application start use your browser to visit: [Map-Points-Trimmer](http://localhost:8088/trimmer/) (http://localhost:8088/trimmer/)

**After you finish don't forget to shut down the application with a special button!** (Otherwise the local server will hang in your RAM.)

For the moment the utility supports only .kml and .kmz formats as the most powerful for keep and transfer points with photos.

The support of .gpx format is being considered but I'm hesitating if it will be useful.

## Minimum system requirements
* Windows 7/Linux
* Intel/AMD Dual Core CPU with 2.0GHz+
* 4Gb+ RAM
* Preinstalled Java JVM with a minimum version [Java SE Runtime Environment 9+]( https://www.oracle.com/java/technologies/javase/javase9-archive-downloads.html "Where to download and install") or the latest [Java SE/SDK 14+](https://www.oracle.com/java/technologies/javase-downloads.html)
* It is expected you use not an outdated version of any Internet browser
 
## In the project

* Dynamic points displaying, i.e. increase an icon when hovering a cursor over it, show point name only on mouseover etc.
* .gpx format support

##  Features

Use it with caution.

## TECH INFO

Used technologies:
* Java v.11
* Maven
* Spring Boot
* JavaScript 2015+
* HTML
* CSS
