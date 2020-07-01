[ENGLISH](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/README.md)

# MAP POINTS TRIMMER
**Удобный обработчик ваших точек с фотографиями на карте**

Программа позволяет переносить все ваши сохраненные точки на карте (POI) с вложенными фотографиями местности из одной программы работы с картами в другую (в первую очередь, между Locus Map Pro и Google Earth Pro для десктопа).
Фотографиям можно сделать правильный предпросмотр, удалить ненужную историю сохранений, выставить любой путь до них, удалить устаревшие данные из описаний и т.п.

Также при экспорте в Google Earth есть опции настройки размера, цвета и прозрачности иконок и текста при отображении их на спутниковой карте.

Примеры результатов обработки в **Locus Map Pro** и **Google Earth Pro**:

<p align="center"> <img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/locusMapPhotoPoint.jpg" width="300px" alt="Свойства точки с фотографией в Locus Map" title="Отображение фотографии местности в теле описания точки Locus Map"> <img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/googleEarthPhoto.jpg" width=350px" alt="Вывод точки с фотографией местности при нажатии на нее в Google Earth Pro" title="Вывод точки с фотографией местности при нажатии на нее в Google Earth Pro"> <img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/googleEarthPoints.jpg" width="350px" alt="Пример текста и точек с разным цветом и размером на спутниковой карте Google Earth" title="Пример текста и точек с разным цветом и размером на спутниковой карте Google Earth"><img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/GoogleEarthDynamic.gif" width="350px" alt="Пример динамического текста и точек с разным цветом и размером на спутниковой карте Google Earth" title="Пример динамического отображения текста и точек с разным цветом и размером на спутниковой карте Google Earth"></p>

## Как использовать
Скачать готовую для автозапуска программу можно [по ссылке](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/releases).

Распаковываете из архива и запускаете файл MAP-POINTS-TRIMMER.jar.

После запуска заходите в приложение через любой браузер по адресу: [Map-Points-Trimmer](http://localhost:8088/trimmer/) (http://localhost:8088/trimmer/)

**По завершению работы не забудьте его выключить!!** (Иначе сервер так и останется висеть у вас в памяти.)

На данный момент программа работает только с .kml и .kmz файлами, как наиболее правильно и точно поддерживающие сохранение и передачу фотографий и других полезных данных внутри точек.

## Интерфейс программы

<details><summary>Развернуть\Свернуть</summary><img src="https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/interfaceScreenshot_ru.jpg" width="950px" alt="Интерфейс программы" title="Интерфейс программы"></details>

## Минимальные системные требования
* Windows 7/Linux
* Intel/AMD Dual Core CPU with 2.0GHz+
* 4Gb+ RAM
* Установленная Ява JVM версии не ниже [Java SE Runtime Environment 9+]( https://www.oracle.com/java/technologies/javase/javase9-archive-downloads.html "Where to download and install") или последняя [Java SE/SDK 14+](https://www.oracle.com/java/technologies/javase-downloads.html)
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
