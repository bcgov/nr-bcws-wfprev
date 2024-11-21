package ca.bc.gov.nrs.wfprev.common.enums;

public enum CodeTables {
  FOREST_AREA("forestAreaCode"), 
  GENERAL_SCOPE("generalScopeCode");

  private final String text;
  private CodeTables(final String text) {
      this.text = text;
  }
  @Override
  public String toString() {
      return text;
  }
}
