package AC2;

public class ProblemInfo {
  // From file
  private String name;
  private String description;
  private String type;
  private Integer dimension;
  private String edgeWeightType;

  @Override
  public String toString() {
    return "NAME : " + name + "; COMMENT : " + description + "; TYPE : " + type + "; DIMENSION : " + dimension
        + "; EDGE_WEIGHT_TYPE : " + edgeWeightType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getDimension() {
    return dimension;
  }

  public void setDimension(Integer dimension) {
    this.dimension = dimension;
  }

  public String getEdgeWeightType() {
    return edgeWeightType;
  }

  public void setEdgeWeightType(String edgeWeightType) {
    this.edgeWeightType = edgeWeightType;
  }
}