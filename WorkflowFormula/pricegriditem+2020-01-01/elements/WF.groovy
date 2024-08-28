//workflow.withDefaultPostApprovalStepLogic("PricegridItemHistory")


workflow.addApprovalStep("PriceGrid")
        .withApprovers("admin")
        .withReasons("reason")
        .withPostStepLogic("PricegridItemHistory")
