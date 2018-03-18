package com.medallia.merci.core.filesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;

/**
 * Unit tests for {@link FilesystemConfigurationFetcher}.
 */
public class FilesystemConfigurationFetcherTest {

    private static final String BASE_PATH = "/configurations";

    private static final String APPLICATION = "myapp";

    private static final String FILE_ONE = "/first-featureflags.json";

    private static final String FILE_TWO = "/second-featureflags.json";

    private static final  String FILE_ONE_CONTENT =
            "{\n" +
            "  \"feature-flags\" : {\n" +
            "    \"enable-none\" : {\n" +
            "      \"value\" : false\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final  String FILE_TWO_CONTENT =
            "{\n" +
                    "  \"feature-flags\" : {\n" +
                    "    \"enable-all\" : {\n" +
                    "      \"value\" : true\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

    private final ConfigurationFetcherMetrics metrics = new ConfigurationFetcherMetrics();

    @Test
    public void testFetchReturnsContentFromSingleFile() throws IOException {
        FileSystem fileSystem = Mockito.mock(FileSystem.class);
        FileSystemProvider provider = Mockito.mock(FileSystemProvider.class);
        Path path = Mockito.mock(Path.class);
        Mockito.doReturn(path).when(fileSystem).getPath(Mockito.anyString());
        Mockito.doReturn(provider).when(fileSystem).provider();
        ByteArrayInputStream fileContent = new ByteArrayInputStream(FILE_ONE_CONTENT.getBytes(StandardCharsets.UTF_8));
        Mockito.doReturn(fileContent).when(provider).newInputStream(Mockito.any(Path.class), Mockito.any(StandardOpenOption.class));

        boolean skipMissingFiles = true;
        FilesystemConfigurationFetcher fetcher = new FilesystemConfigurationFetcher(fileSystem, BASE_PATH, skipMissingFiles, metrics);
        Map<String, String> content = fetcher.fetch(ImmutableList.of(FILE_ONE), APPLICATION);

        ArgumentCaptor<String> pathStringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<OpenOption> openOptionCaptor = ArgumentCaptor.forClass(OpenOption.class);
        Mockito.verify(fileSystem, Mockito.times(1)).getPath(pathStringCaptor.capture());
        Mockito.verify(provider, Mockito.times(1)).newInputStream(pathCaptor.capture(), openOptionCaptor.capture());
        Assert.assertEquals(BASE_PATH + "/" + APPLICATION + FILE_ONE, pathStringCaptor.getValue());
        Assert.assertEquals(StandardOpenOption.READ, openOptionCaptor.getValue());
        Assert.assertSame(path, pathCaptor.getValue());
        Assert.assertEquals(ImmutableMap.of(FILE_ONE, FILE_ONE_CONTENT), content);
        Assert.assertEquals(1, metrics.getRequests());
        Assert.assertEquals(0, metrics.getFailures());
    }

