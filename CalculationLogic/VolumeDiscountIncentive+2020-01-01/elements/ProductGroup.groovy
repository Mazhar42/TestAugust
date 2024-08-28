if (api.isInputGenerationExecution()) {
    return api.inputBuilderFactory()
            .createProductGroupEntry("ProductGroup")
            .setLabel("Product(s)")
            .getInput()
}

return ProductGroup.fromMap(input.ProductGroup)