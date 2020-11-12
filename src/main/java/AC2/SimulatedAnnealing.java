package AC2;

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

public class SimulatedAnnealing {

    // Executin

    private double temperature;
    private double coolingFactor;
    private long maxIterations;
    private long timeLimit;
    private boolean isLoopExecution;

    // Build

    private static ArrayList<Integer> indexes;
    private static ArrayList<Double> xCoords;
    private static ArrayList<Double> yCoords;

    private static ArrayList<City> cities;

    // Export
    private boolean exportOptTour;
    private boolean exportSolutions;
    private boolean exportXLSX;

    // Settings and Other
    public static ProblemInfo problemInfo;

    private static XSSFWorkbook wb;

    private static Scanner s = new Scanner(System.in);

    public SimulatedAnnealing(double temperature, double coolingFactor, long maxIterations, long timeLimit,
            boolean exportOptTour, boolean exportSolutions, boolean exportXLSX, boolean isLoopExecution) {
        this.temperature = temperature;
        this.coolingFactor = coolingFactor;
        this.maxIterations = maxIterations;
        this.timeLimit = timeLimit;
        this.exportOptTour = exportOptTour;
        this.exportSolutions = exportSolutions;
        this.exportXLSX = exportXLSX;
        this.isLoopExecution = isLoopExecution;

        wb = null;
        System.gc();

        solve();
    }

