if(api.isInputGenerationExecution()){
    return api.inputBuilderFactory()
                .createUserEntry("ListPrice")
                .setLabel("Enter ListPrice")
                .getInput()
}
return input.ListPrice