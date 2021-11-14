package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DistanceUnits;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@literal {@literal Either <coordinates>-119.779550,33.829268,0</coordinates>
 * or <coordinates>-122.000,37.002,127.00</coordinates> or <coordinates>-122.000,37.002</coordinates>}
 * A single tuple consisting of floating point values for longitude, latitude, and altitude (IN THAT ORDER!).
 * Longitude and latitude values are in degrees, where
 * longitude ≥ −180 and <= 180
 * latitude ≥ −90 and ≤ 90
 * altitude values (optional) are in meters above sea level.
 * If altitude isn't presented it will be set as 0.0 double value.
 * Do not include spaces between the three values that describe a coordinate.
 */
class ThinOutPointsHandlerTest {

    private ThinOutPointsHandler thinOutPointsHandler = new ThinOutKmlPointsHandler(null, null, null);

    /**
     * Every tuple includes the last value as the distance in meters according to the Google Earth ruler.
     */
    @ParameterizedTest
    @CsvSource(value = {"38.34321845735578,56.27642531546667,38.34303700795801,56.27559566234272,95.00",
            "38.34303700795801,56.27559566234272,38.34545701565047,56.27586260444358,152.0"})
    public void distance_Between_Points_In_Meters_Should_Be_Within_25_Of_Inaccuracy(
            String longitude1, String latitude1, String longitude2, String latitude2, String googleEarthDistance) {
        //GIVEN
        double long1 = Double.parseDouble(longitude1);
        double lat1 = Double.parseDouble(latitude1);
        double long2 = Double.parseDouble(longitude2);
        double lat2 = Double.parseDouble(latitude2);
        double geDistance = Double.parseDouble(googleEarthDistance);

        double inaccuracy = 25.0; //meters

        //WHEN
        double haversineDistanceMeters = thinOutPointsHandler.getHaversineDistance(long1, lat1, long2, lat2,
                DistanceUnits.METERS);

        //THEN
        boolean inaccuracyCondition = (geDistance - inaccuracy) <= haversineDistanceMeters
                && (geDistance + inaccuracy) >= haversineDistanceMeters;

        if (geDistance == 95.00) {
            assertTrue(inaccuracyCondition);
        } else if (geDistance == 152.0) {
            assertTrue(inaccuracyCondition);
        }
//        System.out.println(haversineDistanceMeters);
    }

    /**
     * Every tuple includes the last value as the distance in meters according to the Google Earth ruler.
     */
    @ParameterizedTest
    @CsvSource(value = {"38.34321845735578,56.27642531546667,38.34303700795801,56.27559566234272,100.00",
            "38.34303700795801,56.27559566234272,38.34545701565047,56.27586260444358,167.0"})
    public void distance_Between_Points_In_Yards_Should_Be_Within_27_3_Of_Inaccuracy(
            String longitude1, String latitude1, String longitude2, String latitude2, String googleEarthDistance) {
        //GIVEN
        double long1 = Double.parseDouble(longitude1);
        double lat1 = Double.parseDouble(latitude1);
        double long2 = Double.parseDouble(longitude2);
        double lat2 = Double.parseDouble(latitude2);
        double geDistance = Double.parseDouble(googleEarthDistance);

        double inaccuracy = 27.3; //meters

        //WHEN
        double haversineDistanceMeters = thinOutPointsHandler.getHaversineDistance(long1, lat1, long2, lat2,
                DistanceUnits.YARDS);

        //THEN
        boolean inaccuracyCondition = (geDistance - inaccuracy) <= haversineDistanceMeters
                && (geDistance + inaccuracy) >= haversineDistanceMeters;

        if (geDistance == 100.00) {
            assertTrue(inaccuracyCondition);
        } else if (geDistance == 167.0) {
            assertTrue(inaccuracyCondition);
        }
//        System.out.println(haversineDistanceMeters);
    }

    /**
     * Every tuple includes the last value as the distance in meters according to the Google Earth ruler.
     */
    @ParameterizedTest
    @CsvSource(value = {"38.34545701565047,56.27586260444358,5,38.3454778195248,56.27584436936672,45.00,40.00",
            "38.34545701565047,56.27586260444358,160.0,38.3454778195248,56.27584436936672,260.00,100.00"})
    public void distance_Between_Points_In_Meters_With_Altitude_Should_Be_Within_25_Of_Inaccuracy(
            String longitude1, String latitude1, String altitude1,
            String longitude2, String latitude2, String altitude2,
            String googleEarthDistance) {
        //GIVEN
        //The distance on the surface is about 2-3 meters
        double long1 = Double.parseDouble(longitude1);
        double lat1 = Double.parseDouble(latitude1);
        double alt1 = Double.parseDouble(altitude1);
        double long2 = Double.parseDouble(longitude2);
        double lat2 = Double.parseDouble(latitude2);
        double alt2 = Double.parseDouble(altitude2);
        //The distance by altitude according to Google Earth is from 40 to 100 meters
        double geDistance = Double.parseDouble(googleEarthDistance);

        double inaccuracy = 25.0; //meters

        //WHEN
        double haversineAltitudeDistanceMeters = thinOutPointsHandler.getHaversineDistance(
                long1, lat1, alt1,
                long2, lat2, alt2,
                DistanceUnits.METERS);

        System.out.println(haversineAltitudeDistanceMeters);

        //THEN
        boolean inaccuracyCondition = (geDistance - inaccuracy) <= haversineAltitudeDistanceMeters
                && (geDistance + inaccuracy) >= haversineAltitudeDistanceMeters;

        if (geDistance == 40.00) {
            assertTrue(inaccuracyCondition);
        } else if (geDistance == 100.0) {
            assertTrue(inaccuracyCondition);
        }
    }
}