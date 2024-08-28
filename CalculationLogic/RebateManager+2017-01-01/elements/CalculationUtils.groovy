/*
  Calculation Util functions
 */

//---PREVIOUS---------------------
BigDecimal calculatePreviousBaselineValue(Map params) {
  Map localParams = params?.clone()
  localParams.PreviousData = getPreviousData(params)
  localParams.BaselineType = "Value"
  return calculatePreviousBaseline(localParams)
}

BigDecimal calculatePreviousBaselineQuantity(Map params) {
  Map localParams = params.clone()
  localParams.PreviousData = getPreviousData(params)
  localParams.BaselineType = "Quantity"
  def value = calculatePreviousBaseline(localParams)
  return value
}

BigDecimal calculatePreviousRebateValue(Map params) {
  Map localParams = params.clone()
  localParams.PreviousData = getPreviousData(params)
  localParams.BaselineType = "Rebate"
  return calculatePreviousBaseline(localParams)
}

BigDecimal calculatePreviousBaseline(Map params) {

  BigDecimal value
  if (libs.RebateManager.Util.isRebateRecordContext()) {// check current context
    value = calculatePreviousBaselineInRebateRecord(params)
  } else {
    value = calculatePreviousBaselineInAgreement(params)
  }
  return value
}

protected BigDecimal calculatePreviousBaselineInRebateRecord(Map params) {
  List previousData = params.PreviousData
  String periodName = params.Name
  String baselineType = params.BaselineType
  BigDecimal value = BigDecimal.ZERO
  for (item in previousData) {
    /*
              Because:
                 item.getAt("PeriodName") = 2018-M01
                 periodName = 2019-M01
              --> need to convert and compare
             */
    isEqual = libs.RebateManager.Util.comparePeriodNameForPreviousPeriod(item.getAt("PeriodName"), periodName)
    if (isEqual) {
      if ("Quantity".equalsIgnoreCase(baselineType)) {
        value += (item.getAt("BaselineQuantity") ?: 0) as BigDecimal

      } else if ("Rebate".equalsIgnoreCase(baselineType)) {
        value += (item.getAt("RebateValue") ?: 0) as BigDecimal

      } else if ("Value".equalsIgnoreCase(baselineType)) {
        value += (item.getAt("BaselineValue") ?: 0) as BigDecimal
      }
    }

  }
  return value
}

protected BigDecimal calculatePreviousBaselineInAgreement(Map params) {

  List previousData = params.PreviousData
  String baselineType = params.BaselineType
  BigDecimal value = BigDecimal.ZERO

  for (item in previousData) {
    if ("Quantity".equalsIgnoreCase(baselineType)) {
      value += (item.getAt("BaselineQuantity") ?: 0) as BigDecimal

    } else if ("Rebate".equalsIgnoreCase(baselineType)) {
      value += (item.getAt("RebateValue") ?: 0) as BigDecimal

    } else if ("Value".equalsIgnoreCase(baselineType)) {
      value += (item.getAt("BaselineValue") ?: 0) as BigDecimal
    }
  }
  return value
}

//---CURRENT---------------------
BigDecimal calculateCurrentBaselineValue(Map params) {
  Map localParams = params?.clone()
  localParams.CurrentData = getCurrentData(params)
  localParams.BaselineType = "Value"
  return calculateCurrentBaseline(localParams)
}

BigDecimal calculateCurrentBaselineQuantity(Map params) {
  Map localParams = params?.clone()
  localParams.CurrentData = getCurrentData(params)
  localParams.BaselineType = "Quantity"
  return calculateCurrentBaseline(localParams)
}

BigDecimal calculateCurrentRebateValue(Map params) {
  Map localParams = params.clone()
  localParams.CurrentData = getCurrentData(params)
  localParams.BaselineType = "Rebate"
  return calculateCurrentBaseline(localParams)
}

