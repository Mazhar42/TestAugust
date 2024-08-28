import net.pricefx.formulaengine.AbstractProducer

/*
  Convert from filter to list of ProductId
 */

def convertProductGroupInputToListOfProductId(def productGroup) {
    if (productGroup == null) return null

    List productIdList
    Filter filter
    if (productGroup instanceof Filter) {
        filter = productGroup
    } else {
        filter = productGroup?.asFilter()
    }

    AbstractProducer.ResultIterator streamResult = api.stream("P", "sku", ["sku"], filter)
    productIdList = streamResult?.collect { it?.sku }
    streamResult?.close()
    return productIdList
}
/*
   Filter Product base on Column's Label Of Product Master
   Input:
      - label: column's label want to filter
      - operator: comparation operator - Filter.OP_EQUAL , Filter.OP_GREATER_OR_EQUAL ....
      - value: value to filter
   Return:
      - Return a Filter
 */

Filter getProductFilterByAttributeLabel(String label, Integer operator, def value) {
    //1. get metadata of Customer
    def attributes = api.find("PAM")
    def attribute = attributes?.find { it.label == label }
    //2. build Filter
    Filter filter = new Filter()
    filter.setProperty(attribute?.fieldName)
    filter.setOperator(operator)
    filter.setValue(value)

    return filter
}

Filter getProductExtensionFilterByAttributeLabel(String pxName, String label, Integer operator, def value) {

    //1. get metadata of Product Extension
    def attributes = api.find("PXAM", Filter.equal("name", pxName))
    def attribute = attributes?.find { it.label == label }
    //2. build Filter
    Filter filter = new Filter()
    filter.setProperty(pxName + "__" + attribute?.fieldName)
    filter.setOperator(operator)
    filter.setValue(value)

    return filter
}

/*
  Get Product Filter in user profile
  + Input:
      - loginName: user's login name
  + Return:
      - a filter
 */

Filter getProductFilterOfUserProfile(String loginName) {
    def user
    if (loginName) {
        user = api.find("U", Filter.equal("loginName", loginName))[0]
    } else {
        user = api.user()
    }
    String productFilterString = user?.productFilterCriteria
    return api.filterFromMap(api.jsonDecode(productFilterString))
}

/*
  Check 2 Product Groups
  Return true if they are overlap
 */

Boolean isOverlap(def firstProductGroup, def secondProductGroup) {
    List firstProductIds = convertProductGroupInputToListOfProductId(firstProductGroup)
    List secondProductIds = convertProductGroupInputToListOfProductId(secondProductGroup)
    List intersection = firstProductIds.intersect(secondProductIds)
    Boolean result = false
    if (intersection) {
        result = true
    }
    return result
}
/**
 *
 * @param DatamartName
 * @return
 */
Map getDatamartFields(String DatamartName){
    String owner = "Product"
    def ctx = api.getDatamartContext()
    def dm= ctx.getDatamart(DatamartName)
    Map fields= [:]

    def fieldList = dm.fc()?.fields
    def fileredFileds = fieldList?.findAll{it?.owningFC?.toLowerCase() == owner.toLowerCase() && it?.dimension == true}
    for(item in fileredFileds){
        fields.put(item?.name,item?.label)
    }
    return fields
}