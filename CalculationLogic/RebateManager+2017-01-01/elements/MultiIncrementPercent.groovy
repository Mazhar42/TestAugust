def getCalculationType() {
  return "Increment"
}

def getTargetType() {
  return "Multi"
}

def getTargetValueType() {
  return "Amount"
}

def getIncrementValueType() {
  return "Amount"
}

def getRebateValueType() {
  return "Percent"
}

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {

  def multiIncrementInput = params?.get("MultiIncrementRebateInput")
  BigDecimal baselineValue = params?.get("BaselineValueCurrentPeriod") as BigDecimal

  return libs.RebateManager.Util.getSteppedRebateFactorFromMatrixInput(multiIncrementInput, baselineValue, getRebateValueType(), params.isMax)
}
