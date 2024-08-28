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
  return "Percent"
}

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {

  def multiTierEntry = params?.get("TieredRebateInput")
  BigDecimal actualBaselineValueCurrentPeriod = (params?.get("BaselineValueCurrentPeriod") ?: 0) as BigDecimal
  BigDecimal actualBaselineValuePreviousPeriod = (params?.get("BaselineValuePreviousPeriod") ?: 0) as BigDecimal
  def isGetRebateFactor = params?.get("GetRebateFactor") as Boolean

  if (!multiTierEntry) return BigDecimal.ZERO

  BigDecimal baselineValue

  if (actualBaselineValueCurrentPeriod == BigDecimal.ZERO && actualBaselineValuePreviousPeriod == BigDecimal.ZERO) {
    baselineValue = BigDecimal.ZERO
  } else if (actualBaselineValuePreviousPeriod == BigDecimal.ZERO) {
    baselineValue = 1
  } else {
    baselineValue = (actualBaselineValueCurrentPeriod - actualBaselineValuePreviousPeriod) / actualBaselineValuePreviousPeriod
  }

  BigDecimal rebateValue = libs.RebateManager.Util.getRebateFactorFromMultiTierEntry(multiTierEntry, baselineValue)
  if (isGetRebateFactor) {
    return rebateValue ?: BigDecimal.ZERO
  }
  return (rebateValue ?: BigDecimal.ZERO) * (actualBaselineValueCurrentPeriod - actualBaselineValuePreviousPeriod)

}
