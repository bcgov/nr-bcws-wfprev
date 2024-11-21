package ca.bc.gov.nrs.wfprev.common.enums;

public enum CodeTables {
  FOREST_AREA("forestAreaCodes"), 
  GENERAL_SCOPE("generalScopeCodes"),
  PROJECT_TYPE("projectTypeCodes");

  private final String text;
  private CodeTables(final String text) {
      this.text = text;
  }
  @Override
  public String toString() {
      return text;
  }
}
