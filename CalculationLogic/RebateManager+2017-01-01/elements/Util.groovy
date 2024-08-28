import net.pricefx.common.api.FieldFormatType
import net.pricefx.common.util.CalendarUtil
import net.pricefx.formulaengine.DatamartContext

/**
 Get the rebate code of rebate agreement by name
 @param rebateName The name of rebate type
 @return rebate code, it is a String
 */
def getRebateTypeCodeByName(String rebateName) {
  def result = api.findLookupTableValues("PFXTemplate_RebateTypes", Filter.equal("attribute1", rebateName))
  if (result) {
    return (result.get(0).name as String)?.trim()
  }
  return null
}
/**
 Get the rebate type by unique name
 @param typeName The name of rebate type
 @return rebate type
 */
def getRebateTypeByName(String typeName) {

  List rebateTypes = api.find("RBT", 0, 1, null, Filter.equal("uniqueName", typeName))

  if (rebateTypes && rebateTypes.size() > 0) {
    return rebateTypes.get(0) // get first item
  }
  return null
}

List getRebateTypes() {
  //using find because this function can be use in IsSyntaxCheck mode
  List rebateTypes = []
  Integer maxRow = api.getMaxFindResultsLimit()
  Integer startRow = 0
  def result = api.find("RBT", startRow, maxRow, null)
  while (result) {
    startRow += result.size()
    rebateTypes.addAll(result)
    result = api.find("RBT", startRow, maxRow, null)
  }
  return rebateTypes
}
/**
 Get rebate agreement by Unique Name
 @param uniqueName
 @return rebate agreement
 */

def getRebateAgreementByUniqueName(String uniqueName) {
  def rebateAgreement
  if (uniqueName) {
    rebateAgreement = api.find("RBA", 0, 1, null, Filter.equal("uniqueName", uniqueName))?.get(0)
  }
  return rebateAgreement
}
/**
 *
 * @param Id
 * @return
 */
def getRebateAgreementById(String id) {
  def rebateAgreement
  if (id) {
    rebateAgreement = api.find("RBA", 0, 1, null, Filter.equal("id", id))?.get(0)
  }
  return rebateAgreement
}

/**
 *
 * @param uniqueName
 * @param startDate
 * @param endDate
 * @param rebateAgreementStatus
 * @param attributes
 * @return
 */
List getRebateAgreements(String uniqueName, def startDate, def endDate, String sortString, String rebateAgreementStatus, List attributes) {
  List filters = []
  if (uniqueName) {
    filters.add(Filter.equal("uniqueName", uniqueName))
  }
  if (startDate) {
    filters.add(Filter.greaterOrEqual("endDate", startDate))
  }
  if (endDate) {
    filters.add(Filter.lessOrEqual("endDate", endDate))
  }
  if (rebateAgreementStatus) {
    filters.add(Filter.equal("rebateAgreementStatus", rebateAgreementStatus))
  }

  List rbas = []
  Integer maxRow = api.getMaxFindResultsLimit()
  Integer startRow = 0

  /*
    Must use api.find, can't use api.stream because api.stream is not working in isSyntaxCheck
    Can not use when we need to load data for control in dashboard.
  */
  def result = api.find("RBA", startRow, maxRow, sortString ?: "lastUpdateDate", attributes ?: null, *filters)
  while (result) {
    startRow += result.size()
    rbas.addAll(result)
    result = api.find("RBA", startRow, maxRow, sortString ?: "lastUpdateDate", attributes ?: null, *filters)
  }
  return rbas
}

/**
 Get rebate record base on source Id and apply filter by endDate.
 @param sourceId
 @param startDate
 @param endDate
 @return List. If sourceId is null or empty, return null
 */
List getRebateRecordBySourceId(String sourceId, Date startDate, Date endDate) {
  if (!sourceId) return null
  List filters = []
  filters.add(Filter.equal("sourceId", sourceId))
  if (startDate) {
    filters.add(Filter.greaterOrEqual("endDate", startDate.format("yyyy-MM-dd")))
  }
  if (endDate) {
    filters.add(Filter.lessOrEqual("endDate", endDate.format("yyyy-MM-dd")))
  }

  def streamResult = api.stream("RR", "lastUpdateDate", *filters)
  def rebateRecords = streamResult?.collect()
  streamResult?.close()

  return rebateRecords
}
/**
 * Get List of Rebate Line Item of a Rebate Agreement and include Rebate records of this RB line item
 *  * @param uniqueName of Rebate Agreement
 * @return List of Rebate Line Items
 */
List getListRebateLineItemWithRebateRecordByAgreementUniqueName(String uniqueName, Date startDate, Date endDate, String sortString) {
  List RBALIFilters = []
  RBALIFilters.add(
      Filter.and(
          Filter.isNotNull("clicId"), //important, must have this condition
          Filter.custom("{clicId} in (select id from RebateAgreement where rebateAgreementStatus = 1)") // just get RBALI of approved RB
      )
  )
  if (uniqueName) {
    def rba = getRebateAgreementByUniqueName(uniqueName)
    if (rba) {
      String rbaId = rba.typedId
      RBALIFilters.add(
          Filter.equal("clicId", rbaId.substring(0, rbaId.size() - 4))
      )
    }
  }
  def streamResult = api.stream("RBALI", sortString ?: "typedId", *RBALIFilters)
  def rebateAgreementLineItems = streamResult?.collect()
  streamResult?.close()

  List rebateAgreementWithRebateRecords
  def findResult
  Filter RRFilter

  if (rebateAgreementLineItems) {
    rebateAgreementWithRebateRecords = []
    Map lineItem
    Integer maxRecord = api.getMaxFindResultsLimit()
    for (int i = 0; i < rebateAgreementLineItems.size(); i++) {
      lineItem = rebateAgreementLineItems.getAt(i)
      if (!lineItem.get("folder")) {
        RRFilter = Filter.and(
            Filter.equal("lineId", lineItem?.lineId),
            Filter.equal("agreementStatus", "APPROVED") //maybe it's redundant, but keep it
        )
        if (startDate) {
          RRFilter.add(
              Filter.greaterOrEqual("endDate", startDate.format("yyyy-MM-dd"))
          )
        }
        if (endDate) {
          RRFilter.add(
              Filter.lessOrEqual("endDate", endDate.format("yyyy-MM-dd"))
          )
        }

        findResult = api.find("RR", 0, maxRecord, "name",
            ["lineId", "sourceId", "name", "startDate", "endDate", "payoutDate", "status",
             "attribute1", "attribute2", "attribute3", "attribute4", "attribute5",
             "attribute6", "attribute7", "attribute8", "attribute9", "attribute10"],
            RRFilter
        )

        if (findResult) {
          lineItem.RebateRecords = findResult.collect()
          rebateAgreementWithRebateRecords.add(lineItem)
        }
      }
    }
  }
  return rebateAgreementWithRebateRecords
}

