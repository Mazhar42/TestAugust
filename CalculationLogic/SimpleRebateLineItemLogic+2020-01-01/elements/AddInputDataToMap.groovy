def targetInput = input.VolumeDiscount
api.logInfo("ncb@@targetInput", targetInput)
def configuration = api.getElement("Configuration")
def payoutDates = configuration.get("PayoutDays") // if not set, default = 1
//def currentDate = api.getElement("CurrentDate")
def line = api.currentItem()
Map params = [:]

params.put("PayoutDateAmounts", payoutDates)

params.put("TargetInput", targetInput)

params.put("StartDate", line?.startDate)
params.put("EndDate", line?.endDate)
//params.put("SourceId", line?.sourceId)//sourceId just have data context = 'rebateRecord', other is null
//params.put("Name", line?.name) //name just have data context = 'rebateRecord', other is null
//params.put("CurrentDate", currentDate)

return params