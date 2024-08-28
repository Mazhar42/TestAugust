if (api.isInputGenerationExecution()) {
    return api.inputBuilderFactory()
            .createMultiTierEntryInputBuilder("volumeDiscount")
            .setLabel("Volume Discount")
            .getInput()
}

return api.jsonEncode(input.volumeDiscount)