List getListRebateLineItemWithRebateRecordByRebateTypeName(String rebateType, Date startDate, Date endDate) {
  List RBALIFilters = []
  RBALIFilters.add(
      Filter.and(
          Filter.isNotNull("clicId"), //important, must have this condition
          Filter.custom("{clicId} in (select id from RebateAgreement where rebateAgreementStatus = 1)") // just get RBALI of approved RB
      )
  )
  if (rebateType) {
    RBALIFilters.add(
        Filter.equal("rebateTypeUN", rebateType)
    )

  }
  def streamResult = api.stream("RBALI", "typedId", *RBALIFilters)
  def rebateAgreementLineItems = streamResult?.collect()
  streamResult?.close()
  List rebateLineItemWithRebateRecords
  def findResult
  List RRFilter
  if (rebateAgreementLineItems) {
    rebateLineItemWithRebateRecords = []
    Map lineItem
    Integer maxRecord = api.getMaxFindResultsLimit()
    for (int i = 0; i < rebateAgreementLineItems.size(); i++) {
      lineItem = rebateAgreementLineItems.getAt(i)
      RRFilter = []
      if (!lineItem.get("folder")) {
        RRFilter.add(Filter.and(
            Filter.equal("lineId", lineItem?.lineId),
            Filter.equal("agreementStatus", "APPROVED") //maybe it's redundant, but keep it
        ))

        if (startDate) {
          RRFilter.add(
              Filter.greaterOrEqual("endDate", startDate.format("yyyy-MM-dd"))
          )
        }
        if (endDate) {
          RRFilter.add(
              Filter.lessOrEqual("endDate", endDate.format("yyyy-MM-dd"))
          )
        }

        findResult = api.find("RR", 0, maxRecord, "name",
            ["lineId", "sourceId", "name", "startDate", "endDate", "payoutDate", "status",
             "attribute1", "attribute2", "attribute3", "attribute4", "attribute5",
             "attribute6", "attribute7", "attribute8", "attribute9", "attribute10"],
            *RRFilter
        )
        if (findResult) {
          lineItem.RebateRecords = findResult.collect()
          rebateLineItemWithRebateRecords.add(lineItem)
        }
      }
    }
  }
  return rebateLineItemWithRebateRecords
}

//-----GET DATA FROM SOURCE SECTION-------------------
/**
 Using to get data from a list which get from DataMart for current period
 Keys of element: Month, Quarter HaftYear, Year.
 @param baselineValues The list of Map CURRENT period data. Structure of Map ["Month":"2019-M01", "Quarter":"2019-Q1",
  "HaftYear":"2019-S1", "Year":"2019",
  "BaselineValue":20000, "BaselineQuantity":200,"RebateValue":100]
 @param currentName The name of CURRENT period as: "2019-M01" or "2019-Q1" or "2019-S1" or "2019"
 @return BigDecimal
 */

def getBaselineFromBaselineDataByPeriodName(List baselineValues, String name, String baselineType) {
  BigDecimal resultValue = BigDecimal.ZERO
  String month, quarter, haftYear, year
  for (item in baselineValues) {
    month = item.getAt("Month") ?: ""
    quarter = item.getAt("Quarter") ?: ""
    haftYear = item.getAt("HaftYear") ?: ""
    year = item.getAt("Year") ?: ""
    if (month.equalsIgnoreCase(name) || quarter.equalsIgnoreCase(name) ||
        haftYear.equalsIgnoreCase(name) || year.equalsIgnoreCase(name)) {

      if ("Quantity".equalsIgnoreCase(baselineType)) {
        resultValue += (item.getAt("BaselineQuantity") ?: 0) as BigDecimal
      } else if ("Value".equalsIgnoreCase(baselineType)) {
        resultValue += (item.getAt("BaselineValue") ?: 0) as BigDecimal
      }
    }
  }
  return resultValue
}
/**
 Using to get data from a list which get from DataMart for previous period
 Keys of element: Month, Quarter, HaftYear, Year.
 @param baselineValues : list of Map PREVIOUS period data. Structure of Map ["Month":"2018-M01", "Quarter":"2018-Q1",
  "HaftYear":"2018-S1", "Year":"2018",
  "BaselineValue":20000, "BaselineQuantity":200,"RebateValue":100]
 @param currentName : name of CURRENT period as: "2019-M01" or "2019-Q1" or "2019-S1" or "2019"
 @param baselineType which value you want to get. value "Quantity" or "Value"
 @return BigDecimal
 */
def getBaselineFromPreviousBaselineDataByPeriodName(List baselineValues, String currentName, String baselineType) {

  BigDecimal resultValue = BigDecimal.ZERO
  if (!currentName) return resultValue

  String month, quarter, haftYear, year
  String previousName = getPreviousPeriodNameFromCurrentPeriodName(currentName)

  for (item in baselineValues) {
    month = item.getAt("Month") ?: ""
    quarter = item.getAt("Quarter") ?: ""
    haftYear = item.getAt("HaftYear") ?: ""
    year = item.getAt("Year") ?: ""
    if (month.equalsIgnoreCase(previousName) || quarter.equalsIgnoreCase(previousName) ||
        haftYear.equalsIgnoreCase(previousName) || year.equalsIgnoreCase(previousName)) {

      if ("Quantity".equalsIgnoreCase(baselineType)) {
        resultValue += (item.getAt("BaselineQuantity") ?: 0) as BigDecimal

      } else if ("Value".equalsIgnoreCase(baselineType)) {
        resultValue += (item.getAt("BaselineValue") ?: 0) as BigDecimal
      }
    }
  }
  return resultValue
}
/**
 Parse from current period name to previous name
 E.g:
 Current Period Name = "2019-M01" --> Previous Period Name = "2018-M01"
 Current Period Name = "2019-Q1" --> Previous Period Name = "2018-Q1"
 Current Period Name = "2019-S1" --> Previous Period Name = "2018-S1"
 @param currentName
 @return previousName
 */
String getPreviousPeriodNameFromCurrentPeriodName(String currentName) {
  String previousName = ""
  if (currentName) {
    List nameParts = currentName.split("-") as List
    String strYear
    String s = ""
    String y = nameParts.get(0)
    if (nameParts.size() > 1) {
      s = nameParts.get(1)
    }

    try {
      strYear = (y.toInteger() - 1).toString()
    } catch (e) {
      strYear = y
    }
    previousName = s?.isEmpty() ? strYear : (strYear + "-" + s)
  }
  return previousName
}

/**
 Get data from data source with structure: List of Map ["Month", "TotalQuantity", "TotalValue"]
 Convert to new structure: List of Map ["Month", "Quarter", "HaftYear", "Year", "BaselineQuantity", "BaselineValue"]
 return new structure
 @param List of Map with old structure
 @return List of Map with new structure
 */

List getBaselineDataFromSource(Map params) {

  List data = getBaseline(params)
  List resultList = []
  for (item in data) {
    resultList.add(makeItemWithTimeInfo(item))
  }
  return resultList
}

/**
 Convert old Map to new Map with new structure
 - Old Structure Map ["Month":"2019-M01","TotalValue":100,"TotalQuantity":20]
 - New Structure Map ["Month":"2019-M01","Quarter":"2019-Q1","HaftYear":"2019-S1","Year":"2019","BaselineValue":100,"BaselineQuantity":20]
 @param inputData with old structure
 @return Map with new structure
 */
protected Map makeItemWithTimeInfo(Map inputData) {

  String quarter, haftYear, year, monthName, month

  month = inputData.getAt("Month") ?: ""
  BigDecimal baselineValue = inputData.getAt("TotalValue")
  BigDecimal baselineQuantity = inputData.getAt("TotalQuantity")

  monthName = (month.split("-") as List).get(1)
  year = (month.split("-") as List).get(0)

  switch (monthName) {
    case ["M01", "M02", "M03"]:
      quarter = year + "-Q1"
      haftYear = year + "-S1"
      break
    case ["M04", "M05", "M06"]:
      quarter = year + "-Q2"
      haftYear = year + "-S1"
      break
    case ["M07", "M08", "M09"]:
      quarter = year + "-Q3"
      haftYear = year + "-S2"
      break
    case ["M10", "M11", "M12"]:
      quarter = year + "-Q4"
      haftYear = year + "-S2"
      break
    default:
      quarter = year + "-Q1"
      haftYear = year + "-S1"
  }

  return ["Month"           : month,
          "Quarter"         : quarter,
          "HaftYear"        : haftYear,
          "Year"            : year,
          "BaselineValue"   : baselineValue,
          "BaselineQuantity": baselineQuantity]
}

