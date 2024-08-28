def datamart = [
        "Standard_Sales_Data" : "Standard Sales Data",
        "Standard_Sales_Data_Chem": "Standard Chem Data"
]

def entry = api.createConfiguratorEntry()

def datamartName = api.inputBuilderFactory()
        .createOptionEntry("DatamartName")
        .setOptions(datamart.keySet() as List)
        .setLabels(datamart)
        .setLabel("Select Datamart")
        .buildContextParameter()

entry.createParameter(datamartName)

return entry