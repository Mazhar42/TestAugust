def discountType = input.discountType ?: null
def entry = api.createConfiguratorEntry()

def productGroups = api.find(
        "P",
        0,
        api.getMaxFindResultsLimit(),
        null,
        ["attribute19"],
        null
).unique().collect {
    it.attribute19
}

if(discountType in ["Additional Discount", "Flat Discount", "Quote Level Discount"]){
    def discount = api.inputBuilderFactory()
            .createUserEntry("discount")
            .setLabel("Discount")
            .setFormatType("PERCENT")
            .setFrom(0)
            .setTo(100)
            .buildContextParameter()

    entry.createParameter(discount)
}else if(discountType == "Product Group Level Discount"){

    def columns = ["Product Group", "Discount"]
    def columnsValueOption = [
            "Product Group": productGroups
    ]

    def inputMatrix = api.inputBuilderFactory()
            .createInputMatrix("InputMatrix")
            .setColumns(columns)
            .setColumnValueOptions(columnsValueOption)
            .setLabel("InputMatrix")
            .buildContextParameter()

    entry.createParameter(inputMatrix)
}

return entry