BigDecimal calculateCurrentBaseline(Map params) {
  BigDecimal value
  if (libs.RebateManager.Util.isRebateRecordContext()) {// check current context
    value = calculateCurrentBaselineInRebateRecord(params)
  } else {
    value = calculateCurrentBaselineInAgreement(params)
  }
  return value
}

protected BigDecimal calculateCurrentBaselineInRebateRecord(Map params) {
  List currentData = params.CurrentData
  String periodName = params.Name
  String baselineType = params.get("BaselineType")
  BigDecimal value = BigDecimal.ZERO
  for (item in currentData) {
    if (item.getAt("PeriodName") == periodName) {
      if ("Quantity".equalsIgnoreCase(baselineType)) {
        value += (item.getAt("BaselineQuantity") ?: 0) as BigDecimal

      } else if ("Rebate".equalsIgnoreCase(baselineType)) {
        value += (item.getAt("RebateValue") ?: 0) as BigDecimal

      } else if ("Value".equalsIgnoreCase(baselineType)) {
        value += (item.getAt("BaselineValue") ?: 0) as BigDecimal
      }
    }
  }
  return value
}

protected BigDecimal calculateCurrentBaselineInAgreement(Map params) {
  List currentData = params.CurrentData
  String baselineType = params.BaselineType
  BigDecimal value = BigDecimal.ZERO

  for (item in currentData) {
    if ("Quantity".equalsIgnoreCase(baselineType)) {
      value += (item.getAt("BaselineQuantity") ?: 0) as BigDecimal

    } else if ("Rebate".equalsIgnoreCase(baselineType)) {
      value += (item.getAt("RebateValue") ?: 0) as BigDecimal

    } else if ("Value".equalsIgnoreCase(baselineType)) {
      value += (item.getAt("BaselineValue") ?: 0) as BigDecimal
    }
  }
  return value
}

//---FORECAST---------------------
BigDecimal forecastBaselineValue(Map params) {
  Map localParams = params?.clone()
  localParams.ForecastData = getForecastData(params)
  localParams.BaselineType = "Value"
  return forecastBaseline(localParams)
}

BigDecimal forecastQuantity(Map params) {
  Map localParams = params?.clone()
  localParams.ForecastData = getForecastData(params)
  localParams.BaselineType = "Quantity"
  return forecastBaseline(localParams)
}

BigDecimal forecastRebateValue(Map params) {
  Map localParams = params?.clone()
  localParams.ForecastData = getForecastData(params)
  localParams.BaselineType = "Rebate"
  return forecastBaseline(localParams)
}

BigDecimal forecastBaseline(Map params) {
  BigDecimal value
  if (libs.RebateManager.Util.isRebateRecordContext()) {// check current context
    value = forecastBaselineInRebateRecord(params)
  } else {
    value = forecastBaselineInAgreement(params)
  }
  return value
}

protected BigDecimal forecastBaselineInRebateRecord(Map params) {
  List forecastData = params.ForecastData
  String periodName = params.Name
  String baselineType = params.BaselineType
  BigDecimal value = BigDecimal.ZERO

  for (item in forecastData) {
    if (item.getAt("PeriodName") == periodName) {
      if ("Quantity".equalsIgnoreCase(baselineType)) {
        value += (item.getAt("BaselineQuantity") ?: 0) as BigDecimal

      } else if ("Rebate".equalsIgnoreCase(baselineType)) {
        value += (item.getAt("RebateValue") ?: 0) as BigDecimal

      } else if ("Value".equalsIgnoreCase(baselineType)) {
        value += (item.getAt("BaselineValue") ?: 0) as BigDecimal
      }
    }
  }
  return value
}