    public void solve() {

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
        File solutionFolder = new File("export/solutions/");
        File solutionFile = new File("export/solutions/" + dateNow + "_" + problemInfo.getName() + ".txt");

        if (exportSolutions) {

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

        double t;
        double sol_t;
        boolean isUpdate;
        boolean isNewBest;
        StopReason stopReason = StopReason.RESFRIAMENTO_COMPLETO;

        Runtime runtime = Runtime.getRuntime();

        System.out.println("[INFO] Processando solução");

        for (t = temperature, sol_t = temperature; t > 0.5; t *= coolingFactor) {
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
            }

            // Exportar soluções
            if (exportSolutions) {
                long time = (System.nanoTime() - startTime) / 1_000_000;

                // Write in Solution File
                if (exportSolutions) {
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
            if (maxIterations != 0 && iteration > maxIterations) {
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
        }

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
        if (exportOptTour) {

            System.out.println("[INFO] Criando arquivo de exportação .opt.tour");

            File optTourFolder = new File("export/opttour/");
            File optTourFile = new File("export/opttour/" + dateNow + "_" + problemInfo.getName() + ".opt.tour.txt");

            try {
                if (!optTourFolder.mkdirs()) {
                    throw new IOException(
                            "[ERRO] Não foi possível gerar o arquivo de export do tour (export/opttour/*.opt.tour.txt)");
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
        if (exportXLSX) {

            System.out.println("[INFO] Registrando na planilha o log de execução");

            FileOutputStream xlsxLogFileOut = null;
            XSSFSheet mainSheet = null;

            File xlsxFolder = new File("export/xlsx/");
            File logFile = new File("export/xlsx/execution_log.xlsx");

            try {
                // Criar pasta se não existir
                if (!xlsxFolder.exists()) {
                    if (!xlsxFolder.mkdirs()) {
                        throw new IOException(
                                "[ERRO] Não foi possível gerar o arquivo de export da planilha de execução (export/solutions/*.txt)");
                    }
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
                XSSFCell temperature = logLine.createCell(2);
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
                temperature.setCellValue(this.temperature);
                cool_factor.setCellValue(this.coolingFactor);
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
        System.out.println("Temperatura: " + this.temperature);
        System.out.println("Fator de Resfriamento: " + this.coolingFactor);
        System.out.println("Tempo de execução: " + stopTimeMs + " ms (" + stopTimeSe + " s)");
        System.out.println("Razão de parada: " + stopReason);
        System.out.println("Iterações: " + (iteration - 1));
        System.out.println("Atualizações: " + (update - 1));
        System.out.println("Melhor solução: " + best.getDistance());
        System.out.println("Última solução: " + current.getDistance());
        System.out.println("Temperatura da melhor solução: " + sol_t + (t < 0.5 ? " (fim)" : ""));
        System.out.println("Temperatura atual: " + t + (t < 0.5 ? " (fim)" : ""));

        System.out.println("\n\n[DEBUG] Total Memory (in bytes): " + runtime.totalMemory());
        System.out.println("[DEBUG] Free Memory (in bytes): " + runtime.freeMemory());
        System.out.println("[DEBUG] Max Memory (in bytes): " + runtime.maxMemory());

        if (!isLoopExecution) {
            System.out.println("\n[Pressione enter para continuar]");
            s.nextLine();
        } else {
            System.out.println("\n");
        }
    }

    public static void setProblemPlotting(ArrayList<Integer> indexes, ArrayList<Double> xCoords,
            ArrayList<Double> yCoords) {
        SimulatedAnnealing.indexes = indexes;
        SimulatedAnnealing.xCoords = xCoords;
        SimulatedAnnealing.yCoords = yCoords;
    }

    public static void setProblemInfo(ProblemInfo info) {
        SimulatedAnnealing.problemInfo = info;
    }

    public enum StopReason {
        RESFRIAMENTO_COMPLETO(1), LIMITE_DE_ITERACOES(2), LIMITE_DE_TEMPO(3),;

        private final int id;

        StopReason(int id) {
            this.id = id;
        }

        public static StopReason fromId(int reasonId) {
            for (StopReason reason : values()) {
                if (reason.getId() == reasonId) {
                    return reason;
                }
            }
            return RESFRIAMENTO_COMPLETO;
        }

        public int getId() {
            return id;
        }
    }

    public static class City {
        private int index;
        private double x;
        private double y;

        public City(int index, double x, double y) {
            this.index = index;
            this.x = x;
            this.y = y;
        }

        public int getIndex() {
            return index;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        @Override
        public String toString() {
            return "[ " + index + " ]";
        }
    }

    public static class Tour {
        private ArrayList<City> cities;
        private int distance;

        public Tour(ArrayList<City> cities) {
            this.cities = new ArrayList<>(cities);
        }

        public static Tour initAndShuffle(ArrayList<City> cities) {
            Tour tour = new Tour(cities);
            tour.shuffle();
            return tour;
        }

        public City getCity(int index) {
            return this.cities.get(index);
        }

        public void shuffle() {
            Collections.shuffle(cities);
        }

        public int getTourLenght() {
            if (distance != 0)
                return distance;

            int totalDistance = 0;

            for (int i = 0; i < numberOfCities(); i++) {
                City start = getCity(i);
                City end = getCity(i + 1 < numberOfCities() ? i + 1 : 0);
                totalDistance += Util.distance(start, end);
            }

            distance = totalDistance;
            return totalDistance;
        }

        public Tour duplicate() {
            return new Tour(new ArrayList<>(cities));
        }

        public int numberOfCities() {
            return cities.size();
        }

        @Override
        public String toString() {
            return cities.toString();
        }

        public ArrayList<Integer> getIndexes() {
            ArrayList<Integer> indexes = new ArrayList<>();
            for (City city : cities) {
                indexes.add(city.getIndex());
            }
            return indexes;
        }

        public ArrayList<City> getCities() {
            return cities;
        }

        public int getDistance() {
            return getTourLenght();
        }
    }

    public static class Util {
        public static double probability(double f1, double f2, double temp) {
            if (f2 < f1)
                return 1;
            return Math.exp((f1 - f2) / temp);
        }

        public static double distance(City city1, City city2) {
            double xDist = Math.abs(city1.getX() - city2.getX());
            double yDist = Math.abs(city1.getY() - city2.getY());
            return Math.sqrt(xDist * xDist + yDist * yDist);
        }
    }
}
