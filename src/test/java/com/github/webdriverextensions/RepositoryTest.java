package com.github.webdriverextensions;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Getter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RepositoryTest {

    static Stream<Arguments> data() throws IOException {
        String json = Files.readString(Path.of("repository-3.0.json"));
        RepositoryJson repositoryJson = new Gson().fromJson(json, RepositoryJson.class);

        Driver[] drivers = repositoryJson.getDrivers();
        return Stream.of(drivers)
                .map(driver -> Arguments.of(driver.getName(), driver.getBit(), driver.getPlatform(), driver.getVersion(), driver.getUrl(), driver.getFileMatchInside()));
    }

    @ParameterizedTest(name = "{0} {1}bit {2} version{3}")
    @MethodSource("data")
    void test(final String name, final String bit, final String platform, final String version, final String url, final String fileMatchInside) throws IOException, InterruptedException {
        URI uri = URI.create(url);
        String fileName = Path.of(uri.getPath()).getFileName().toString();
        boolean isIEDriver = "internetexplorerdriver".equals(name);
        boolean isEdgeDriver = "edgedriver".equals(name);
        boolean isChromeBetaDriver = "chromedriver-beta".equals(name);
        if (isIEDriver) {
            assertThat(fileName)
                    .describedAs("url '" + url + "' should contain name 'IEDriverServer_'")
                    .matches("IEDriverServer_.*");
        } else if (isEdgeDriver) {
            assertThat(fileName)
                    .describedAs("url '" + url + "' should contain name 'MicrosoftWebDriver' or 'edgedriver'")
                    .matches("MicrosoftWebDriver.*|edgedriver.*");
        } else if (isChromeBetaDriver) {
            assertThat(fileName)
                    .describedAs("url '" + url + "' should contain 'chromedriver'")
                    .matches("chromedriver.*");
        } else {
            assertThat(fileName)
                    .describedAs("url '" + url + "' should contain name '" + name + "'")
                    .matches(name + "[_-].*");
        }

        assertThat(fileName)
                .describedAs("url '" + url + "' should contain address extension 'zip','tar.bz2','gz','exe'")
                .matches(".*(zip|tar\\.bz2|gz|exe)");

        if (fileMatchInside != null) {
            assertThat(fileMatchInside).isNotBlank();
            assertThatCode(() -> Pattern.compile(fileMatchInside))
                    .describedAs("fileMatchInside should be a valid regular expression")
                    .doesNotThrowAnyException();
        }

        String description = "url '" + url + "' is invalid";
        int expectedStatusCode = 200;
        final var requestBuilder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMinutes(1))
                .setHeader("User-Agent", "webdriverextensions-maven-plugin-repository tests")
                .method("HEAD", HttpRequest.BodyPublishers.noBody());
        final var clientBuilder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(5));

        // need to handle bitbucket.org different from others
        // it is hosted on s3, no HEAD and no HTTP/2 allowed
        if (uri.getHost().endsWith("bitbucket.org")) {
            requestBuilder.GET();
            clientBuilder.version(HttpClient.Version.HTTP_1_1);
        }

        final var response = clientBuilder.build().send(requestBuilder.build(), HttpResponse.BodyHandlers.discarding());
        assertThat(response.statusCode()).describedAs(description).isEqualTo(expectedStatusCode);
    }

    @Getter
    private static class RepositoryJson {

        private Driver[] drivers;
    }

    @Getter
    private static class Driver {

        private String name;
        private String platform;
        private String bit;
        private String version;
        private String url;
        private String fileMatchInside;
    }
}
