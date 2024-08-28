import net.pricefx.common.api.FieldFormatType

def columnFormats = [
        "a": FieldFormatType.NUMERIC,
        "b": FieldFormatType.NUMERIC,
        "Pricelist": FieldFormatType.LINK
]
def inputMatrix = [
        ["a": 1, "b": 2,"Pricelist": "Hi"],
]
def result = api.newMatrix()
        .withEnableClientFilter(true)
        .withColumnFormats(columnFormats)
        .withRows(inputMatrix)

def valueA = 15
def valueB = 17

def styledValueA = result.styledCell(valueA, "#ff0000", "transparent", "bold")
def styledValueB = result.styledCell(valueB, "#00ff00", "transparent", "bold")
result.addRow([ "a" : styledValueA, "b": styledValueB, "Pricelist" : result  .linkToPriceList("Open Pricelist", 1080, null)])


return result