protected BigDecimal forecastBaselineInAgreement(Map params) {

  List forecastData = params.ForecastData
  String baselineType = params.BaselineType
  BigDecimal value = BigDecimal.ZERO
  for (item in forecastData) {
    if ("Quantity".equalsIgnoreCase(baselineType)) {
      value += (item.getAt("BaselineQuantity") ?: 0) as BigDecimal

    } else if ("Rebate".equalsIgnoreCase(baselineType)) {
      value += (item.getAt("RebateValue") ?: 0) as BigDecimal

    } else if ("Value".equalsIgnoreCase(baselineType)) {
      value += (item.getAt("BaselineValue") ?: 0) as BigDecimal
    }
  }
  return value
}

//---REBATE RECORD -------------
def createRebateRecords(Map params) {
  if (api.isSyntaxCheck()) {
    return
  }

  def rebateRecords = params.RebateRecords
  def paymentPeriod = params.PaymentPeriod
  def startDate = params.StartDate
  def endDate = params.EndDate
  def payoutDateAmounts = params.PayoutDateAmounts
  def payTo = params.PayTo

  def periods = libs.RebateManager.Util.getPayoutPeriods(paymentPeriod, startDate, endDate)
  def payoutDate
  String name
  return periods?.collectEntries {
    name = it.name
    payoutDate = libs.RebateManager.Util.getPayoutDate(it, payoutDateAmounts)
    def rrMap = [
        "calculationDate": api.targetDate(),
        "startDate"      : it.start?.format('yyyy-MM-dd'),
        "endDate"        : it.end?.format('yyyy-MM-dd'),
        "payoutDate"     : payoutDate,
        "attribute3"     : payTo?.toString()
    ]
    if (it.label) {
      rrMap["label"] = it.label
    }
    rebateRecords.add(name, rrMap) // create rebate records
    [it.name, it]
  }
}
//---COMMON---------------------
def calculateRebateForPeriod(Map params) {

  String targetFor = params.TargetFor
  String rebateFormulas = params.RebateFormulas
  Integer periodSize = params.PeriodSize as Integer
  Integer counter = params.Counter as Integer
  String rebateCode = params.RebateCode
  BigDecimal accrualRebate = params.AccrualRebate as BigDecimal
  BigDecimal actualRebatePeriod, periodRebate
  if ("Annual".equalsIgnoreCase(targetFor)) {

    actualRebatePeriod = libs.RebateManager.(rebateCode.toString()).calculationRebate(params)
    if ("Cumulative".equalsIgnoreCase(rebateFormulas) && periodSize) {
      periodRebate = actualRebatePeriod / periodSize * counter - accrualRebate

    } else if ("Non-Cumulative".equalsIgnoreCase(rebateFormulas)) {
      periodRebate = actualRebatePeriod / periodSize
    }

  } else if ("Payment Period".equalsIgnoreCase(targetFor)) {
    periodRebate = libs.RebateManager.(rebateCode.toString()).calculationRebate(params)
  }

  return (periodRebate ?: BigDecimal.ZERO)
}

def getPreviousData(Map params) {
  String sourceId = params.SourceId
  String rebateCode = params.RebateCode
  String fieldValue =  params.QueryCondition.FieldValue
  String localKey = rebateCode + "_PreviousData" + "_" + fieldValue + (params.isMax?.toString() ?: "")

  if (api.local[localKey] == null) {
    List previousData
    Map localParams = params?.clone()
    if (sourceId) { // context = 'rebateRecord'
      def rebateAgreement = findRebateAgreementByName(sourceId, rebateCode)
      if (rebateAgreement) {
        localParams.StartDate = rebateAgreement?.startDate
        localParams.EndDate = rebateAgreement?.endDate
        previousData = getListPreviousBaselineForEachPeriod(localParams)
      }
    } else { // context != 'rebateRecord'
      previousData = getListPreviousBaselineForEachPeriod(localParams)
    }
    api.local[localKey] = previousData
  }
  return api.local[localKey]
}

