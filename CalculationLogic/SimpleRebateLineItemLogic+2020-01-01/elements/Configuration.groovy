//1. Get Data from AC
def customerGroup = api.input("CustomerGroup")
String industryKey
if (customerGroup) {
    String industry = api.find("C", 0, 1, "customerId", ["attribute24"], CustomerGroup.fromMap(customerGroup)?.asFilter())?.getAt(0)?.attribute24
    industryKey = industry == "Chemicals" ? "Chem Data" : "Standard Sales Data"
} else {
    industryKey = "Standard Sales Data"
}
Map configuration = libs.RebateManager.Util.getAdvancedConfigurationOption(industryKey)

String payoutDays = api.currentItem("rebateType.Payout after (days)")

if (payoutDays && !payoutDays.isEmpty()) {
    configuration.PayoutDays = payoutDays
}
return configuration