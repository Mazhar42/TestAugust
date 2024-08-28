/**
 *
 * @param rebateAgreementData
 * @return a List
 */
List getRebateAgreementReportData(def rebateAgreementData) {
  if (!rebateAgreementData) return []
  def lineItems = rebateAgreementData.getAt("lineItems").findAll { !it.folder }
  Integer lineNumber = 0
  List rebateTypes = []
  Map rebateTypeItem
  String strTarget
  BigDecimal rebates

  for (item in lineItems) {
    strTarget = getTargetOfRebateType(item)
    rebates = item.outputs.find { "CurrentRebate".equalsIgnoreCase(it.resultName) }?.result ?: 0
    rebates = libs.SharedLib.RoundingUtils.round(rebates, 2)
    //push data into map
    rebateTypeItem = getCommonReportInfo(item)
    lineNumber++
    rebateTypeItem.LineNumber = lineNumber
    rebateTypeItem.Target = strTarget
    rebateTypeItem.Rebates = formatNumber(rebates)
    // this is string after format
    rebateTypeItem.RebateValue = rebates // this is value, using to sum
    //add item in to list of rebate types
    rebateTypes.add(rebateTypeItem)
  }
  return rebateTypes
}
/**
 *
 * @param rebateAgreementData
 * @return a List
 */
List getRebateReportDataByPeriod(def rebateAgreementData, Date startDate, Date endDate) {
  String agreement = rebateAgreementData?.getAt("uniqueName")
  if (!agreement) return []
  List rebateRecords = libs.RebateManager.Util.getRebateRecordBySourceId(agreement, startDate, endDate)
  if (!rebateRecords) return []

  def lineItems = rebateAgreementData.getAt("lineItems").findAll { !it.folder }
  Integer lineNumber = 0
  List rebateTypes = [], rebateRecordOfType
  Map rebateTypeItem
  String strPayoutDate
  BigDecimal rebates, baselineValue

  for (item in lineItems) {
    //push data into map
    rebateRecordOfType = rebateRecords.findAll { (item.lineId == it.lineId) }
    if (rebateRecordOfType) {
      baselineValue = libs.RebateManager.Util.getTotalValue(rebateRecordOfType, "BaselineValue")
      baselineValue = libs.SharedLib.RoundingUtils.round(baselineValue, 2)
      rebates = libs.RebateManager.Util.getTotalValue(rebateRecordOfType, "Rebate")
      rebates = libs.SharedLib.RoundingUtils.round(rebates, 2)
      strPayoutDate = rebateRecordOfType?.get(0)?.payoutDate
      rebateTypeItem = getCommonReportInfo(item)

      lineNumber++
      rebateTypeItem.LineNumber = lineNumber
      rebateTypeItem.PayoutDate = strPayoutDate
      rebateTypeItem.Baselines = formatNumber(baselineValue)
      rebateTypeItem.BaselineValue = baselineValue
      rebateTypeItem.Rebates = formatNumber(rebates) // this is string after format
      rebateTypeItem.RebateValue = rebates // this is value, using to sum
      //add item in to list of rebate types
      rebateTypes.add(rebateTypeItem)
    }
  }
  return rebateTypes
}
/**
 *
 * @param reportData is a list of data return by function getRebateAgreementReportData/getRebateReportDataByPeriod
 * @return BigDecimal
 */
BigDecimal getTotalRebateFromReportData(List reportData) {
  return reportData?.sum { (it.RebateValue ?: 0) as BigDecimal } ?: 0
}
/**
 *
 * @param reportData is a list of data return by function getRebateAgreementReportData/getRebateReportDataByPeriod
 * @return BigDecimal
 */
BigDecimal getTotalBaselineFromReportData(List reportData) {
  return reportData?.sum { (it.BaselineValue ?: 0) as BigDecimal } ?: 0
}

