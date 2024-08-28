
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
  return "Percent"
}

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

def calculationRebate(Map params) {
  def target = params?.get("TargetInput") as BigDecimal
  def rebate = params?.get("RebateInput") as BigDecimal
  def baselineValue = params?.get("BaselineValueCurrentPeriod") as BigDecimal
  def isGetRebateFactor = params?.get("GetRebateFactor") as Boolean
  BigDecimal value = BigDecimal.ZERO
  if (baselineValue > target || params.isMax) {

    if (isGetRebateFactor) { // just return rebate factor, not rebate value
      value = rebate ?: BigDecimal.ZERO
    } else {
      value = (rebate ?: BigDecimal.ZERO) * (baselineValue ?: BigDecimal.ZERO)
    }
  }
  return value
}
