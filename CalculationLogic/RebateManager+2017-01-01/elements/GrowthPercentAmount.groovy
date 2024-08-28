def getCalculationType() {
  return "Growth"
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
  return "Amount"
}

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {

  def multiTierEntry = params?.get("TieredRebateInput")
  BigDecimal actualBaselineValueCurrentPeriod = (params?.get("BaselineValueCurrentPeriod") ?: 0) as BigDecimal
  BigDecimal actualBaselineValuePreviousPeriod = (params?.get("BaselineValuePreviousPeriod") ?: 0) as BigDecimal

  if (multiTierEntry == null || multiTierEntry?.size() == 0) return BigDecimal.ZERO

  BigDecimal baselineValue

  if (actualBaselineValueCurrentPeriod == BigDecimal.ZERO && actualBaselineValuePreviousPeriod == BigDecimal.ZERO) {
    baselineValue = BigDecimal.ZERO
  } else if (actualBaselineValuePreviousPeriod == BigDecimal.ZERO) {
    baselineValue = 1
  } else {
    baselineValue = (actualBaselineValueCurrentPeriod - actualBaselineValuePreviousPeriod) / actualBaselineValuePreviousPeriod
  }
  return libs.RebateManager.Util.getRebateFactorFromMultiTierEntry(multiTierEntry, baselineValue)
}

