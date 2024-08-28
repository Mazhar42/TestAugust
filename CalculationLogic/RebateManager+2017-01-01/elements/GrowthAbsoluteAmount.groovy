def getCalculationType() {
  return "Growth"
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

  def multiTierEntry = params?.get("TieredRebateInput")
  BigDecimal actualBaselineValueCurrentPeriod = (params?.get("BaselineValueCurrentPeriod") ?: 0) as BigDecimal
  BigDecimal actualBaselineValuePreviousPeriod = (params?.get("BaselineValuePreviousPeriod") ?: 0) as BigDecimal

  BigDecimal baselineValue = actualBaselineValueCurrentPeriod - actualBaselineValuePreviousPeriod

  if (!multiTierEntry || baselineValue == null) return BigDecimal.ZERO
  return libs.RebateManager.Util.getRebateFactorFromMultiTierEntry(multiTierEntry, baselineValue)
}