/**
 Get data from source as: Datamart, Datasource, Price Parameter
 Return a Map ["Month", "TotalQuantity", "TotalValue"]
 @param Map with some properties
 @return Map data which get from data source
 */

def getBaseline(Map params) {

  def sourceType = params.SourceType
  def sourceName = params.SourceName
  def startDate = params.StartDate
  def endDate = params.EndDate
  def customerGroupInclusion = params.CustomerGroupInclusion
  def customerGroupExclusion = params.CustomerGroupExclusion
  def productGroupInclusion = params.ProductGroupInclusion
  def productGroupExclusion = params.ProductGroupExclusion
  def customerFieldName = params.CustomerFieldName
  def productFieldName = params.ProductFieldName
  def fieldDate = params.FieldDate
  def fieldValue = params.FieldValue
  def fieldQuantity = params.FieldQuantity

  def value
  if ("Price Parameter".equalsIgnoreCase(sourceType) || "PP".equalsIgnoreCase(sourceType)) {
    value = getDataFromPriceParameter(sourceName,
        startDate,
        endDate,
        customerGroupInclusion,
        customerGroupExclusion,
        productGroupInclusion,
        productGroupExclusion,
        customerFieldName,
        productFieldName,
        fieldDate,
        fieldValue,
        fieldQuantity)
  } else {
    value = getDataFromDatamart(sourceType,
        sourceName,
        startDate,
        endDate,
        customerGroupInclusion,
        customerGroupExclusion,
        productGroupInclusion,
        productGroupExclusion,
        fieldDate,
        fieldValue,
        fieldQuantity)
  }
  return value
}

/**
 * get data from datamart/datasource base on condition
 * @param sourceType
 * @param sourceName
 * @param startDate
 * @param endDate
 * @param customerGroupInclusion
 * @param customerGroupExclusion
 * @param productGroupInclusion
 * @param productGroupExclusion
 * @param fieldDate
 * @param fieldValue
 * @param fieldQuantity
 * @return
 */
protected def getDataFromDatamart(String sourceType, def sourceName, def startDate, def endDate, def customerGroupInclusion, def customerGroupExclusion, def productGroupInclusion, def productGroupExclusion, def fieldDate, def fieldValue, def fieldQuantity) {

  def ctx = api.getDatamartContext()
  def dm = ctx.getDatamart(sourceName) // default get from DM
  if ("DataSource".equalsIgnoreCase(sourceType) || "DMDS".equalsIgnoreCase(sourceType)) {
    dm = ctx.getDataSource(sourceName)
  }
  def timeFilter = Filter.and(
      Filter.greaterOrEqual(fieldDate, startDate),
      Filter.lessOrEqual(fieldDate, endDate)
  )
  DatamartContext.DataSlice thresholdSlice = ctx.newDatamartSlice()
  if (customerGroupInclusion) {
    thresholdSlice.addFilter(customerGroupInclusion) // changed customerGroupInclusion is filter
  }
  if (customerGroupExclusion) {
    thresholdSlice.exclude(customerGroupExclusion)
  }
  if (productGroupInclusion) {
    thresholdSlice.addFilter(productGroupInclusion) // changed productGroupInclusion is filter
  }
  if (productGroupExclusion) {
    thresholdSlice.exclude(productGroupExclusion)
  }

  String monthColumn = fieldDate + "Month"
  def query = ctx.newQuery(dm, true)
  query.select(monthColumn, "Month")
  query.select("SUM(${fieldValue})", "TotalValue")
  query.select("SUM(${fieldQuantity})", "TotalQuantity")
  query.where(timeFilter)
  query.where(thresholdSlice)

  List resultData, returnList
  def result = ctx.executeQuery(query)
  resultData = result?.getData()?.collect()
  if (resultData) {
    returnList = []
    for (row in resultData) {
      returnList.add(
          ["Month"        : row.getAt("Month"),
           "TotalValue"   : (row.getAt("TotalValue") ?: BigDecimal.ZERO),
           "TotalQuantity": (row.getAt("TotalQuantity") ?: BigDecimal.ZERO)
          ]
      )
    }
  }
  return returnList
}

/**
 * Get data/Query from PP base on condition
 INPUT:
 - tableName         : Table Name
 - startDate         : value of start date need to be calculate
 - endDate           : value of end date need to be calculate
 - customerGroup     : Customer filter value which need to be calculate
 - productGroup      : product filter value which need to be calculate
 - customerFieldName : customer column name in price parameter table
 - productFieldName  : product column name in price parameter table
 - fieldDate         : column name of pricing Date in price parameter table
 - fieldValue        : column name of revenue in price parameter table
 - fieldQuantity     : column name of quantity in price parameter table
 RETURN: Map ["TotalQuantity", "TotalValue"]
 */

protected def getDataFromPriceParameter(def tableName, def startDate, def endDate, def customerGroupInclusion, def customerGroupExclusion, def productGroupInclusion, def productGroupExclusion, def customerFieldName, def productFieldName, def fieldDate, def fieldValue, def fieldQuantity) {
  if (!tableName || !fieldValue) {
    return []
  }

  Map columns = getTableColumnName(tableName)
  BigDecimal value, quantity
  if (columns) {
    def filters = []
    if (startDate && fieldDate) {
      filters.add(Filter.greaterOrEqual(columns.get(fieldDate), startDate))
    }
    if (endDate && fieldDate) {
      filters.add(Filter.lessOrEqual(columns.get(fieldDate), endDate))
    }
    if (customerFieldName) {
      if (customerGroupInclusion) {
        def customerIdIns = libs.RebateManager.Customer.convertCustomerGroupInputToListOfCustomerId(customerGroupInclusion)
        filters.add(Filter.in(columns.get(customerFieldName), customerIdIns))
      }
      if (customerGroupExclusion) {
        def customerIdExs = libs.RebateManager.Customer.convertCustomerGroupInputToListOfCustomerId(customerGroupExclusion)
        filters.add(Filter.in(columns.get(customerFieldName), customerIdExs))
      }
    }
    if (productFieldName) {
      if (productGroupInclusion) {
        def productIdIns = libs.RebateManager.Product.convertProductGroupInputToListOfProductId(productGroupInclusion)
        filters.add(Filter.in(columns.get(productFieldName), productIdIns))
      }
      if (productGroupExclusion) {
        def productIdExs = libs.RebateManager.Product.convertProductGroupInputToListOfProductId(productGroupExclusion)
        filters.add(Filter.in(columns.get(productFieldName), productIdExs))
      }

    }
    def result = api.findLookupTableValues(tableName, *filters)
    value = result.collect().sum { it.getAt(columns.get(fieldValue)) as BigDecimal }
    quantity = result.collect().sum { it.getAt(columns.get(fieldQuantity)) as BigDecimal }
  }
  return ["TotalValue"   : (value ?: BigDecimal.ZERO),
          "TotalQuantity": (quantity ?: BigDecimal.ZERO)]
}

Map getTableColumnName(String tableName) {
  def id = api.findLookupTable(tableName)?.id
  Map columns = [:]
  def data = api.find("MLTVM", Filter.equal("lookupTableId", id))
  for (item in data) {
    columns.put(item.label, item.fieldName)
  }
  return columns
}

/*
  Get Label By Field Name
 */

