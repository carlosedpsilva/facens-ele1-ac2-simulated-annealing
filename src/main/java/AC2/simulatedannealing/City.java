package AC2.simulatedannealing;

public class City {
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