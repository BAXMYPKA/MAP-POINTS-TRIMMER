[ENGLISH LANGUAGE](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/README.md)

# MAP POINTS TRIMMER

**Удобный обработчик ваших точек с фотографиями на карте в форматах KML, KMZ, ZIP**

**Программа позволяет:**

* Переносить все ваши сохраненные точки на карте (POI) с вложенными фотографиями местности из одной программы работы с картами в другую в форматах KML и KMZ (в первую очередь, между Locus Map Pro и Google Earth Pro для десктопа). Основной бонус для того же Locus Map - получить фотографии прямо в описании, плюс, при импорте из других источников, сделать их доступными во вкладке "Вложения". А если источников фотографий для точек много - хранить их централизованно в любом месте на устройстве.

* При просмотре на любом устройстве - выводить фото в нужном вам размере. Фотографиям можно сделать правильный предпросмотр, в зависимости от используемого приложения и платформы, удалить ненужную историю сохранений и т.п.

* При экспорте точек в Google Earth Pro можно выставить нужный размер иконок, подписей к ним, цвет текста, прозрачность, поведение при наведении мышью и т.п.

* Если точек ооочень много, а устройство не самое мощное - удаление ненужной информации может ускорить вывод на экран.

Примеры результатов обработки в **Locus Map Pro** и **Google Earth Pro**:

<p align="center"> <img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/locusMapPhotoPoint2.jpg" width="300px" alt="Свойства точки с фотографией в Locus Map" title="Отображение фотографии местности в теле описания точки Locus Map"> <img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/googleEarthPhoto.jpg" width=350px" alt="Вывод точки с фотографией местности при нажатии на нее в Google Earth Pro" title="Вывод точки с фотографией местности при нажатии на нее в Google Earth Pro"> <img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/googleEarthPoints.jpg" width="350px" alt="Пример текста и точек с разным цветом и размером на спутниковой карте Google Earth" title="Пример текста и точек с разным цветом и размером на спутниковой карте Google Earth"><img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/GoogleEarthDynamic.gif" width="350px" alt="Пример динамического текста и точек с разным цветом и размером на спутниковой карте Google Earth" title="Пример динамического отображения текста и точек с разным цветом и размером на спутниковой карте Google Earth"></p>

## Интерфейс программы

<img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/InterfaceRU.png" width="950px" alt="Интерфейс программы" title="Интерфейс программы">

## Как использовать

Скачать готовую для автозапуска программу можно [по ссылке](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/releases).

Распаковываете из архива и запускаете файл MAP-POINTS-TRIMMER.jar.

После запуска заходите в приложение через любой браузер по адресу: [Map-Points-Trimmer](http://localhost:8088/trimmer/) (http://localhost:8088/trimmer/)

**Приложение автоматически закроется, если вы не подключитесь в течение пары минут или если закроете вкладку программы в браузере.
Также его можно выключить большой зеленой кнопкой.**

На данный момент программа работает только с .kml и .kmz файлами, как наиболее правильно и точно поддерживающие сохранение и передачу фотографий и других полезных данных внутри точек.

## Минимальные системные требования

* Windows 7/Linux
* Intel/AMD Dual Core CPU with 2.0GHz+
* 4Gb+ RAM
* Установленная Ява JVM версии не ниже [Java SE Runtime Environment 11+]( https://www.oracle.com/java/technologies/javase-downloads.html "Where to download and install")
* Крайне желательна последняя версия любого интернет браузера.
 
##  Особенности

Используйте на свой страх и риск )))

## ТЕХНИЧЕСКИЕ ПОДРОБНОСТИ

При создании использовались:
* Java v.11
* Maven
* Spring Boot
* JavaScript 2015+
* HTML
* CSS