def getLabelByFieldName(Map params) {
  def sourceType = params.SourceType
  def sourceName = params.SourceName
  def fiedlName = params.BaselineFieldName
  def value
  if ("DataMart".equalsIgnoreCase(sourceType) || "DM".equalsIgnoreCase(sourceType)) {
    value = getLabelByFieldNameFromDataMart(sourceName, fiedlName)

  } else if ("DataSource".equalsIgnoreCase(sourceType) || "DMDS".equalsIgnoreCase(sourceType)) {
    value = getLabelByFieldNameFromDataSource(sourceName, fiedlName)

  } else if ("Price Parameter".equalsIgnoreCase(sourceType) || "PP".equalsIgnoreCase(sourceType)) {
    value = getLabelByFieldNameFromPriceParameter(sourceName, fiedlName)
  }
  return value
}
/*
  Get Label By Field Name in DataMart
 */

protected getLabelByFieldNameFromDataMart(def datamartName, def fieldName) {
  def ctx = api.getDatamartContext()
  def dm = ctx.getDatamart(datamartName)
  def fieldList = dm.fc()?.fields
  def fied = fieldList?.find { it?.name?.toLowerCase() == fieldName?.toLowerCase() }

  return fied?.label
}
/*
  Get Label By Field Name in DataSource
 */

protected getLabelByFieldNameFromDataSource(def datasourceName, def fieldName) {
  def ctx = api.getDatamartContext()
  def dm = ctx.getDataSource(datasourceName)
  def fieldList = dm.fc()?.fields
  def fied = fieldList?.find { it.name?.toLowerCase() == fieldName?.toLowerCase() }

  return fied?.label
}
/*
  Get Label By Field Name in Price Parameter
 */

protected getLabelByFieldNameFromPriceParameter(String tableName, String fieldName) {

  def id = api.findLookupTable(tableName)?.id
  def data = api.find("MLTVM", Filter.equal("lookupTableId", id))
  def label
  for (item in data) {
    if (fieldName.equalsIgnoreCase(item.fieldName)) {
      label = item.label
      break
    }
  }
  return label
}
//------CREATE REBATE RECORD SECTION------------------
def SalesPeriod(startDate, endDate) {
  if (api.isSyntaxCheck()) return

  def calendarObj = api.getDatamartContext().calendar()
  def fromDate = calendarObj.parseDate(startDate?.toString())
  def toDate = calendarObj.parseDate(endDate?.toString())

  return new CalendarUtil.TimePeriod(fromDate, toDate, CalendarUtil.TimeUnit.DAY)
}

/*
  Calculate period
  @param strPaymentPeriod String
  @param startDate Date
  @param endDate Date
  @return TIMEPERIOD
 */

def getPayoutPeriods(def strPaymentPeriod, def startDate, def endDate) {
  CalendarUtil.TimePeriod validity = SalesPeriod(startDate, endDate)
  if (strPaymentPeriod == null || "Manually".equalsIgnoreCase(strPaymentPeriod?.toString())) {
    return [["start": validity?.startDate, "end": validity?.endDate, "name": "RR"]]
  }
  def paymentPeriod = convertPaymentPeriodToTimeUnit(strPaymentPeriod)
  def timeUnit = "Semi-Annually".equalsIgnoreCase(paymentPeriod?.toString()) ? CalendarUtil.TimeUnit.MONTH : paymentPeriod

  def ctx = api.getDatamartContext()
  def calDS = ctx.getDataSource("cal")
  def q = ctx.newQuery(calDS, false)
  q.select("CalDate")
  q.select([(CalendarUtil.TimeUnit.YEAR): "CalYear", (CalendarUtil.TimeUnit.MONTH): "CalMonth", (CalendarUtil.TimeUnit.QUARTER): "CalQuarter"]?.get(timeUnit), "period")
  q.where(Filter.greaterOrEqual("CalDate", validity?.getStartDate()), Filter.lessOrEqual("CalDate", validity?.getEndDate()))
  q.orderBy("CalDate")

  def res = ctx.executeQuery(q)
  def result = []

  def data = res?.getData()
  if (data?.getValue()) {
    def act = ["start": data.getValue(0, 0), "name": data.getValue(0, 1)]
    for (int row = 0; row < data?.getRowCount(); ++row) {
      def date = data.getValue(row, 0)
      def name = data.getValue(row, 1)
      if (name != act["name"]) {
        act["end"] = date - 1
        result.add(act)
        act = ["start": date, "name": name]

      } else if (row == data.getRowCount() - 1) {
        act["end"] = date
        result.add(act)
        act = ["start": date, "name": name]
      }
    }
  }
  if ("Semi-Annually".equalsIgnoreCase(paymentPeriod?.toString())) {
    def numberOfSemiYears = result.size().intdiv(6)
    resultBi = []
    for (def i = 0; i < numberOfSemiYears; i++) {
      def startHalf = result[i * 6].start
      def endHalf = result[i * 6 + 5].end
      resultBi.add(["start": startHalf, "end": endHalf, "name": getHaftYearName(startHalf)])
    }
    result = resultBi
  }
  return result
}

def getPayoutDate(def period, def payoutDays) {

  Integer offset = payoutDays //settings?.get(0)?:1
  Boolean workDays = true
  def payout = period.end?.plus(offset)
  if (workDays) {
    def day = payout?.getAt(Calendar.DAY_OF_WEEK)
    payout = payout?.plus(day == Calendar.SATURDAY ? 2 : day == Calendar.SUNDAY ? 1 : 0)
  }
  return payout?.format('yyyy-MM-dd')
}

protected def getHaftYearName(def inputDate) {
  Date date = new Date(inputDate?.getTime())
  def year = date[Calendar.YEAR]
  def month = date[Calendar.MONTH]
  String s
  if (month < 6) {
    s = "S1"
  } else {
    s = "S2"
  }
  return (year.toString() + "-" + s)
}

protected def convertPaymentPeriodToTimeUnit(String paymentPeriod) {
  def period = null
  switch (paymentPeriod) {
    case "Monthly":
      period = CalendarUtil.TimeUnit.MONTH
      break
    case "Quarterly":
      period = CalendarUtil.TimeUnit.QUARTER
      break
    case "Haft-Yearly":
      period = "Semi-Annually"
      break
    case "Semi-Annually":
      period = "Semi-Annually"
      break
    case "Yearly":
      period = CalendarUtil.TimeUnit.YEAR
      break
    case "Annually":
      period = CalendarUtil.TimeUnit.YEAR
      break
    case "Manually":
      period = "Manually"
      break
    default:
      period = CalendarUtil.TimeUnit.QUARTER
  }
  return period
}
//------GET REBATE FROM INPUT------
BigDecimal getRebateFactorFromMultiTierEntry(def multiTierEntry, def baselineValue, def isMax = false) {

  if (multiTierEntry == null) {
    return BigDecimal.ZERO
  }

  def tier, target, rebate
  BigDecimal rebateValue = BigDecimal.ZERO
  for (int i = multiTierEntry.size() - 1; i >= 0; i--) {
    tier = multiTierEntry.get(i)
    target = (tier.target as BigDecimal)
    rebate = (tier.value as BigDecimal)
    if (target != null) {
      if (isMax) {
        if ((rebate ?: BigDecimal.ZERO) > rebateValue) {
          rebateValue = (rebate ?: BigDecimal.ZERO)
          break
        }
      } else {
        if ((baselineValue ?: BigDecimal.ZERO) > target) {
          rebateValue = (rebate ?: BigDecimal.ZERO)
          break
        }
      }

    }
  }

  return rebateValue
}
/**
 *
 * @param multiTierEntry
 * @param baselineValue
 * @param rebateValueType
 * @return BigDecimal
 */
