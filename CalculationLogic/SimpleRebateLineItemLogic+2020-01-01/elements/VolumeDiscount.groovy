if (api.isInputGenerationExecution()) {
    return api.inputBuilderFactory()
            .createMultiTierEntryInputBuilder("VolumeDiscount")
            .setLabel("VolumeDiscount")
            .getInput()
}

def discount = input.VolumeDiscount
return api.jsonEncode(discount)
