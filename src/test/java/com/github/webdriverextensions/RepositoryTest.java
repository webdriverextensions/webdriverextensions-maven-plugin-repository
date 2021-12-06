package com.github.webdriverextensions;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.stream.Stream;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryTest {
    static Stream<Arguments> data() throws IOException {
        String json = FileUtils.readFileToString(new File("repository-3.0.json"), StandardCharsets.UTF_8);
        RepositoryJson repositoryJson = new Gson().fromJson(json, RepositoryJson.class);

        Driver[] drivers = repositoryJson.getDrivers();
        return Stream.of(drivers)
			.map(driver -> Arguments.of(driver.getName(), driver.getBit(), driver.getPlatform(), driver.getVersion(), driver.getUrl()));
    }

    @ParameterizedTest(name = "{0} {1}bit {2} version{3}")
	@MethodSource("data")
    void test(final String name, final String bit, final String platform, final String version, final String url) throws IOException {
        URI uri = URI.create(url);
        String fileName = Paths.get(uri.getPath()).getFileName().toString();
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

        String description = "url '" + url + "' is invalid";
        Request request = new Request.Builder().head().url(url).build();
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            if ("phantomjs".equals(name)) {
                // need to handle phantomjs different from others
                // it is hosted on s3, no HEAD allowed
                assertThat(response.code()).describedAs(description).isEqualTo(403);
            } else {
                assertThat(response.code()).describedAs(description).isEqualTo(200);
            }
        }
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
    }
}
