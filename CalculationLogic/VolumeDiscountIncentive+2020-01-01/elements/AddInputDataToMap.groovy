def targetInput = input.VolumeDiscount
def configuration = api.getElement("Configuration")
def payoutDates = configuration.get("PayoutDays")
def line = api.currentItem()
Map params = [:]

params.put("PayoutDateAmounts", payoutDates)

params.put("TargetInput", targetInput)

params.put("StartDate", line?.startDate)
params.put("EndDate", line?.endDate)

return params