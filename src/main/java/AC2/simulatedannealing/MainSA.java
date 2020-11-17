package AC2.simulatedannealing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import AC2.util.FileSearch;

public class MainSA {

  private static ArrayList<Integer> indexes;
  private static ArrayList<Double> xCoords;
  private static ArrayList<Double> yCoords;

  private static ProblemInfo problemInfo = null;
  private static ExecutionConfig executionConfig = new ExecutionConfig();

  private static Scanner s = new Scanner(System.in);

  public static void main(String[] args) {
    // Encontra arquivos com a extensão .tsp ou .opt.tour
    String[] tspFileInputs = FileSearch.getArrayOfTsps();

    // Cancela execução se não há arquivos tsp para serem lidos
    if (tspFileInputs == null) {
      System.out.println("[Erro] Não há arquivos .tsp para serem lidos.");
      return;
    }

    // Menu
    String chosenFile = "";
    String input;

    do {
      System.out.println("\n\nRECOZIMENTO SIMULADO - EXECUÇÃO");
      System.out.println("1) Executar");
      System.out.println("2) Alterar valores padrao");
      System.out.println("3) Voltar");
      System.out.print("> ");
      input = s.nextLine();

      switch (input) {

        case "1": // Executar
          tspFileInputs = FileSearch.getArrayOfTsps();

          if (tspFileInputs.length == 0) {
            System.out.println("\n\n[Erro] Não há arquivos .tsp para serem lidos.");
            break;
          } else if (tspFileInputs.length > 1) {

            do { // Choose File
              {
                int i = 0;
                System.out.println("\n\nQual arquivo deseja utilizar?");
                for (String string : tspFileInputs) {
                  System.out.println(++i + ") " + string);
                }
                System.out.println("0) Voltar");
                System.out.print("> ");
              }

              try { // Tratamento de user input
                input = s.nextLine();
                if (Integer.parseInt(input) > 0 && Integer.parseInt(input) <= tspFileInputs.length) {
                  chosenFile = tspFileInputs[Integer.parseInt(input) - 1];
                }
                break;
              } catch (NumberFormatException e) {
                System.out.println("\n\n[Erro] Opção Inválida.");
              }
            } while (true);

            if (Integer.parseInt(input) == 0) {
              break;
            }
          } else {
            chosenFile = tspFileInputs[0];
          }

          // Carregar configurações
          if (!loadSettings()) {
            break;
          }

          SimulatedAnnealing.setConfig(executionConfig);

          // Carregar indexes, xCoords e yCoords
          if (!readTspFile(chosenFile)) {
            break;
          }

          SimulatedAnnealing.setProblemPlotting(indexes, xCoords, yCoords);

          for (int i = 0; i < executionConfig.getExecutions(); i++) {

            System.out.println("[INFO] Execução " + (i + 1) + ".0 de " + executionConfig.getExecutions());
          }

          do {
            if (SimulatedAnnealing.solve())
              break;
          } while (true);

          break;
        case "2":
          if (!loadSettings()) {
            break;
          }
          executionConfig.setNewDefaults();
          System.out.println("\n\n[INFO] Valores padroes alterados com sucesso");
          break;
        case "3":
          return;
      }
    } while (true);
  }