BigDecimal getSteppedRebateFactorFromMultiTierEntry(def multiTierEntry, BigDecimal baselineValue, String rebateValueType, Boolean isMax = false) {

  if (multiTierEntry == null) {
    return BigDecimal.ZERO
  }

  def tier, target, rebate, currentBaselineValue
  BigDecimal rebateValue = BigDecimal.ZERO

  currentBaselineValue = baselineValue
  for (int i = multiTierEntry.size() - 1; i >= 0; i--) {
    tier = multiTierEntry.get(i)
    target = (tier.target as BigDecimal)
    rebate = (tier.value as BigDecimal)

    if (target != null) {
      if (isMax) {
        if ((rebate ?: BigDecimal.ZERO) > rebateValue) {
          if ("Percent".equalsIgnoreCase(rebateValueType)) {
            rebateValue = rebateValue + (rebate ?: BigDecimal.ZERO) * (currentBaselineValue - target)

          } else if ("Amount".equalsIgnoreCase(rebateValueType)) {
            rebateValue = rebateValue + (rebate ?: BigDecimal.ZERO)
          }
          currentBaselineValue = target
          break
        }
      } else {
        if ((currentBaselineValue ?: BigDecimal.ZERO) > target) {
          if ("Percent".equalsIgnoreCase(rebateValueType)) {
            rebateValue = rebateValue + (rebate ?: BigDecimal.ZERO) * (currentBaselineValue - target)

          } else if ("Amount".equalsIgnoreCase(rebateValueType)) {
            rebateValue = rebateValue + (rebate ?: BigDecimal.ZERO)
          }
          currentBaselineValue = target
        }
      }
    }
  }
  return rebateValue
}
/**
 *
 * @param multiTierEntry
 * @param previousBaselineValue
 * @param currentBaselineValue
 * @param targetValueType
 * @param rebateValueType
 * @return BigDecimal
 */
BigDecimal getGrowthSteppedRebateFactorFromMultiTierEntry(def multiTierEntry,
                                                          BigDecimal previousBaselineValue,
                                                          BigDecimal currentBaselineValue,
                                                          String targetValueType,
                                                          String rebateValueType) {

  if (multiTierEntry == null) {
    return BigDecimal.ZERO
  }

  def tier, target, rebate
  BigDecimal rebateValue = BigDecimal.ZERO, growth

  if ("Percent".equalsIgnoreCase(targetValueType)) {
    if (currentBaselineValue == BigDecimal.ZERO && previousBaselineValue == BigDecimal.ZERO) {
      growth = BigDecimal.ZERO
    } else if (previousBaselineValue == BigDecimal.ZERO) {
      growth = BigDecimal.ONE
    } else {
      growth = (currentBaselineValue - previousBaselineValue) / previousBaselineValue
    }
    for (int i = multiTierEntry.size() - 1; i >= 0; i--) {
      tier = multiTierEntry.get(i)
      target = (tier.target as BigDecimal)
      rebate = (tier.value as BigDecimal)
      if (target != null) {
        if (growth > target) {
          if ("Percent".equalsIgnoreCase(rebateValueType)) {
            rebateValue += (growth - target) * (previousBaselineValue ?: 0) * rebate
          } else if ("Amount".equalsIgnoreCase(rebateValueType)) {
            rebateValue += rebate
          }
          growth = target
        }
      }
    }

  } else if ("Amount".equalsIgnoreCase(targetValueType)) {
    BigDecimal baselineValue = (currentBaselineValue ?: 0) - (previousBaselineValue ?: 0)
    rebateValue = getSteppedRebateFactorFromMultiTierEntry(multiTierEntry, baselineValue, rebateValueType)
  }
  return rebateValue
}
/**
 *
 * @param linearInputs
 * @param baselineValue
 * @param type
 * @return
 */
def getLinearRebateFactorFromInput(def linearInputs, BigDecimal baselineValue, String type, Boolean isMax = false) {
  if (linearInputs == null || linearInputs.size() != 2) {
    return BigDecimal.ZERO
  }

  def rebateValue = BigDecimal.ZERO
  BigDecimal minTarget, maxTarget, minRebate, maxRebate
  minTarget = linearInputs.get(0).get("MinTarget") ?: BigDecimal.ZERO
  minRebate = linearInputs.get(0).get("MinRebate") ?: BigDecimal.ZERO
  maxTarget = linearInputs.get(1).get("MaxTarget") ?: BigDecimal.ZERO
  maxRebate = linearInputs.get(1).get("MaxRebate") ?: BigDecimal.ZERO

  if (isMax) {
    rebateValue = maxRebate
  } else if ((baselineValue >= minTarget) && (maxTarget > minTarget)) {
    rebateValue = (baselineValue - minTarget) / (maxTarget - minTarget)
    if ("Amount".equalsIgnoreCase(type)) {
      if (baselineValue == minTarget) {
        rebateValue = minRebate
      } else if (baselineValue >= maxTarget) {
        rebateValue = maxRebate
      } else {
        rebateValue = rebateValue * (maxRebate - minRebate)
        rebateValue = rebateValue + minRebate
      }

    } else if ("Percent".equalsIgnoreCase(type)) {
      if (baselineValue == minTarget) {
        rebateValue = minRebate * minTarget
      } else if (baselineValue >= maxTarget) {
        rebateValue = maxRebate * maxTarget
      } else {
        rebateValue = rebateValue * (maxRebate * maxTarget - minRebate * minTarget)
        rebateValue = rebateValue + (minRebate * minTarget)
      }
    }
  }
  return rebateValue
}

/*
  - Calculate incremental rebate value base on Target is a matrix Input
  - Value of Rebate Column of matrix input can be Percent or Amount
  - In case type is percent, rebate must be divide by 100
 */

/**
 * Rebate value is calculated base on one level (non-stepped)
 * @param matrixInput
 * @param baselineValue
 * @param type
 * @return
 */
def getRebateFactorFromMatrixInput(def matrixInput, BigDecimal baselineValue, String type) {
  if (matrixInput == null) {
    return BigDecimal.ZERO
  }

  List rows = matrixInput as List
  rows?.sort { a, b -> return (a.Target as BigDecimal) <=> (b.Target as BigDecimal) }

  Map row
  String TargetColumnName = "Target"
  String IncrementColumnName = "Increment"
  String RebateColumnName = "Rebate"

  if ("Percent".equalsIgnoreCase(type)) {
    RebateColumnName = "Rebate %"
  }

  BigDecimal target, increment, rebate
  BigDecimal rebateValue = BigDecimal.ZERO
  BigDecimal currentBaselineValue = baselineValue

  for (int i = rows.size() - 1; i >= 0; i--) {
    row = matrixInput.get(i)
    target = (row.get(TargetColumnName) ?: 0) as BigDecimal
    increment = (row.get(IncrementColumnName)?.toString() ?: 0) as BigDecimal
    rebate = (row.get(RebateColumnName) ?: 0) as BigDecimal

    if (currentBaselineValue > target && increment != 0 && (currentBaselineValue - target) >= increment) {

      if ("Percent".equalsIgnoreCase(type)) {
        rebateValue = (((currentBaselineValue - target) / increment) as BigDecimal).toInteger() * (rebate / 100) * increment
        //--rebate/100: because input is amount value, not percent value

      } else if ("Amount".equalsIgnoreCase(type)) {
        rebateValue = (((currentBaselineValue - target) / increment) as BigDecimal).toInteger() * rebate

      }
      break
    }
  }
  return rebateValue
}

/**
 * Rebate value is calculated base on multi levels (stepped)
 * @param matrixInput
 * @param baselineValue
 * @param type
 * @return
 */
