if(api.isInputGenerationExecution()){
    api.inputBuilderFactory()
        .createUserEntry("OverridePrice")
        .setLabel("Override Price")
        .getInput()
}
return input.OverridePrice