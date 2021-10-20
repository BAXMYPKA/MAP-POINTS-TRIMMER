package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ResourceLoader;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private MessageSource messageSource;
    private ResourceLoader resourceLoader;
    private FileService fileService;
    private final String CLASSPATH_TO_DIRECTORY = "classpath:static/pictograms";
    private final String PICTOGRAM_NAME_1 = "Pictogram1.png";
    private final String PICTOGRAM_NAME_2 = "Pictogram2.png";
    private final String NON_PICTOGRAM_NAME_1 = "ReadMe.txt";
    private final String NON_PICTOGRAM_NAME_2 = "image.jpg";

    @BeforeEach
    public void beforeEach() {
        messageSource = Mockito.mock(MessageSource.class);
        fileService = new FileService(messageSource);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "files/",
            "../myFiles/",
            "/storage/0/data/media/",
            "file:///D:/Folder/MyPOI/",
            "http://site/",
            "file:///C:/Users/Directory/PROGRAMS/CARTOGRAPHY/Locus/IMPORTED%20LOCUS%20POINTS/data-media-photo/",
            "file:///C:/Разные Users/A new Directory/MANY PROGRAMS/CARTOGRAPHY DIRECTORY/Locus/IMPORTED%20LOCUS%20POINTS/data-media-photo/"})
    public void only_Filename_Should_Be_Returned_Whet_GetFilename(String path) {
        //GIVEN
        final String pictureFilename = "Pic тест ture.png";
        String pathWithFilename = path + pictureFilename;

        //WHEN
        String fileName = fileService.getFileName(pathWithFilename);

        //THEN
        assertEquals(pictureFilename, fileName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"files/pic", "./myFiles/pic.j", "files/pic.", "pic.", "pic.jpegui"})
    public void empty_String_Should_Be_Returned_When_Filename_Not_Valid(String pathWithNotValidFilename) {
        //GIVEN

        //WHEN
        String fileName = fileService.getFileName(pathWithNotValidFilename);

        //THEN
        assertEquals("", fileName);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "files/",
            "../myFiles/",
            "/storage/0/data/media/",
            "file:///D:/Folder/MyPOI/",
            "http://site/",
            "file:///C:/Users/Directory/PROGRAMS/CARTOGRAPHY/Locus/IMPORTED%20LOCUS%20POINTS/data-media-photo/",
            "file:///C:/Разные Users/A new Directory/MANY PROGRAMS/CARTOGRAPHY DIRECTORY/Locus/IMPORTED%20LOCUS%20POINTS/data-media-photo/"})
    public void only_Path_Should_Be_Returned_When_GetPath(String path) {
        //GIVEN
        final String pictureFilename = "Pic тест.jpeg";
        String pathWithFilename = path + pictureFilename;

        //WHEN
        String onlyPath = fileService.getPath(pathWithFilename);

        //THEN
        assertEquals(path, onlyPath);
    }

    @ParameterizedTest
    @ValueSource(strings = {"files/pic", "./myFiles/pic.j", "files/pic.", "files pic.jpg", "pic.jp", "pic.jpegui"})
    public void empty_String_Should_Be_Returned_When_Path_With_Filename_Not_Valid(String pathWithNotValidFilename) {
        //GIVEN

        //WHEN
        String fileName = fileService.getPath(pathWithNotValidFilename);

        //THEN
        assertEquals("", fileName);
    }

    @Test
    public void pictogram_Names_Should_Be_Returned_When_GetPictogramNames() {
        //GIVEN

        //WHEN
        ArrayList<String> pictogramsNames = fileService.getPictogramsNames();

        //THEN
        assertEquals(2, pictogramsNames.size());
        assertTrue(pictogramsNames.contains(PICTOGRAM_NAME_1));
        assertTrue(pictogramsNames.contains(PICTOGRAM_NAME_2));
    }

    @Test
    public void only_Pictogram_Names_Should_Be_Returned_When_GetPictogramNames() {
        //GIVEN

        //WHEN
        ArrayList<String> pictogramsNames = fileService.getPictogramsNames();

        //THEN
        assertEquals(2, pictogramsNames.size());
        assertTrue(pictogramsNames.contains(PICTOGRAM_NAME_1));
        assertTrue(pictogramsNames.contains(PICTOGRAM_NAME_2));

        assertFalse(pictogramsNames.contains(NON_PICTOGRAM_NAME_1));
    }

    @Test
    public void pictogram_Names_Map_Should_Be_Returned_When_GetPictogramNamesMap() {
        //GIVEN

        //WHEN
        Map<String, String> pictogramsNamesWithPaths = fileService.getPictogramsNamesPaths();

        //THEN
        assertEquals(2, pictogramsNamesWithPaths.size());

        assertTrue(pictogramsNamesWithPaths.containsKey(PICTOGRAM_NAME_1));
        assertEquals("pictograms/" + PICTOGRAM_NAME_1, pictogramsNamesWithPaths.get(PICTOGRAM_NAME_1));

        assertTrue(pictogramsNamesWithPaths.containsKey(PICTOGRAM_NAME_2));
        assertEquals("pictograms/" + PICTOGRAM_NAME_2, pictogramsNamesWithPaths.get(PICTOGRAM_NAME_2));
    }

    @Test
    public void only_Pictogram_Names_Map_Should_Be_Returned_When_GetPictogramNamesMap() {
        //GIVEN

        //WHEN
        Map<String, String> pictogramsNamesWithPaths = fileService.getPictogramsNamesPaths();

        //THEN
        assertEquals(2, pictogramsNamesWithPaths.size());

        assertTrue(pictogramsNamesWithPaths.containsKey(PICTOGRAM_NAME_1));
        assertEquals("pictograms/" + PICTOGRAM_NAME_1, pictogramsNamesWithPaths.get(PICTOGRAM_NAME_1));

        assertTrue(pictogramsNamesWithPaths.containsKey(PICTOGRAM_NAME_2));
        assertEquals("pictograms/" + PICTOGRAM_NAME_2, pictogramsNamesWithPaths.get(PICTOGRAM_NAME_2));

        assertFalse(pictogramsNamesWithPaths.containsKey(NON_PICTOGRAM_NAME_2));
    }
}