def getSteppedRebateFactorFromMatrixInput(def matrixInput, BigDecimal baselineValue, String type, Boolean isMax = false) {
  if (matrixInput == null) {
    return BigDecimal.ZERO
  }

  List rows = matrixInput as List
  rows?.sort { a, b -> return (a.Target as BigDecimal) <=> (b.Target as BigDecimal) }

  Map row
  String TargetColumnName = "Target"
  String IncrementColumnName = "Increment"
  String RebateColumnName = "Rebate"

  if ("Percent".equalsIgnoreCase(type)) {
    RebateColumnName = "Rebate %"
  }

  BigDecimal target, increment, rebate
  BigDecimal rebateValue = BigDecimal.ZERO
  BigDecimal currentBaselineValue = baselineValue
  for (int i = rows.size() - 1; i >= 0; i--) {
    row = matrixInput.get(i)
    target = (row.get(TargetColumnName) ?: 0) as BigDecimal
    increment = (row.get(IncrementColumnName)?.toString() ?: 0) as BigDecimal
    rebate = (row.get(RebateColumnName) ?: 0) as BigDecimal
    if (((currentBaselineValue > target) || (isMax && (rebate > rebateValue))) && increment != 0) {
      if ("Percent".equalsIgnoreCase(type)) {
        rebateValue = rebateValue + (((currentBaselineValue - target) / increment) as BigDecimal).toInteger() * (rebate / 100) * increment
        //--rebate/100: because input is amount value, not percent value
      } else if ("Amount".equalsIgnoreCase(type)) {
        rebateValue = rebateValue + (((currentBaselineValue - target) / increment) as BigDecimal).toInteger() * rebate

      }
      currentBaselineValue = target
    }
  }
  return rebateValue
}

/**
 *
 * @param value
 * @param labelOfEntry
 * @return Map
 */
Map validateSingleEntry(def value, String labelOfEntry) {
  Map result = [:]
  result.ErrorCode = 0
  result.ErrorMessage = ""
  if (value && value < BigDecimal.ZERO) {
    result.ErrorCode = 10
    result.ErrorMessage = labelOfEntry + " must be greater than 0"
  }
  return result
}
/**
 *
 * @param multiTierEntry
 * @return Map
 */
Map validateMultiTierEntry(def multiTierEntry) {
  def tier
  Map result = [:]
  result.ErrorCode = 0
  result.ErrorMessage = ""

  BigDecimal target, rebate
  for (int i = 0; i < multiTierEntry.size(); i++) {
    tier = multiTierEntry.get(i)
    target = (tier.target as BigDecimal)
    rebate = (tier.value as BigDecimal)
    if (target && target < 0) {
      result.ErrorCode = 10
      result.ErrorMessage = "Target must be greater than 0"
    } else if (rebate && rebate < 0) {
      result.ErrorCode = 10
      result.ErrorMessage = "Rebate must be greater than 0"
    }
  }
  return result
}
/**
 *
 * @param matrixInput
 * @return Map
 */
Map validateMatrixInput(def matrixInput) {
  Map result = [:]
  result.ErrorCode = 0
  result.ErrorMessage = ""

  List rows = matrixInput as List
  rows?.sort { a, b -> return (a.Target as BigDecimal) <=> (b.Target as BigDecimal) }

  String TargetColumnName = "Target"
  String IncrementColumnName = "Increment"
  String RebateColumnName = "Rebate"
  String RebateColumnNamePercent = "Rebate %"

  BigDecimal target, increment, rebate
  for (row in rows) {
    target = (row.get(TargetColumnName) ?: 0) as BigDecimal
    increment = (row.get(IncrementColumnName)?.toString() ?: 0) as BigDecimal
    rebate = (row.get(RebateColumnName) ?: (row.get(RebateColumnNamePercent) ?: 0)) as BigDecimal

    if (target < 0) {
      result.ErrorCode = 10
      result.ErrorMessage = "Target must be greater than 0"
    } else if (increment < 0) {
      result.ErrorCode = 10
      result.ErrorMessage = "Increment must be greater than 0"
    } else if (rebate < 0) {
      result.ErrorCode = 10
      result.ErrorMessage = "Rebate must be greater than 0"
    }
  }
  return result
}
/**
 * Validate
 * @param linearInput
 * @return Map
 */
Map validateLinearInput(def linearInput) {
  BigDecimal minTarget, maxTarget, minRebate, maxRebate
  Map result = [:]
  result.ErrorCode = 0
  result.ErrorMessage = ""

  minTarget = linearInput.get(0).get("MinTarget")
  minRebate = linearInput.get(0).get("MinRebate")
  maxTarget = linearInput.get(1).get("MaxTarget")
  maxRebate = linearInput.get(1).get("MaxRebate")

  //jus check in case having input data
  if (minTarget || maxTarget || minRebate || maxRebate) {
    if (minTarget < 0 || maxTarget < 0) {
      result.ErrorCode = 10
      result.ErrorMessage = "Target must be greater than 0"

    } else if (minRebate < 0 || maxRebate < 0) {
      result.ErrorCode = 10
      result.ErrorMessage = "Rebate must be greater than 0"

    } else if (minTarget >= maxTarget) {
      result.ErrorCode = 20
      result.ErrorMessage = "Min Target should be less than Max Target"
    }
  }

  return result
}
//-------SUM VALUE FROM REBATERECORDS--------------
/**
 * Sum of value from rebate records
 * @param rebateRecords
 * @param valueType one of item: [BaselineValue, BaselineQuantity, Rebate]
 * @return sum of value
 */
BigDecimal getTotalValue(List rebateRecords, String valueType) {
  Map attributeMapping = ["BaselineValue"   : "attribute1",
                          "BaselineQuantity": "attribute4",
                          "Rebate"          : "attribute2"
  ]
  String attribute = attributeMapping.get(valueType) ?: ""
  BigDecimal value = BigDecimal.ZERO
  for (rebateRecord in rebateRecords) {
    value += (rebateRecord.(attribute.toString()) ?: 0) as BigDecimal
  }
  return value
}

//-------DATAMART FILTER PARSER--------------
/*
  Input: a filter
  Return: a filter
  Logic: change fieldName of filter input(.property)base on mapping PP.
 */

Filter buildFilterFromAdvancedCriteria(String sourceType, Filter advancedCriteria) {
  def newFilter
  def mappingField
  Map data = loadMappingFieldName(sourceType)
  newFilter = api.walkFilter(advancedCriteria, null,
      { filter ->
        if (filter != null) {
          mappingField = data?.get(filter?.property) //getMappingFieldName(sourceType, filter.property)
          if (mappingField) {
            filter.property = mappingField
          }
        }
        filter
      }
      , false)
  return newFilter
}

/*
  Load data in PP 'PFXTemplate_Product_MappingDatamartFieldName' Or 'PFXTemplate_Customer_MappingDatamartFieldName' to Map
 */

private Map loadMappingFieldName(String sourceType) {
  String lookupTableName
  if ("P".equalsIgnoreCase(sourceType)) {
    lookupTableName = "PFXTemplate_Product_MappingDatamartFieldName"

  } else if ("C".equalsIgnoreCase(sourceType)) {
    lookupTableName = "PFXTemplate_Customer_MappingDatamartFieldName"

  }
  def tableValues = api.findLookupTableValues(lookupTableName)
  Map mapData = [:]
  for (item in tableValues) {
    mapData.put(item.name, item.attribute1)
  }
  return mapData
}

//------DATE Utils----
/**
 * Get start date of current month. If param currentDate=null system will set currentDate = today
 * @param currentDate
 * @return StartDate of current month
 */
Date getStartDateOfCurrentMonth(Date currentDateInput) {
  Calendar calendar = Calendar.getInstance()
  Date currentDate = currentDateInput ?: calendar.getTime()
  calendar.set(currentDate[Calendar.YEAR], currentDate[Calendar.MONTH], 1)
  return calendar.getTime()
}

/**
 * Get end date of current month
 * @param currentDate
 * @return endDate of current month
 */