  private static boolean loadSettings() {
    String input;
    do { // Loop Confirmação

      boolean skipConfirmation = false;

      do { // Settings
        System.out.println("\n\n\t[Valores padrões]");
        System.out.println("Temperatura:\t\t\t" + executionConfig.getDefaultTemperature());
        System.out.println("Fator de Resfriamento:\t\t" + executionConfig.getDefaultCoolFactor());
        System.out.println("Estrátegia de Resfriamento:\t" + executionConfig.getDefaultScheduleType());
        System.out.println("Número de execuções:\t\t" + executionConfig.getDefaultExecutions());
        System.out.println("Limite de iterações:\t\t" + executionConfig.getDefaultIterLimit());
        System.out.println("Limite de tempo:\t\t" + executionConfig.getDefaultTimelimit());
        System.out.println("Parar na Solução:\t\t" + executionConfig.getDefaultStopOnSol());
        System.out.println("=======================================");
        System.out.println("Exportar .opt.tour:\t\t" + executionConfig.getDefaultExpOptTour());
        System.out.println("Exportar soluções:\t\t" + executionConfig.getDefaultExpSolution());
        System.out.println("Exportar .xlsx:\t\t\t" + executionConfig.getDefaultExpXLSXLog());
        System.out.println("=======================================");
        System.out.println("Utilizar valores padrões? [Sim/Nao] [0 para cancelar]");
        System.out.print("> ");

        input = s.nextLine();
        try {
          if (Integer.parseInt(input) == 0) {
            System.out.println("\n\n[INFO] Execução cancelada");
            return false;
          }
        } catch (NumberFormatException e) {
          // ignore
        }

        if (input.isEmpty() || input.equalsIgnoreCase("sim") || input.equalsIgnoreCase("s")
            || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y") || input.equalsIgnoreCase("true")) {
          executionConfig = new ExecutionConfig();
          skipConfirmation = true;
          break;
        } else {
          do { // Temperatura
            System.out.println("\n\n[Enter para utilizar '" + executionConfig.getDefaultTemperature() + "']");
            System.out.print("Temperatura [número]: ");
            input = s.nextLine();
            try {
              if (input.isEmpty()) {
                executionConfig.setTemperature(1000.0);
                break;
              } else if (Double.parseDouble(input) > 0) { // Set
                executionConfig.setTemperature(Double.parseDouble(input));
                break;
              } else { // Error
                System.out.println("\n\n[Erro] Valor Inválido.");
              }
            } catch (NumberFormatException e) {
              System.out.println("\n\n[Erro] Entrada Inválida.");
            }
          } while (true);

          do { // Fator de Resfriamento
            System.out.println("\n\n[Enter para utilizar '" + executionConfig.getDefaultCoolFactor() + "']");
            System.out.print("Fator de Resfriamento [0 < n < 1]: ");
            input = s.nextLine();
            try {
              if (input.isEmpty()) { // Default
                executionConfig.setCoolFactor(4.0);
                break;
              } else if (Double.parseDouble(input) > 0) {
                executionConfig.setCoolFactor(Double.parseDouble(input));
                break;
              } else {
                System.out.println("\n\n[Erro] Valor Inválido.");
              }
            } catch (NumberFormatException e) {
              System.out.println("\n\n[Erro] Entrada inválida.");
            }
          } while (true);
        }

        // Estratégia de Resfriamento
        do {
          System.out.println("\n\n[Enter para utilizar '" + executionConfig.getDefaultScheduleType() + "']");
          System.out.print("Estratégia de Resfriamento [ 1 - Geométrica | 2 - Linear]: ");
          input = s.nextLine();
          try {
            if (input.isEmpty()) { // Default
              executionConfig.setScheduleType(1);
              break;
            } else if (Integer.parseInt(input) > 0 && Integer.parseInt(input) < 3) { // Set
              executionConfig.setScheduleType(Integer.parseInt(input));
              break;
            } else { // Error
              System.out.println("\n\n[Erro] Valor Inválido.");
            }
          } catch (NumberFormatException e) {
            System.out.println("\n\n[Erro] Entrada inválida.");
          }
        } while (true);

        // Número de execuções
        do {
          System.out.println("\n\n[Enter para utilizar '" + executionConfig.getDefaultExecutions() + "']");
          System.out.print("Número de execuções [número]: ");
          input = s.nextLine();
          try {
            if (input.isEmpty()) { // Default
              executionConfig.setExecutions(1);
              break;
            } else if (Integer.parseInt(input) > 0) { // Set
              executionConfig.setExecutions(Integer.parseInt(input));
              break;
            } else { // Error
              System.out.println("\n\n[Erro] Valor Inválido.");
            }
          } catch (NumberFormatException e) {
            System.out.println("\n\n[Erro] Entrada inválida.");
          }
        } while (true);

        // Limite de Iterações
        do {
          System.out.println(
              "\n\n[Enter para utilizar '" + executionConfig.getDefaultIterLimit() + "'] [0 para desabilitar]");
          System.out.print("Limite de iterações [número]: ");
          input = s.nextLine();
          try {
            if (input.isEmpty() || Integer.parseInt(input) == 0 || input.equalsIgnoreCase("f")
                || input.equalsIgnoreCase("false") || input.equalsIgnoreCase("disabled")) { // Default
              executionConfig.setIterLimit("disabled");
              break;
            } else if (Integer.parseInt(input) > 0) { // Set
              executionConfig.setIterLimit(input);
              break;
            } else { // Error
              System.out.println("\n\n[Erro] Valor Inválido.");
            }
          } catch (NumberFormatException e) {
            System.out.println("\n\n[Erro] Entrada inválida.");
          }
        } while (true);

        // Limite de Tempo
        do {
          System.out.println(
              "\n\n[Enter para utilizar '" + executionConfig.getDefaultTimelimit() + "'] [0 para desabilitar]");
          System.out.print("Limite de tempo [segundos]: ");
          input = s.nextLine();
          try {
            if (input.isEmpty() || Integer.parseInt(input) == 0 || input.equalsIgnoreCase("f")
                || input.equalsIgnoreCase("false") || input.equalsIgnoreCase("disabled")) { // Default
              executionConfig.setTimelimit("disabled");
              break;
            } else if (Integer.parseInt(input) > 0) { // Set
              executionConfig.setTimelimit(input);
              break;
            } else { // Error
              System.out.println("\n\n[Erro] Valor Inválido.");
            }
          } catch (NumberFormatException e) {
            System.out.println("\n\n[Erro] Entrada inválida.");
          }
        } while (true);

        // Limite de Tempo
        do {
          System.out.println(
              "\n\n[Enter para utilizar '" + executionConfig.getDefaultStopOnSol() + "'] [0 para desabilitar]");
          System.out.print("Solução [número]: ");
          input = s.nextLine();
          try {
            if (input.isEmpty() || Integer.parseInt(input) == 0 || input.equalsIgnoreCase("f")
                || input.equalsIgnoreCase("false") || input.equalsIgnoreCase("disabled")) { // Default
              executionConfig.setStopOnSol("disabled");
              break;
            } else if (Integer.parseInt(input) > 0) { // Set
              executionConfig.setStopOnSol(input);
              break;
            } else { // Error
              System.out.println("\n\n[Erro] Valor Inválido.");
            }
          } catch (NumberFormatException e) {
            System.out.println("\n\n[Erro] Entrada inválida.");
          }
        } while (true);

        System.out.println("\n\n=======================================");

        // Exportar .tour
        System.out.println("\n\n[Enter para utilizar '" + executionConfig.getDefaultExpOptTour() + "']");
        System.out.print("Exportar .opt.tour [true/false/prompt]: ");
        input = s.nextLine();

        if (input.isEmpty()) {
          executionConfig.setExpOptTour("false");
        } else if (input.equals("prompt")) {
          executionConfig.setExpOptTour("prompt");
        } else {
          executionConfig.setExpOptTour(String.valueOf(Boolean.parseBoolean(input)));
        }

        // Exportar soluções
        System.out.println("\n\n[Enter para utilizar default '" + executionConfig.getDefaultExpSolution() + "']");
        System.out.print("Exportar soluções [true/false]: ");
        input = s.nextLine();

        if (input.isEmpty()) { // Default
          executionConfig.setExpSolution("false");
        } else { // Change
          executionConfig.setExpSolution(String.valueOf(Boolean.parseBoolean(input)));
        }

        // Exportar .xlsx
        System.out.println("\n\n[Enter para utilizar default '" + executionConfig.getDefaultExpXLSXLog() + "']");
        System.out.print("Exportar .xlsx [true/false]: ");
        input = s.nextLine();

        if (input.isEmpty()) { // Default
          executionConfig.setExpXLSXLog("true");
        } else if (input.equals("prompt")) {
          executionConfig.setExpXLSXLog("prompt");
        } else {
          executionConfig.setExpXLSXLog(String.valueOf(Boolean.parseBoolean(input)));
        }
        input = "done";
        break;

      } while (true);

      // Cancelado no menu settings
      try {
        if (skipConfirmation) {
          break;
        }
      } catch (NumberFormatException e) {
        // ignore
      }

      // Input Confirmação
      System.out.println("\n\n\t[Valores lidos]");
      System.out.println("Temperatura:\t\t\t" + executionConfig.getTemperature());
      System.out.println("Fator de Resfriamento:\t\t" + executionConfig.getCoolFactor());
      System.out.println("Estrátegia de Resfriamento:\t" + executionConfig.getScheduleType());
      System.out.println("Número de execuções:\t\t" + executionConfig.getExecutions());
      System.out.println("Limite de iterações:\t\t" + executionConfig.getIterLimit());
      System.out.println("Limite de tempo:\t\t" + executionConfig.getTimelimit());
      System.out.println("Parar na Solução:\t\t" + executionConfig.getStopOnSol());
      System.out.println("=======================================");
      System.out.println("Exportar .opt.tour:\t\t" + executionConfig.getExpOptTour());
      System.out.println("Exportar soluções:\t\t" + executionConfig.getExpSolution());
      System.out.println("Exportar .xlsx:\t\t\t" + executionConfig.getExpXLSXLog());
      System.out.println("=======================================");
      System.out.println("Utilizar valores lidos? [Sim/Nao] [0 para cancelar]");

      input = s.nextLine();
      try {
        if (input.isEmpty() || input.equalsIgnoreCase("sim") || input.equalsIgnoreCase("s")
            || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y") || input.equalsIgnoreCase("true")
            || Integer.parseInt(input) == 0) {
          break;
        }
      } catch (NumberFormatException e) {
        // ignore (considerar como "nao")
      }

    } while (true);

    // Cancelado na confirmação
    try

    {
      if (Integer.parseInt(input) == 0) {
        System.out.println("\n\n[INFO] Execução cancelada");
        return false;
      }
    } catch (NumberFormatException e) {
      // ignore
    }

    return true;
  }

