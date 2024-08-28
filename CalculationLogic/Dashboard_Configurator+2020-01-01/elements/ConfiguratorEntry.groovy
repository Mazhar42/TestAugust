def entry = api.createConfiguratorEntry()

def productAttributes = api.findLookupTableValues("Dashboard_Input_PP_Mazhar")
        .collectEntries {
            [(it.name): it.value]
        }


def productAttribute = api.inputBuilderFactory()
        .createOptionsEntry("ProductAttribute")
        .setOptions(productAttributes.keySet() as List)
        .setLabels(productAttributes)
        .setLabel("Product Attributes")
        .buildContextParameter()

entry.createParameter(productAttribute)


return entry

