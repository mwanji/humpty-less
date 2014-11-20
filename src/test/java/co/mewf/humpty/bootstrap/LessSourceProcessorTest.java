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
    Pipeline pipeline = pipeline("/humpty.toml");
    String output = pipeline.process("bootstrapVanilla.css");

    String expected = IOUtils.toString(getClass().getResourceAsStream("should_compile_vanilla_bootstrap.css"));
    
    assertEquals(expected, output);
  }

  @Test
  public void should_compile_bootstrap_with_custom_variables() throws IOException {
    Pipeline pipeline = pipeline("/humpty.toml");
    String output = pipeline.process("bootstrapCustom.css");

    assertTrue("Did not replace @icon-font-name", output.contains("icon-font-name"));
  }

  private Pipeline pipeline(String humptyFile) {
    return new HumptyBootstrap(humptyFile).createPipeline();
  }
}
