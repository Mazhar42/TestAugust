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
  return "Amount"
}

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {
  BigDecimal rebate = params?.get("RebateInput") as BigDecimal
  return (rebate ?: BigDecimal.ZERO)
}

