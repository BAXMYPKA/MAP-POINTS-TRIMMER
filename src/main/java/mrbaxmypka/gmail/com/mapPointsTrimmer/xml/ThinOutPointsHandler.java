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
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param distanceType {@link Nullable} If null, the value will be returned in miles.
     * @return If a given {@link DistanceUnits} is null the value will be returned in miles.
     * Otherwise it will be returned according to the given units.
     */
    protected double getDistance(double lat1, double lon1, double lat2, double lon2, @Nullable DistanceUnits distanceType) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
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

//    System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, 'M') + " Miles\n");
//    System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, 'K') + " Kilometers\n");
//    System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, 'N') + " Nautical Miles\n");
}
