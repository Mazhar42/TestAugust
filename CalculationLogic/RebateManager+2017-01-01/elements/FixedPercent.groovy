def getCalculationType() {
  return "Fixed"
}

def getTargetType() {
  return null
}

def getTargetValueType() {
  return null
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
  def rebate = params?.get("RebateInput") as BigDecimal
  def baselineValue = params?.get("BaselineValueCurrentPeriod") as BigDecimal
  def isGetRebateFactor = params?.get("GetRebateFactor") as Boolean

  if (isGetRebateFactor) {
    return rebate ?: BigDecimal.ZERO
  } else {
    return (rebate ?: BigDecimal.ZERO) * (baselineValue ?: BigDecimal.ZERO)
  }

}

