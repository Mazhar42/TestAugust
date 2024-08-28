if(quoteProcessor.isPostPhase()){
    def customerId = quoteProcessor.getHelper().getRoot().getInputByName("Customer")?.value
    def lineItems = quoteProcessor.quoteView.lineItems
    def quantity

    lineItems.each { item ->
        def productSku = item?.sku
        quantity = item.inputs?.find { it.name == "Quantity"}?.value.toDouble()
        def listPrice = item.inputs?.find { it.name == "ListPrice"}?.value.toDouble()
        def invoicPrice
        def discount
        def filters = [
                Filter.equal("label","Volume Discount Incentive"),
                api.customerToRelatedObjectsFilter("PR", customerId),
                api.productToRelatedObjectsFilter("PR", productSku),
                Filter.greaterOrEqual("expiryDate", api.targetDate()),
                Filter.lessOrEqual("validAfter", api.targetDate())
        ]

        def result = api.find("PR", 0, 1,null, null, *filters)?.find()?.attribute3
        def records = api.jsonDecode(result)
        def volumes = records.keySet().sort { it.toDouble()}

        volumes.each {
            if(quantity.toDouble() >= it.toDouble()){
                discount = records[it]?.toDouble() / 100.0
            }
        }

        quoteProcessor.addOrUpdateOutput(item.lineId,
                [
                        resultName : "VolumeDiscount",
                        resultLabel: "Volume Discount",
                        result     : discount
                ]
        )
        quoteProcessor.addOrUpdateOutput(item.lineId,[
                "resultName": "InvoicePrice",
                "label": "Invoice Price",
                "result": listPrice * (1-discount),
        ])

        quoteProcessor.addOrUpdateOutput(item.lineId,[
                "resultName": "TotalInvoicePrice",
                "label": "TotalInvoicePrice",
                "result": listPrice * (1-discount) * quantity
        ])
    }
}