if (api.isInputGenerationExecution()) {
    api.inputBuilderFactory()
            .createConfiguratorInputBuilder("Config_Master", "Dashboard_Master_Configurator", true)
            .setLabel("Open")
            .getInput()
}

return input["Config_Master"]