def filters = [
        Filter.equal("name","InvoicePriceHistory"),
        Filter.equal("sku",input.Product)
]

def res = api.find("PX6",0,api.getMaxFindResultsLimit(),null,null,*filters)

return res