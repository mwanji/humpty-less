package co.mewf.humpty.bootstrap;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.spi.processors.SourceProcessor;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

public class LessSourceProcessor implements SourceProcessor {

  private static final LessCompiler LESS_COMPILER = new DefaultLessCompiler();
  private WebJarAssetLocator locator;

  @Override
  public String getName() {
    return "less";
  }

  @Override
  public boolean accepts(String assetName) {
    return assetName.endsWith(".less");
  }

  @Override
  public CompilationResult compile(CompilationResult compilationResult, PreProcessorContext context) {
    try {
      LessSource lessSource = new WebJarLessSource(compilationResult.getAssetName(), locator);
      com.github.sommeri.less4j.LessCompiler.CompilationResult lessCompilationResult = LESS_COMPILER.compile(lessSource);
      
      return new CompilationResult(compilationResult.getAssetName().replace(".less", ".css"), lessCompilationResult.getCss());
    } catch (Less4jException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Inject
  public void configure(WebJarAssetLocator locator) {
    this.locator = locator;
  }
  
  private static class WebJarLessSource extends LessSource.URLSource {
    
    private final WebJarAssetLocator locator;
    private final Path path;

    private WebJarLessSource(String assetName, WebJarAssetLocator locator) {
      super(WebJarLessSource.class.getResource("/" + assetName));
      this.path = Paths.get(assetName.replace(WebJarAssetLocator.WEBJARS_PATH_PREFIX + "/", ""));
      this.locator = locator;
    }
    
    @Override
    public LessSource relativeSource(String filename) throws FileNotFound, CannotReadFile {
      return new WebJarLessSource(locator.getFullPath(path.getParent().resolve(Paths.get(filename)).normalize().toString()), locator);
    }
  }
}