protected Map getCommonReportInfo(def rebateType) {
  Map data = [:]
  String label, rebateTypeName, customer, product, paymentPeriod

  label = rebateType.getAt("label") as String
  rebateTypeName = rebateType.getAt("rebateType") as String
  customer = getCustomerGroupFromRebateLineItem(rebateType)
  product = getProductGroupFromRebateLineItem(rebateType)
  paymentPeriod = getPaymentPeriodFromRebateLineItem(rebateType)

  data.Label = label
  data.RebateType = rebateTypeName
  data.Customer = customer
  data.Product = product
  data.PaymentPeriod = paymentPeriod

  return data
}

String getUniqueNameFromRebateAgreement(Map rebateAgreement) {
  return rebateAgreement.uniqueName ?: ""
}

String getLabelFromRebateAgreement(Map rebateAgreement) {
  return rebateAgreement.label ?: ""
}

String getStartDateFromRebateAgreement(Map rebateAgreement) {
  return rebateAgreement.startDate ?: ""
}

String getEndDateFromRebateAgreement(Map rebateAgreement) {
  return rebateAgreement.endDate ?: ""
}

String getTargetDateFromRebateAgreement(Map rebateAgreement) {
  return rebateAgreement.targetDate ?: ""
}

String getCreatedByFromRebateAgreement(Map rebateAgreement) {
  return rebateAgreement.get("createdByName") ?: ""
}

String getCreatedDateFromRebateAgreement(Map rebateAgreement) {
  def calendarObj = api.getDatamartContext()?.calendar()
  def createdDate = rebateAgreement.createDate
  if (createdDate) {
    createdDate = calendarObj.parseDate(createdDate)
    createdDate = createdDate.format('yyyy-MM-dd')
  }
  return createdDate ?: ""
}

String getApprovedByFromRebateAgreement(Map rebateAgreement) {
  return rebateAgreement.approvedByName ?: ""
}

String getStatusFromRebateAgreement(Map rebateAgreement) {
  return rebateAgreement.rebateAgreementStatus ?: ""
}

String getHeaderTextRebateAgreement(Map rebateAgreement) {
  return rebateAgreement.headerText ?: ""
}

/**
 Return data of rebate agreement
 In case: report run in rebate agreement -> return api.currentItem
 Other cases as: run in dashboard ... -> find RBA with uniqueName and return it
 */
def getRebateAgreementData() {
  def rebate = api.currentItem() //case: report run in rebate agreement
  if (!rebate) { //case: report run in dashboard
    String uniqueName = api.input("uniqueName")
    if (uniqueName) {
      rebate = libs.RebateManager.Util.getRebateAgreementByUniqueName(uniqueName)
    }
  }
  return rebate
}
/**
 *
 * @param currentDate
 * @param reportType
 * @return date with format 'yyyy-MM-dd'
 */
String getReportStartDate(def currentDate, String reportType) {
  def contextCalendar = api.getDatamartContext().calendar()
  Date targetDate = contextCalendar.parseDate(currentDate?.toString())
  Date startDate
  if ("Yearly".equalsIgnoreCase(reportType)) {
    startDate = libs.RebateManager.Util.getStartDateOfCurrentYear(targetDate)
  } else if ("Monthly".equalsIgnoreCase(reportType)) {
    startDate = libs.RebateManager.Util.getStartDateOfCurrentMonth(targetDate)
  }
  return startDate?.format("yyyy-MM-dd") ?: targetDate.format("yyyy-MM-dd")
}
/**
 *
 * @param currentDate
 * @param reportType
 * @return date with format 'yyyy-MM-dd'
 */
String getReportEndDate(def currentDate) {
  def contextCalendar = api.getDatamartContext().calendar()
  Date targetDate = contextCalendar.parseDate(currentDate?.toString())
  Date startDate = libs.RebateManager.Util.getEndDateOfCurrentMonth(targetDate)
  return startDate?.format("yyyy-MM-dd") ?: targetDate.format("yyyy-MM-dd")
}
/**
 * Get data from rebate records include info:
 *     name
 *     startDate, endDate
 *     currentBaselineValue, currentRebateValue
 *     paidRebateValue
 *     forecastBaselineValue, forecastRebateValue
 * @param rebateRecords
 * @return List
 */
