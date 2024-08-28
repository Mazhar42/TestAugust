def entry = api.createConfiguratorEntry()

def discountTypes = [
        "Additional Discount",
        "Flat Discount",
        "Quote Level Discount",
        "Product Group Level Discount"
]

def discountTypesMap = discountTypes.collectEntries {
    [(it): it]
}

def discountType = api.inputBuilderFactory()
        .createOptionEntry("discountType")
        .setOptions(discountTypes)
        .setLabels(discountTypesMap)
        .setLabel("Discount Type")
        .buildContextParameter()

entry.createParameter(discountType)

return entry






