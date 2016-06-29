package com.github.webdriverextensions;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class RepositoryTest {
    @Parameterized.Parameters(name = "{0} {1}bit {2} version{3}")
    public static Collection<Object[]> data() throws IOException {
        String json = FileUtils.readFileToString(new File("repository-3.0.json"));
        RepositoryJson repositoryJson = new Gson().fromJson(json, RepositoryJson.class);

        Driver[] drivers = repositoryJson.getDrivers();
        return Lists.newArrayList(drivers)
                    .stream()
                    .map(driver->new Object[]{driver.getName(), driver.getBit(), driver.getPlatform(), driver.getVersion(), driver.getUrl()})
                    .collect(Collectors.toList());
    }

    private final String name;
    private final String bit;
    private final String platform;
    private final String version;
    private final String url;

    @Test
    public void test() throws Exception {
        URI uri = URI.create(url);
        String fileName = Paths.get(uri.getPath()).getFileName().toString();
        boolean isIEDriver = "internetexplorerdriver".equals(name);
        boolean isEdgeDriver = "edgedriver".equals(name);
        if (isIEDriver) {
            assertThat(fileName)
                    .describedAs("url '" + url + "' should contain name 'IEDriverServer_'")
                    .matches("IEDriverServer_.*");
        } else if (isEdgeDriver) {
            assertThat(fileName)
                    .describedAs("url '" + url + "' should contain name 'MicrosoftWebDriver'")
                    .matches("MicrosoftWebDriver.*");
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
        Response response = new OkHttpClient().newCall(request).execute();
        if ("phantomjs".equals(name)) {
            // need to handle phantomjs different from others
            // it is hosted on s3, no HEAD allowed
            assertThat(response.code()).describedAs(description).isEqualTo(403);
        } else if("geckodriver".equals(name)) {
            // need to handle geckodriver different from others
            // it is hosted on s3, no HEAD allowed
            assertThat(response.code()).describedAs(description).isEqualTo(403);
        } else {
            assertThat(response.code()).describedAs(description).isEqualTo(200);
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
