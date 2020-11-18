package AC2.simulatedannealing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import AC2.util.Util;

public class SimulatedAnnealing {

    /**
     * Configuração
     */

    // estrátegia de resfriamento
    private static double temperature;
    private static double coolFactor;
    private static boolean scheduleType;
    // critérios de parada
    private static long iterLimit;
    private static long timeLimit;
    private static long stopOnSol;
    // export
    private static String exportOptTour;
    private static String exportSolutions;
    private static String exportXLSX;
    // other
    private static boolean isLoopExecution;

    /**
     * Problem Tour Build
     */

    private static ArrayList<Integer> indexes;
    private static ArrayList<Double> xCoords;
    private static ArrayList<Double> yCoords;

    private static ArrayList<City> cities;

    /**
     * Java Execution Settings
     */

    public static ProblemInfo problemInfo;

    private static XSSFWorkbook wb;

    private static Scanner s = new Scanner(System.in);

    public static void solve() {

        wb = null;
        System.gc();

        // Registrar date time da execução
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy-MM-dd_HH-mm-ss");
        String dateNow = dtf.format(LocalDateTime.now()).toString();

        /**
         * INICIALIZAÇÃO DA SOLUÇÃO
         */

        System.out.println("[INFO] Preparando solução");

        // Inicializar Array de Cidades
        cities = new ArrayList<>();
        for (Integer i : indexes) {
            City city = new City(i, xCoords.get(i - 1), yCoords.get(i - 1));
            cities.add(city);
        }

        // Inicializar Tour
        Tour current = Tour.initAndShuffle(cities);
        Tour best = current.duplicate();

        // Inicializar tempo de início de processamento e iterações
        long startTime = System.nanoTime(), currentTime = 0;
        int iteration = 0;
        int update = 0;

        /**
         * PREPARAR ARQUIVO DE SOLUÇÃO
         */

        // Preparar arquivo de export de soluções .txt
        File solutionFolder = new File("files/export/solution/");
        File solutionFile = new File("files/export/solution/" + dateNow + "_" + problemInfo.getName() + ".txt");

        if (Boolean.valueOf(exportSolutions)) {

            System.out.println("[INFO] Preparando o arquivo de exportação da solução (export/solutions/*.txt)");

            try {
                solutionFolder.mkdirs(); // create folder
                solutionFile.createNewFile(); // create unique file
                PrintWriter out = new PrintWriter(new FileWriter(solutionFile, true));
                out.append("NAME : " + problemInfo.getName() + "\n");
                out.append("DIMENSION : " + problemInfo.getDimension() + "\n");
                for (Integer index : indexes) {
                    out.append(index + " " + xCoords.get(index - 1) + " " + yCoords.get(index - 1) + "\n");
                }
                out.close();
            } catch (IOException e) {
                System.out.println(
                        "[ERRO] Não foi possível gerar ou ler arquivo de export de soluções (export/solutions/*.txt)");
            }
        }

        /**
         * PROCESSAMENTO
         */

        double t = temperature;
        double sol_t = temperature;
        boolean isUpdate;
        boolean isNewBest;
        StopReason stopReason = StopReason.RESFRIAMENTO_COMPLETO;

        System.out.println("[INFO] Processando solução");

        do {
            // Gerar solução vizinha
            Tour neighbor = current.duplicate();

            // Trocar posição da ordem de duas cidades aleatórias
            int index1 = (int) (neighbor.numberOfCities() * Math.random());
            int index2 = (int) (neighbor.numberOfCities() * Math.random());
            Collections.swap(neighbor.getCities(), index1, index2);

            // Registrar distância dos Tours para comparação
            double currentLenght = current.getDistance();
            double neighborLenght = neighbor.getDistance();

            isUpdate = false;
            isNewBest = false;

            // Probabilidade de troca
            if (neighborLenght < currentLenght || Math.random() < Util.probability(currentLenght, neighborLenght, t)) {
                current = neighbor.duplicate();

                // Registrar se a nova solução é melhor
                if (current.getDistance() < best.getDistance()) {
                    best = current.duplicate();
                    sol_t = t;
                    isNewBest = true;
                }

                isUpdate = true;
                update++;

                // Exportar soluções
                if (Boolean.valueOf(exportSolutions)) {
                    long time = (System.nanoTime() - startTime) / 1_000_000;

                    // Write in Solution File
                    try (PrintWriter out = new PrintWriter(new FileWriter(solutionFile, true))) {
                        // Info
                        out.append(String.format("%d %d %d %d %.4f %b %b ", update, iteration, time,
                                current.getDistance(), t, isUpdate, isNewBest));
                        // Sequência tour
                        out.append(current.getIndexes().stream().map(n -> String.valueOf(n))
                                .collect(Collectors.joining(" ")) + "\n");
                    } catch (IOException e) {
                        System.out.println("[ERRO] Não foi possível escrever em arquivo de export de soluções [.txt]");
                    }

                }
            }

            iteration++;

            if (iterLimit != 0 && iteration > iterLimit) {
                stopReason = StopReason.LIMITE_DE_ITERACOES;
                break;
            }

            if (timeLimit != 0) {
                currentTime = (System.nanoTime() - startTime);
                if ((currentTime / 1_000_000_000) > timeLimit) {
                    stopReason = StopReason.LIMITE_DE_TEMPO;
                    break;
                }
            }

            if (stopOnSol != 0 && best.getDistance() <= stopOnSol) {
                stopReason = StopReason.SOL_IDEAL_ENCONTRADA;
                break;
            }

            if (scheduleType) {
                t *= coolFactor;
            } else {
                t -= coolFactor;
            }
        } while (scheduleType ? (t > (1 - coolFactor)) : (t >= 0.0));

        /**
         * FIM PROCESSAMENTO
         */

        // Registrar tempo de parada do processamento
        currentTime = (System.nanoTime() - startTime);
        long stopTimeMs = currentTime / 1_000_000;
        long stopTimeSe = stopTimeMs / 1_000;

        System.out.println("[INFO] Processamento concluído");

        /**
         * FINALIZAR EXPORTS
         */

        // Exportar Tour
        if (exportOptTour.equals("prompt")) {
            System.out.print("Deseja exportar o Opt Tour atual? (" + best.getDistance() + ")\n> ");
            String input = s.nextLine();
            if (input.isEmpty() || input.equalsIgnoreCase("sim") || input.equalsIgnoreCase("s")
                    || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y") || input.equalsIgnoreCase("true")) {
                exportOptTour = "true";
            }
        }

        File optTourFolder = new File("files/export/opttour/");
        File optTourFile = new File("files/export/opttour/" + dateNow + "_" + problemInfo.getName() + ".opt.tour.txt");

        if (Boolean.valueOf(exportOptTour)) {

            System.out.println("[INFO] Criando arquivo de exportação .opt.tour");

            try {

                if (!optTourFolder.exists()) {
                    optTourFolder.mkdirs();
                }

                // Arquivo com nome único
                optTourFile.createNewFile();

                PrintWriter out = new PrintWriter(new FileWriter(optTourFile, true));
                out.append("NAME : " + problemInfo.getName() + "\n");
                out.append(String.format("COMMENT : Optimum solution for %s [ %d ]\n", problemInfo.getName(),
                        best.getDistance()));
                out.append("TYPE : TOUR\n");
                out.append("DIMENSION : " + problemInfo.getDimension() + "\n");
                out.append(best.getIndexes().stream().map(n -> String.valueOf(n)).collect(Collectors.joining("\n"))
                        + "\n");
                out.close();
            } catch (IOException e) {
                System.out.println("[ERRO] Não foi possível gerar arquivo de export de tour");
                System.out.println(e.getMessage());
            }
        }

        // Exportar log de execução
        if (exportXLSX.equals("prompt"))

        {
            System.out.print("Deseja exportar o Opt Tour atual?\n> ");
            String input = s.nextLine();
            if (input.isEmpty() || input.equalsIgnoreCase("sim") || input.equalsIgnoreCase("s")
                    || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y") || input.equalsIgnoreCase("true")) {
                exportXLSX = "true";
            }
        }

        if (Boolean.valueOf(exportXLSX)) {

            System.out.println("[INFO] Registrando na planilha o log de execução");

            FileOutputStream xlsxLogFileOut = null;
            XSSFSheet mainSheet = null;

            File xlsxFolder = new File("files/export/xlsx/");
            File logFile = new File("files/export/xlsx/execution_log.xlsx");

            try {
                // Criar pasta se não existir
                if (!xlsxFolder.exists()) {
                    xlsxFolder.mkdirs();
                }

                // Criar planilha se não existir
                if (!logFile.exists()) {
                    wb = new XSSFWorkbook();
                } else {
                    wb = new XSSFWorkbook(new FileInputStream(logFile));
                }

                // new fileOut and create main
                xlsxLogFileOut = new FileOutputStream(logFile);

                if ((mainSheet = wb.getSheet("main")) == null) {
                    mainSheet = wb.createSheet("main");

                    XSSFRow newLine = mainSheet.createRow(0);

                    XSSFCell header0 = newLine.createCell(0);
                    XSSFCell header1 = newLine.createCell(1);
                    XSSFCell header2 = newLine.createCell(2);
                    XSSFCell header3 = newLine.createCell(3);
                    XSSFCell header4 = newLine.createCell(4);
                    XSSFCell header5 = newLine.createCell(5);
                    XSSFCell header6 = newLine.createCell(6);
                    XSSFCell header7 = newLine.createCell(7);
                    XSSFCell header8 = newLine.createCell(8);
                    XSSFCell header9 = newLine.createCell(9);
                    XSSFCell header10 = newLine.createCell(10);
                    XSSFCell header11 = newLine.createCell(11);

                    header0.setCellValue("date/time");
                    header1.setCellValue("file");
                    header2.setCellValue("temperature");
                    header3.setCellValue("cool. factor");
                    header4.setCellValue("exec. time");
                    header5.setCellValue("stop reason");
                    header6.setCellValue("iterations");
                    header7.setCellValue("updates");
                    header8.setCellValue("best sol.");
                    header9.setCellValue("curr. sol.");
                    header10.setCellValue("sol. temperature");
                    header11.setCellValue("curr. temperature");
                }

                // Registrar linha do log de execução

                XSSFRow logLine = mainSheet.createRow(mainSheet.getLastRowNum() + 1);

                XSSFCell date_time = logLine.createCell(0);
                XSSFCell file = logLine.createCell(1);
                XSSFCell temp = logLine.createCell(2);
                XSSFCell cool_factor = logLine.createCell(3);
                XSSFCell exec_time = logLine.createCell(4);
                XSSFCell stop_reason = logLine.createCell(5);
                XSSFCell iterations = logLine.createCell(6);
                XSSFCell updates = logLine.createCell(7);
                XSSFCell best_sol = logLine.createCell(8);
                XSSFCell curr_sol = logLine.createCell(9);
                XSSFCell sol_temperature = logLine.createCell(10);
                XSSFCell curr_temperature = logLine.createCell(11);

                date_time.setCellValue(dateNow);
                file.setCellValue(problemInfo.getName());
                temp.setCellValue(temperature);
                cool_factor.setCellValue(coolFactor);
                exec_time.setCellValue(stopTimeMs);
                stop_reason.setCellValue(stopReason.name());
                iterations.setCellValue((iteration - 1));
                updates.setCellValue((update - 1));
                best_sol.setCellValue(best.getDistance());
                curr_sol.setCellValue(current.getDistance());
                sol_temperature.setCellValue(sol_t);
                curr_temperature.setCellValue(t);

                // Gravar
                wb.write(xlsxLogFileOut);
                wb.close();

            } catch (Exception e) {
                System.out.println(
                        "[ERRO] Não foi possível gerar, gravar ou ler o arquivo de export da planilha de log (export/xlsx/*.xlsx)");
            }
        }

        // Output terminal
        System.out.println("\n\nDate/Time: " + dateNow);
        System.out.println("File: " + problemInfo.getName());
        System.out.println("Temperatura: " + temperature);
        System.out.println("Fator de Resfriamento: " + coolFactor);
        System.out.println("Tempo de execução: " + stopTimeMs + " ms (" + stopTimeSe + " s)");
        System.out.println("Razão de parada: " + stopReason);
        System.out.println("Iterações: " + (iteration - 1));
        System.out.println("Atualizações: " + (update - 1));
        System.out.println("Melhor solução: " + best.getDistance());
        System.out.println("Última solução: " + current.getDistance());
        System.out.println("Temperatura da melhor solução: " + sol_t);
        System.out.println("Temperatura atual: " + t + (t < 0.5 ? " (fim)" : ""));

        if (!isLoopExecution) {
            System.out.println("\n[Pressione enter para continuar]");
            s.nextLine();
        } else {
            System.out.println("\n");
        }

        if (best.getDistance() > 34000) {
            solutionFile.delete();
            optTourFile.delete();
        }
    }

