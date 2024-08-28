def getCalculationType() {
  return "Conditional"
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

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {
  def baselineValue = params?.get("BaselineValueCurrentPeriod") as BigDecimal
  def multiTierEntry = params?.get("TieredRebateInput")
  def isGetRebateFactor = params?.get("GetRebateFactor") as Boolean

  if (!multiTierEntry || baselineValue == null) return BigDecimal.ZERO

  BigDecimal rebateValue = libs.RebateManager.Util.getRebateFactorFromMultiTierEntry(multiTierEntry, baselineValue, params.isMax)

  if (isGetRebateFactor) {
    return rebateValue ?: BigDecimal.ZERO
  }
  return (rebateValue ?: BigDecimal.ZERO) * (baselineValue ?: BigDecimal.ZERO)
}
