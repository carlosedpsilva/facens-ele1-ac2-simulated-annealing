package AC2.util;

import AC2.simulatedannealing.City;

public class Util {
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