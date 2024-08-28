Map params = (api.getElement("AddInputDataToMap") as Map)?.clone()
params?.put("RebateRecords", rebateRecords)
libs.RebateManager.CalculationUtils.createRebateRecords(params)