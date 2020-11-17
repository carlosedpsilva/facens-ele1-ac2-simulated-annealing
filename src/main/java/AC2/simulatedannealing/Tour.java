package AC2.simulatedannealing;

import java.util.ArrayList;
import java.util.Collections;

import AC2.util.Util;

public class Tour {
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