import net.pricefx.server.dto.TieredValue

def getCalculationType() {
  def calculationTypeInputName = getCalculationTypeInputName()
  return getValueFromConfigurator(calculationTypeInputName)
}

def getTargetType() {
  return (getCalculationType() == "Growth") ? "Multi" : null
}

def getTargetValueType() {
  if (getCalculationType() == "Growth") {
    def targetMetricInputName = getTargetMetricInputName()
    return getValueFromConfigurator(targetMetricInputName)
  }
}

def getIncrementValueType() {
  return null
}

def getRebateValueType() {
  def rebateValueTypeInputName = (getCalculationType() == "Growth") ? getIncentiveTargetMetricInputName() : getFixedRebateTypeInputName()
  return getValueFromConfigurator(rebateValueTypeInputName)
}

def getPreviousPeriodInfo() {
  return ["Number": -1, "Unit": "YEAR"]
}

BigDecimal calculationRebate(Map params) {
  if (getCalculationType() == "Growth") {
    def rebateValueType = getRebateValueType()
    def targetValueType = getTargetValueType()

    if (targetValueType == "Amount" && rebateValueType == "Amount") {
      return libs.RebateManager.GrowthAbsoluteAmount.calculationRebate(params)
    } else if (targetValueType == "Amount" && rebateValueType == "Percent") {
      return libs.RebateManager.GrowthAbsolutePercent.calculationRebate(params)
    } else if (targetValueType == "Percent" && rebateValueType == "Amount") {
      return libs.RebateManager.GrowthPercentAmount.calculationRebate(params)
    } else if (targetValueType == "Percent" && rebateValueType == "Percent") {
      return libs.RebateManager.GrowthPercentPercent.calculationRebate(params)
    }
  } else {
    def fixedRebateInputName = getFixedRebateInputName()
    BigDecimal rebate = getValueFromConfigurator(fixedRebateInputName) as BigDecimal
    return (rebate ?: BigDecimal.ZERO)
  }
}

def getFlexTypeOptions() {
  return ["Fixed", "Growth"]
}

def getGrowthMetricOptions() {
  return ["Margin", "Revenue", "Volume"]
}

def getTargetMetricOptions() {
  return ["Amount", "Percent"]
}

def getCurrencyOptions() {
  return libs.SharedLib.CacheUtils.getOrSet("currencies", [], {return getCurrencies()})
}

List getCurrencies() {
  def conversionUtil = libs.CPQ_SharedLib.ConversionUtils
  List currencies = conversionUtil.getDefinedCurrencies()

  return currencies
}

def getConfiguratorName() {
  return "Inputs"
}

def getCalculationTypeInputName() {
  return "CalculationType"
}

def getCurrencyInputName() {
  return "Currency"
}

def getTargetMetricInputName() {
  return "TargetMetric"
}

def getGrowthMetricInputName() {
  return "TargetValue"
}

def getIncentiveTargetMetricInputName() {
  return "RebateMetric"
}

def getIncentiveCurrencyInputName() {
  return "RebateCurrency"
}

def getValueFromConfigurator(inputName) {
  def configuratorName = getConfiguratorName()

  return api.input(configuratorName)?.get(inputName)
}

def getTiers(Map configurator, String sourceName) {
  Map configuratorTier = configurator?.get("Tier")
  TieredValue tier = TieredValue.fromMap(configuratorTier)
  def targetCurrency = getTargetCurrency()
  def rebateCurrency = getRebateValueCurrency()
  String finalCurrency = libs.RebateManager.Util.getBaseCurrencyOfDatamart(sourceName)
  Date date = libs.CPQ_SharedLib.TimeUtils.getCurrentDate()
  Map exchangeRateMap = getExchangeRates(date)
  BigDecimal targetExchangeRate = exchangeRateMap?.get(targetCurrency)?.get(finalCurrency)?.getAt(0)?.attribute1?.toBigDecimal()
  BigDecimal rebateExchangeRate = exchangeRateMap?.get(rebateCurrency)?.get(finalCurrency)?.getAt(0)?.attribute1?.toBigDecimal()

  if (tier) {
    if ("Amount".equalsIgnoreCase(targetValueType)) {

      if ("Amount".equalsIgnoreCase(rebateValueType)) {
        if (tier) {
          if (targetExchangeRate) tier = tier.multiplyTargets(targetExchangeRate)
          if (rebateExchangeRate) tier = tier.multiplyValues(rebateExchangeRate)
        }
      } else if ("Percent".equalsIgnoreCase(rebateValueType)) {
        if (tier) {
          if (targetExchangeRate) tier = tier.multiplyTargets(targetExchangeRate)
          tier = tier.multiplyValues(0.01)
        }
      }

    } else if ("Percent".equalsIgnoreCase(targetValueType)) {
      if ("Amount".equalsIgnoreCase(rebateValueType)) {
        if (tier) {
          if (rebateExchangeRate) tier = tier.multiplyValues(rebateExchangeRate)
          tier = tier.multiplyTargets(0.01)
        }

      } else if ("Percent".equalsIgnoreCase(rebateValueType)) {
        if (tier) {
          tier = tier.multiplyTargets(0.01)
          tier = tier.multiplyValues(0.01)
        }
      }
    }
  }

  return tier
}

String getTargetValueDMField(String targetValue) {
  Map dmFieldMap = [
          Margin: "Margin",
          Revenue: "InvoicePrice",
          Volume: "Quantity",
  ]

  return dmFieldMap?.get(targetValue)
}

def getTargetCurrency() {
    def currencyInputName = getCurrencyInputName()
    return getValueFromConfigurator(currencyInputName)
}

def getRebateValueCurrency() {
    def currencyInputName = getIncentiveCurrencyInputName()
    return getValueFromConfigurator(currencyInputName)
}

Map getExchangeRates(Date date) {
  String tableName = "ExchangeRates"
  def dateUtils = libs.CustomerInsights.DateUtils
  List filters = [
          Filter.equal("lookupTable.id", api.findLookupTable(tableName).id),
          Filter.lessOrEqual("key3", dateUtils.formatDateToString(date))
  ]
  String typeCode = "MLTV3"
  List fields = ["key1", "key2", "key3", "attribute1"]
  String sortAttribute = "-key3"
  return libs.ChemicalsContractUtils.FindUtils.findAllDistinct(typeCode, sortAttribute, fields, *filters)?.groupBy ({it.key1}, {it.key2})
}

def getFixedRebateTypeInputName() {
  return "FixedRebateType"
}

def getFixedRebateInputName() {
  return "FixedRebate"
}