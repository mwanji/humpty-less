package co.mewf.humpty.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.spi.processors.SourceProcessor.CompilationResult;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

/**
 * Builds bootstrap.css from LESS files.
 *
 * Bootstrap's variables can be overridden by putting an asset called bootstrap.less.variables in a {@link Bundle}. It can be in any folder, as long as the name matches.
 *
 * Options:
 * responsive: if false, the responsive CSS is not included. Defaults to true.
 * fontAwesome: indicates that you are using FontAwesome and lets you configure it in bootstrap.less.variables. Defaults to true if the FontAwesome WebJar is on the classpath. Defaults to false otherwise.
 */
public class BootstrapLessSourceProcessor {

  private static final LessCompiler lessCompiler = new DefaultLessCompiler();

  private WebJarAssetLocator locator;
  private BootstrapLessConfiguration configuration;

  public String getName() {
    return "bootstrapLess";
  }
  
  public boolean accepts(String uri) {
    return FilenameUtils.getName(uri).equals("bootstrap.less.variables");
  }

  public CompilationResult compile(String assetName, Reader rawOverrides, PreProcessorContext context) {
    try {
      File destDir = File.createTempFile("humptyBootstrapLess", "");
      destDir.delete();
      destDir.mkdir();

      String[] path = locator.listAssets("/bootstrap/").iterator().next().split("/");
      String version = path[path.length - 3];
      Set<String> assets = locator.listAssets("/bootstrap/" + version + "/less/");
      for (String asset : assets) {
        File file = new File(destDir, FilenameUtils.getName(asset));

        Reader input = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + asset)));
        FileWriter output = new FileWriter(file);
        IOUtils.copy(input, output);
        input.close();
        output.close();
      }

      File bootstrapLess = new File(destDir, "bootstrap.less");
      File responsiveLess = new File(destDir, "responsive.less");
      File variablesLess = new File(destDir, "variables.less");
      List<String> variables = FileUtils.readLines(variablesLess);

      List<String> overrides = IOUtils.readLines(rawOverrides);
      HashMap<String, String> overriddenVariables = new HashMap<String, String>();
      for (String override : overrides) {
        String[] split = override.split(":");
        overriddenVariables.put(split[0].trim(), split[1].trim());
      }

      for (int i = 0; i < variables.size(); i++) {
        String variable = variables.get(i);
        if (variable.startsWith("@")) {
          String key = variable.substring(0, variable.indexOf(':'));
          if (overriddenVariables.containsKey(key)) {
            variables.set(i, key + ": " + overriddenVariables.get(key));
          }
          if ("@iconSpritePath".equals(key)) {
            variables.set(i, key + ": \"/webjars/bootstrap/" + version + "/img/glyphicons-halflings.png\";");
          }
          if ("@iconWhiteSpritePath".equals(key)) {
            variables.set(i, key + ": \"/webjars/bootstrap/" + version + "/img/glyphicons-halflings-white.png\";");
          }
        }
      }

      FileUtils.writeLines(variablesLess, variables);

      if (configuration.isUsingFontAwesome(locator)) {
        File fontAwesomeDir = new File(destDir, "fontAwesome");
        fontAwesomeDir.mkdir();
        Set<String> fontAwesomeAssets = locator.listAssets("/font-awesome/");
        for (String asset : fontAwesomeAssets) {
          if (asset.endsWith(".less")) {
            File file = new File(fontAwesomeDir, FilenameUtils.getName(asset));
            Reader input = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + asset)));
            FileWriter output = new FileWriter(file);
            IOUtils.copy(input, output);
            input.close();
            output.close();

            if (asset.endsWith("variables.less")) {
              List<String> fontAwesomeVariables = FileUtils.readLines(file);
              for (int i = 0; i < fontAwesomeVariables.size(); i++) {
                String variable = fontAwesomeVariables.get(i);
                if (variable.startsWith("@")) {
                  String key = variable.substring(0, variable.indexOf(':'));
                  if (overriddenVariables.containsKey(key)) {
                    fontAwesomeVariables.set(i, key + ": " + overriddenVariables.get(key));
                  }
                  if (key.equals("@FontAwesomePath")) {
                    String fontUrl = locator.getFullPath("fontawesome-webfont.eot").substring("META-INF/resources".length());
                    String fontDirUrl = fontUrl.substring(0, fontUrl.lastIndexOf('/'));
                    fontAwesomeVariables.set(i, key + ": \"" + fontDirUrl + "\";");
                  }
                }
              }
              FileUtils.writeLines(file, fontAwesomeVariables);
            }
          }
        }

        String bootstrapLessString = FileUtils.readFileToString(bootstrapLess);
        bootstrapLessString = bootstrapLessString.replace("@import \"sprites.less\";", "@import \"fontAwesome/font-awesome.less\";");
        FileUtils.write(bootstrapLess, bootstrapLessString);
      }

      String bootstrapString = lessCompiler.compile(bootstrapLess).getCss();
      StringBuilder stringBuilder = new StringBuilder(bootstrapString);

      if (configuration.isResponsive()) {
        String responsiveString = lessCompiler.compile(responsiveLess).getCss();
        stringBuilder.append(responsiveString);
      }
      FileUtils.deleteDirectory(destDir);

      return new CompilationResult("bootstrap.css", stringBuilder.toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Inject
  public void init(WebJarAssetLocator locator, Configuration.Options options) {
    this.locator = locator;
    this.configuration = new BootstrapLessConfiguration(options);
  }
}
