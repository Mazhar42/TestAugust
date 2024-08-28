import net.pricefx.common.api.FieldFormatType

if(quoteProcessor.isPrePhase()){
    return
}

def discountType = quoteProcessor.quoteView.inputs[1]?.value?.discountType
def discount = quoteProcessor.quoteView.inputs[1]?.value?.discount

def output1 = [
        resultName   : "DiscountType",
        resultLabel  : "Discount Type",
        result       :  discountType,
        resultType   : "SIMPLE",
        cssProperties: "background-color:#99FFDD"
]
quoteProcessor.addOrUpdateOutput(output1)
if(discount){
    def output2 = [
            resultName   : "Discount",
            resultLabel  : "Discount",
            result       :  discount,
            resultType   : "SIMPLE",
            cssProperties: "background-color:#EEFFDD"
    ]
    quoteProcessor.addOrUpdateOutput(output2)
}else{
    def columnFormats = [
            "Product Group": FieldFormatType.TEXT,
            "Discount": FieldFormatType.NUMERIC
    ]
    def inputMatrix = quoteProcessor.quoteView.inputs[1]?.value?.InputMatrix
    def result = api.newMatrix()
            .withEnableClientFilter(true)
            .withWidth(10)
            .withColumnFormats(columnFormats)
            .withRows(inputMatrix)

    def output2 = [
            resultName   : "Discount",
            resultLabel  : "Discount",
            result       :  result,
            resultType   : "MATRIX",
            cssProperties: "background-color:#EEFFDD"
    ]
    quoteProcessor.addOrUpdateOutput(output2)
}


