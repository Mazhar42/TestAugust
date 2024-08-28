if(api.isInputGenerationExecution()){
    return api.inputBuilderFactory()
        .createProductEntry("Product")
        .setLabel("Offered Product")
        .getInput()
}
return input.Product