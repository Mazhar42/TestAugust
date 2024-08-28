if(null in [out.ProductCost, out.RelativeMarkup]){
    api.criticalAlert("Markup cannot be calculated, Product Cost or Relative Markup is not available")
    return null
}
return out.ProductCost * out.RelativeMarkup