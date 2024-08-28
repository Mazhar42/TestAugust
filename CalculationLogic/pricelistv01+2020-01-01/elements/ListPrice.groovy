if(null in [out.ProductCost, out.MarkupAbs]){
    api.criticalAlert("List Price cannot be calculated, Product Cost or Markup value is not available")
    return null
}
return out.ProductCost + out.MarkupAbs