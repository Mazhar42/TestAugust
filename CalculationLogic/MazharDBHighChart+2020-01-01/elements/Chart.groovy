def dmData = out.NewElement
def categoryData = dmData?.keySet()//list of xAxis values
def seriesData = [[
                          name: 'Product-Quantity', //name of the series
                          data: dmData?.values() //list of yAxis values, or list of maps with y value
                  ]]

def definition = [
        chart      : [
                type: 'bar'
        ],

        title      : [
                text: 'Title'
        ],

        subtitle   : [
                text: 'Subtitle'
        ],

        plotOptions: [
                line: [
                        dataLabels         : [
                                enabled: true
                        ],
                        enableMouseTracking: false
                ]
        ],

        series     : seriesData,

        xAxis      : [
                categories: categoryData
        ],

        yAxis      : [
                title: [
                        text: 'Y-axis Title'
                ]
        ],

        credits    : [
                enabled: false
        ]
]

return api.buildHighchart(definition)