    public static void setConfig(ExecutionConfig executionConfig) {
        // estrategia de resfriamento
        temperature = executionConfig.getTemperature();
        scheduleType = executionConfig.getScheduleType() == 1;
        double auxCoolFac = executionConfig.getCoolFactor();
        if (scheduleType) {
            if (auxCoolFac >= 1) {
                coolFactor = 1 - Math.pow(10, -auxCoolFac);
            } else {
                coolFactor = auxCoolFac;
            }
        } else {
            if (auxCoolFac >= 1) {
                coolFactor = Math.pow(10, -auxCoolFac);
            } else {
                coolFactor = auxCoolFac;
            }
        }

        // criterios de parada
        String aux;
        aux = executionConfig.getIterLimit();
        iterLimit = aux.equals("disabled") ? 0L : Long.valueOf(aux);
        aux = executionConfig.getTimelimit();
        timeLimit = aux.equals("disabled") ? 0L : Long.valueOf(aux);
        aux = executionConfig.getStopOnSol();
        stopOnSol = aux.equals("disabled") ? 0L : Long.valueOf(aux);
        // export
        exportOptTour = executionConfig.getExpOptTour();
        exportSolutions = executionConfig.getExpSolution();
        exportXLSX = executionConfig.getExpXLSXLog();
        // other
        isLoopExecution = executionConfig.getExecutions() > 1 ? true : false;
    }

    public static void setProblemInfo(ProblemInfo problemInfo) {
        SimulatedAnnealing.problemInfo = problemInfo;
    }

    public static void setProblemPlotting(ArrayList<Integer> indexes, ArrayList<Double> xCoords,
            ArrayList<Double> yCoords) {
        SimulatedAnnealing.indexes = indexes;
        SimulatedAnnealing.xCoords = xCoords;
        SimulatedAnnealing.yCoords = yCoords;
    }

    public enum StopReason {
        RESFRIAMENTO_COMPLETO, LIMITE_DE_ITERACOES, LIMITE_DE_TEMPO, SOL_IDEAL_ENCONTRADA;
    }
}
