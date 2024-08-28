def ctx = api.getDatamartContext()

def dm = ctx.getDatamart("Standard_Sales_Data")

def query = ctx.newQuery(dm, true)
        .select("ProductId")
        .select("SUM(Quantity)", "Quantity")

def result = ctx.executeQuery(query)

def t=  result?.getData()?.collect()?.collectEntries{
    [(it?.ProductId): it?.Quantity]
}

return t