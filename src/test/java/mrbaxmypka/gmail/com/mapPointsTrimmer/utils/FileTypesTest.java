package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class FileTypesTest {
	
	@ParameterizedTest
	@ValueSource(strings = {"TeSt.KML", "test.kml", "test_Again.kMl", "t.e.s-t.ing.kmL"})
	public void filename_With_Correct_Extension_Kml_Should_Return_True(String filename) {
		//GIVEN
		//WHEN
		boolean sameExtension = FileTypes.KML.isSameExtension(filename);
		
		//THEN
		assertTrue(sameExtension);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"TeSt.KM", "test.ml", "test_Again,kMl", "t.e.s-t.ingkmL", "testkml"})
	public void filename_With_Incorrect_Extension_Kml_Should_Return_False(String filename) {
		//GIVEN
		//WHEN
		boolean sameExtension = FileTypes.KML.isSameExtension(filename);
		
		//THEN
		assertFalse(sameExtension);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"TeSt.KMZ", "test.kmz", "test_Again.kMz", "t.e.s-t.ing.kmZ"})
	public void filename_With_Correct_Extension_Kmz_Should_Return_True(String filename) {
		//GIVEN
		//WHEN
		boolean sameExtension = FileTypes.KMZ.isSameExtension(filename);
		
		//THEN
		assertTrue(sameExtension);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"TeSt.KM", "test.mz", "test_Again,kMz", "t.e.s-t.ingkmZ", "testkmz"})
	public void filename_With_Incorrect_Extension_Kmz_Should_Return_False(String filename) {
		//GIVEN
		//WHEN
		boolean sameExtension = FileTypes.KML.isSameExtension(filename);
		
		//THEN
		assertFalse(sameExtension);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"KML", "kml", "kMl", "KMl"})
	public void any_Correct_Value_Should_Return_Correct_FileTypes(String value) {
		//GIVEN
		//WHEN
		FileTypes kml = FileTypes.getValue(value);
		
		//THEN
		assertEquals(FileTypes.KML, kml);
	}
	
}