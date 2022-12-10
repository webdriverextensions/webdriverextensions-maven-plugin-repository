![Plugin Repository CI](https://github.com/webdriverextensions/webdriverextensions-maven-plugin-repository/workflows/Plugin%20Repository%20CI/badge.svg)

WebDriver Extensions Maven Plugin Repository 3.0
===================
This is the home of the drivers repository used by the [WebDriver Extensions Maven Plugin](https://github.com/webdriverextensions/webdriverextensions-maven-plugin) for version 1.X.X and and 3.X.X and later.

## Want to add a driver version to the repo?
Simply just fork this repo, add the driver info in the [repository-3.0.json](https://github.com/webdriverextensions/webdriverextensions-maven-plugin-repository/blob/master/repository-3.0.json) file and do a PR. 
No need to add the drivers to repository.json since that repo is no longer maintained.

### How does an entry of a driver look like?
The repository JSON must be validated against [the driver schema](https://github.com/webdriverextensions/webdriverextensions-maven-plugin/blob/master/drivers-schema.json). Each driver entry is an object with the following properties (*the ones in bold are required*).

- **`name`**: the name of the driver. one of "chromedriver", "edgedriver", "geckodriver", "internetexplorerdriver", "operadriver" or "phantomjs"
- **`platform`**: the operating system. one of "linux", "windows" or "mac"
- **`bit`**: "64" for 64-bit or "32" for 32-bit
- **`version`**: the actual version of the driver
- **`url`**: the URL where the driver can be downloaded. compressed files and archives of the following types are supported: `.gz`, `.bz`, `.tar`, `.tar.gz`, `.tar.bz`, `.zip`
- `arch`: driver os/cpu architecture. one of "x86", "amd64", "aarch64" or "ia64"
- `fileMatchInside`: a regular expression to select only the files that match the specified pattern
- `customFileName`: a custom name for the driver file in the installation directory

**Example**
```json
{
    "name": "edgedriver",
    "platform": "windows",
    "bit": "64",
    "arch": "amd64",
    "version": "108.0.1462.38",
    "fileMatchInside": "^msedgedriver\\.exe$",
    "url": "https://msedgedriver.azureedge.net/108.0.1462.38/edgedriver_win64.zip"
}
```

#### A note about the `arch` property
If there is only one variant of a driver for a given platform and bit, the property can be omitted.

If there is more than one variant of a driver for a certain platform and bit, e.g. edgedriver for Windows 64-bit and the architectures "amd64" and "aarch64" (M1), then both entries must contain the `arch` property and the "amd64" variant must be first!

**Example**
```json
{
...
	{
	    "name": "edgedriver",
	    "platform": "windows",
	    "bit": "64",
	    "arch": "amd64",
	    "version": "108.0.1462.38",
	    "fileMatchInside": "^msedgedriver\\.exe$",
	    "url": "https://msedgedriver.azureedge.net/108.0.1462.38/edgedriver_win64.zip"
	},
	{
	    "name": "edgedriver",
	    "platform": "windows",
	    "bit": "64",
	    "arch": "aarch64",
	    "version": "108.0.1462.38",
	    "fileMatchInside": "^msedgedriver\\.exe$",
	    "url": "https://msedgedriver.azureedge.net/108.0.1462.42/edgedriver_arm64.zip"
	}
...
}
```

## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