  private static boolean readTspFile(String file) {

    System.out.println("\n\n[INFO] Lendo arquivo " + file);

    problemInfo = new ProblemInfo();

    indexes = new ArrayList<>();
    xCoords = new ArrayList<>();
    yCoords = new ArrayList<>();

    List<String> strLine = new ArrayList<>();

    try (FileInputStream f = new FileInputStream("files/.tour/" + file);
        BufferedReader br = new BufferedReader(new InputStreamReader(f))) {

      for (String line; (line = br.readLine()) != null;) {
        if (line.startsWith("NODE_COORD_SECTION") || line.startsWith("EOF")) {
          continue;

        } else if (line.startsWith("NAME")) {
          Matcher m = Pattern.compile("NAME ?: ?(.*)").matcher(line);
          if (m.find()) {
            problemInfo.setName(m.group(1));
          }

        } else if (line.startsWith("TYPE")) {
          Matcher m = Pattern.compile("TYPE ?: ?(.*)").matcher(line);
          if (m.find()) {
            problemInfo.setType(m.group(1));
          }

        } else if (line.startsWith("COMMENT")) {
          Matcher m = Pattern.compile("COMMENT ?: ?(.*)").matcher(line);
          if (m.find()) {
            problemInfo.setDescription(m.group(1));
          }

        } else if (line.startsWith("DIMENSION")) {
          Matcher m = Pattern.compile("DIMENSION ?: ?(.*)").matcher(line);
          if (m.find()) {
            try {
              problemInfo.setDimension(Integer.parseInt(m.group(1)));
            } catch (NumberFormatException e) {
              System.out.println("\n\n[Erro] Valor para 'DIMENSION' inválido.");
            }
          }

        } else if (line.startsWith("EDGE_WEIGHT_TYPE")) {
          Matcher m = Pattern.compile("EDGE_WEIGHT_TYPE ?: ?(.*)").matcher(line);
          if (m.find()) {
            problemInfo.setEdgeWeightType(m.group(1));
          }

        } else { // Build Model
          strLine = Arrays.asList(line.split(" "));

          for (int i = 0; i < strLine.size(); i++) {
            switch (i) {
              case 0:
                indexes.add(Integer.parseInt(strLine.get(i)));
                break;
              case 1:
                xCoords.add(Double.parseDouble(strLine.get(i)));
                break;
              case 2:
                yCoords.add(Double.parseDouble(strLine.get(i)));
                break;
            }
          }
        }
      }

      SimulatedAnnealing.setProblemInfo(problemInfo);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return true;
  }
}
