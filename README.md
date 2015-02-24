# humpty-less

Compiles LESS files in the [humpty](https://github.com/mwanji/humpty) pipeline.

## Installation

Requires Java 8 and Maven.

Add the following dependency to your POM:

````xml
<dependency>
  <groupId>co.mewf.humpty</groupId>
  <artifactId>humpty-less</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
````

## Usage

Add LESS files to humpty bundles:

````toml
"styles.css" = ["app.less"]
````

### Imports

Imports are resolved relative to the file they are declared in. If it cannot be resolved, then it is considered to be pointing at a WebJar, as in the following example.

### Bootstrap Example

To integrate Bootstrap and use custom values for its variables, you could have the following in the above-mentioned app.less, (if it is in `src/main/resources/assets`):

````less
@import "bootstrap/3.3.1/less/bootstrap.less";

@gray-base: #111;
@link-color: @brand-danger;
````

and so on for the rest of your variables.
