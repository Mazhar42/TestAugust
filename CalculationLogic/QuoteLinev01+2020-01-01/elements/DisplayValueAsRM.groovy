import net.pricefx.common.api.FieldFormatType
if(out.InvoicePrice == null || out.InvoicePrice == 0) return null

def columnFormats = [
        "List Price": FieldFormatType.TEXT,
        "Discount": FieldFormatType.NUMERIC,
        "Invoice Price": FieldFormatType.TEXT,
]
def inputMatrix = [
        ["List Price": out.ListPrice, "Discount": out.FixedDiscount, "Invoice Price": out.InvoicePrice]
]
def result = api.newMatrix()
        .withEnableClientFilter(true)
        .withColumnFormats(columnFormats)
        .withRows(inputMatrix)

return result