if(api.isInputGenerationExecution()){
    return api.inputBuilderFactory()
            .createProductEntry("OfferProduct")
            .setLabel("Offer Product")
            .getInput()
}

return input.OfferProduct