package AC2.simulatedannealing;

public class ExecutionConfig {
  private double temperature;
  private double coolFactor;
  private int scheduleType;
  private int executions;
  private String iterLimit;
  private String timelimit;
  private String stopOnSol;
  private String expOptTour;
  private String expSolution;
  private String expXLSXLog;

  private double defaultTemperature = 1000.0;
  private double defaultCoolFactor = 4.0;
  private int defaultScheduleType = 1;
  private int defaultExecutions = 1;
  private String defaultIterLimit = "disabled";
  private String defaultTimelimit = "disabled";
  private String defaultStopOnSol = "disabled";
  private String defaultExpOptTour = "false";
  private String defaultExpSolution = "false";
  private String defaultExpXLSXLog = "true";

  public ExecutionConfig() {
    temperature = defaultTemperature;
    coolFactor = defaultCoolFactor;
    scheduleType = defaultScheduleType;
    executions = defaultExecutions;
    iterLimit = defaultIterLimit;
    timelimit = defaultTimelimit;
    stopOnSol = defaultStopOnSol;
    expOptTour = defaultExpOptTour;
    expSolution = defaultExpSolution;
    expXLSXLog = defaultExpXLSXLog;
  }

  public void setNewDefaults() {
    defaultTemperature = temperature;
    defaultCoolFactor = coolFactor;
    defaultScheduleType = scheduleType;
    defaultExecutions = executions;
    defaultIterLimit = iterLimit;
    defaultTimelimit = timelimit;
    defaultStopOnSol = stopOnSol;
    defaultExpOptTour = expOptTour;
    defaultExpSolution = expSolution;
    defaultExpXLSXLog = expXLSXLog;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public double getCoolFactor() {
    return coolFactor;
  }

  public void setCoolFactor(double coolFactor) {
    this.coolFactor = coolFactor;
  }

  public int getScheduleType() {
    return scheduleType;
  }

  public void setScheduleType(int scheduleType) {
    this.scheduleType = scheduleType;
  }

  public int getExecutions() {
    return executions;
  }

  public void setExecutions(int executions) {
    this.executions = executions;
  }

  public String getIterLimit() {
    return iterLimit;
  }

  public void setIterLimit(String iterLimit) {
    this.iterLimit = iterLimit;
  }

  public String getTimelimit() {
    return timelimit;
  }

  public void setTimelimit(String timelimit) {
    this.timelimit = timelimit;
  }

  public String getStopOnSol() {
    return stopOnSol;
  }

  public void setStopOnSol(String stopOnSol) {
    this.stopOnSol = stopOnSol;
  }

  public String getExpOptTour() {
    return expOptTour;
  }

  public void setExpOptTour(String expOptTour) {
    this.expOptTour = expOptTour;
  }

  public String getExpSolution() {
    return expSolution;
  }

  public void setExpSolution(String expSolution) {
    this.expSolution = expSolution;
  }

  public String getExpXLSXLog() {
    return expXLSXLog;
  }

  public void setExpXLSXLog(String expXLSXLog) {
    this.expXLSXLog = expXLSXLog;
  }

  public double getDefaultTemperature() {
    return defaultTemperature;
  }

  public void setDefaultTemperature(double defaultTemperature) {
    this.defaultTemperature = defaultTemperature;
  }

  public double getDefaultCoolFactor() {
    return defaultCoolFactor;
  }

  public void setDefaultCoolFactor(double defaultCoolFactor) {
    this.defaultCoolFactor = defaultCoolFactor;
  }

  public int getDefaultScheduleType() {
    return defaultScheduleType;
  }

  public void setDefaultScheduleType(int defaultScheduleType) {
    this.defaultScheduleType = defaultScheduleType;
  }

  public int getDefaultExecutions() {
    return defaultExecutions;
  }

  public void setDefaultExecutions(int defaultExecutions) {
    this.defaultExecutions = defaultExecutions;
  }

  public String getDefaultIterLimit() {
    return defaultIterLimit;
  }

  public void setDefaultIterLimit(String defaultIterLimit) {
    this.defaultIterLimit = defaultIterLimit;
  }

  public String getDefaultTimelimit() {
    return defaultTimelimit;
  }

  public void setDefaultTimelimit(String defaultTimelimit) {
    this.defaultTimelimit = defaultTimelimit;
  }

  public String getDefaultStopOnSol() {
    return defaultStopOnSol;
  }

  public void setDefaultStopOnSol(String defaultStopOnSol) {
    this.defaultStopOnSol = defaultStopOnSol;
  }

  public String getDefaultExpOptTour() {
    return defaultExpOptTour;
  }

  public void setDefaultExpOptTour(String defaultExpOptTour) {
    this.defaultExpOptTour = defaultExpOptTour;
  }

  public String getDefaultExpSolution() {
    return defaultExpSolution;
  }

  public void setDefaultExpSolution(String defaultExpSolution) {
    this.defaultExpSolution = defaultExpSolution;
  }

  public String getDefaultExpXLSXLog() {
    return defaultExpXLSXLog;
  }

  public void setDefaultExpXLSXLog(String defaultExpXLSXLog) {
    this.defaultExpXLSXLog = defaultExpXLSXLog;
  }

}
