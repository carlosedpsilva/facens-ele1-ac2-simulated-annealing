package AC2;

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

public class Main {

  private static Double[] defaultNumValues = { 1_000.0, 0.999999, 0.0, 0.0, 5.0 };
  private static Boolean[] defaultBoolValues = { true, true, true };

  private static Double[] inputNumValues = { null, null, null, null, null };
  private static Boolean[] inputBoolValues = { null, null, null };

  private static ArrayList<Integer> indexes;
  private static ArrayList<Double> xCoords;
  private static ArrayList<Double> yCoords;

  private static ProblemInfo problemInfo = null;

  private static Scanner s = new Scanner(System.in);

  /**
   * ### MAIN ###
   */
  public static void main(String[] args) throws IOException {
    // Encontra arquivos com a extensão .tsp ou .opt.tour
    String[] tspFileInputs = FileSearch.getArrayOfTsps();

    if (tspFileInputs == null) {
      System.out.println("[Erro] Não há arquivos .tsp ou .opt.tour para serem lidos.");
      return;
    }

    // Menu
    String chosenFile = "";
    String opt;

    do {
      System.out.println("\n\nRECOZIMENTO SIMULADO");
      System.out.println("1) Executar");
      System.out.println("2) Alterar valores padrao");
      System.out.println("3) Sair");
      System.out.print("> ");
      opt = s.nextLine();

      switch (opt) {

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
                opt = s.nextLine();
                if (Integer.parseInt(opt) > 0 && Integer.parseInt(opt) <= tspFileInputs.length) {
                  chosenFile = tspFileInputs[Integer.parseInt(opt) - 1];
                }
                break;
              } catch (NumberFormatException e) {
                System.out.println("\n\n[Erro] Opção Inválida.");
              }
            } while (true);

            if (Integer.parseInt(opt) == 0) {
              break;
            }
          } else {
            chosenFile = tspFileInputs[0];
          }

          // Carregar configurações
          if (!loadSettings()) {
            break;
          }

          // Carregar indexes, xCoords e yCoords
          if (!readTspFile(chosenFile)) {
            break;
          }

          SimulatedAnnealing.setProblemPlotting(indexes, xCoords, yCoords);

          boolean isLoopExecution = inputNumValues[4] > 1 ? true : false;


          for (int i = 0; i < inputNumValues[4]; i++) {

            // Clear memory
            System.gc();

            System.out.println("[INFO] Execução " + (i + 1) + ".0 de " + inputNumValues[4]);
            new SimulatedAnnealing(temp[j], inputNumValues[1], inputNumValues[2].longValue(),
                inputNumValues[3].longValue(), inputBoolValues[0], inputBoolValues[1], inputBoolValues[2],
                isLoopExecution);
          }
       

          // SimulatedAnnealing.closeWb();

          break;
        case "2":
          if (!loadSettings()) {
            break;
          }
          defaultNumValues = inputNumValues;
          defaultBoolValues = inputBoolValues;

