package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class MultipartDtoTest {
	
	private MultipartDto multipartDto;
	
	/**
	 * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
	 * Where 1.0 is the scale of default window font.
	 * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
	 * 10% is "scale = '0.1'",
	 * 90% is "scale = '0.9'" etc.
	 */
	@ParameterizedTest
	@ValueSource(ints = {0, 7, 15, 37, 51, 99, 100, 106, 116, 213, 295})
	public void percentage_Integer_Input_from_0_to_213_Should_Be_Returned_As_Scale_With_0_1_Step(int percentage) {
		//GIVEN User input as a percentage
		multipartDto = new MultipartDto(new MockMultipartFile("Name", (byte[]) null));
		
		//WHEN it has being transformed into scale
		multipartDto.setPointIconSize(percentage);
		multipartDto.setPointTextSize(percentage);
		
		//THEN the result has to be from 0.0 to 2.1 with the step equals 0.1
		System.out.println("icon scale = " + multipartDto.getPointIconSizeScaled());
		System.out.print("text scale = " + multipartDto.getPointTextSizeScaled());
		
		Pattern pattern = Pattern.compile("[\\d]\\.[\\d]");
		Matcher matcherIconSize = pattern.matcher(multipartDto.getPointIconSizeScaled().toString());
		Matcher matcherIconTextSize = pattern.matcher(multipartDto.getPointTextSizeScaled().toString());
		
		assertTrue(matcherIconSize.matches());
		assertTrue(matcherIconTextSize.matches());
	}
	
	/**
	 * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
	 * Where 1.0 is the scale of default window font.
	 * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
	 * 10% is "scale = '0.1'",
	 * 90% is "scale = '0.9'" etc.
	 */
	@ParameterizedTest
	@ValueSource(doubles = {0.0, 0.1, 0.5, 0.9, 1.0, 2.2})
	public void essential_Scale_Input_As_Double_Should_Return_Correct_Percentage(double doubleVal) {
		//GIVEN User input as a percentage
		multipartDto = new MultipartDto(new MockMultipartFile("Name", (byte[]) null));
		
		
		//WHEN it has being transformed into scale
		multipartDto.setPointIconSizeScaled(doubleVal);
		multipartDto.setPointTextSizeScaled(doubleVal);
		
		//THEN the result has to be from 0(%) to 220(%)
		System.out.println("icon percent = " + multipartDto.getPointIconSize());
		System.out.print("text percent = " + multipartDto.getPointTextSize());
		
		Pattern pattern = Pattern.compile("\\d{1,3}");
		Matcher matcherIconSize = pattern.matcher(multipartDto.getPointIconSize().toString());
		Matcher matcherIconTextSize = pattern.matcher(multipartDto.getPointTextSize().toString());
		
		assertTrue(matcherIconSize.matches());
		assertTrue(matcherIconTextSize.matches());
	}
	
	/**
	 * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
	 * Where 1.0 is the scale of default window font.
	 * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
	 * 10% is "scale = '0.1'",
	 * 90% is "scale = '0.9'" etc.
	 */
	@Test
	public void percentage_Integer_Above_300_Should_Throw_NumberFormatException() {
		//GIVEN User input as a percentage above threshold
		Integer illegalPercentage = 301;
		multipartDto = new MultipartDto(new MockMultipartFile("Name", (byte[]) null));
		
		//WHEN
		
		//THEN
		NumberFormatException iconSizeException = assertThrows(NumberFormatException.class,
			  () -> multipartDto.setPointIconSize(illegalPercentage));
		NumberFormatException textSizeException = assertThrows(NumberFormatException.class,
			  () -> multipartDto.setPointTextSize(illegalPercentage));
		
		assertEquals("Point Icon Size cannot exceed 300%!", iconSizeException.getMessage());
		assertEquals("Point Text Size cannot exceed 300%!", textSizeException.getMessage());
		
	}
	
	/**
	 * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
	 * Where 1.0 is the scale of default window font.
	 * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
	 * 10% is "scale = '0.1'",
	 * 90% is "scale = '0.9'" etc.
	 */
	@ParameterizedTest
	@ValueSource(doubles = {-1.1, -0.0, 0.11, 1.23, 3.05})
	public void scale_as_Incorrect_Double_Should_Throw_NumberFormatException(Double incorrectScale) {
		//GIVEN User input as a percentage above threshold
		multipartDto = new MultipartDto(new MockMultipartFile("Name", (byte[]) null));
		
		//WHEN
		//THEN
		NumberFormatException iconSizeException = assertThrows(NumberFormatException.class,
			  () -> multipartDto.setPointIconSizeScaled(incorrectScale));
		NumberFormatException textSizeException = assertThrows(NumberFormatException.class,
			  () -> multipartDto.setPointTextSizeScaled(incorrectScale));
		
		assertEquals("Scale has to be represented as value from 0.0 to 3.0 with the step of 0.1", iconSizeException.getMessage());
		assertEquals("Scale has to be represented as value from 0.0 to 3.0 with the step of 0.1", textSizeException.getMessage());
		
	}
	
}