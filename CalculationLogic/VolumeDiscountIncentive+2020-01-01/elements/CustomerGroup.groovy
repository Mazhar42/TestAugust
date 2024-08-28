if (api.isInputGenerationExecution()) {
    return api.inputBuilderFactory()
            .createCustomerGroupEntry("CustomerGroup")
            .setLabel("Customer(s)")
            .getInput()
}

return CustomerGroup.fromMap(input.CustomerGroupEntry)