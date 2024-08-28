if(api.isInputGenerationExecution()){
    api.inputBuilderFactory()
        .createProductEntry("Product")
        .setLabel("Enter Product")
        .getInput()
}

return input.Product