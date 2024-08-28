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
  return "Amount"
}

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {
  def target = params?.get("TargetInput") as BigDecimal
  def rebate = params?.get("RebateInput") as BigDecimal
  def baselineQuantity = params?.get("BaselineQuantityCurrentPeriod") as BigDecimal

  if (baselineQuantity > target || params.isMax) {
    return (rebate ?: BigDecimal.ZERO)
  }
  return BigDecimal.ZERO
}