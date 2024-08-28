/*
  Create rebate records
 */
Map params = (api.getElement("AddInputDataToMap") as Map)?.clone()
//Add more params to Map for calculate rebate in rebate record context
params?.put("RebateRecords", rebateRecords)
libs.RebateManager.CalculationUtils.createRebateRecords(params)