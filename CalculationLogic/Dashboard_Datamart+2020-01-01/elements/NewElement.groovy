def entry = api.createConfiguratorEntry()
//def inp = api.inputBuilderFactory()
//        .createHiddenEntry("testName")
//        .buildContextParameter()
//
//entry.createParameter(inp)

api.createConfiguratorEntry(InputType.HIDDEN, "testName")


def datamartName = entry.getInputs()?.find { it.name == "DatamartName"}
api.logInfo("mazhar#####",datamartName)

def isChecked = api.inputBuilderFactory()
        .createBooleanUserEntry("isChecked")
        .setLabel("is checked")
        .buildContextParameter()

entry.createParameter(isChecked)

if(input["isChecked"] == true){

    def ctx = api.getDatamartContext()

    def dm = ctx.getDatamart(datamartName)

    def query = ctx.newQuery(dm, true)
            .select("Region")

    def result = ctx.executeQuery(query)

    def attributeList = result?.getData().collect {it.values().find()}
    def productAttributeMap = attributeList
            .collectEntries {
                [(it): it]
            }


    def region = api.inputBuilderFactory()
            .createOptionsEntry("Region")
            .setOptions(productAttributeMap.keySet() as List)
            .setLabels(productAttributeMap)
            .setLabel("Region")
            .buildContextParameter()

    entry.createParameter(region)
}

return entry