          System.out.println("\n\n[INFO] Valores padroes alterados com sucesso");
          break;
        case "3":
          s.close();
          System.exit(1);
          break;
      }
    } while (true);
  }

  private static boolean loadSettings() {
    String opt;
    do { // Loop Confirmação

      boolean skipConfirmation = false;

      do { // Settings
        System.out.println("\n\n\t[Valores padrões]");
        System.out.println("Temperatura:\t\t\t" + defaultNumValues[0]);
        System.out.println("Fator de Resfriamento:\t\t" + defaultNumValues[1]);
        System.out.println("Limite de iterações:\t\t" + (defaultNumValues[2] == 0 ? "false" : defaultNumValues[2]));
        System.out.println("Limite de tempo:\t\t" + (defaultNumValues[3] == 0 ? "false" : defaultNumValues[3]));
        System.out.println("Número de execuções:\t\t" + defaultNumValues[4]);
        System.out.println("=======================================");
        System.out.println("Exportar .opt.tour:\t\t" + defaultBoolValues[0]);
        System.out.println("Exportar soluções:\t\t" + defaultBoolValues[1]);
        System.out.println("Exportar .xlsx:\t\t\t" + defaultBoolValues[2]);
        System.out.println("=======================================");
        System.out.println("Utilizar valores padrões? [Sim/Nao] [0 para cancelar]");
        System.out.print("> ");

        opt = s.nextLine();
        try {
          if (Integer.parseInt(opt) == 0) {
            System.out.println("\n\n[INFO] Execução cancelada");
            return false;
          }
        } catch (NumberFormatException e) {
          // ignore
        }

        if (opt.isEmpty() || opt.equalsIgnoreCase("sim") || opt.equalsIgnoreCase("s") || opt.equalsIgnoreCase("yes")
            || opt.equalsIgnoreCase("y") || opt.equalsIgnoreCase("true")) {
          inputNumValues = defaultNumValues;
          inputBoolValues = defaultBoolValues;
          skipConfirmation = true;
          break;
        } else {
          do { // Temperatura
            System.out.println("\n\n[Enter para utilizar default (" + defaultNumValues[0] + ")]");
            System.out.print("Temperatura [número]: ");
            opt = s.nextLine();
            try {
              if (opt.isEmpty()) { // Default
                inputNumValues[0] = defaultNumValues[0];
                break;
              } else if (Double.parseDouble(opt) > 0) { // Set
                inputNumValues[0] = Double.parseDouble(opt);
                break;
              } else { // Error
                System.out.println("\n\n[Erro] Valor Inválido.");
              }
            } catch (NumberFormatException e) {
              System.out.println("\n\n[Erro] Entrada Inválida.");
            }
          } while (true);

          do { // Fator de Resfriamento
            System.out.println("\n\n[Enter para utilizar default (" + defaultNumValues[1] + ")]");
            System.out.print("Fator de Resfriamento [0 < n < 1]: ");
            opt = s.nextLine();
            try {
              if (opt.isEmpty()) { // Default
                inputNumValues[1] = defaultNumValues[1];
                break;
              } else if (Double.parseDouble(opt) > 0) { // Set
                if (Double.parseDouble(opt) > 1) {
                  System.out.println("\n\n[Erro] Valor Inválido. Valor deve ser entre 0 e 1.");
                  continue;
                }
                inputNumValues[1] = Double.parseDouble(opt);
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
            System.out.println("\n\n[Enter para utilizar default "
                + (defaultNumValues[2] == 0 ? "false" : defaultNumValues[2]) + "] [0 para desabilitar]");
            System.out.print("Limite de iterações [número]: ");
            opt = s.nextLine();
            try {
              if (opt.isEmpty() || Double.parseDouble(opt) == 0) { // Default
                inputNumValues[2] = defaultNumValues[2];
                break;
              } else if (Integer.parseInt(opt) > 0) { // Set
                inputNumValues[2] = Double.parseDouble(opt);
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
            System.out.println("\n\n[Enter para utilizar default "
                + (defaultNumValues[3] == 0 ? "false" : defaultNumValues[3]) + "] [0 para desabilitar]");
            System.out.print("Limite de tempo [segundos]: ");
            opt = s.nextLine();
            try {
              if (opt.isEmpty() || Integer.parseInt(opt) == 0) { // Default
                inputNumValues[3] = defaultNumValues[3];
                break;
              } else if (Double.parseDouble(opt) > 0) { // Set
                inputNumValues[3] = Double.parseDouble(opt);
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
            System.out.println("\n\n[Enter para utilizar default " + defaultNumValues[4]);
            System.out.print("Número de execuções [número]: ");
            opt = s.nextLine();
            try {
              if (opt.isEmpty() || Integer.parseInt(opt) == 0) { // Default
                inputNumValues[4] = defaultNumValues[4];
                break;
              } else if (Double.parseDouble(opt) > 0) { // Set
                inputNumValues[4] = Double.parseDouble(opt);
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
          System.out.println("\n\n[Enter para utilizar default (" + defaultBoolValues[0] + ")]");
          System.out.print("Exportar .opt.tour [true/false]: ");
          opt = s.nextLine();

          if (opt.isEmpty()) { // Default
            inputBoolValues[0] = defaultBoolValues[0];
          } else { // Change
            inputBoolValues[0] = Boolean.parseBoolean(opt);
          }

          // Exportar soluções
          System.out.println("\n\n[Enter para utilizar default (" + defaultBoolValues[1] + ")]");
          System.out.print("Exportar soluções [true/false]: ");
          opt = s.nextLine();

          if (opt.isEmpty()) { // Default
            inputBoolValues[1] = defaultBoolValues[1];
          } else { // Change
            inputBoolValues[1] = Boolean.parseBoolean(opt);
          }

          // Exportar .xlsx
          System.out.println("\n\n[Enter para utilizar default (" + defaultBoolValues[2] + ")]");
          System.out.print("Exportar .xlsx [true/false]: ");
          opt = s.nextLine();

          if (opt.isEmpty()) { // Default
            inputBoolValues[2] = defaultBoolValues[2];
          } else {
            inputBoolValues[2] = Boolean.parseBoolean(opt);
          }

          opt = "done";
          break;
        }
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
      System.out.println("Temperatura:\t\t\t" + inputNumValues[0]);
      System.out.println("Fator de Resfriamento:\t\t" + inputNumValues[1]);
      System.out.println("Limite de iterações:\t\t" + (inputNumValues[2] == 0 ? "false" : inputNumValues[2]));
      System.out.println("Limite de tempo:\t\t" + (inputNumValues[3] == 0 ? "false" : inputNumValues[3]));
      System.out.println("Número de execuções:\t\t" + inputNumValues[4]);
      System.out.println("=======================================");
      System.out.println("Exportar .opt.tour:\t\t" + inputBoolValues[0]);
      System.out.println("Exportar soluções:\t\t" + inputBoolValues[1]);
      System.out.println("Exportar .xlsx:\t\t\t" + inputBoolValues[2]);
      System.out.println("=======================================");
      System.out.println("Utilizar valores lidos? [Sim/Nao] [0 para cancelar]");

      opt = s.nextLine();
      try {
        if (opt.isEmpty() || opt.equalsIgnoreCase("sim") || opt.equalsIgnoreCase("s") || opt.equalsIgnoreCase("yes")
            || opt.equalsIgnoreCase("y") || opt.equalsIgnoreCase("true") || Integer.parseInt(opt) == 0) {
          break;
        }
      } catch (NumberFormatException e) {
        // ignore (considerar como "nao")
      }

    } while (true);

    // Cancelado na confirmação
    try {
      if (Integer.parseInt(opt) == 0) {
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

    try (FileInputStream f = new FileInputStream(".tour/" + file);
        BufferedReader br = new BufferedReader(new InputStreamReader(f))) {

      for (String line; (line = br.readLine()) != null;) {
        if (line.startsWith("COMMENT") || line.startsWith("NODE_COORD_SECTION") || line.startsWith("EOF")) {
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