def getCurrentData(Map params) {
  String sourceId = params.SourceId
  String rebateCode = params.RebateCode
  String fieldValue =  params.QueryCondition.FieldValue
  String localKey = rebateCode + "_CurrentData" + "_" + fieldValue + (params.isMax?.toString() ?: "")

  if (api.local[localKey] == null) {
    Map localParams = params?.clone()
    List currentData
    if (sourceId) { // context = 'rebateRecord'
      def rebateAgreement = findRebateAgreementByName(sourceId, rebateCode)
      if (rebateAgreement) {
        localParams.StartDate = rebateAgreement?.startDate
        localParams.EndDate = rebateAgreement?.endDate
        currentData = getListCurrentBaselineForEachPeriod(localParams)
      }
    } else { // context != 'rebateRecord'
      currentData = getListCurrentBaselineForEachPeriod(localParams)
    }
    api.local[localKey] = currentData
  }

  return api.local[localKey]
}

def getForecastData(Map params) {
  String sourceId = params.SourceId
  String rebateCode = params.RebateCode
  String fieldValue =  params.QueryCondition.FieldValue
  String localKey = rebateCode + "_ForecastData" + "_" + fieldValue + (params.isMax?.toString() ?: "") +( params.ForecastBaselineValueOverride?.toString() ?: "")

  if (api.local[localKey] == null) {
    Map localParams = params?.clone()
    List forecastData
    if (sourceId) { // context = 'rebateRecord'
      def rebateAgreement = findRebateAgreementByName(sourceId, rebateCode)

      if (rebateAgreement) {
        localParams.StartDate = rebateAgreement?.startDate
        localParams.EndDate = rebateAgreement?.endDate
        forecastData = getForecastValueForEachPeriod(localParams)
      }
    } else { // context != 'rebateRecord'
      forecastData = getForecastValueForEachPeriod(localParams)
    }
    api.local[localKey] = forecastData
  }
  return api.local[localKey]
}

/*
  Forecast sales/quantity base on:
     -  Sale/Quantity of previous period : previousBaseline
     -  StartDate, EndDate of previous period
     -  StartDate, EndDate of current period
 */

BigDecimal forecastBaselineForPeriod(def productivity, def currentData, def period, def currentDate, String baselineType) {
  def forecastValueResult = BigDecimal.ZERO

  if (currentDate?.before(period.end)) {
    if (currentDate?.after(period.start)) {
      forecastValueResult = getBaselineFromDataByPeriodName(currentData, period.name, baselineType)
      forecastValueResult += libs.RebateManager.Util.forecastValue(productivity,
          currentDate.format("yyyy-MM-dd"),
          period.end.format("yyyy-MM-dd"))
    } else {
      forecastValueResult = libs.RebateManager.Util.forecastValue(productivity,
          period.start.format("yyyy-MM-dd"),
          period.end.format("yyyy-MM-dd"))
    }

  } else {
    forecastValueResult = getBaselineFromDataByPeriodName(currentData, period.name, baselineType)
  }

  return forecastValueResult
}

protected List getListPreviousBaselineForEachPeriod(Map params) {
  Map localParams = params?.clone()
  localParams.IsPreviousPeriod = true
  List result = getListValueForEachPeriod(localParams)
  return result
}

protected List getListCurrentBaselineForEachPeriod(Map params) {
  Map localParams = params?.clone()

  localParams.IsPreviousPeriod = false
  List result = getListValueForEachPeriod(localParams)
  return result
}

protected def getBaselineFromDataByPeriodName(List data, String periodName, String baselineType) {
  BigDecimal resultValue = BigDecimal.ZERO
  for (item in data) {

    if (periodName.equalsIgnoreCase(item.getAt("PeriodName"))) {
      if ("Quantity".equalsIgnoreCase(baselineType)) {
        resultValue += (item.getAt("BaselineQuantity") ?: 0) as BigDecimal

      } else if ("Rebate".equalsIgnoreCase(baselineType)) {
        resultValue += (item.getAt("RebateValue") ?: 0) as BigDecimal

      } else if ("Value".equalsIgnoreCase(baselineType)) {
        resultValue += (item.getAt("BaselineValue") ?: 0) as BigDecimal
      }
    }
  }
  return resultValue
}

