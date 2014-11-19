package co.mewf.humpty.bootstrap;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.Configuration;

public class BootstrapLessConfiguration {

  private static final String FONT_AWESOME = "fontAwesome";
  private static final String RESPONSIVE = "responsive";

  private Boolean responsive = TRUE;
  private Boolean fontAwesome = null;

  public BootstrapLessConfiguration() {}

  public BootstrapLessConfiguration nonResponsive() {
    this.responsive = FALSE;
    return this;
  }

  public BootstrapLessConfiguration useFontAwesome() {
    this.fontAwesome = TRUE;
    return this;
  }

  BootstrapLessConfiguration(Configuration.Options configuration) {
    this.responsive = configuration.get(RESPONSIVE, TRUE);
    this.fontAwesome = configuration.get(FONT_AWESOME, null);
  }

  boolean isResponsive() {
    return responsive;
  }

  boolean isUsingFontAwesome(WebJarAssetLocator locator) {
    if (Boolean.FALSE.equals(fontAwesome)) {
      return false;
    }

    return !locator.listAssets("/font-awesome").isEmpty();
  }
}
