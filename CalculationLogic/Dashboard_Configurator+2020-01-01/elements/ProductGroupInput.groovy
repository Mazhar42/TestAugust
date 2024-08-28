def productAttribute = input.ProductAttribute ?: []
def customMap = out.DataMartQuery
def entry = api.createConfiguratorEntry()

productAttribute?.each {
    def attribute = it
    def ctx = api.getDatamartContext()
    def dm = ctx.getDatamart("Standard_Sales_Data")
    def query = ctx.newQuery(dm, true)
            .select(attribute)
    def result = ctx.executeQuery(query)
    def attributeList = result?.getData().collect {it.values().find()}
    def productAttributeMap = attributeList
            .collectEntries {
                [(it): it]
            }

    def productGroup = api.inputBuilderFactory()
            .createOptionsEntry(attribute)
            .setOptions(productAttributeMap.keySet() as List)
            .setLabels(productAttributeMap)
            .setLabel(attribute)
            .buildContextParameter()

    entry.createParameter(productGroup)
}

return entry


//if(productAttribute.contains("PG")) {
//
//    def productGroupList = customMap?.ProductGroup
//    def productGroupMap = productGroupList
//            .collectEntries {
//                [(it): it]
//            }
//
//    def productGroup = api.inputBuilderFactory()
//            .createOptionsEntry("ProductGroup")
//            .setOptions(productGroupMap.keySet() as List)
//            .setLabels(productGroupMap)
//            .setLabel("Product Group")
//            .buildContextParameter()
//
//    entry.createParameter(productGroup)
//
//}
//
//
//if(productAttribute.contains("PL")) {
//
//    def productLineList = customMap?.ProductLine
//    def productLineMap = productLineList
//            .collectEntries {
//                [(it): it]
//            }
//
//    def productLine = api.inputBuilderFactory()
//            .createOptionsEntry("ProductLine")
//            .setOptions(productLineMap.keySet() as List)
//            .setLabels(productLineMap)
//            .setLabel("Product Line")
//            .buildContextParameter()
//
//    entry.createParameter(productLine)
//
//}