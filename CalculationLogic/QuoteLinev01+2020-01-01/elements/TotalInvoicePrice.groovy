if(null in [out.InvoicePrice, out.Quantity]){
    return null
}
return out.InvoicePrice * out.Quantity