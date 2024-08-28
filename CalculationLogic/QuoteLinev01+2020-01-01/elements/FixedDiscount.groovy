def filters = [
        Filter.equal("label","Constant Value Discount"),
        api.productToRelatedObjectsFilter("PR", api.product("sku")),
        api.customerToRelatedObjectsFilter("PR", out.Customer)

]
return api.find("PR",0,1,null,null, *filters)?.find()?.attribute3 as BigDecimal ?: 0.2
