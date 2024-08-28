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
  return "Amount"
}

Map getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {
  def baselineValue = params?.get("BaselineValueCurrentPeriod") as BigDecimal
  def linearInputs = params?.get("LinearInput") //params?.get("TieredRebateInput")

  if (!linearInputs || baselineValue == null) return BigDecimal.ZERO

  return libs.RebateManager.Util.getLinearRebateFactorFromInput(linearInputs, baselineValue, getRebateValueType(), params.isMax)
}
