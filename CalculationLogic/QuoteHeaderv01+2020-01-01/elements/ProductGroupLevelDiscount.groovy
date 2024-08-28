if(quoteProcessor.isPrePhase()){
    api.inputBuilderFactory()
        .createConfiguratorInputBuilder("Config01","Quote_Configurator",true)
        .setLabel("Config01")
        .addOrUpdateInput(quoteProcessor,"ROOT")
}