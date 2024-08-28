def getCalculationType() {
  return "GrowthStepped"
}

def getTargetType() {
  return "Multi"
}

def getTargetValueType() {
  return "Percent"
}

def getIncrementValueType() {
  return null
}

def getRebateValueType() {
  return "Percent"
}

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {

  def multiTierEntry = params?.get("TieredRebateInput")
  BigDecimal actualBaselineValueCurrentPeriod = (params?.get("BaselineValueCurrentPeriod") ?: 0) as BigDecimal
  BigDecimal actualBaselineValuePreviousPeriod = (params?.get("BaselineValuePreviousPeriod") ?: 0) as BigDecimal

  if (!multiTierEntry) return BigDecimal.ZERO
  return libs.RebateManager.Util.getGrowthSteppedRebateFactorFromMultiTierEntry(multiTierEntry,
      actualBaselineValuePreviousPeriod,
      actualBaselineValueCurrentPeriod,
      getTargetValueType(),
      getRebateValueType())
}