Date getEndDateOfCurrentMonth(Date currentDateInput) {
  Calendar calendar = Calendar.getInstance()
  Date currentDate = currentDateInput ?: calendar.getTime()
  calendar.set(currentDate[Calendar.YEAR], currentDate[Calendar.MONTH], 1)
  calendar.add(Calendar.MONTH, 1)
  calendar.add(Calendar.DATE, -1)
  return calendar.getTime()
}
/**
 * Get start date of current year
 * @param currentDate
 * @return startDate of current year
 */
Date getStartDateOfCurrentYear(Date currentDateInput) {
  Calendar calendar = Calendar.getInstance()
  Date currentDate = currentDateInput ?: calendar.getTime()
  calendar.set(currentDate[Calendar.YEAR], 0, 1, 0, 0, 0)
  calendar.set(Calendar.MILLISECOND, 0)
  return calendar.getTime()
}
/**
 * Get end date of current year
 * @param currentDate
 * @return end date of current year
 */
Date getEndDateOfCurrentYear(Date currentDateInput) {
  Calendar calendar = Calendar.getInstance()
  Date currentDate = currentDateInput ?: calendar.getTime()
  calendar.set(currentDate[Calendar.YEAR], 0, 1, 0, 0, 0)
  calendar.add(Calendar.YEAR, 1)
  calendar.add(Calendar.DATE, -1)
  calendar.set(Calendar.MILLISECOND, 0)
  return calendar.getTime()
}

//-------OTHERS-------
Boolean isRebateRecordContext() {
  String context = api.getCalculationContext() // current context
  return ("rebateRecord".equalsIgnoreCase(context))
}

Boolean isAgreementContext() {
  String context = api.getCalculationContext() // current context
  return ("agreement".equalsIgnoreCase(context))
}

/*
  Checking if current calculation context is "agreementReadOnly"
  Return Boolean
 */

Boolean isAgreementReadOnlyContext() {
  String context = api.getCalculationContext() // current context
  return ("agreementReadOnly".equalsIgnoreCase(context))
}

/*
Forecast sales/quantity base on:
     -  Sale/Quantity of previous period : previousBaseline
     -  StartDate, EndDate
 */

BigDecimal forecastValue(def productivity, def currentStartDate, def currentEndDate) {

  def calendarObj = api.getDatamartContext()?.calendar()
  def totalDays = calendarObj.getDaysDiff(currentEndDate?.toString(), currentStartDate?.toString())

  BigDecimal value = (productivity ?: BigDecimal.ZERO) * (totalDays ?: BigDecimal.ZERO)
  return value
}

/*
  Calculate productivity base on historical data
 */

def getProductivityPerDay(def baseline, def startDate, def endDate) {

  def calendarObj = api.getDatamartContext()?.calendar()
  def daysOfTransactions = calendarObj.getDaysDiff(endDate?.toString(), startDate?.toString())

  def valuePerDay
  if (daysOfTransactions) {
    valuePerDay = (baseline ?: BigDecimal.ZERO) / daysOfTransactions
  }
  return (valuePerDay ?: BigDecimal.ZERO)
}

/*
  Get Productivity for previous period
 */

def getProductivityForPeriod(def previousData, def startDate, def endDate, Map periodInfo, String baselineType) {

  def calendarObj = api.getDatamartContext().calendar()
  def previousBaseline = BigDecimal.ZERO

  for (item in previousData) {
    if ("Quantity".equalsIgnoreCase(baselineType)) {
      previousBaseline += (item.getAt("BaselineQuantity") ?: 0) as BigDecimal
    } else {
      previousBaseline += (item.getAt("BaselineValue") ?: 0) as BigDecimal
    }
  }
  def previousStartDate = calendarObj.add(startDate, periodInfo.Number, periodInfo.Unit)
  def previousEndDate = calendarObj.add(endDate, periodInfo.Number, periodInfo.Unit)
  def productivity = getProductivityPerDay(previousBaseline,
      previousStartDate,
      previousEndDate
  )
  return productivity
}

/**
 Just highlight result value by comparison current value and previous value
 - If rebateValue > preValue --> text color of rebateValue = Green
 - If rebateValue = preValue --> text color of rebateValue = Blue
 - If rebateValue < preValue --> text color of rebateValue = Red
 @param currentValue
 @param previousValue
 */

def formatColorForReturnValue(def currentValue, def previousValue) {

  String color = "green"
  if (currentValue < previousValue) {
    color = "red"
  } else if (currentValue == previousValue) {
    color = "DarkOrange"
  }
  return api.attributedResult(currentValue).withTextColor(color)
}

/*
  Compare currentPeriodName and previousPeriodName
  currentPeriodName = "2019-M01"
  previousPeriodName = "2018-M01"
  --> they should be equals
  @param previousPeriodName the name of previous period .eg: "2018-M01", "2018-Q1", "2018-S1", "2018"
  @param currentPeriodName the name of current period .eg: "2019-M01", "2019-Q1", "2019-S1", "2019"
  @return Boolean.
    eg: "2019-M01" == "2018-M01" --> true
    eg: "2019-Q1" == "2018-Q1" --> true
    eg: "2019-S1" == "2018-S1" --> true
    eg: "2019" == "2018" --> true
 */

Boolean comparePeriodNameForPreviousPeriod(String previousPeriodName, String currentPeriodName) {
  def currentPeriodNameFormat = currentPeriodName.contains("-")
  def previousPeriodNameFormat = previousPeriodName.contains("-")

  def result = false

  if (currentPeriodNameFormat == previousPeriodNameFormat) { // format is the same
    if (currentPeriodNameFormat) { //include '-'.
      def s1 = (currentPeriodName.split("-") as List).get(1)
      def s2 = (previousPeriodName.split("-") as List).get(1)
      def y1 = (currentPeriodName.split("-") as List).get(0)
      def y2 = (previousPeriodName.split("-") as List).get(0)
      if (s1 == s2 && ((y1 as Integer) == ((y2 as Integer) + 1))) {
        result = true
      }

    } else { //not include '-'. It mean just have year
      if ((currentPeriodName as Integer) == ((previousPeriodName as Integer) + 1)) {
        result = true
      }
    }
  }

  return result
}
/**
 * Get base currency of data mart
 * @param datamartName
 * @return
 */
String getBaseCurrencyOfDatamart(String datamartName) {
  def datamart = api.find("DM", 0, 1, null, Filter.equal("uniqueName", datamartName))
  if (datamart) {
    return datamart.get(0)?.baseCcyCode
  }
  return null
}
/**
 *
 * @param currency
 * @return
 */
FieldFormatType getMoneyFormatType(String currency) {
  FieldFormatType fieldFormatType
  switch (currency) {
    case "EUR":
      fieldFormatType = FieldFormatType.MONEY_EUR
      break
    case "USD":
      fieldFormatType = FieldFormatType.MONEY_USD
      break
    case "GBP":
      fieldFormatType = FieldFormatType.MONEY_GBP
      break
    case "JPY":
      fieldFormatType = FieldFormatType.MONEY_JPY
      break
    case "PLN":
      fieldFormatType = FieldFormatType.MONEY_PLN
      break
    case "CHF":
      fieldFormatType = FieldFormatType.MONEY_CHF
      break
    default: fieldFormatType = FieldFormatType.MONEY
  }
  return fieldFormatType
}
/**
 * Read data in AP with name ="rebate-manager-accelerator" and push into a map
 * @return Map
 */
Map getAdvancedConfigurationOption(industryKey = null) {
  def advancedConfiguration = readAPObject("rebate-manager-accelerator")
  def advancedConfigurationObj
  if (industryKey) {
    advancedConfigurationObj = advancedConfiguration.find { it.getKey() == industryKey }?.getValue()
  } else {
    advancedConfigurationObj = (advancedConfiguration instanceof List) ? advancedConfiguration.first() : advancedConfiguration
  }

  return putAPdataToMap(advancedConfigurationObj)
}

