package AC2;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class ChartGenerator extends Application {

  private static String chosenFile;
  private static String problemName;
  private static int dimension;

  private static List<Integer> indexes;
  private static List<Double> xCoords;
  private static List<Double> yCoords;
  private static List<String> solutionLines;

  private static GifSequenceWriter gifWriter;
  private static ImageOutputStream output;

  public static void main(String[] args) {

    String[] solutionFiles = FileSearch.getArrayOfSolutions();

    if (solutionFiles == null) {
      System.out.println("[Erro] Não há arquivos de solução para serem lidos.");
      System.exit(1);
    }

    Scanner s = new Scanner(System.in);
    String opt;

    do { // Choose File
      {
        int i = 0;
        System.out.println("\n\nQual arquivo deseja utilizar?");
        for (String string : solutionFiles) {
          System.out.println(++i + ") " + string);
        }
        System.out.println("0) Sair");
        System.out.print("> ");
      }

      try { // Tratamento de user input
        opt = s.nextLine();
        if (Integer.parseInt(opt) == 0) {
          System.exit(1);
        }
        if (Integer.parseInt(opt) > 0 && Integer.parseInt(opt) <= solutionFiles.length) {
          chosenFile = solutionFiles[Integer.parseInt(opt) - 1];
          break;
        }
      } catch (NumberFormatException e) {
        System.out.println("\n\n[Erro] Opção Inválida.");
      }
    } while (true);

    s.close();

    readSolutionFile(chosenFile);
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd_HH-mm-ss");

    primaryStage.setTitle("Recozimento Simulado");

    // Eixos Gráfico de Linhas
    final NumberAxis xAxisLC = new NumberAxis();
    final NumberAxis yAxisLC = new NumberAxis();
    // Eixos Gráfico de Pontos
    final NumberAxis xAxisSC = new NumberAxis();
    final NumberAxis yAxisSC = new NumberAxis();

    // Criar gráfico de plotagem
    LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxisLC, yAxisLC);
    lineChart.setTitle("Recozimento Simulado - " + chosenFile);
    lineChart.setAnimated(false);
    lineChart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);

    // Criar gráfico de soluções
    ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxisSC, yAxisSC);
    scatterChart.setTitle("Solução x Tempo");
    scatterChart.setAnimated(false);

    // Adicionar gráficos ao layout
    FlowPane root = new FlowPane(Orientation.HORIZONTAL);
    root.getChildren().addAll(lineChart, scatterChart);

    // Adicionar layout à scene, e a scene à window
    Scene scene = new Scene(root, 1020, 440);
    scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

    // Create folder to export
    File folder = new File("export/charts/" + dtf.format(LocalDateTime.now()).toString() + "_" + problemName + "/");
    if (!folder.exists()) {
      if (folder.mkdirs()) {
        System.out.println("\n\n[INFO] Pasta " + folder.getName() + " para exportação criada");
      } else {
        System.out.println("\n\n[ERRO] Não foi possível criar pasta para exportar os gráficos");
        System.exit(1);
      }
    }

    /**
     * Exportação
     */

    System.out.println("\n\n[INFO] Iniciando exportação de arquivos");

    // Create scatter chart series
    XYChart.Series<Number, Number> currSolSeries = new XYChart.Series<>();
    XYChart.Series<Number, Number> bestSolSeries = new XYChart.Series<>();
    XYChart.Series<Number, Number> temperatureSeries = new XYChart.Series<>();
    scatterChart.getData().add(temperatureSeries);
    scatterChart.getData().add(currSolSeries);
    scatterChart.getData().add(bestSolSeries);
    scatterChart.setId("solutionxtime-scatter-chart"); // diminuir tamanho dos pontos

    long startTime = System.nanoTime();

    int iteration = 1; // for files
    for (String str : solutionLines) {
      XYChart.Series<Number, Number> tourItSeries = new XYChart.Series<>(); // resetar series

      // Adicionar valores aos series
      int i = 0;
      List<String> values = Arrays.stream(str.split(" ")).collect(Collectors.toList());
      tourItSeries.setName("Iteração " + values.get(0)); // Iteração
      int xValue = Integer.valueOf(values.get(1)); // time
      int yValue = Integer.valueOf(values.get(2)); // solution
      double temperature = Double.valueOf(values.get(3).replace(",", ".")); // solution

      if (iteration == 1) { // first best
        bestSolSeries.setName("Melhor solução: " + yValue);
        bestSolSeries.getData().add(new XYChart.Data<>(xValue, yValue));
      }
      if (Boolean.valueOf(values.get(4))) { // newBest?
        bestSolSeries.getData().clear();
        bestSolSeries.setName("Melhor solução: " + yValue);
        bestSolSeries.getData().add(new XYChart.Data<>(xValue, yValue));
      }
      temperatureSeries.setName("Temperatura: " + temperature);

      currSolSeries.setName("Solução atual: " + yValue);
      currSolSeries.getData().add(new XYChart.Data<>(xValue, yValue));

      for (String value : values) {
        if (i++ > 4) {
          int indexValue = Integer.valueOf(value);
          tourItSeries.getData().add(new XYChart.Data<>(xCoords.get(indexValue - 1), yCoords.get(indexValue - 1)));

          if (i == values.size()) {
            int firstPoint = Integer.valueOf(values.get(5));
            tourItSeries.getData().add(new XYChart.Data<>(xCoords.get(firstPoint - 1), yCoords.get(firstPoint - 1)));
          }
        }
      }

      lineChart.getData().add(tourItSeries);

      File file = new File(folder.getAbsoluteFile() + "/chart.png");

      // Export files
      try {
        if (folder.exists()) {
          // Generate png file

          WritableImage image = scene.snapshot(null);

          ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);

          if (iteration == 1) {
            System.out.println("\n\n[DEBUG] GifWriter iniciado na iteração " + iteration);
            BufferedImage first = ImageIO.read(file);
            output = new FileImageOutputStream(new File(folder.getAbsolutePath() + "/chart_animation.gif"));
            gifWriterInit(first, output);
          }

          BufferedImage next = ImageIO.read(file);
          gifWriter.writeToSequence(next);

          float progress = ((float) iteration / (float) solutionLines.size()) * 100;
          System.out.println(String.format("\n\n[INFO] [Progresso: %.1f%%] Exportado frame %d de %d", progress,
              iteration, solutionLines.size()));
        }
      } catch (IOException e) {
        System.out.println("\n\n[ERRO] Não foi possível exportar imagem " + iteration);
      }

      iteration++;
      lineChart.getData().clear();
    }

    gifWriter.close();
    output.close();

    long stopTimeMs = (System.nanoTime() - startTime) / 1_000_000;
    long stopTimeSe = stopTimeMs / 1_000;

    System.out.println(String.format("\n\n[INFO] Exportação concluída [%d imagens geradas] em %d ms (%d s)",
        (iteration - 1), stopTimeMs, stopTimeSe));
    primaryStage.close();
    System.exit(1);
  }

  private static void gifWriterInit(BufferedImage first, ImageOutputStream output) throws IOException {
    gifWriter = new GifSequenceWriter(output, first.getType(), 0, true);
    gifWriter.writeToSequence(first);
  }

  private static boolean readSolutionFile(String file) {

    System.out.println("\n\n[INFO] Lendo arquivo " + file);

    int registeredCordinates = 0;

    indexes = new ArrayList<>();
    xCoords = new ArrayList<>();
    yCoords = new ArrayList<>();
    solutionLines = new ArrayList<>();

    List<String> lineArgs = new ArrayList<>();

    try (FileInputStream f = new FileInputStream("export/solutions/" + file);
        BufferedReader br = new BufferedReader(new InputStreamReader(f))) {

      for (String line; (line = br.readLine()) != null;) {
        if (line.startsWith("COMMENT") || line.startsWith("NODE_COORD_SECTION") || line.startsWith("EOF")) {
          continue;

        } else if (line.startsWith("NAME")) {
          Matcher m = Pattern.compile("NAME ?: ?(.*)").matcher(line);
          if (m.find()) {
            problemName = m.group(1);
          }

        } else if (line.startsWith("DIMENSION")) {
          Matcher m = Pattern.compile("DIMENSION ?: ?(.*)").matcher(line);
          if (m.find()) {
            try {
              dimension = Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
              System.out.println("\n\n[Erro] Valor para 'DIMENSION' inválido.");

              try { // Sleep
                Thread.sleep(500);
              } catch (InterruptedException ex) {
                // ignore
              }
            }
          }

        } else if (registeredCordinates < dimension) { // Build Model
          lineArgs = Arrays.asList(line.split(" "));

          for (int i = 0; i < lineArgs.size(); i++) {
            switch (i) {
              case 0:
                indexes.add(Integer.parseInt(lineArgs.get(i)));
                break;
              case 1:
                xCoords.add(Double.parseDouble(lineArgs.get(i)));
                break;
              case 2:
                yCoords.add(Double.parseDouble(lineArgs.get(i)));
                break;
            }
          }

          registeredCordinates++;

        } else {
          solutionLines.add(line);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return true;
  }

}