List getListRebateRecordData(List rebateRecords) {
  def detailRows = []
  for (rebateRecord in rebateRecords) {
    detailRows.add(getRebateRecordData(rebateRecord))
  }
  return detailRows
}
/**
 * Calculate total value of below attributes of rebaterecord as:
 *      CurrentBaselineValue
 *      CurrentRebateValue
 *      PaidRebateValue
 *      ForecastBaselineValue
 *      ForecastRebateValue
 * @param rebateRecords
 * @return map value of above attributes
 */
Map getTotalValueFromRebateRecords(List rebateRecords) {
  BigDecimal currentBaselineValue = BigDecimal.ZERO
  BigDecimal currentRebateValue = BigDecimal.ZERO
  BigDecimal paidRebateValue = BigDecimal.ZERO
  BigDecimal forecastBaselineValue = BigDecimal.ZERO
  BigDecimal forecastRebateValue = BigDecimal.ZERO

  for (rebateRecord in rebateRecords) {
    currentBaselineValue += (rebateRecord.getAt("currentBaselineValue") ?: 0) as BigDecimal
    currentRebateValue += (rebateRecord.getAt("currentRebateValue") ?: 0) as BigDecimal
    paidRebateValue += (rebateRecord.getAt("paidRebateValue") ?: 0) as BigDecimal
    forecastBaselineValue += (rebateRecord.getAt("forecastBaselineValue") ?: 0) as BigDecimal
    forecastRebateValue += (rebateRecord.getAt("forecastRebateValue") ?: 0) as BigDecimal

  }
  return [
      "CurrentBaselineValue" : currentBaselineValue,
      "CurrentRebateValue"   : currentRebateValue,
      "PaidRebateValue"      : paidRebateValue,
      "ForecastBaselineValue": forecastBaselineValue,
      "ForecastRebateValue"  : forecastRebateValue
  ]
}
//-----PRIVATE FUNCTION----
/*
  input is rebateType / a line in RA
 */

protected String getRebateTypeCodeFromRebateType(def rebateType) {
  String rebateTypeName = rebateType.getAt("rebateType")
  def rebateName = libs.RebateManager.Util.getRebateTypeByName(rebateTypeName)?.attribute4
  //attribute4 save rebateType in RebateType screen
  return libs.RebateManager.Util.getRebateTypeCodeByName(rebateName)
}

protected def getTargetOfRebateType(def rebateType) {
  def rebateCode = getRebateTypeCodeFromRebateType(rebateType)
  if (!rebateCode) {
    return
  }
  def targetType = libs.RebateManager.(rebateCode.toString()).getTargetType()
  def calculationType = libs.RebateManager.(rebateCode.toString()).getCalculationType()
  List targetInput
  String targetInputString = ""

  if ("Linear".equalsIgnoreCase(calculationType)) {
    targetInput = getTargetOfLinearRebateType(rebateType)

  } else if ("Single".equalsIgnoreCase(targetType) || "Fixed".equalsIgnoreCase(calculationType)) {
    targetInput = getTargetOfSingleRebateType(rebateType)

  } else if ("Multi".equalsIgnoreCase(targetType)) {
    targetInput = getTargetOfMultiRebateType(rebateType)
  }
  for (item in targetInput) {
    targetInputString += item.toString()
    if (targetInput?.size() > 1) {
      targetInputString += ";" + "\r\n"
    }
  }
  return targetInputString
}
/*
  For Single Rebate Type
 */

