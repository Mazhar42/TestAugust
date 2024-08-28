if(null in [out.BasePrice,input.Discount]){
    api.addWarning("Base Price or Discount is null")
    return null
}
return out.BasePrice * input.Discount