    @Test
    public void testFetchReturnsContentFromMultipleFiles() throws IOException {
        FileSystem fileSystem = Mockito.mock(FileSystem.class);
        FileSystemProvider provider = Mockito.mock(FileSystemProvider.class);
        Path pathOne = Mockito.mock(Path.class);
        Path pathTwo = Mockito.mock(Path.class);
        Mockito.doReturn(pathOne).doReturn(pathTwo).when(fileSystem).getPath(Mockito.anyString());
        Mockito.doReturn(provider).when(fileSystem).provider();
        ByteArrayInputStream firstFileContent = new ByteArrayInputStream(FILE_ONE_CONTENT.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream secondFileContent = new ByteArrayInputStream(FILE_TWO_CONTENT.getBytes(StandardCharsets.UTF_8));
        Mockito.doReturn(firstFileContent).doReturn(secondFileContent).when(provider).newInputStream(Mockito.any(Path.class), Mockito.any(StandardOpenOption.class));

        boolean skipMissingFiles = true;
        FilesystemConfigurationFetcher fetcher = new FilesystemConfigurationFetcher(fileSystem, BASE_PATH, skipMissingFiles, metrics);
        Map<String, String> content = fetcher.fetch(ImmutableList.of(FILE_ONE, FILE_TWO), APPLICATION);

        ArgumentCaptor<String> pathStringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<OpenOption> openOptionCaptor = ArgumentCaptor.forClass(OpenOption.class);
        Mockito.verify(fileSystem, Mockito.times(2)).getPath(pathStringCaptor.capture());
        Mockito.verify(provider, Mockito.times(2)).newInputStream(pathCaptor.capture(), openOptionCaptor.capture());
        Assert.assertEquals(BASE_PATH + "/" + APPLICATION + FILE_ONE, pathStringCaptor.getAllValues().get(0));
        Assert.assertEquals(BASE_PATH + "/" + APPLICATION + FILE_TWO, pathStringCaptor.getAllValues().get(1));
        Assert.assertEquals(ImmutableList.of(StandardOpenOption.READ, StandardOpenOption.READ), openOptionCaptor.getAllValues());
        Assert.assertSame(pathOne, pathCaptor.getAllValues().get(0));
        Assert.assertSame(pathTwo, pathCaptor.getAllValues().get(1));
        Assert.assertEquals(ImmutableMap.of(FILE_ONE, FILE_ONE_CONTENT, FILE_TWO, FILE_TWO_CONTENT), content);
        Assert.assertEquals(1, metrics.getRequests());
        Assert.assertEquals(0, metrics.getFailures());
    }

    @Test
    public void testFetchReturnsContentFromSecondFileIfFirstIsMissing() throws IOException {
        boolean skipMissingFiles = true;
        FileSystem fileSystem = Mockito.mock(FileSystem.class);
        FileSystemProvider provider = Mockito.mock(FileSystemProvider.class);
        Path pathOne = Mockito.mock(Path.class);
        Path pathTwo = Mockito.mock(Path.class);
        Mockito.doReturn(pathOne).doReturn(pathTwo).when(fileSystem).getPath(Mockito.anyString());
        Mockito.doReturn(provider).when(fileSystem).provider();
        ByteArrayInputStream secondFileContent = new ByteArrayInputStream(FILE_TWO_CONTENT.getBytes(StandardCharsets.UTF_8));
        Mockito.doThrow(new NoSuchFileException("Could not find.")).doReturn(secondFileContent)
                .when(provider).newInputStream(Mockito.any(Path.class), Mockito.any(StandardOpenOption.class));

        FilesystemConfigurationFetcher fetcher = new FilesystemConfigurationFetcher(fileSystem, BASE_PATH, skipMissingFiles, metrics);
        Map<String, String> content = fetcher.fetch(ImmutableList.of(FILE_ONE, FILE_TWO), APPLICATION);

        ArgumentCaptor<String> pathStringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<OpenOption> openOptionCaptor = ArgumentCaptor.forClass(OpenOption.class);
        Mockito.verify(fileSystem, Mockito.times(2)).getPath(pathStringCaptor.capture());
        Mockito.verify(provider, Mockito.times(2)).newInputStream(pathCaptor.capture(), openOptionCaptor.capture());
        Assert.assertEquals(BASE_PATH + "/" + APPLICATION + FILE_ONE, pathStringCaptor.getAllValues().get(0));
        Assert.assertEquals(BASE_PATH + "/" + APPLICATION + FILE_TWO, pathStringCaptor.getAllValues().get(1));
        Assert.assertEquals(ImmutableList.of(StandardOpenOption.READ, StandardOpenOption.READ), openOptionCaptor.getAllValues());
        Assert.assertSame(pathOne, pathCaptor.getAllValues().get(0));
        Assert.assertSame(pathTwo, pathCaptor.getAllValues().get(1));
        Assert.assertEquals(ImmutableMap.of(FILE_TWO, FILE_TWO_CONTENT), content);
        Assert.assertEquals(1, metrics.getRequests());
        Assert.assertEquals(0, metrics.getFailures());
    }

    @Test
    public void testFetchThrowsNoSuchFileExceptionIfFileIsMissing() throws IOException {
        boolean skipMissingFiles = false;
        FileSystem fileSystem = Mockito.mock(FileSystem.class);
        FileSystemProvider provider = Mockito.mock(FileSystemProvider.class);
        Path path = Mockito.mock(Path.class);
        Mockito.doReturn(path).when(fileSystem).getPath(Mockito.anyString());
        Mockito.doReturn(provider).when(fileSystem).provider();
        NoSuchFileException cause = new NoSuchFileException("Could not find.");
        Mockito.doThrow(cause).when(provider).newInputStream(Mockito.any(Path.class), Mockito.any(StandardOpenOption.class));

        FilesystemConfigurationFetcher fetcher = new FilesystemConfigurationFetcher(fileSystem, BASE_PATH, skipMissingFiles, metrics);
        try {
            fetcher.fetch(ImmutableList.of(FILE_ONE, FILE_TWO), APPLICATION);
        } catch (NoSuchFileException exception) {
            Assert.assertSame(exception, cause);
        }

        ArgumentCaptor<String> pathStringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<OpenOption> openOptionCaptor = ArgumentCaptor.forClass(OpenOption.class);
        Mockito.verify(fileSystem, Mockito.times(1)).getPath(pathStringCaptor.capture());
        Mockito.verify(provider, Mockito.times(1)).newInputStream(pathCaptor.capture(), openOptionCaptor.capture());
        Assert.assertEquals(BASE_PATH + "/" + APPLICATION + FILE_ONE, pathStringCaptor.getValue());
        Assert.assertEquals(StandardOpenOption.READ, openOptionCaptor.getValue());
        Assert.assertSame(path, pathCaptor.getAllValues().get(0));
        Assert.assertEquals(1, metrics.getRequests());
        Assert.assertEquals(1, metrics.getFailures());
    }
}