protected List getTargetOfSingleRebateType(def rebateType) {
  def rebateCode = getRebateTypeCodeFromRebateType(rebateType)
  if (!rebateCode) {
    return
  }
  def calculationType = libs.RebateManager.(rebateCode.toString()).getCalculationType()
  def targetType = libs.RebateManager.(rebateCode.toString()).getTargetType()
  def targetValueType = libs.RebateManager.(rebateCode.toString()).getTargetValueType()
  def incrementValueType = libs.RebateManager.(rebateCode.toString()).getIncrementValueType()
  def rebateValueType = libs.RebateManager.(rebateCode.toString()).getRebateValueType()

  def targetObj, incrementObj, rebateObj
  String strTarget, strIncrement, strRebate
  String targetInputString = "", targetInputName, incrementInputName, rebateInputName
  List resultData = []
  if ("Single".equalsIgnoreCase(targetType) || "Fixed".equalsIgnoreCase(calculationType)) {
    //Case 1: target, increment, rebate is userEntry
    //---Get Target
    if ("Amount".equalsIgnoreCase(targetValueType)) {
      targetInputName = "Target"
    } else if ("Percent".equalsIgnoreCase(targetValueType)) {
      targetInputName = "Target %"
    } else if ("Quantity".equalsIgnoreCase(targetValueType)) {
      targetInputName = "Target Quantity"
    }
    targetObj = getInputObjectOfRebateTypeByName(rebateType.inputs, targetInputName)
    strTarget = formatNumber(targetObj?.value as BigDecimal)

    //---Get Increment
    if ("Amount".equalsIgnoreCase(incrementValueType)) {
      incrementInputName = "Increment"
    } else if ("Percent".equalsIgnoreCase(incrementValueType)) {
      incrementInputName = "Increment %"
    }
    incrementObj = getInputObjectOfRebateTypeByName(rebateType.inputs, incrementInputName)
    strIncrement = formatNumber(incrementObj?.value as BigDecimal)

    //---Get Rebate
    if ("Amount".equalsIgnoreCase(rebateValueType)) {
      rebateInputName = "Rebate"
    } else if ("Percent".equalsIgnoreCase(rebateValueType)) {
      rebateInputName = "Rebate %"
    } else if ("AmountPerUnit".equalsIgnoreCase(rebateValueType)) {
      rebateInputName = "Rebate Per Unit"
    }
    rebateObj = getInputObjectOfRebateTypeByName(rebateType.inputs, rebateInputName)
    strRebate = formatNumber(rebateObj?.value as BigDecimal)

    //--- add to targetInputString
    if (targetObj) {
      targetInputString += targetObj.label + ": " + strTarget + " - "
    }
    if (incrementObj) {
      targetInputString += incrementObj.label + ": " + strIncrement + " - "
    }
    if (rebateObj) {
      targetInputString += rebateObj.label + ": " + strRebate
    }

    resultData.add(targetInputString)
  }

  return resultData
}

/*
  For Multi Rebate Type
 */

protected List getTargetOfMultiRebateType(def rebateType) {
  def rebateCode = getRebateTypeCodeFromRebateType(rebateType)
  if (!rebateCode) {
    return
  }
  def targetType = libs.RebateManager.(rebateCode.toString()).getTargetType()
  def targetValueType = libs.RebateManager.(rebateCode.toString()).getTargetValueType()
  def incrementValueType = libs.RebateManager.(rebateCode.toString()).getIncrementValueType()
  def rebateValueType = libs.RebateManager.(rebateCode.toString()).getRebateValueType()
  def calculationType = libs.RebateManager.(rebateCode.toString()).getCalculationType()

  def multiTierEntry, matrixInput
  String incrementLabel = "", targetLabel = "", rebateLabel = ""
  String inputName
  List resultData = []

  if ("Amount".equalsIgnoreCase(targetValueType)) {
    targetLabel = "Target"
  } else if ("Percent".equalsIgnoreCase(targetValueType)) {
    targetLabel = "Target %"
  } else if ("Quantity".equalsIgnoreCase(targetValueType)) {
    targetLabel = "Target Quantity"
  }

  if ("Amount".equalsIgnoreCase(incrementValueType)) {
    incrementLabel = "Increment"
  } else if ("Percent".equalsIgnoreCase(incrementValueType)) {
    incrementLabel = "Increment %"
  }

  if ("Amount".equalsIgnoreCase(rebateValueType)) {
    rebateLabel = "Rebate"
  } else if ("Percent".equalsIgnoreCase(rebateValueType)) {
    rebateLabel = "Rebate %"
  } else if ("AmountPerUnit".equalsIgnoreCase(rebateValueType)) {
    rebateLabel = "Rebate Per Unit"
  }

  if ("Multi".equalsIgnoreCase(targetType)) {
    //Case 2: target, increment, rebate can be MultiTarget(Tier) or MultiIncrement(InputMatrix)
    if ("Increment".equalsIgnoreCase(calculationType)) {
      //Case 2.1: MultiIncrement(InputMatrix)
      matrixInput = getInputObjectOfRebateTypeByName(rebateType.inputs, "Incremental Target")?.value
      resultData = combineLabelForInputMatrix(matrixInput, targetLabel, incrementLabel, rebateLabel)

    } else {
      //Case 2.2: MultiTarget(Tier) with out increment
      if ("Amount".equalsIgnoreCase(targetValueType)) {
        inputName = "Target"
      } else if ("Percent".equalsIgnoreCase(targetValueType)) {
        inputName = "Target %"
      }
      multiTierEntry = getInputObjectOfRebateTypeByName(rebateType.inputs, inputName)?.value
      resultData = combineLabelForMultiTierEntry(multiTierEntry, targetLabel, rebateLabel)
    }
  }

  return resultData
}

