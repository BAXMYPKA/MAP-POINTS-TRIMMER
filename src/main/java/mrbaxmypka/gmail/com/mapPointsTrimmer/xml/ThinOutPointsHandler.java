package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.AccessLevel;
import lombok.Getter;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DistanceUnits;
import org.springframework.lang.Nullable;
import org.w3c.dom.Document;

/**
 * Thins out .kml points by a given distance.
 * If two points are placed to each other closer than a {@link MultipartMainDto#getThinOutDistance()}
 * the second point will be deleted.
 * If a .KMZ file given AND also a .KMZ is supposed to be returned
 * the close spaced points will be deleted with their icons and photos.
 */
@Getter(AccessLevel.PROTECTED)
public abstract class ThinOutPointsHandler {

    public ThinOutPointsHandler() {
    }

    abstract void thinOutPoints(Document kmlDocument, MultipartMainDto multipartMainDto);

    /*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::                                                                         :*/
    /*::  This routine calculates the distance between two points (given the     :*/
    /*::  latitude/longitude of those points). It is being used to calculate     :*/
    /*::  the distance between two locations using GeoDataSource (TM) products   :*/
    /*::                                                                         :*/
    /*::  Definitions:                                                           :*/
    /*::    Southern latitudes are negative, eastern longitudes are positive     :*/
    /*::                                                                         :*/
    /*::  Function parameters:                                                   :*/
    /*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
    /*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
    /*::    unit = the unit you desire for results                               :*/
    /*::           where: 'MILES' is statute miles (default)                     :*/
    /*::                  'KILOMETERS' is kilometers                             :*/
    /*::                  'METERS' is meters                                     :*/
    /*::                  'NAUTICAL_MILES' is nautical miles                     :*/
    /*::  Worldwide cities and other features databases with latitude longitude  :*/
    /*::  are available at https://www.geodatasource.com                         :*/
    /*::                                                                         :*/
    /*::  For enquiries, please contact sales@geodatasource.com                  :*/
    /*::                                                                         :*/
    /*::  Official Web site: https://www.geodatasource.com                       :*/
    /*::                                                                         :*/
    /*::           GeoDataSource.com (C) All Rights Reserved 2019                :*/
    /*::                                                                         :*/
    /*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    /**
     * The haversine formula determines the great-circle distance between two points on a sphere given their longitudes and latitudes.
     * Important in navigation, it is a special case of a more general formula in spherical trigonometry,
     * the law of haversines, that relates the sides and angles of spherical triangles
     *
     * @param longitude1
     * @param latitude1
     * @param longitude2
     * @param latitude2
     * @param distanceType {@link Nullable} If null, the value will be returned in miles.
     * @return A haversine (by a semi sphere, NOT a straight line!) distance between two points.
     * If a given {@link DistanceUnits} is null the value will be returned in miles.
     * Otherwise it will be returned according to the given units.
     */
    protected double getHaversineDistance(double longitude1, double latitude1,
                                           double longitude2, double latitude2,
                                          @Nullable DistanceUnits distanceType) {
        if (distanceType == null) {
            distanceType = DistanceUnits.MILES;
        }
        double theta = longitude1 - longitude2;
        double dist = Math.sin(deg2rad(latitude1)) * Math.sin(deg2rad(latitude2)) + Math.cos(deg2rad(latitude1)) * Math.cos(deg2rad(latitude2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515; //In miles
        if (distanceType.equals(DistanceUnits.YARDS)) {
            dist = dist * 1760;
        } else if (distanceType.equals(DistanceUnits.KILOMETERS)) {
            dist = dist * 1.609344;
        } else if (distanceType.equals(DistanceUnits.METERS)) {
            dist = dist * 1.609344 * 1000;
        } else if (distanceType.equals(DistanceUnits.NAUTICAL_MILES)) {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    /**
     * The haversine formula determines the great-circle distance between two points on a sphere given their longitudes and latitudes.
     * Important in navigation, it is a special case of a more general formula in spherical trigonometry,
     * the law of haversines, that relates the sides and angles of spherical triangles.
     * This method also invokes the given altitudes difference to determine the distance between two points
     *
     * 1)высота одного дерева 20 м а второго 9 м расстояние между деревьями составляет 60 м Найдите расстояние между их верхушками этих деревьев
     * 2)высоты двух сосен соответственно равны 21 м и 28 м расстояние между ними составляет 24 м Найдите расстояние между верхушками этих деревьев
     *
     * Решение1:
     * Высота1=20
     * Высота2=9
     * РасстояниеВплоскости=60
     * РазницаВысот=Высота1 МИНУС Высота2
     * По теореме Пифагора:
     * Расстояние=КВАДРАТНЫЙ КОРЕНЬ из (РазницаВысот В КВАДРАТЕ) + (Расстояние В КВАДРАТЕ)
     *
     * Решение2:
     * Высота1=28
     * Высота2=21
     * РасстояниеВплоскости=24
     * РазницаВысот=7
     * По теореме Пифагора:
     * Расстояние=КВАДРАТНЫЙ КОРЕНЬ из (РасстояниеВплоскости В КВАДРАТЕ) ПЛЮС (РазницаВысот В КВАДРАТЕ)
     *
     * @param longitude1
     * @param latitude1
     * @param longitude2
     * @param latitude2
     * @param distanceType {@link Nullable} If null, the value will be returned in miles.
     * @return A haversine (by a semi sphere, NOT a straight line!) distance between two points
     * including the altitudes difference.
     * If a given {@link DistanceUnits} is null the value will be returned in miles.
     * Otherwise it will be returned according to the given units.
     */
    protected double getHaversineDistance(
            double longitude1, double latitude1, double altitude1,
            double longitude2, double latitude2, double altitude2,
            @Nullable DistanceUnits distanceType) {
        double haversineDistance = getHaversineDistance(longitude1, latitude1, longitude2, latitude2, distanceType);
        double altitudeDifference = Math.max(altitude1, altitude2) - Math.min(altitude1, altitude2);
        return Math.sqrt((Math.pow(haversineDistance, 2.0)) + (Math.pow(altitudeDifference, 2)));
    }

//    System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, 'M') + " Miles\n");
//    System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, 'K') + " Kilometers\n");
//    System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, 'N') + " Nautical Miles\n");
}
