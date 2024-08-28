def filters = [
        Filter.equal("key1", input.Country),
        Filter.equal("key2", api.product("attribute19") )
]
def data = api.findLookupTableValues("CountryMarkup", *filters)?.find()?.attribute1

if(data == null){
    filters.remove(1)
    filters.add( Filter.equal("key2", "*"))
    data = api.findLookupTableValues("CountryMarkup", *filters)?.find()?.attribute1
}

if(data == null) {
    api.addWarning("Relative Markup for this country and product group is not found")
    return null
}

return data