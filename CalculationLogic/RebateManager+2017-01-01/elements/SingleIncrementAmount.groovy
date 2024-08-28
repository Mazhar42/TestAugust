def getCalculationType() {
  return "Increment"
}

def getTargetType() {
  return "Single"
}

def getTargetValueType() {
  return "Amount"
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
  def target = params?.get("TargetInput") as BigDecimal
  def increment = params?.get("IncrementInput")
  def rebate = params?.get("RebateInput") as BigDecimal
  def baselineValue = params?.get("BaselineValueCurrentPeriod") as BigDecimal

  BigDecimal incrementValue = (baselineValue ?: BigDecimal.ZERO) - (target ?: BigDecimal.ZERO)
  BigDecimal rebateValue = BigDecimal.ZERO
  if ((incrementValue > BigDecimal.ZERO || params.isMax) && increment) {
    rebateValue = ((incrementValue / increment) as BigDecimal).toInteger() * (rebate ?: BigDecimal.ZERO)
  }
  return rebateValue
}
