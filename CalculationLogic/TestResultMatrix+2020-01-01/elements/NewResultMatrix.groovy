import net.pricefx.common.api.FieldFormatType

def columnFormats = [
        "List Price": FieldFormatType.NUMERIC,
        "Discount": FieldFormatType.PERCENT,
        "Invoice Price": FieldFormatType.NUMERIC,
]
def inputMatrix = [
        ["List Price": 12.00, "Discount": 0.07, "Invoice Price": 11.16],
        ["List Price": 13.00, "Discount": 0.08, "Invoice Price": 11.96],
        ["List Price": 14.00, "Discount": 0.12, "Invoice Price": 12.32]
]
def result = api.newMatrix()
        .withEnableClientFilter(true)
        .withColumnFormats(columnFormats)
        .withRows(inputMatrix)

def value = "15.00"
def styledValue = result.styledCell(value, "#ff0000", "transparent", "bold")
result.addRow([ "List Price" : styledValue, "Discount" : 0.10, "Invoice Price":13.50])

return result