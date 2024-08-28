def ctx = api.getDatamartContext()

def dm = ctx.getDataSource("Product")

def query = ctx.newQuery(dm, true)
        .select("ProductId")
        .select("ProductName")


def result = ctx.executeQuery(query)

def testMap = result?.getData()?.collect()?.collectEntries {
    [(it.ProductId): it.ProductName]
}

api.global.productMap = testMap