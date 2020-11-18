package AC2;

import java.util.Scanner;

import AC2.chartgenerator.MainCG;
import AC2.simulatedannealing.MainSA;

public class Main {
  public static Scanner s = new Scanner(System.in);

  public static void main(String[] args) {
    String input;
    do {
      System.out.println("\n\nRECOZIMENTO SIMULADO");
      System.out.println("1) Executar");
      System.out.println("2) Gerar GIF");
      System.out.println("3) Sair");
      System.out.print("> ");
      input = s.nextLine();

      switch (input) {
        case "1":
          MainSA.main(args);
          break;
        case "2":
          MainCG.main(args);
          break;
        case "3":
          System.exit(1);
          break;
        default:
          System.out.println("\n\n[ERRO] Opção inválida");
          break;
      }
    } while (true);
  }
}
