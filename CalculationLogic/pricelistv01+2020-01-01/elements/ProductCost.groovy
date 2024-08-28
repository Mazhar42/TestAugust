def filters = [
        Filter.lessOrEqual("attribute1", api.targetDate().format("yyyy-MM-dd")),
        Filter.equal("attribute3","USD")
]
def productCost = api.productExtension("ProductCost", *filters)?.sort {it.attribute1}?.reverse()?.find()?.attribute2

return productCost
