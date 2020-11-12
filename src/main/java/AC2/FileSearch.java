package AC2;

import java.io.File;
import java.io.FilenameFilter;

public class FileSearch {
  private static File tourFolder = new File(".tour/.");
  private static File optTourFolder = new File("export/opttour/.");
  private static File solutionsFolder = new File("export/solutions/.");
  private static TspSearchFilter tspFiles = new TspSearchFilter();
  private static TxtSearchFilter txtFiles = new TxtSearchFilter();

  public static String[] getArrayOfTsps() {
    return tourFolder.list(tspFiles);
  }

  public static String[] getArratOfOptTours() {
    return optTourFolder.list(txtFiles);
  }

  public static String[] getArrayOfSolutions() {
    return solutionsFolder.list(txtFiles);
  }

  public static class TspSearchFilter implements FilenameFilter {

    @Override
    public boolean accept(File loc, String name) {
      if (name.lastIndexOf('.') > 0) {
        // obter último índice para '.'
        int lastIndex = name.lastIndexOf('.');

        // obter extensão
        String str = name.substring(lastIndex);

        // retornar se extensão bate
        if (str.equalsIgnoreCase(".tsp")) {
          return true;
        }
      }
      return false;
    }
  }

  public static class TxtSearchFilter implements FilenameFilter {

    @Override
    public boolean accept(File loc, String name) {
      if (name.lastIndexOf('.') > 0) {
        // obter último índice para '.'
        int lastIndex = name.lastIndexOf('.');

        // obter extensão
        String str = name.substring(lastIndex);

        // retornar se extensão bate
        if (str.equalsIgnoreCase(".txt")) {
          return true;
        }
      }
      return false;
    }
  }
}