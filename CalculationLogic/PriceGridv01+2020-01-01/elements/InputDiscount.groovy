if(api.isInputGenerationExecution()){
    api.inputBuilderFactory()
        .createUserEntry("Discount")
        .setLabel("Enter Discount")
        .getInput()
}
return input.Discount