def dmData = out.resultPrice
def categoryData = dmData?.collect { it?.lastUpdateDate }
def seriesData = [
        [
              name: 'Invoice Price',
              data: dmData?.collect { it?.attribute2 as BigDecimal }
        ]
]

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