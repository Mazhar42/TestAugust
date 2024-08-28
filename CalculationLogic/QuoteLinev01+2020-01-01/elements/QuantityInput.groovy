if(api.isInputGenerationExecution()){
    return api.inputBuilderFactory()
                .createIntegerUserEntry("Quantity")
                .setLabel("Enter Quantity")
                .getInput()
}
return input.Quantity