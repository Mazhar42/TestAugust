def isOfferProduct = quoteProcessor.quoteView.lineItems?.collectEntries {
    [(it?.sku): false]
}

def customerId = quoteProcessor.getHelper().getRoot().getInputByName("Customer")?.value

def offerMap = quoteProcessor.quoteView.lineItems?.collectEntries {
    def filters = []

    filters << Filter.equal("label","Buy One Get One Offer")
    filters << Filter.equal("attribute14",it?.sku)
    filters << api.customerToRelatedObjectsFilter("PR",customerId)
    def offeredProductSku = api.find("PR",0,1,null,null,*filters)?.find()?.attribute15
    isOfferProduct[offeredProductSku] = true
    return [(it?.sku):offeredProductSku]
}

def parentMap = quoteProcessor.quoteView.lineItems?.collectEntries {
    def filters = []
    filters << Filter.equal("label","Buy One Get One Offer")
    filters << Filter.equal("attribute14",it?.sku)
    filters << api.customerToRelatedObjectsFilter("PR","CID-0001")
    def offeredProductSku = api.find("PR",0,1,null,null,*filters)?.find()?.attribute15
    return [(offeredProductSku):it]
}

def lineIdMap = quoteProcessor.quoteView.lineItems?.collectEntries{
    [(it.sku):it.lineId]
}

if(quoteProcessor.isPrePhase()){
    quoteProcessor.quoteView.lineItems?.each {
        if(offerMap[it.sku] != null){
            if(offerMap[it.sku] in quoteProcessor.quoteView.lineItems){
                return
            }else {
                quoteProcessor.addLineItem("ROOT", offerMap[it.sku])
            }
        }
    }
}

if(quoteProcessor.isPostPhase()){

        quoteProcessor.quoteView.lineItems?.each {
            if(isOfferProduct[it.sku]){
                quoteProcessor.addOrUpdateOutput(it.lineId,[
                        "resultName": "InvoicePrice",
                        "label": "InvoicePrice",
                        "result": 0
                ])
                quoteProcessor.addOrUpdateOutput(it.lineId,[
                        "resultName": "Quantity",
                        "label": "Quantity",
                        "result": parentMap[it.sku].inputs?.find{
                                    it.name == "Quantity"
                                }?.value
                ])
                quoteProcessor.addOrUpdateInput(it.lineId,[
                        "name": "Quantity",
                        "readOnly":true,
                        "value": parentMap[it.sku].inputs?.find{
                            it.name == "Quantity"
                        }?.value
                ])
                quoteProcessor.addOrUpdateInput(it.lineId,[
                        "name": "ListPrice",
                        "readOnly":true,
                ])
          }
        }

}