protected def findRebateAgreementByName(String name, String rebateCode) {
  String localKey = rebateCode + "_RebateAgreement"

  if (api.local[localKey] == null) {
    api.local[localKey] = libs.RebateManager.Util.getRebateAgreementByUniqueName(name)
  }
  return api.local[localKey]
}

protected List getListValueForEachPeriod(Map params) {
  def paymentPeriod = params.PaymentPeriod
  def startDate = params.StartDate
  def endDate = params.EndDate
  Map queryCondition = params.QueryCondition
  def rebateCode = params.RebateCode
  Boolean isPreviousPeriod = (params.IsPreviousPeriod ?: false) as Boolean
  def previousStartDate, previousEndDate
  def calendarObj = api.getDatamartContext().calendar()

  Map previousPeriodInfo = libs.RebateManager.(rebateCode.toString()).getPreviousPeriodInfo()
  String targetValueType = libs.RebateManager.(rebateCode.toString()).getTargetValueType()
  String calculationType = libs.RebateManager.(rebateCode.toString()).getCalculationType()

  previousStartDate = calendarObj.add(startDate, previousPeriodInfo.Number, previousPeriodInfo.Unit)
  previousEndDate = calendarObj.add(endDate, previousPeriodInfo.Number, previousPeriodInfo.Unit)
  if (isPreviousPeriod) {
    startDate = previousStartDate
    endDate = previousEndDate
  }

  def periods = libs.RebateManager.Util.getPayoutPeriods(paymentPeriod, startDate, endDate)
  queryCondition.StartDate = startDate
  queryCondition.EndDate = endDate

  List baselineData = libs.RebateManager.Util.getBaselineDataFromSource(queryCondition)
  List previousBaselineData
  Boolean growthCalculationTypeChecking = isGrowthCalculationType(calculationType)
  if (growthCalculationTypeChecking) {
    previousStartDate = calendarObj.add(startDate, previousPeriodInfo.Number, previousPeriodInfo.Unit)
    previousEndDate = calendarObj.add(endDate, previousPeriodInfo.Number, previousPeriodInfo.Unit)
    queryCondition.StartDate = previousStartDate
    queryCondition.EndDate = previousEndDate
    previousBaselineData = libs.RebateManager.Util.getBaselineDataFromSource(queryCondition)
  }
  Map newParams = params.clone()
  newParams.TargetValueType = targetValueType
  newParams.CalculationType = calculationType
  newParams.BaselineData = baselineData
  newParams.PreviousBaselineData = previousBaselineData

  return calculateValueForPeriod(newParams, periods, false)
}

