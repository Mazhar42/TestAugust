/*
  Because Rebate Type is a hard code at name
 */

def getCalculationType() {
  return "Conditional"
}

def getTargetType() {
  return "Single"
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

def calculationRebate(Map params) {
  def target = params?.get("TargetInput") as BigDecimal
  def rebate = params?.get("RebateInput") as BigDecimal
  def baselineValue = params?.get("BaselineValueCurrentPeriod") as BigDecimal

  if (baselineValue > target || params.isMax) {
    return rebate ?: BigDecimal.ZERO
  }
  return BigDecimal.ZERO
}
