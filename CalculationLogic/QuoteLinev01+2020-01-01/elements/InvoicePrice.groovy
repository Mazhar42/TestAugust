if(null in [out.ListPrice]){
    return null
}
def discount = out.VolumeDiscount?:0.0

def invoicePrice =  out.ListPrice * (1-discount)
return invoicePrice