protected List getForecastValueForEachPeriod(Map params) {

  def paymentPeriod = params.PaymentPeriod
  def startDate = params.StartDate
  def endDate = params.EndDate
  def currentDate = params.CurrentDate
  Map queryCondition = params.QueryCondition
  String rebateCode = params.RebateCode
  def periods = libs.RebateManager.Util.getPayoutPeriods(paymentPeriod, startDate, endDate)
  def calendarObj = api.getDatamartContext().calendar()
  BigDecimal productivityValue, productivityQuantity
  List previousBaselineValues

  Map previousPeriodInfo = libs.RebateManager.(rebateCode.toString()).getPreviousPeriodInfo()
  String targetValueType = libs.RebateManager.(rebateCode.toString()).getTargetValueType()
  String calculationType = libs.RebateManager.(rebateCode.toString()).getCalculationType()
  Boolean growthCalculationTypeChecking = isGrowthCalculationType(calculationType)
  def previousStartDate = calendarObj.add(startDate, previousPeriodInfo.Number, previousPeriodInfo.Unit)
  def previousEndDate = calendarObj.add(endDate, previousPeriodInfo.Number, previousPeriodInfo.Unit)

  //--- calculate productivity base on last period of Agreement---
  productivityValue = libs.RebateManager.Util.getProductivityForPeriod(
      getPreviousData(params),
      startDate,
      endDate,
      previousPeriodInfo,
      "Value"
  )
  if ("Quantity".equalsIgnoreCase(targetValueType)) {
    productivityQuantity = libs.RebateManager.Util.getProductivityForPeriod(
        getPreviousData(params),
        startDate,
        endDate,
        previousPeriodInfo,
        "Quantity"
    )
  }

  if (growthCalculationTypeChecking) {
    queryCondition.StartDate = previousStartDate
    queryCondition.EndDate = previousEndDate
    previousBaselineValues = libs.RebateManager.Util.getBaselineDataFromSource(queryCondition)
  }

  Map newParams = params.clone()
  newParams.CurrentDate = currentDate
  newParams.ProductivityValue = productivityValue
  newParams.ProductivityQuantity = productivityQuantity
  newParams.PreviousBaselineValues = previousBaselineValues
  newParams.TargetValueType = targetValueType
  newParams.CalculationType = calculationType

  return calculateValueForPeriod(newParams, periods, true)
}

/**
 * Calculate value for each period and push into list
 * @param params
 * @param periods : list of period
 * @param isForeCast : if function called in Forecast context --> tru. Else context false
 * @return list
 */
protected List calculateValueForPeriod(Map params, def periods, Boolean isForeCast) {

  Integer counter = 0
  List returnData = []
  Map periodData, calRebateParams
  BigDecimal periodRebate, baselineValueOfPeriod, baselineQuantityOfPeriod, baselineValueOfPreviousPeriod
  BigDecimal accrualRebate, accrualBaselineValue, accrualBaselineQuantity, accrualBaselineValueOfPreviousPeriod

  String targetFor = params.TargetFor
  def currentDate = params.CurrentDate
  BigDecimal productivityValue = params.ProductivityValue
  BigDecimal productivityQuantity = params.ProductivityQuantity
  def previousBaselineValues = params.PreviousBaselineValues
  def baselineData = params.BaselineData
  def previousBaselineData = params.PreviousBaselineData

  String targetValueType = params.TargetValueType
  String calculationType = params.CalculationType
  Boolean growthCalculationTypeChecking = isGrowthCalculationType(calculationType)

  accrualRebate = 0.0
  accrualBaselineValue = 0.0
  accrualBaselineQuantity = 0.0
  accrualBaselineValueOfPreviousPeriod = 0.0

  for (period in periods) {
    // 1. calculate actual sale OR forecast for each period
    if (isForeCast) {
      if (params.ForecastBaselineValueOverride) {
        Integer numPeriods = periods.size()
        baselineValueOfPeriod = numPeriods ? (params.ForecastBaselineValueOverride ?: 0.0) / numPeriods : 0.0
      } else {
        baselineValueOfPeriod = forecastBaselineForPeriod(
                productivityValue,
                getCurrentData(params),
                period,
                currentDate,
                "Value")
      }

      accrualBaselineValue += baselineValueOfPeriod
      if ("Quantity".equalsIgnoreCase(targetValueType)) {
        baselineQuantityOfPeriod = forecastBaselineForPeriod(
            productivityQuantity,
            getCurrentData(params),
            period,
            currentDate,
            "Quantity")
        accrualBaselineQuantity += baselineQuantityOfPeriod
      }

      if (growthCalculationTypeChecking) {
        baselineValueOfPreviousPeriod = libs.RebateManager.Util.getBaselineFromPreviousBaselineDataByPeriodName(previousBaselineValues, period.name, "Value")
        accrualBaselineValueOfPreviousPeriod += baselineValueOfPreviousPeriod
      }
    } else {
      baselineValueOfPeriod = libs.RebateManager.Util.getBaselineFromBaselineDataByPeriodName(baselineData, period.name, "Value")
      accrualBaselineValue += baselineValueOfPeriod

      if ("Quantity".equalsIgnoreCase(targetValueType)) {
        baselineQuantityOfPeriod = libs.RebateManager.Util.getBaselineFromBaselineDataByPeriodName(baselineData, period.name, "Quantity")
        accrualBaselineQuantity += baselineQuantityOfPeriod
      }

      if (growthCalculationTypeChecking) {
        baselineValueOfPreviousPeriod = libs.RebateManager.Util.getBaselineFromPreviousBaselineDataByPeriodName(previousBaselineData, period.name, "Value")
        accrualBaselineValueOfPreviousPeriod += baselineValueOfPreviousPeriod
      }

    }
    counter = counter + 1
    //2. set data for params to pass to calculateRebateForPeriod
    if ("Annual".equalsIgnoreCase(targetFor)) {
      calRebateParams = concatenateCommonParams(params,
          periods.size(),
          counter,
          accrualRebate,
          accrualBaselineValue,
          accrualBaselineQuantity,
          accrualBaselineValueOfPreviousPeriod)
    } else {
      calRebateParams = concatenateCommonParams(params,
          periods.size(),
          counter,
          accrualRebate,
          baselineValueOfPeriod,
          baselineQuantityOfPeriod,
          baselineValueOfPreviousPeriod)
    }

    periodRebate = calculateRebateForPeriod(calRebateParams)
    accrualRebate += periodRebate

    periodData = [PeriodName      : period.name,
                  BaselineQuantity: baselineQuantityOfPeriod,
                  BaselineValue   : baselineValueOfPeriod,
                  RebateValue     : periodRebate]
    returnData.add(periodData)
  }

  return returnData
}
/**
 * Extract common params from input Map and put into a Map
 * Using this function to create pamrameters for calculation function
 * @param params
 * @param periodSize
 * @param counter
 * @param accrualRebate
 * @param baselineValueCurrentPeriod
 * @param baselineQuantityCurrentPeriod
 * @param baselineValuePreviousPeriod
 * @return
 */
