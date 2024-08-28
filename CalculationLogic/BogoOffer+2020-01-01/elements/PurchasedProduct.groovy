if(api.isInputGenerationExecution()){
    return api.inputBuilderFactory()
        .createProductEntry("PurchasedProduct")
        .setLabel("Purchased Product")
        .getInput()
}
return input.PurchasedProduct