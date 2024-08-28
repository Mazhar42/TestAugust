import net.pricefx.formulaengine.AbstractProducer

/*
  Covert filter to List of Customer
 */

def convertCustomerGroupInputToListOfCustomerId(def customerGroup) {
    if (customerGroup == null) return null

    List customerIdList
    Filter filter
    if (customerGroup instanceof Filter) {
        filter = customerGroup
    } else {
        filter = customerGroup?.asFilter()
    }

    AbstractProducer.ResultIterator streamResult = api.stream("C", "customerId", ["customerId"], filter)
    customerIdList = streamResult?.collect { it?.customerId }
    streamResult?.close()

    return customerIdList
}

/*
   Filter Customer base on Column's Label Of Customer Master
   Input:
      - label: column's label want to filter
      - operator: comparation operator - Filter.OP_EQUAL , Filter.OP_GREATER_OR_EQUAL ....
      - value: value to filter
   Return:
      - Return a Filter
 */

Filter getCustomerFilterByAttributeLabel(String label, Integer operator, def value) {
    //1. get metadata of Customer
    def attributes = api.find("CAM")
    def attribute = attributes?.find { label?.equalsIgnoreCase(it.label) }
    //2. build Filter
    Filter filter = new Filter()
    filter.setProperty(attribute?.fieldName)
    filter.setOperator(operator)
    filter.setValue(value)

    return filter
}

/*
  Just special logic. Support for some business
 */

Filter getCustomerFilterBySalesOrgOfUser(String loginName) {
    def user
    if (loginName) {
        user = api.find("U", Filter.equal("loginName", loginName))[0]
    } else {
        user = api.user()
    }
    String salesOrg = user?.additionalInfo3 //using additionalInfo3 to save SalesOrg
    Filter customerFilter = getCustomerFilterByAttributeLabel("Sales Org", Filter.OP_EQUAL, salesOrg)
    return customerFilter
}

/*
  Get Customer Filter was setting in user's profile
  + Input:
      - loginName: user's login name
  + Return:
      - a filter
 */

Filter getCustomerFilterOfUserProfile(String loginName) {
    def user
    if (loginName) {
        user = api.find("U", Filter.equal("loginName", loginName))[0]
    } else {
        user = api.user()
    }
    String customerFilterString = user?.customerFilterCriteria
    return api.filterFromMap(api.jsonDecode(customerFilterString))
}

/*
  Check 2 Customer Groups
  Return true if they are overlap
 */

Boolean isOverlap(def firstCustomerGroup, def secondCustomerGroup) {
    List firstCustomerIds = convertCustomerGroupInputToListOfCustomerId(firstCustomerGroup)
    List secondCustomerIds = convertCustomerGroupInputToListOfCustomerId(secondCustomerGroup)
    List intersection = firstCustomerIds.intersect(secondCustomerIds)
    Boolean result = false
    if (intersection) {
        result = true
    }
    return result
}
/**
 *
 * @param DatamartName
 * @return Map
 */
Map getDatamartFields(String DatamartName){
    String owner = "Customer"
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