protected Map concatenateCommonParams(Map params, Integer periodSize, Integer counter, BigDecimal accrualRebate,
                                      BigDecimal baselineValueCurrentPeriod, BigDecimal baselineQuantityCurrentPeriod, BigDecimal baselineValuePreviousPeriod) {
  Map commonParam = [:]

  commonParam.TargetFor = params.TargetFor
  commonParam.RebateFormulas = params.RebateFormulas
  commonParam.RebateCode = params.RebateCode
  //---for single
  commonParam.TargetInput = params.TargetInput ?: 0
  commonParam.RebateInput = params.RebateInput ?: 0
  //for: singleTarget, Fixed, singleVolume, singleIncrement, singleIncrementGrowth
  commonParam.IncrementInput = params.IncrementInput ?: 0  // singleIncrement, singleIncrementGrowth
  //---for multi
  commonParam.TieredRebateInput = params.TieredRebateInput  // multiTarget, stepped, growth
  //for multi increment
  commonParam.MultiIncrementRebateInput = params.MultiIncrementRebateInput
  //---for linear
  commonParam.LinearInput = params.LinearInput

  commonParam.PeriodSize = periodSize
  commonParam.Counter = counter
  commonParam.AccrualRebate = accrualRebate

  commonParam.BaselineValueCurrentPeriod = baselineValueCurrentPeriod //for singleVolume
  commonParam.BaselineQuantityCurrentPeriod = baselineQuantityCurrentPeriod //for growth, incrementGrowth
  commonParam.BaselineValuePreviousPeriod = baselineValuePreviousPeriod

  commonParam.isMax = params.isMax

  return commonParam
}

private boolean isGrowthCalculationType(String calculationType) {
  List growths = ["Growth", "IncrementGrowth", "GrowthStepped"]
  return growths.contains(calculationType)
}