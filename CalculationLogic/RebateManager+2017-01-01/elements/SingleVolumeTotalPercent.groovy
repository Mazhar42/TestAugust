def getCalculationType() {
  return "Conditional"
}

def getTargetType() {
  return "Single"
}

def getTargetValueType() {
  return "Quantity"
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
  def target = params?.get("TargetInput") as BigDecimal
  def rebate = params?.get("RebateInput") as BigDecimal
  def baselineValue = params?.get("BaselineValueCurrentPeriod") as BigDecimal
  def baselineQuantity = params?.get("BaselineQuantityCurrentPeriod") as BigDecimal
  def isGetRebateFactor = params?.get("GetRebateFactor") as Boolean

  if (baselineQuantity > target || params.isMax) {
    if (isGetRebateFactor) { // just return rebate factor, not rebate value
      return rebate ?: BigDecimal.ZERO
    } else {
      return (rebate ?: BigDecimal.ZERO) * (baselineValue ?: BigDecimal.ZERO)
    }
  }
  return BigDecimal.ZERO
}