protected List combineLabelForInputMatrix(def matrixInput, String targetLabel, String incrementLabel, String rebateLabel) {
  List resultData = []
  def row
  BigDecimal target, increment, rebate
  String targetInputString
  List sortedTarget = matrixInput as List
  sortedTarget?.sort { a, b -> return (a.Target as BigDecimal) <=> (b.Target as BigDecimal) }

  for (int i = 0; i < sortedTarget.size(); i++) {
    row = sortedTarget.get(i)
    target = (row.get(targetLabel) ?: 0) as BigDecimal
    increment = (row.get(incrementLabel)?.toString() ?: 0) as BigDecimal
    rebate = (row.get(rebateLabel) ?: 0) as BigDecimal

    targetInputString = targetLabel + ": " + formatNumber(target) + " - "
    targetInputString += incrementLabel + ": " + formatNumber(increment) + " - "
    targetInputString += rebateLabel + ": " + formatNumber(rebate)

    resultData.add(targetInputString)
  }

  return resultData
}

protected List combineLabelForMultiTierEntry(def multiTierEntry, String targetLabel, String rebateLabel) {
  List resultData = []
  String targetInputString
  Map sortedTarget = multiTierEntry
  sortedTarget = sortedTarget.sort { a, b -> (a.key as BigDecimal) <=> (b.key as BigDecimal) }
  for (item in sortedTarget) {
    targetInputString = targetLabel + ": " + formatNumber(item.key as BigDecimal) +
        " - " + rebateLabel + ": " + formatNumber(item.value as BigDecimal)

    resultData.add(targetInputString)
  }

  return resultData
}

/*
  For Linear Rebate Type
 */

protected List getTargetOfLinearRebateType(def rebateType) {
  def rebateCode = getRebateTypeCodeFromRebateType(rebateType)
  if (!rebateCode) {
    return
  }

  def targetValueType = libs.RebateManager.(rebateCode.toString()).getTargetValueType()
  def rebateValueType = libs.RebateManager.(rebateCode.toString()).getRebateValueType()
  def calculationType = libs.RebateManager.(rebateCode.toString()).getCalculationType()

  String strMinTargetObj, strMaxTargetObj, strMinRebateObj, strMaxRebateObj
  String targetInputString = ""
  def minTargetObj, maxTargetObj, minRebateObj, maxRebateObj
  List resultData = []

  if ("Linear".equalsIgnoreCase(calculationType) && rebateType?.inputs) {
    //---Get Target
    if ("Amount".equalsIgnoreCase(targetValueType)) {
      minInputName = "Min Target"
      maxInputName = "Max Target"

    } else if ("Percent".equalsIgnoreCase(targetValueType)) {
      minInputName = "Min Target %"
      maxInputName = "Max Target %"

    }
    minTargetObj = getInputObjectOfRebateTypeByName(rebateType.inputs, minInputName)
    maxTargetObj = getInputObjectOfRebateTypeByName(rebateType.inputs, maxInputName)
    strMinTargetObj = formatNumber(minTargetObj?.value)
    strMaxTargetObj = formatNumber(maxTargetObj?.value)

    //---Get Rebate
    if ("Amount".equalsIgnoreCase(rebateValueType)) {
      minInputName = "Min Rebate"
      maxInputName = "Max Rebate"

    } else if ("Percent".equalsIgnoreCase(rebateValueType)) {
      minInputName = "Min Rebate %"
      maxInputName = "Max Rebate %"

    } else if ("AmountPerUnit".equalsIgnoreCase(rebateValueType)) {
      minInputName = "Min Rebate Per Unit"
      maxInputName = "Max Rebate Per Unit"

    }
    minRebateObj = getInputObjectOfRebateTypeByName(rebateType.inputs, minInputName)
    maxRebateObj = getInputObjectOfRebateTypeByName(rebateType.inputs, maxInputName)
    strMinRebateObj = formatNumber(minRebateObj?.value)
    strMaxRebateObj = formatNumber(maxRebateObj?.value)

    //--- add to targetInputString
    if (minTargetObj) {
      targetInputString += minTargetObj.label + ": " + strMinTargetObj + " - "
    }
    if (minRebateObj) {
      targetInputString += minRebateObj.label + ": " + strMinRebateObj
    }
    resultData.add(targetInputString)

    targetInputString = ""
    if (maxTargetObj) {
      targetInputString += maxTargetObj.label + ": " + strMaxTargetObj + " - "
    }
    if (maxRebateObj) {
      targetInputString += maxRebateObj.label + ": " + strMaxRebateObj
    }
    resultData.add(targetInputString)
  }
  return resultData
}

