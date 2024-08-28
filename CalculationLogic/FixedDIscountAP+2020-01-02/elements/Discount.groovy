if(api.isInputGenerationExecution()){
    return api.inputBuilderFactory()
            .createUserEntry("Discount")
            .setFormatType("PERCENT")
            .getInput()
}

return input.Discount