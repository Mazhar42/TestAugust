def entry = api.createConfiguratorEntry()

def temp1 = api.inputBuilderFactory()
        .createConfiguratorInputBuilder("testConf01","Dashboard_Datamart_Configurator",true)
        .setLabel("Select Datamart")
        .buildContextParameter()

entry.createParameter(temp1)

def temp2 = api.inputBuilderFactory()
        .createConfiguratorInputBuilder("testConf02","Dashboard_Datamart",true)
        .setLabel("Test")
        .setValue(["testName": input["testConf01"]])
        .buildContextParameter()

entry.createParameter(temp2)

return entry
