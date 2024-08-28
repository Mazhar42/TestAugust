if(null in [out.BasePrice, out.DiscountPrice]){
    api.addWarning("Base Price or Discount is not available")
    return null
}

return out.BasePrice - out.DiscountPrice