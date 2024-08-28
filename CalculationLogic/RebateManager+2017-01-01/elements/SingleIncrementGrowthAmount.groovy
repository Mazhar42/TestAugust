
def getCalculationType() {
  return "IncrementGrowth"
}

def getTargetType() {
  return "Single"
}

def getTargetValueType() {
  return "Percent"
}

def getIncrementValueType() {
  return "Amount"
}

def getRebateValueType() {
  return "Amount"
}

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {

  BigDecimal target = (params?.get("TargetInput") ?: 0) as BigDecimal
  BigDecimal increment = (params?.get("IncrementInput") ?: 0) as BigDecimal
  BigDecimal rebate = (params?.get("RebateInput") ?: 0) as BigDecimal
  BigDecimal actualBaselineValueCurrentPeriod = (params?.get("BaselineValueCurrentPeriod") ?: 0) as BigDecimal
  BigDecimal actualBaselineValuePreviousPeriod = (params?.get("BaselineValuePreviousPeriod") ?: 0) as BigDecimal

  BigDecimal targetBaselineValueCurrentPeriod = actualBaselineValuePreviousPeriod * (1 + target) //percent
  Integer incrementFactor = 0
  if (targetBaselineValueCurrentPeriod < actualBaselineValueCurrentPeriod && increment != 0) {
    incrementFactor = (((actualBaselineValueCurrentPeriod - targetBaselineValueCurrentPeriod) / increment) as BigDecimal).toInteger()
  }
  return (rebate ?: BigDecimal.ZERO) * incrementFactor
}
