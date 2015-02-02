package co.mewf.humpty.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.HumptyBootstrap;

public class LessSourceProcessorTest {

  @Test
  public void should_accept_less_file() {
    LessSourceProcessor processor = new LessSourceProcessor();
    
    assertTrue(processor.accepts("bootstrap.less"));
    assertFalse(processor.accepts("b-v.less.variables"));
  }
  
  @Test
  public void should_compile_vanilla_bootstrap() throws Exception {
    Pipeline pipeline = pipeline("humpty.toml");
    Pipeline.Output output = pipeline.process("bootstrapVanilla.css");

    String expected = IOUtils.toString(getClass().getResourceAsStream("should_compile_vanilla_bootstrap.css"));
    
    assertEquals(expected, output.getAsset());
  }

  @Test
  public void should_compile_bootstrap_with_custom_variables() throws IOException {
    Pipeline pipeline = pipeline("humpty.toml");
    Pipeline.Output output = pipeline.process("bootstrapCustom.css");

    assertTrue("Did not replace @icon-font-name", output.getAsset().contains("icon-font-name"));
  }
  
  @Test
  public void should_compile_file_in_assets_dir() throws Exception {
    String css = new HumptyBootstrap().createPipeline().process("app.css").getAsset();
    
    assertEquals("h1 {\n  color: #ff0000;\n}\n/*# sourceMappingURL=appLess.css.map */\n\n", css);
  }
  
  @Test
  public void should_compile_file_with_imports() throws Exception {
    String css = new HumptyBootstrap().createPipeline().process("imports.css").getAsset();
    
    assertEquals("h1 {\n  color: #ff0000;\n}\nh2 {\n  color: #0000ff;\n}\n/*# sourceMappingURL=withImports.css.map */\n\n", css);
  }
  
  @Test
  public void should_compile_file_with_webjar_import() throws Exception {
    String css = new HumptyBootstrap().createPipeline().process("webJarImports.css").getAsset();
    
    assertEquals(".app {\n  opacity: 15;\n  filter: alpha(opacity=1500);\n}\n/*# sourceMappingURL=withWebJarImport.css.map */\n\n", css);
  }

  private Pipeline pipeline(String humptyFile) {
    return new HumptyBootstrap(humptyFile).createPipeline();
  }
}
