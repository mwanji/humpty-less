package co.mewf.humpty.bootstrap;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Configuration.GlobalOptions;
import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.spi.processors.SourceProcessor;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

public class LessSourceProcessor implements SourceProcessor {

  private static final LessCompiler LESS_COMPILER = new DefaultLessCompiler();
  private WebJarAssetLocator locator;
  private GlobalOptions globalOptions;
  private Path assetsDir;

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
      LessSource lessSource;
      Path assetPath = Paths.get(compilationResult.getAssetName());
      if (assetPath.startsWith(globalOptions.getAssetsDir())) {
        lessSource = new WebJarLessSource(assetPath, locator);
      } else {
        lessSource = new LessSource.URLSource(toURL(assetPath));
      }
      com.github.sommeri.less4j.LessCompiler.CompilationResult lessCompilationResult = LESS_COMPILER.compile(lessSource);
      
      return new CompilationResult(compilationResult.getAssetName().replace(".less", ".css"), lessCompilationResult.getCss());
    } catch (Less4jException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Inject
  public void configure(WebJarAssetLocator locator, Configuration.GlobalOptions globalOptions, Configuration.Options options) {
    this.locator = locator;
    this.globalOptions = globalOptions;
    this.assetsDir = globalOptions.getAssetsDir();
  }
  
  private static class WebJarLessSource extends LessSource.URLSource {
    
    private final Path path;
    private final WebJarAssetLocator locator;

    private WebJarLessSource(Path assetPath, WebJarAssetLocator locator) {
      super(toURL(assetPath));
      this.path = assetPath;
      this.locator = locator;
    }
    
    @Override
    public LessSource relativeSource(String filename) throws FileNotFound, CannotReadFile {
      Path relativeSourcePath = path.getParent().resolve(Paths.get(filename)).normalize();
      if (toURL(relativeSourcePath) != null) {
        return new WebJarLessSource(relativeSourcePath, locator);
      }
      
      return new LessSource.URLSource(toURL(locator.getFullPath(filename)));
    }
  }
  
  private static URL toURL(Path path) {
    return toURL(path.toString());
  }
  
  private static URL toURL(String path) {
    return Thread.currentThread().getContextClassLoader().getResource(path);
  }
}
