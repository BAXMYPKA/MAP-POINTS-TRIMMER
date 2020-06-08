[ENGLISH](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/README.md)

# MAP POINTS TRIMMER
**Удобный обработчик ваших точек с фотографиями на карте**

Программа позволяет переносить все ваши сохраненные точки на карте (POI) с вложенными фотографиями местности из одной программы работы с картами в другую (в первую очередь, между Locus Map Pro и Google Earth Pro для десктопа).
Фотографиям можно сделать правильный предпросмотр, удалить ненужную историю сохранений, выставить любой путь до фотографий и т.п.

Примеры результатов обработки в **Locus Map Pro** и **Google Earth Pro**:

![img](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/locusScreenshot420x700.jpg)
![img](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/blob/master/src/main/resources/static/img/GEscreenshot1060x700.jpg)

[Список версий программы](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/releases)

## Как использовать
Скачать готовую для автозапуска программу можно [по ссылке](https://github.com/BAXMYPKA/MAP-POINTS-TRIMMER/releases/download/v2.0/MAP-POINTS-TRIMMER.zip).

После запуска заходите в приложение через любой браузер по адресу: [Map-Points-Trimmer](http://localhost:8088/trimmer/) (http://localhost:8088/trimmer/)

**По завершению работы не забудьте его выключить!!** (Иначе сервер так и останется висеть у вас в памяти.)

На данный момент программа работает только с .kml и .kmz файлами, как наиболее правильно и точно поддерживающие сохранение и передачу фотографий и других полезных данных внутри точек.

Поддержка формата точек .gpx рассматривается в будущем. Хотя и не знаю, насколько это будет актуальным.

## Минимальные системные требования
* Windows 7/Linux
* Intel/AMD Dual Core CPU with 2.0GHz+
* 4Gb+ RAM
* Установленная Ява JVM версии не ниже [Java SE Runtime Environment 9+]( https://www.oracle.com/java/technologies/javase/javase9-archive-downloads.html "Where to download and install") или последняя [Java SE/SDK 14+](https://www.oracle.com/java/technologies/javase-downloads.html)
* Крайне желательна последняя версия любого интернет браузера.
 
## В проекте

* Динамическое отображение иконок и текста на Google Earth карте, т.е., увеличение иконки при наведении, отображение названия точки только при наведении.
* Поддержка формата .gpx. Пока под вопросом, т.к. не до конца решил, нужно ли это.

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
