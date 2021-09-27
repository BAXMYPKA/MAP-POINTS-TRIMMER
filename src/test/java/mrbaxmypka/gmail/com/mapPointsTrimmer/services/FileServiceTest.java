package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private MessageSource messageSource;
    private ResourceLoader resourceLoader;
    private FileService fileService;
    private Resource resource;
    private final String CLASSPATH_TO_DIRECTORY = "classpath:static/pictograms";
    
    @BeforeEach
    public void beforeEach() throws IOException {
        messageSource = Mockito.mock(MessageSource.class);
        resourceLoader = Mockito.mock(ResourceLoader.class);
        resource = Mockito.mock(Resource.class);
        Mockito.when(resourceLoader.getResource(CLASSPATH_TO_DIRECTORY)).thenReturn(resource);
        Mockito.when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("Pictogram1.png".getBytes(StandardCharsets.UTF_8)));
        fileService = new FileService(messageSource, resourceLoader);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"files/", "../myFiles/", "/storage/0/data/media/", "file:///D:/Folder/MyPOI/", "http://site/"})
    public void only_Filename_Should_Be_Returned_Whet_GetFilename(String path) {
        //GIVEN
        final String pictureFilename = "Pic ture.png";
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
    @ValueSource(strings = {"files/", "../myFiles/", "/storage/0/data/media/", "file:///D:/Folder/MyPOI/", "http://site/"})
    public void only_Path_Should_Be_Returned_When_GetPath(String path) {
        //GIVEN
        final String pictureFilename = "Pic.jpeg";
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
    public void pictogram_Names_Should_Be_Returned_When_GetPictogramNames() throws IOException {
        //GIVEN
        final String PICTOGRAMS_IN_DIRECTORY = "pic1.png\npic2.PNG";
        InputStream inputStream = new ByteArrayInputStream(PICTOGRAMS_IN_DIRECTORY.getBytes(StandardCharsets.UTF_8));
        Mockito.when(resource.getInputStream()).thenReturn(inputStream);
        fileService = new FileService(messageSource, resourceLoader);
    
        //WHEN
        ArrayList<String> pictogramsNames = fileService.getPictogramsNames();

        //THEN
        assertEquals(2, pictogramsNames.size());
        assertTrue(pictogramsNames.contains("pic1.png"));
        assertTrue(pictogramsNames.contains("pic2.PNG"));
    }

    @Test
    public void only_Pictogram_Names_Should_Be_Returned_When_GetPictogramNames() throws IOException {
        //GIVEN
        final String PICTOGRAMS_TEXT_IN_DIRECTORY = "pic1.PNG\nreadMe.txt\npic2.png";
        InputStream inputStream = new ByteArrayInputStream(PICTOGRAMS_TEXT_IN_DIRECTORY.getBytes(StandardCharsets.UTF_8));
        Mockito.when(resource.getInputStream()).thenReturn(inputStream);

        fileService = new FileService(messageSource, resourceLoader);
    
        //WHEN
        ArrayList<String> pictogramsNames = fileService.getPictogramsNames();

        //THEN
        assertEquals(2, pictogramsNames.size());
        assertTrue(pictogramsNames.contains("pic1.PNG"));
        assertTrue(pictogramsNames.contains("pic2.png"));

        assertFalse(pictogramsNames.contains("readMe.txt"));
    }

    @Test
    public void empty_List_Should_Be_Returned_When_No_Pictograms_In_Directory() throws IOException {
        //GIVEN
        final String PICTOGRAMS_IN_DIRECTORY = "wrongFile.jpg";
        InputStream inputStream = new ByteArrayInputStream(PICTOGRAMS_IN_DIRECTORY.getBytes(StandardCharsets.UTF_8));
        Mockito.when(resource.getInputStream()).thenReturn(inputStream);
    
        fileService = new FileService(messageSource, resourceLoader);
    
        //WHEN
        ArrayList<String> pictogramsNames = fileService.getPictogramsNames();

        //THEN
        assertEquals(0, pictogramsNames.size());
    }

    @Test
    public void pictogram_Names_Map_Should_Be_Returned_When_GetPictogramNamesMap() throws IOException {
        //GIVEN
        final String PICTOGRAMS_IN_DIRECTORY = "pic1.png\npic2.PNG";
        InputStream inputStream = new ByteArrayInputStream(PICTOGRAMS_IN_DIRECTORY.getBytes(StandardCharsets.UTF_8));
        Mockito.when(resource.getInputStream()).thenReturn(inputStream);
        fileService = new FileService(messageSource, resourceLoader);
    
        //WHEN
        Map<String, String> pictogramsNamesWithPaths = fileService.getPictogramsNamesPaths();

        //THEN
        assertEquals(2, pictogramsNamesWithPaths.size());

        assertTrue(pictogramsNamesWithPaths.containsKey("pic1.png"));
        assertEquals("pictograms/pic1.png", pictogramsNamesWithPaths.get("pic1.png"));

        assertTrue(pictogramsNamesWithPaths.containsKey("pic2.PNG"));
        assertEquals("pictograms/pic2.PNG", pictogramsNamesWithPaths.get("pic2.PNG"));
    }

    @Test
    public void only_Pictogram_Names_Map_Should_Be_Returned_When_GetPictogramNamesMap() throws IOException {
        //GIVEN
        final String PICTOGRAMS_IN_DIRECTORY = "pic1.png\npic2.PNG\npic3.jpg";
        InputStream inputStream = new ByteArrayInputStream(PICTOGRAMS_IN_DIRECTORY.getBytes(StandardCharsets.UTF_8));
        Mockito.when(resource.getInputStream()).thenReturn(inputStream);
    
        fileService = new FileService(messageSource, resourceLoader);
    
        //WHEN
        Map<String, String> pictogramsNamesWithPaths = fileService.getPictogramsNamesPaths();

        //THEN
        assertEquals(2, pictogramsNamesWithPaths.size());

        assertTrue(pictogramsNamesWithPaths.containsKey("pic1.png"));
        assertEquals("pictograms/pic1.png", pictogramsNamesWithPaths.get("pic1.png"));

        assertTrue(pictogramsNamesWithPaths.containsKey("pic2.PNG"));
        assertEquals("pictograms/pic2.PNG", pictogramsNamesWithPaths.get("pic2.PNG"));

        assertFalse(pictogramsNamesWithPaths.containsKey("pic3.jpg"));
    }

}