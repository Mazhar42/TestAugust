def getCalculationType() {
  return "Linear"
}

def getTargetType() {
  return "Multi"
}

def getTargetValueType() {
  return "Amount"
}

def getIncrementValueType() {
  return null
}

def getRebateValueType() {
  return "Percent"
}

Map getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

def calculationRebate(Map params) {
  def baselineValue = params?.get("BaselineValueCurrentPeriod") as BigDecimal
  def linearInputs = params?.get("LinearInput")

  if (!linearInputs || baselineValue == null) return BigDecimal.ZERO
  return libs.RebateManager.Util.getLinearRebateFactorFromInput(linearInputs, baselineValue, getRebateValueType(), params.isMax)
}