protected String getCustomerGroupFromRebateLineItem(def lineItem) {
  String customer = lineItem.outputs
      .find { "Display_RebateTypeInfo_CustomerGroup".equalsIgnoreCase(it.resultName) }
      ?.result ?: ""
  return customer.replaceAll("\"", "").replaceAll("`", "")
}

protected String getProductGroupFromRebateLineItem(def lineItem) {
  String product = lineItem.outputs
      .find { "Display_RebateTypeInfo_ProductGroup".equalsIgnoreCase(it.resultName) }
      ?.result ?: ""
  return product.replaceAll("\"", "").replaceAll("`", "")
}

protected String getPaymentPeriodFromRebateLineItem(def lineItem) {
  String paymentPeriod = lineItem.outputs
      .find { "Display_RebateTypeInfo_PaymentPeriod".equalsIgnoreCase(it.resultName) }
      ?.result ?: ""
  return paymentPeriod
}

protected Map getRebateRecordData(Map rebateRecord) {
  Map dataRow = [:]
  BigDecimal paidRebateValue = 0, rebateValue = 0
  BigDecimal baselineValue, forecastValue, forecastRebate

  baselineValue = (rebateRecord.getAt("attribute1") ?: rebateRecord.getAt("attribute4")) as BigDecimal
  if ("Approved".equalsIgnoreCase(rebateRecord.getAt("status").toString())) {
    paidRebateValue = rebateRecord.getAt("attribute2") as BigDecimal
  } else {
    rebateValue = rebateRecord.getAt("attribute2") as BigDecimal
  }
  forecastValue = (rebateRecord.getAt("attribute5") ?: rebateRecord.getAt("attribute6")) as BigDecimal
  forecastRebate = rebateRecord.getAt("attribute7") as BigDecimal

  dataRow.name = rebateRecord.getAt("name")
  dataRow.startDate = rebateRecord.getAt("startDate") as Date
  dataRow.endDate = rebateRecord.getAt("endDate") as Date
  dataRow.currentBaselineValue = (baselineValue ?: 0) as BigDecimal
  dataRow.currentRebateValue = (rebateValue ?: 0) as BigDecimal
  dataRow.paidRebateValue = (paidRebateValue ?: 0) as BigDecimal
  dataRow.forecastBaselineValue = (forecastValue ?: 0) as BigDecimal
  dataRow.forecastRebateValue = (forecastRebate ?: 0) as BigDecimal

  return dataRow
}

protected String formatNumber(BigDecimal number) {
  return api.formatNumber("#,##0.00", number ?: 0)
}

protected def getInputObjectOfRebateTypeByName(List inputs, String inputName) {
  if (!inputs || !inputName) {
    return null
  }
  def inputObject = inputs.find { inputName.equalsIgnoreCase(it.name) }
  return inputObject
}