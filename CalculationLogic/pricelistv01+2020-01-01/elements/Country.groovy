if(api.isInputGenerationExecution()){
    def countries = api.findLookupTableValues("CountryInfo")?.collectEntries{
        [(it.name), (it.attribute1)]
    }
    def options = api.findLookupTableValues("CountryInfo")?.collect { it.name }

    api.inputBuilderFactory()
        .createOptionEntry("Country")
        .setLabel("Enter Country")
        .setOptions(options)
        .setLabels(countries)
        .getInput()
}else{
    return input.Country
}