protected def readAPObject(String uniqueName) {
  def advancedConf = api.find("AP", Filter.equal("uniqueName", uniqueName))
  if (!advancedConf?.value) {
    return null
  }
  def advancedConfigList = advancedConf.value
  def advancedConfigurationString = (advancedConfigList?.size() == 1) ? advancedConfigList.first() : advancedConfigList
  def advancedConfigurationObj
  try {
    advancedConfigurationObj = api.jsonDecode(advancedConfigurationString)
  } catch (Exception e) {
    api.addWarning("AP: ${uniqueName} is not JSON")
  }
  return advancedConfigurationObj
}

protected Map putAPdataToMap(advancedConfigurationObj) {
  if (!advancedConfigurationObj) {
    return [:]
  }
  Map configuration = [:]
  if (advancedConfigurationObj.SourceType) {
    configuration.SourceType = advancedConfigurationObj.SourceType
  }
  if (advancedConfigurationObj.SourceName) {
    configuration.SourceName = advancedConfigurationObj.SourceName
  }
  if (advancedConfigurationObj.RebateBaseFieldValue) {
    configuration.RebateBaseFieldValue = advancedConfigurationObj.RebateBaseFieldValue
  }
  if (advancedConfigurationObj.RebateBaseFieldDate) {
    configuration.RebateBaseFieldDate = advancedConfigurationObj.RebateBaseFieldDate
  }
  if (advancedConfigurationObj.RebateBaseFieldQuantity) {
    configuration.RebateBaseFieldQuantity = advancedConfigurationObj.RebateBaseFieldQuantity
  }
  if (advancedConfigurationObj.RebateBaseFieldCustomerId) {
    configuration.RebateBaseFieldCustomer = advancedConfigurationObj.RebateBaseFieldCustomerId
  }
  if (advancedConfigurationObj.RebateBaseFieldProductId) {
    configuration.RebateBaseFieldProduct = advancedConfigurationObj.RebateBaseFieldProductId
  }
  if (advancedConfigurationObj.CustomerFilterFormula) {
    configuration.CustomerFilterFormula = advancedConfigurationObj.CustomerFilterFormula
  }

  configuration.RebateFieldName = advancedConfigurationObj.RebateFieldName ?: "Rebates"
  
  if (advancedConfigurationObj.MarginFieldName) {
    configuration.MarginFieldName = advancedConfigurationObj.MarginFieldName
  }
  if (advancedConfigurationObj.CustomerSelection) {
    configuration.CustomerSelection = advancedConfigurationObj.CustomerSelection
  }
  if (advancedConfigurationObj.PaymentPeriod) {
    configuration.PaymentPeriod = advancedConfigurationObj.PaymentPeriod
  }
  if (advancedConfigurationObj.PayoutAfterDay != null) {
    configuration.PayoutDays = advancedConfigurationObj.PayoutAfterDay
  }
  if (advancedConfigurationObj.TargetFor) {
    configuration.TargetFor = advancedConfigurationObj.TargetFor
  }
  if (advancedConfigurationObj.RebateFormulas) {
    configuration.RebateFormulas = advancedConfigurationObj.RebateFormulas
  }

  return configuration
}

def createRebateRecordsMatrix(records, page, fieldMapping) {
  def matrix = api.newMatrix("ID", "Label","Status")

  matrix.setColumnFormat("ID", FieldFormatType.LINK)
  matrix.setColumnFormat("Label", FieldFormatType.TEXT)
  matrix.setColumnFormat("Status", FieldFormatType.TEXT)

  records?.each{
    def row = [
            "ID"    : matrix.linkCell(it.get(fieldMapping.get("ID")), page, it.get(fieldMapping.get("ID"))),
            "Label" : it.get(fieldMapping.get("Label")),
            "Status": it.get(fieldMapping.get("Status")),
    ]
    matrix.addRow(row)
  }

  return matrix
}

def createMonthlyLineChart(java.util.Map inputs) {
  def hLib = libs.HighchartsLibrary

  java.util.Map legend = hLib.Legend.newLegend().setEnabled(true)

  java.util.Map tooltip = hLib.Tooltip.newTooltip()

  java.util.Map xAxis = hLib.Axis.newAxis()
          .setTitle(inputs.xAxisName)
          .setCategories(inputs.categories)
  java.util.Map yAxis = hLib.Axis.newAxis().setTitle(inputs.yAxisName)
  if (inputs.yAxisLabelsFormat) yAxis.setLabels([format: inputs.yAxisLabelsFormat])

  java.util.Map yTooltip = hLib.Tooltip.newTooltip()
  if (inputs.yAxisPointFormat) {
    yTooltip.setPointFormat(inputs.yAxisPointFormat)
            .setUseHTML(true)
            .setHeaderFormat("<table>")
            .setFooterFormat("</table>")
  }

  List ySeries = []

  if (inputs.multipleYSeries) {
    ySeries = inputs.ySeriesData.collect { map ->
      java.util.Map series = hLib.LineSeries.newLineSeries()
              .setData(map.data)
              .setXAxis(xAxis)
              .setYAxis(yAxis)
              .setName(map.name)
              .setTooltip(yTooltip)

      if (map.dashStyle) series.setDashStyle(map.dashStyle)
      if (inputs.yMin) yAxis.setMin(inputs.yMin)
      if (inputs.yMax) yAxis.setMax(inputs.yMax)

      return series
    }
  } else {
    java.util.Map series = hLib.LineSeries.newLineSeries()
            .setData(inputs.ySeriesData)
            .setXAxis(xAxis)
            .setYAxis(yAxis)
            .setName(inputs.yAxisName)
            .setTooltip(yTooltip)

    if (inputs.yMin) yAxis.setMin(inputs.yMin)
    if (inputs.yMax) yAxis.setMax(inputs.yMax)

    ySeries = [series]
  }
  List zSeries = []

  if (inputs.hasZAxis) {
    java.util.Map zTooltip = hLib.Tooltip.newTooltip()
            .setPointFormat("{series.name}: {point.y:${hLib.ConstConfig.TOOLTIP_POINT_X_FORMAT_TYPES.get(inputs.zAxisPointFormat)}")
            .setUseHTML(true)

    java.util.Map zAxis = hLib.Axis.newAxis().setTitle(inputs.zAxisName).setOpposite(true)
            .setLabels([format: inputs.zAxisFormat])

    if (inputs.zMin) zAxis.setMin(inputs.zMin)
    if (inputs.zMax) zAxis.setMax(inputs.zMax)

    zSeries = inputs.zSeriesData?.collect { map ->
      hLib.LineSeries.newLineSeries()
              .setData(map.data)
              .setXAxis(xAxis)
              .setYAxis(zAxis)
              .setName(map.name)
              .setTooltip(zTooltip)
    }

  }

  return hLib.Chart.newChart()
          .setTitle(inputs.title)
          .setSeries(*ySeries, *zSeries)
          .setLegend(legend)
          .setTooltip(tooltip)
          .getHighchartFormatDefinition()
}

Boolean isPercentRebate(String rebateCode) {
  if (rebateCode == "FlexGrowth") {
    def flexLib = libs.RebateManager.(rebateCode?.toString())
    def rebateValueType = flexLib.getRebateValueType()
    return rebateValueType == "Percent"
  } else {
    List percentRebateTypes = libs.RebateManager.Constant.PERCENT_REBATE_TYPES

    return percentRebateTypes?.collect { it.replaceAll(" ", "") }?.contains(rebateCode)
  }
}