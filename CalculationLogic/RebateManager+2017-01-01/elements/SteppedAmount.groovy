def getCalculationType() {
  return "Stepped"
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

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {
  def baselineValue = params?.get("BaselineValueCurrentPeriod") as BigDecimal
  def multiTierEntry = params?.get("TieredRebateInput")

  if (!multiTierEntry || baselineValue == null) return BigDecimal.ZERO
  return libs.RebateManager.Util.getSteppedRebateFactorFromMultiTierEntry(multiTierEntry, baselineValue, getRebateValueType(), params.isMax)
}
